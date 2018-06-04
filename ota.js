import mist from './index.js';

// var crypto = require("crypto");

// var image_data = fs.readFileSync("image.bin");
// console.log("image.bin length is ", image_data.length);
// var image_hash = crypto.createHash('sha256').update(image_data).digest();

export class OtaUpdater {
    otaUpdate(peer, image) {
        console.log("Commencing OTA update with", peer.name);
        var hash = image.hash;
        var image_data = image.data;
        var image_size = image.data.length;
        
        console.log("image.bin length is ", image_size);
        
        var ota_next = function(offset, num_bytes) {   
            console.log("Uploading at offset", offset, "num bytes", num_bytes, "("+ Number.parseFloat(offset/image_size*100).toFixed(2)+"%)");
            mist.request('mist.control.invoke', [peer, 'ota', { op: "ota_push", hash: hash, offset: offset, data: image_data.slice(offset, offset + num_bytes)} ], ota_callback);
            if (typeof global_timer === 'undefined') {
                global_timer = setTimeout(invokeTimeout, 10*1000);
                
            }
            else {
                clearTimeout(global_timer);
                global_timer = setTimeout(invokeTimeout, 10*1000);;
            }
            
        }
        
        var ota_callback = function (err, data) {
            if (err) {
                console.log("Invoke error:", data);
                return;
            }
            
            if (data.op === "ota_next") {
                //console.log("ota next!");
                ota_next(data.next_offset, data.block_size);
                
            }
            else if (data.op === "ota_finish") {
                console.log("ota is finished!", data);
                console.log("Rebooting target in 5 seconds");
                setTimeout(function() {
                    console.log("Rebooting now");
                    mist.request('mist.control.invoke', [peer, 'ota', { op: "ota_reboot", hash: hash} ], 
                        function(err, data) { 
                            if (err) { 
                                console.log(err);
                            } 
                            console.log(data); 
                            clearTimeout(global_timer);
                        });
                }, 10000);
            }
            else {
                console.log("Unexepected ota data", data);
            }
        }
        
        function invokeTimeout() {
            console.log("invoke timeout!");
            mist.request('mist.control.invoke', [peer, 'ota', { op: "ota_resync", hash: hash }], ota_callback);
        }
        
        
        mist.request('mist.control.invoke', [peer, 'ota', { op: "ota_begin", hash: hash, size: image_size } ], ota_callback);
    }
    
}

window.OtaUpdater = OtaUpdater;


