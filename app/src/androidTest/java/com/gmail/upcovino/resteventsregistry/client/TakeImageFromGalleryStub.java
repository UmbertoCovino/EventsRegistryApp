package com.gmail.upcovino.resteventsregistry.client;

import android.app.Activity;
import android.app.Instrumentation;
import android.content.Intent;
import android.net.Uri;
import androidx.test.runner.intent.IntentCallback;
import androidx.test.runner.intent.IntentMonitorRegistry;

import static androidx.test.espresso.intent.Intents.intending;
import static androidx.test.espresso.intent.matcher.IntentMatchers.hasAction;

public class TakeImageFromGalleryStub {
    private static final String GALLERY_PHOTO_ID = "/205"; // è un intero che identifica la foto ma non so da dove vien fuori; l'ho preso stampandolo dalla RegistrationActivity durante una normale interazione con l'app (mentre prendevo una foto dalla galleria)

    public static void exec() {
        // Gallery handling by stub ----------------------------------------------------------------

        final Intent resultData = new Intent(Intent.ACTION_PICK,
                android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);

        IntentCallback intentCallback = new IntentCallback() {
            @Override
            public void onIntentSent(Intent intent) {
                if (intent.getAction().equals(Intent.ACTION_CHOOSER)) {
                    if (intent.hasExtra(Intent.EXTRA_INITIAL_INTENTS)) {
                        Intent pickPhotoIntent = (Intent) intent.getParcelableArrayExtra(Intent.EXTRA_INITIAL_INTENTS)[1]; // not used but important to have

                        resultData.setData(Uri.parse(resultData.getData() + GALLERY_PHOTO_ID));
                        //Log.i("TEST_LOG", resultData.getData() + "");
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
