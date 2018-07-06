
package com.reactlibrary;

import android.content.Intent;
import android.support.annotation.Nullable;
import android.util.Base64;
import android.util.Log;

import com.facebook.react.bridge.LifecycleEventListener;
import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.modules.core.DeviceEventManagerModule;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;

import addon.AddonReceiver;
import mist.api.MistApi;
import mist.api.Service;
import mist.api.request.Commission;
import mist.api.request.Sandbox;
import mist.api.request.Sandboxed;
import wish.WishApp;
import wish.request.Connection;

public class RNMistLibraryModule extends ReactContextBaseJavaModule implements LifecycleEventListener, AddonReceiver.Receiver {

    private final String TAG = "E_WIND_RN";

    Intent mistService;

    private byte[] sid = new byte[32];
    private String sandboxName = "ReactNativeSandbox";

    private ReactApplicationContext reactContext;

    public RNMistLibraryModule(ReactApplicationContext reactContext) {
        super(reactContext);
        this.reactContext = reactContext;

        reactContext.addLifecycleEventListener(this);

        // TODO: Fix this temporary sandbox id
        sid[0] = 0x32;

        mistService = new Intent(reactContext, Service.class);


    }

    @Override
    public String getName() {
        return "RNMistLibraryModule";
    }

    private boolean mistRunning;

    @Override
    public void onHostResume() {
        Log.d(TAG, "onHostResume");

        synchronized (this) {
            if (!mistRunning) {
                AddonReceiver mistReceiver = new AddonReceiver(this);

                mistService.putExtra("receiver", mistReceiver);

                getReactApplicationContext().startService(mistService);
            }
        }

    }

    @Override
    public void onHostPause() {
        Log.d(TAG, "onHostPause");
        final RNMistLibraryModule mistModule = this;
        Commission.getState(new Commission.GetStateCb() {
            @Override
            public void cb(String currentState) {
                Log.d(TAG, "onHostPause, current commissioning state is " + currentState);
                switch (currentState) {
                    default:
                        Log.d(TAG, "onHostPause, not stopping Wish, commissioning in state " + currentState);
                        break;
                    case "COMMISSION_STATE_INITIAL":
                    case "COMMISSION_STATE_FINISHED_OK":
                    case "COMMISSION_STATE_FINISHED_FAIL":
                    case "COMMISSION_STATE_ABORTED":
                        Log.d(TAG, "onHostPause, stopping Wish, commissioning in state " + currentState);
                        Connection.disconnectAll(new Connection.DisconnectAllCb() {
                            @Override
                            public void cb(boolean b) {
                                synchronized (mistModule) {
                                    if (mistRunning) {
                                        getReactApplicationContext().stopService(mistService);
                                    }
                                }
                            }
                        });

                        break;
                }
            }
        });

    }

    @Override
    public void onHostDestroy() {
        Log.d(TAG, "onHostDestroy");

    }

    @Override
    public void onConnected() {
        Log.d(TAG, "onConnected (to service?)");
        synchronized (this) {
            mistRunning = true;
        }
        Sandbox.create(sid, sandboxName, new Sandbox.CreateCb() {
            @Override
            public void cb(boolean b) {
                Log.d(TAG, "sandbox login cb: " + b);

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
        Log.d(TAG, "onDisconnected (from service)");
        synchronized (this) {
            mistRunning = false;
        }
    }

    // REACT METHODS ---------------------------------------

    /**
     * TEMP STORAGE METHODS BECAUSE ASYNCSTORAGE IS BORK
     * https://github.com/facebook/react-native/issues/14101
     */
    String FILENAME = "ewind_settings";

    @ReactMethod
    public void tempStorageGet(Promise promise) {
        Log.d(TAG, "tempStorageGet()");
        try {
            InputStreamReader in = new InputStreamReader(getReactApplicationContext().openFileInput(FILENAME));
            BufferedReader bf = new BufferedReader(in);
            StringBuilder sb = new StringBuilder();
            String jsonString;
            while ((jsonString = bf.readLine()) != null) {
                sb.append(jsonString);
            }
            in.close();
            Log.d(TAG, "tempStorageGet() returns " + sb.toString());
            promise.resolve(sb.toString());

        } catch (FileNotFoundException fErr) {
            Log.d(TAG, "tempStorageGet() first time, no file found");
            // first time fails so return null
            promise.resolve("null");
        } catch (IOException ioErr) {
            Log.d(TAG, "tempStorageGet() ioErr");
            promise.reject(ioErr);
        }

    }

    @ReactMethod
    public void tempStorageSet(String jsonString, Promise promise) {
        Log.d(TAG, "tempStorageSet()" + jsonString);
        try {
            FileOutputStream fos = getReactApplicationContext().openFileOutput(FILENAME, getReactApplicationContext().MODE_PRIVATE);
            fos.write(jsonString.getBytes());
            fos.close();
            promise.resolve(true);
        } catch (FileNotFoundException fErr) {
            promise.reject(fErr);
        } catch (IOException ioErr) {
            promise.reject(ioErr);
        }
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
    public void sandboxed(String message) {
        Log.d(TAG, "send msg: " + message);

        byte[] args = Base64.decode(message, Base64.DEFAULT);

        WishApp.getInstance().bsonConsolePrettyPrinter(TAG, args);

        Sandboxed.request(sid, args, new Sandboxed.SandboxedCb() {
            @Override
            public void cb(byte[] bson) {
                Log.d(TAG, "sandbox.request cb");
                final String base64String = Base64.encodeToString(bson, Base64.NO_WRAP);
                emit(getReactApplicationContext(), "sandboxed", base64String);
            }
        });
    }

    @ReactMethod
    public void wishApp(String message) {
        Log.d(TAG, "send msg: " + message);

        byte[] args = Base64.decode(message, Base64.DEFAULT);

        WishApp.getInstance().bsonConsolePrettyPrinter(TAG, args);

        wish.request.RawRequest.request(args, new wish.request.RawRequest.RawRequestCb() {
            @Override
            public void cb(byte[] bson) {
                Log.d(TAG, "sandbox.request cb");
                final String base64String = Base64.encodeToString(bson, Base64.NO_WRAP);
                emit(getReactApplicationContext(), "wishApp", base64String);
            }
        });
    }


    @ReactMethod
    public void mistApi(String message) {
        Log.d(TAG, "send msg: " + message);

        byte[] args = Base64.decode(message, Base64.DEFAULT);

        WishApp.getInstance().bsonConsolePrettyPrinter(TAG, args);

        mist.api.request.RawRequest.request(args, new mist.api.request.RawRequest.RawRequestCb() {
            @Override
            public void cb(byte[] bson) {
                Log.d(TAG, "sandbox.request cb");
                final String base64String = Base64.encodeToString(bson, Base64.NO_WRAP);
                emit(getReactApplicationContext(), "mistApi", base64String);
            }
        });
    }
}
