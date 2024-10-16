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
import bson from './bson.js';
import { Buffer } from 'buffer'
import { NativeEventEmitter } from 'react-native';
import { NativeModules, Platform } from 'react-native';

// TODO: fix issue on Android so we can use the name RNMistLibrary on both platforms.
var lib;
if (Platform.OS === 'ios') {
    const { RNMistLibrary } = NativeModules;
    lib = RNMistLibrary;
} else {
    const { RNMistLibrary } = NativeModules;
    lib = RNMistLibrary;
}

var MistModule = lib;

var BSON = bson().BSON;

var base64encode;
var base64decode;

/*
 * base64-arraybuffer
 * https://github.com/niklasvh/base64-arraybuffer
 *
 * Copyright (c) 2012 Niklas von Hertzen
 * Licensed under the MIT license.
 */
(function(){
  "use strict";

  var chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/";

  // Use a lookup table to find the index.
  var lookup = new Uint8Array(256);
  for (var i = 0; i < chars.length; i++) {
    lookup[chars.charCodeAt(i)] = i;
  }

  base64encode = function(arraybuffer) {
    var bytes = new Uint8Array(arraybuffer),
    i, len = bytes.length, base64 = "";

    for (i = 0; i < len; i+=3) {
      base64 += chars[bytes[i] >> 2];
      base64 += chars[((bytes[i] & 3) << 4) | (bytes[i + 1] >> 4)];
      base64 += chars[((bytes[i + 1] & 15) << 2) | (bytes[i + 2] >> 6)];
      base64 += chars[bytes[i + 2] & 63];
    }

    if ((len % 3) === 2) {
      base64 = base64.substring(0, base64.length - 1) + "=";
    } else if (len % 3 === 1) {
      base64 = base64.substring(0, base64.length - 2) + "==";
    }

    return base64;
  };

  base64decode =  function(base64) {
    var bufferLength = base64.length * 0.75,
    len = base64.length, i, p = 0,
    encoded1, encoded2, encoded3, encoded4;

    if (base64[base64.length - 1] === "=") {
      bufferLength--;
      if (base64[base64.length - 2] === "=") {
        bufferLength--;
      }
    }

    var bytes = new Uint8Array(bufferLength);
    
    for (i = 0; i < len; i+=4) {
      encoded1 = lookup[base64.charCodeAt(i)];
      encoded2 = lookup[base64.charCodeAt(i+1)];
      encoded3 = lookup[base64.charCodeAt(i+2)];
      encoded4 = lookup[base64.charCodeAt(i+3)];

      bytes[p++] = (encoded1 << 2) | (encoded2 >> 4);
      bytes[p++] = ((encoded2 & 15) << 4) | (encoded3 >> 2);
      bytes[p++] = ((encoded3 & 3) << 6) | (encoded4 & 63);
    }

    return bytes;
  };
})();

function receive(data) {
    // console.log("receive:" , BSON.deserialize(data));
    rpc.response(BSON.deserialize(data));
}

function send(data) {
  //console.log("sending data", data, MistModule);
  var msg = BSON.serialize(data);
  MistModule.wishApp(base64encode(msg));
}

var api = {
    handlers: {},
    send: function(args, cb) {
        send(args, cb);
    },
    cancel: function(id) {
        rpc.cancel(id);
    },
    on: function(signal, callback) {
        api.handlers[signal] = callback;
    },
    connectWebSocketProxy: function(proxy) {
        connectWebSocket(proxy);
    },
    proxyDisconnect: function() {
        proxySocket.close();
    }
};

var rpc = {
    id: 1,
    reqs: {},
    // make a rpc request
    request: function(op, args, cb) {
        if(!cb) { cb = (err, data) => { console.log(rpc.id, op, 'cb:', err, data); rpc.result = data; } }
        rpc.reqs[rpc.id] = cb;
        send({ op: op, args: args, id: rpc.id });
        //setTimeout((function(id) { return function() { if (typeof rpc.reqs[id] === 'function') { console.log('request', op, 'timed out.'); rpc.reqs[id]({ timeout: true }); delete rpc.reqs[id]; } }; })(rpc.id), 2000);
        return rpc.id++;
    },
    // cancel rpc request by id
    cancel: function(id) {
        if (typeof rpc.reqs[id] !== 'function') { console.log('could not cancel request '+ id +': No such request is active.'); return; }
        send({ end: id });
        delete rpc.reqs[id];
    },
    // handle responses coming back to rpc
    response: function(msg) {
        if (msg.ack && typeof rpc.reqs[msg.ack] === 'function') {
            rpc.reqs[msg.ack](null, msg.data);
            delete rpc.reqs[msg.ack];
        } else if (msg.sig && typeof rpc.reqs[msg.sig] === 'function') {
            rpc.reqs[msg.sig](null, msg.data);
        } else if (msg.err && typeof rpc.reqs[msg.err] === 'function') {
            rpc.reqs[msg.err](true, msg.data);
            delete rpc.reqs[msg.err];
        }
    },
    // dynamically create the api from methods in remote rpc server
    methods: function(cb) {
        rpc.request('methods', [], function(err, data) {
            for (var i in data) {
                var path = i.split('.');
                var node = rpc;
                while (path.length>1) {
                    if (!node[path[0]]) {
                        node[path[0]] = {};
                    }
                    node = node[path[0]];
                    path.shift();
                }

                node[path[0]] = (function(i) { 
                    return function() {
                        var args = [];
                        var cb = arguments[arguments.length-1];

                        if ( typeof cb !== 'function') { 
                            cb = function(err, data) { console.log(i+'('+reqId+'):', err, data); rpc.result = data; rpc.err = err; }; 
                            for (var j=0; j < arguments.length; j++) {
                                args.push(arguments[j]);
                            }
                        } else {
                            for (var j=0; j < arguments.length-1; j++) {
                                args.push(arguments[j]);
                            }
                        }

                        var reqId = rpc.request(i, args, function() { api.result = arguments[1]; cb.apply(this, arguments); });
                        return { reqId: reqId };
                    };
                })(i);
            }

            cb();
        });
    }
};

const mistEmitter = new NativeEventEmitter( MistModule );

mistEmitter.addListener('wishApp', (e) => {
   // console.log('mist-res in NativeEventEmitter listener', typeof e, e.length, base64decode(e) instanceof Uint8Array );

    receive(new Buffer(base64decode(e)));
});

rpc.methods(() => {
    // console.log('Methods done.');
});

window.WishApp = rpc;

module.exports = {
  WishApp: rpc
}
