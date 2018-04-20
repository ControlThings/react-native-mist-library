
package com.reactlibrary;

import android.content.Intent;
import android.support.annotation.Nullable;
import android.util.Base64;
import android.util.Log;

import com.facebook.react.bridge.LifecycleEventListener;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.modules.core.DeviceEventManagerModule;

import addon.AddonReceiver;
import mist.api.Service;
import mist.api.request.Sandbox;
import mist.api.request.Sandboxed;

public class RNMistLibraryModule extends ReactContextBaseJavaModule implements LifecycleEventListener, AddonReceiver.Receiver {
    private static String TAG = "RNMist";

    private final ReactApplicationContext reactContext;

    Intent mistService;

    private byte[] sid = new byte[32];
    private String sandboxName = "ToastSandbox";

    public RNMistLibraryModule(ReactApplicationContext reactContext) {
        super(reactContext);
        this.reactContext = reactContext;

        reactContext.addLifecycleEventListener(this);

        // TODO: Fix this temporary sandbox id
        sid[0] = 0x32;

        mistService = new Intent(reactContext, Service.class);
        //mistService.putExtra("name", "eWind_react_native");

    }

    @Override
    public String getName() {
    return "RNMistLibrary";
    }

    @Override
    public void onHostResume() {
        Log.d(TAG, "onHostResume");

        AddonReceiver mistReceiver = new AddonReceiver(this);

        mistService.putExtra("receiver", mistReceiver);

        getReactApplicationContext().startService(mistService);
    }

    @Override
    public void onHostPause() {
        Log.d(TAG, "onHostPause");
    }

    @Override
    public void onHostDestroy() {
        Log.d(TAG, "onHostDestroy");

    }

    @Override
    public void onConnected() {
        Log.d(TAG, "onConnected (to service?)");

        Sandbox.create(sid, sandboxName, new Sandbox.CreateCb() {
            @Override
            public void cb(boolean b) {
                emit(getReactApplicationContext(), "ctEvent", "sandbox.create cb");
            }

            @Override
            public void err(int i, String s){
                Log.d(TAG, "Sandbox create error:" + s);
            }

            @Override
            public void end() {}
        });

    }

    @Override
    public void onDisconnected() {
        Log.d(TAG, "onDisconnected (to service?)");
    }

    /**
     * Emit event to ReactNative
     *
     * @param reactContext
     * @param eventName
     * @param params
     */
    private void emit(ReactContext reactContext, String eventName, @Nullable String params) {
        reactContext
                .getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class)
                .emit(eventName, params);
    }

    @ReactMethod
    public void send(String message) {
        Log.d(TAG, "send msg: " + message);

        byte[] args = Base64.decode(message, Base64.DEFAULT);

        //WishApp.getInstance().bsonConsolePrettyPrinter("bson from ui", args);

        Sandboxed.request(sid, args, new Sandboxed.SandboxedCb() {
            @Override
            public void cb(byte[] bson) {
                Log.d(TAG, "sandbox.request cb");
                final String base64String = Base64.encodeToString(bson, Base64.NO_WRAP);
                emit(getReactApplicationContext(), "mist-rpc", base64String);
            }
        });

    }

}