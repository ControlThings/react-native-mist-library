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
import { Platform, NativeModules} from 'react-native';
import bson from './bson.js';
import { Buffer } from 'buffer';
import { base64encode, base64decode } from './base64-buffer.js'

var BSON = bson().BSON;

const Share = (uid) => {
var MistModule;
if (Platform.OS === 'ios') {
    Console.log("Share functunality is not implemented for ios...");
    return;
} else {
    const { RNMistLibrary } = NativeModules;
    MistModule = RNMistLibrary;
}

WishApp.request("identity.export", [uid], (err, data) => {
    if (err) {console.log("identity.export error", data); return}
    const buffer = BSON.serialize(data);
    const contact = base64encode(buffer);
    const alias = BSON.deserialize(data.data).alias;
    MistModule.share(contact, alias, null);
  })
}

const ReceivedFriendrequest = (uid, contact) => {

    const buffer = new Buffer(base64decode(contact));
    const bsonContact = BSON.deserialize(buffer);
WishApp.request("identity.friendRequest", [uid, bsonContact], (err, data) => {
            if (err) {console.log("identity.friendRequest error", data); return}
            console.log("identity.friendRequest successfully sent...");
          })
}

export { Share, ReceivedFriendrequest };


