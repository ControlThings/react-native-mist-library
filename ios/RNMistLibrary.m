
#import "RNMistLibrary.h"
#import "React/RCTLog.h"

#import "MistPort.h"
#import "Sandbox.h"

Sandbox *sandbox;

@implementation RNMistLibrary

- (id) init {
    self = [super init];
    
    [MistPort launchWish];
    [MistPort launchMistApi];
    
    sandbox = [[Sandbox alloc] initWithCallback:^(NSData *responseData) {
        NSString *base64Encoded = [responseData base64EncodedStringWithOptions:0];
        NSLog(@"mist-rpc sandbox callback with response len=%lu", responseData.length);
        //
        [self sendEventWithName:@"mist-rpc" body:base64Encoded];
    }];
    
    
    return self;
}


- (dispatch_queue_t)methodQueue
{
    return dispatch_get_main_queue();
}
RCT_EXPORT_MODULE();

- (NSArray<NSString *> *)supportedEvents
{
    return @[@"mist-rpc"];
}

RCT_EXPORT_METHOD(send:(NSString *)base64Data)
{
    NSData *unmarshalled = [[NSData alloc] initWithBase64EncodedString:base64Data options:0];
    RCTLogInfo(@"mist-rpc send, with data len=%lu", unmarshalled.length);


    [sandbox requestWithData:unmarshalled];
    
}


@end
  
