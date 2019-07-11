//
//  ZenWeex.m Cordova Plugin iOS Implementation
//
//  Author: 扣丁禅师 <v@yinqisen.cn>
//

#import <Cordova/CDV.h>
#import <WeexSDK/WeexSDK.h>
#import "JSValue+Weex.h"
#import "ZenViewController.h"

// workaround: WeexSDK not explode as public, so copy them to plugin project
#import "WXBridgeMethod_Private.h"
#import "WXModuleMethod_Private.h"

@interface ZenWeex : CDVPlugin {
  // Member variables go here.
}

- (void)inject:(CDVInvokedUrlCommand*)command;
- (void)openWindow:(CDVInvokedUrlCommand*)command;
- (void)closeWindow:(CDVInvokedUrlCommand*)command;
- (void)getEnvironment:(CDVInvokedUrlCommand*)command;
- (void)callNativeModule:(CDVInvokedUrlCommand*)command;
@end

@implementation ZenWeex

// - (void)coolMethod:(CDVInvokedUrlCommand*)command
// {
//     CDVPluginResult* pluginResult = nil;
//     NSString* echo = [command.arguments objectAtIndex:0];

//     if (echo != nil && [echo length] > 0) {
//         pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsString:echo];
//     } else {
//         pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR];
//     }

//     [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
// }

- (void)inject:(CDVInvokedUrlCommand*)command
{
    // 不破坏原有代码，HOOK [WXUtility getEnvironment]，添加 rootDir 路径
    static dispatch_once_t onceToken;
    dispatch_once(&onceToken, ^{
        Class originalClass = [WXUtility class];

        typedef NSDictionary* (*t_Callback)(id, SEL);
        SEL originalSelector = @selector(getEnvironment);
        Method originalMethod = class_getClassMethod(originalClass, originalSelector);
        t_Callback fnCallback = (t_Callback)method_getImplementation(originalMethod);

        id hookBlock = ^NSDictionary*() {
            NSMutableDictionary *data = (NSMutableDictionary *)fnCallback([WXUtility class], originalSelector);
            data[@"rootDir"] = [NSBundle mainBundle].bundlePath;
            return data;
        };
        IMP hookCallbackImpl = imp_implementationWithBlock(hookBlock);
        method_setImplementation(originalMethod, hookCallbackImpl);
    });

    [WXSDKEngine initSDKEnvironment];
    CDVPluginResult* pluginResult = nil;
    pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK];
    [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
}

- (void)getEnvironment:(CDVInvokedUrlCommand *)command
{
    NSMutableDictionary *data = (NSMutableDictionary *)[WXUtility getEnvironment];
    CDVPluginResult* pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsDictionary:data];
    [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
}

- (void)callNativeModule:(CDVInvokedUrlCommand *)command
{
    WXJSCallNativeModule callNativeModuleBlock = ^NSInvocation*(NSString *instanceId, NSString *moduleName, NSString *methodName, NSObject *arguments, NSDictionary *options) {

        WXSDKInstance *instance = [WXSDKManager instanceForID:instanceId];
        if (!instance) {
            instance = [[WXSDKInstance alloc] init];
            // 修改 Main.storageboard，UIViewController, Menu => Editor -> embbed -> navigationController
            instance.viewController = [self navigationController];
//            instance.frame = CGRectMake(0, 0, 0, 0);

            instance.rootView = [UIApplication sharedApplication].keyWindow;
//            [[WXSDKManager bridgeMgr] createInstance:instanceId template:@"// { \"framework\": \"Vue\" }" options:@{} data:nil];

        }

#ifdef DEBUG
        WXLogDebug(@"flexLayout -> action: callNativeModule : %@ . %@",moduleName,methodName);
#endif
        NSMutableDictionary * newOptions = options ? [options mutableCopy] : [NSMutableDictionary new];
        NSMutableArray * newArguments = [arguments mutableCopy];

        WXModuleMethod *method = [[WXModuleMethod alloc] initWithModuleName:moduleName methodName:methodName arguments:[newArguments copy] options:[newOptions copy] instance:instance];
        if(![moduleName isEqualToString:@"dom"] && instance.needPrerender){
            [WXPrerenderManager storePrerenderModuleTasks:method forUrl:instance.scriptURL.absoluteString];
            return nil;
        }
        return [method invoke];
    };

    NSString* moduleNameString = [command.arguments objectAtIndex:0];
    NSString* methodNameString = [command.arguments objectAtIndex:1];
    NSObject *argObj = [command.arguments objectAtIndex:2];
    NSDictionary *optionsDic = [command.arguments objectAtIndex:3];
    if ([optionsDic isKindOfClass:[NSNull class]]) {
        optionsDic = @{};
    }

    NSString *instanceIdString = @"0";
    NSInvocation *invocation = callNativeModuleBlock(instanceIdString, moduleNameString, methodNameString, [NSArray arrayWithObject:argObj], optionsDic);

    CDVPluginResult* pluginResult = nil;
    pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK];
    [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
}

- (void)openWindow:(CDVInvokedUrlCommand *)command
{
    if ([command.arguments count] == 0) { return; }
    
    CDVPluginResult* pluginResult = nil;
    NSDictionary *args = @{};
    ZenViewController *viewController = [[ZenViewController alloc] init];

    NSDictionary *options = [command.arguments objectAtIndex:0];
    do {
        if (!options || !options[@"url"]) {
            pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR messageAsString:@"missing url param"];
            break;
        }

        NSString *rootDir = [NSString stringWithFormat:@"file://%@", [NSBundle mainBundle].bundlePath];
        NSString *fullpath = [options[@"url"] stringByReplacingOccurrencesOfString:@"local://" withString:rootDir];

        // 判断路径是否有效
        if ([fullpath hasPrefix:@"file://"]) {
            NSString *pathCheck = [fullpath substringFromIndex:7];
            if (![[NSFileManager defaultManager] fileExistsAtPath:pathCheck]) {
                pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR messageAsString:@"invalid url"];
                break;
            }
        }
        viewController.sourceURL = [NSURL URLWithString:fullpath];

        // 传入 WXEnvironment 参数
        if (options[@"args"]) {
            args = options[@"args"];
        }
        viewController.args = args;

        BOOL animated = YES;
        if (options[@"animated"]) {
            animated = [options[@"animated"] boolValue];
        }

        // 设置屏幕方向
        NSString *orientation = @"portrait";
        if (options[@"orientation"]) {
            orientation = options[@"orientation"];
        }
        if ([orientation isEqual:@"landscape"]) {
            viewController.pageOrientation = UIInterfaceOrientationMaskLandscapeLeft;
        } else {
            viewController.pageOrientation = UIInterfaceOrientationMaskPortrait;
        }

        UINavigationController *navigator = [self navigationController];
        if (!navigator) {
            pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR messageAsString:@"not found UINavigationController"];
            break;
        }
        
        [navigator pushViewController:viewController animated:animated];
        pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK];
    } while(0); // end while

    [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
}

- (void)closeWindow:(CDVInvokedUrlCommand*)command;
{
    CDVPluginResult* pluginResult = nil;
    BOOL animated = YES;

    if ([command.arguments count] > 0) {
        NSDictionary *options = [command.arguments objectAtIndex:0];
        if (options[@"animated"]) {
            animated = [options[@"animated"] boolValue];
        }
    }

    UINavigationController *navigator = [self navigationController];
    if (!navigator) {
        pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR messageAsString:@"not found UINavigationController"];
    } else {
        [navigator popViewControllerAnimated:animated];
        pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK];
    }

    [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
}

- (UINavigationController *)navigationController {
    return (UINavigationController *)[UIApplication sharedApplication].keyWindow.rootViewController;
}

@end
