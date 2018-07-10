
#import "RNMistLibrary.h"
#import "React/RCTLog.h"

#import "MistPort.h"
#import "Sandbox.h"
#import "PassthroughRequest.h"

Sandbox *sandbox;

@implementation RNMistLibrary

- (id) init {
    self = [super init];
    
    [MistPort launchWish];
    [MistPort launchMistApi];
    
    /* Run with a hard-coded, pre-defined Sandbox ID:
     [0xaa 0xaa 0xaa ...] */
    const size_t sandbox_id_len = 32;
    char sandbox_id[sandbox_id_len];
    memset(sandbox_id, 0, sandbox_id_len);
    for (int i = 0; i < sandbox_id_len; i++) {
        sandbox_id[i] = 0xaa;
    }
    NSData* sandboxId = [[NSData alloc] initWithBytes:sandbox_id length:sandbox_id_len];
    sandbox = [[Sandbox alloc] initWithSandboxId:sandboxId callback:^(NSData *responseData) {
        NSString *base64Encoded = [responseData base64EncodedStringWithOptions:0];
        //NSLog(@"mist-rpc sandbox callback with response len=%lu", responseData.length);
        //
        [self sendEventWithName:@"sandboxed" body:base64Encoded];
    }];
    self.hasLogined = NO;
    
    [PassthroughRequest setMistApiCallback:^(NSData *responseData) {
        NSString *base64Encoded = [responseData base64EncodedStringWithOptions:0];
        [self sendEventWithName:@"mistApi" body:base64Encoded];
    }];
    
    [PassthroughRequest setWishApiCallback:^(NSData *responseData) {
        NSString *base64Encoded = [responseData base64EncodedStringWithOptions:0];
        [self sendEventWithName:@"wishApp" body:base64Encoded];
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
    return @[@"sandboxed", @"mistApi", @"wishApp"];
}

RCT_EXPORT_METHOD(sandboxed:(NSString *)base64Data)
{
    if (self.hasLogined == NO) {
        [sandbox login];
        self.hasLogined = YES;
    }
    
    NSData *unmarshalled = [[NSData alloc] initWithBase64EncodedString:base64Data options:0];
    //RCTLogInfo(@"mist-rpc send, with data len=%lu", unmarshalled.length);


    [sandbox requestWithData:unmarshalled];
    
}

RCT_EXPORT_METHOD(mistApi:(NSString *)base64Data)
{
    if (self.hasLogined == NO) {
        [sandbox login];
        self.hasLogined = YES;
    }
    
    NSData *unmarshalled = [[NSData alloc] initWithBase64EncodedString:base64Data options:0];
    //RCTLogInfo(@"mist-rpc send, with data len=%lu", unmarshalled.length);
    
    
    [PassthroughRequest mistApiRequestWithData:unmarshalled];
    
}

RCT_EXPORT_METHOD(wishApp:(NSString *)base64Data)
{
    NSData *unmarshalled = [[NSData alloc] initWithBase64EncodedString:base64Data options:0];
    //RCTLogInfo(@"mist-rpc send, with data len=%lu", unmarshalled.length);
    
    [PassthroughRequest wishApiRequestWithData:unmarshalled];
}

@end
  
