
package com.reactlibrary;

import android.util.Log;

import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.Callback;

public class RNMistLibraryModule extends ReactContextBaseJavaModule {

  private final ReactApplicationContext reactContext;

  public RNMistLibraryModule(ReactApplicationContext reactContext) {
    super(reactContext);
    this.reactContext = reactContext;
  }

  @Override
  public String getName() {
    return "RNMistLibrary";
  }

  @ReactMethod
  public void send(String message) {
      Log.d("RNMistLibrary", "send msg: " + message);
/*
      byte[] args = Base64.decode(message, Base64.DEFAULT);

      //WishApp.getInstance().bsonConsolePrettyPrinter("bson from ui", args);

      Sandboxed.request(sid, args, new Sandboxed.SandboxedCb() {
          @Override
          public void cb(byte[] bson) {
              final String base64String = Base64.encodeToString(bson, Base64.NO_WRAP);
              sendString(getReactApplicationContext(), "mist-rpc", base64String);
          }
      });
*/
  }  
}