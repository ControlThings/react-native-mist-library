/**
 * Copyright (C) 2020, ControlThings Oy Ab
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may
 * not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * @license Apache-2.0
 */

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

#define TEMP_STORAGE_FILENAME @"temp_storage.txt"

RCT_EXPORT_METHOD(tempStorageGet: (RCTPromiseResolveBlock)resolve
                  rejecter:(RCTPromiseRejectBlock)reject)
{
    RCTLogInfo(@"Actually reading file");
    
    NSArray *paths = NSSearchPathForDirectoriesInDomains(NSDocumentDirectory, NSUserDomainMask, YES);
    NSString *documentsDirectory = [paths objectAtIndex:0];
    NSString *filePath = [documentsDirectory stringByAppendingPathComponent:TEMP_STORAGE_FILENAME];
    NSString *str = [NSString stringWithContentsOfFile:filePath encoding:NSUTF8StringEncoding error:NULL];
    
    if (str == nil) {
        resolve(@"null");
    }
    else {
        resolve(str);
    }
}

RCT_EXPORT_METHOD(tempStorageSet:(NSString*) data
                  resolver:(RCTPromiseResolveBlock)resolve
                  rejecter:(RCTPromiseRejectBlock)reject)
{
    RCTLogInfo(@"Actually writing file");
    
    NSArray *paths = NSSearchPathForDirectoriesInDomains(NSDocumentDirectory, NSUserDomainMask, YES);
    NSString *documentsDirectory = [paths objectAtIndex:0];
    NSString *filePath = [documentsDirectory stringByAppendingPathComponent:TEMP_STORAGE_FILENAME];
    
    [data writeToFile:filePath atomically:TRUE encoding:NSUTF8StringEncoding error:NULL];
    
    resolve(@"true");
}

RCT_EXPORT_METHOD(getIdentities){
    RCTLogInfo(@"getIdentities not implemented");
}


@end
  
