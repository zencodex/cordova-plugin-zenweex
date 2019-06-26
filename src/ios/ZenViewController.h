//
//  ZenViewController.h
//
//  Author: 扣丁禅师 <v@yinqisen.cn>
//

#import <UIKit/UIKit.h>

NS_ASSUME_NONNULL_BEGIN

@interface ZenViewController : UIViewController

@property (nonatomic, strong) NSURL *sourceURL;
@property (nonatomic, assign) int pageOrientation;
@property (nonatomic, strong) NSDictionary *args;

@end

NS_ASSUME_NONNULL_END
