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
package fi.controlthings.rnmist;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import androidx.core.content.FileProvider;

import com.facebook.react.bridge.ReactApplicationContext;

import java.io.File;
import java.io.FileOutputStream;

public class ShareFile {

    private static final String FILE_NAME = "Mist-Contact.html";
    private static final String SHARE_MSG = "Share Mist contact using!";

    private final ReactApplicationContext reactContext;
    private String shareMsg;


    public ShareFile(ReactApplicationContext reactContext) {
        this.reactContext = reactContext;
    }

    protected File createHtmlFile(String fileContent) {
        File file = new File(this.reactContext.getCacheDir(), FILE_NAME);
        try {
            FileOutputStream outputStream = new FileOutputStream(file);
            outputStream.flush();
            outputStream.write(fileContent.getBytes());
            outputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        return file;
    }

    protected void share(File file) {

        final String authority = ((MistApplication) reactContext.getApplicationContext()).getFileProviderAuthority();
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/html");
        intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        Uri uri = FileProvider.getUriForFile(this.reactContext, authority+".provider", file);
        intent.putExtra(Intent.EXTRA_STREAM, uri);
        PackageManager pm = this.reactContext.getPackageManager();
        if (intent.resolveActivity(pm) != null) {
            this.reactContext.startActivity(Intent.createChooser(intent, shareMsg == null ? SHARE_MSG : shareMsg));
        }
    }

    protected void setShareMsg(String shareMsg) {
        this.shareMsg = shareMsg;
    }
}
