import mist from './index.js';

// var crypto = require("crypto");

// var image_data = fs.readFileSync("image.bin");
// console.log("image.bin length is ", image_data.length);
// var image_hash = crypto.createHash('sha256').update(image_data).digest();

export class OtaUpdater {
    canceled: bool = false;

    otaUpdate(peer, image, cb) {
        console.log("Commencing OTA update with", peer.name);
        var self = this;
        var hash = image.hash;
        var image_data = image.data;
        var image_size = image.data.length;
        var timeout;
        self.canceled = false;

        if (typeof cb !== "function") { cb = () => {} }
        
        console.log("image.bin length is ", image_size);
        
        var ota_next = function(offset, num_bytes) {
            if (self.canceled) { clearTimeout(timeout); cb(0); return; }

            const progress = Number.parseFloat(offset/image_size*100).toFixed(2);

            cb(progress);

            console.log("Uploading at offset", offset, "num bytes", num_bytes, "("+ progress +"%)");
            mist.request('mist.control.invoke', [peer, 'ota', { op: "ota_push", hash: hash, offset: offset, data: image_data.slice(offset, offset + num_bytes)} ], ota_callback);

            clearTimeout(timeout);

            timeout = setTimeout(invokeTimeout, 10*1000);;
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
                            clearTimeout(timeout);
                        });
                }, 10000);
            }
            else {
                console.log("Unexepected ota data", data);
            }
        }
        
        function invokeTimeout() {
            console.log("invoke timeout!");
            cb(0);
            mist.request('mist.control.invoke', [peer, 'ota', { op: "ota_resync", hash: hash }], ota_callback);
        }
        
        
        mist.request('mist.control.invoke', [peer, 'ota', { op: "ota_begin", hash: hash, size: image_size } ], ota_callback);
    }

    cancel() {
        this.canceled = true;
    }
    
}

window.OtaUpdater = OtaUpdater;


