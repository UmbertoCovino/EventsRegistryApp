package com.gmail.upcovino.resteventsregistry.client;

import android.app.Activity;
import android.app.Instrumentation;
import android.content.Intent;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.support.v4.content.FileProvider;
import android.util.Log;

import com.gmail.upcovino.resteventsregistry.BuildConfig;

import java.io.File;

import androidx.test.runner.intent.IntentCallback;
import androidx.test.runner.intent.IntentMonitorRegistry;

import static androidx.test.espresso.intent.Intents.intending;
import static androidx.test.espresso.intent.matcher.IntentMatchers.hasAction;
import static androidx.test.platform.app.InstrumentationRegistry.getInstrumentation;

public class TakeImageFromGalleryStub {
    private static final String GALLERY_PHOTO_PATH = "/storage/emulated/0/Download/test.jpg";
    private static final Object lock = new Object();

    public static void exec() {
        synchronized(lock) {
            MediaScannerConnection.scanFile(getInstrumentation().getTargetContext(),
                    new String[]{new File(GALLERY_PHOTO_PATH).getAbsolutePath()},
                    null,
                    (path, uri) -> {
                        synchronized(lock) {
                            continueExec(uri);
                            lock.notifyAll();
                        }
                    });

            try {
                lock.wait();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private static void continueExec(Uri outputUri) {
        // Gallery handling by stub ----------------------------------------------------------------

        final Intent resultData = new Intent(Intent.ACTION_PICK,
                android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);

        IntentCallback intentCallback = new IntentCallback() {
            @Override
            public void onIntentSent(Intent intent) {
                if (intent.getAction().equals(Intent.ACTION_CHOOSER)) {
                    if (intent.hasExtra(Intent.EXTRA_INITIAL_INTENTS)) {
                        resultData.setData(outputUri);
                    }
                }
            }
        };
        IntentMonitorRegistry.getInstance().addIntentCallback(intentCallback);

        // Stubbing
        intending(hasAction(Intent.ACTION_CHOOSER)).respondWith(
                new Instrumentation.ActivityResult(Activity.RESULT_OK, resultData));

        // End gallery stub ------------------------------------------------------------------------
    }
}