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
//
//  MistApi.h
//  Mist
//
//  Created by Jan on 09/04/2018.
//  Copyright © 2018 Jan. All rights reserved.
//

#import "MistRequest.h"
#include "bson.h"

@interface MistApi : NSObject
+(void)startMistApi:(NSString *)appName;
+(int)mistApiRequestWithBson:(bson *)reqBson callback:(id <MistApiResponseHandler>)cb;
+(void)mistApiCancel:(int) rpcId;
+(int)wishApiRequestWithBson:(bson *)reqBson callback:(id <MistApiResponseHandler>)cb;
+(void)wishApiCancel:(int) rpcId;

+(void)connected:(id) param;
+(void)sendToMistApp:(NSArray *) params;
@end
