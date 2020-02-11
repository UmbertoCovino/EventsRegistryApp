package com.gmail.upcovino.resteventsregistry.client;

import android.app.Activity;
import android.app.Instrumentation;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;

import com.gmail.upcovino.resteventsregistry.R;

import java.io.IOException;
import java.io.OutputStream;

import androidx.test.runner.intent.IntentCallback;
import androidx.test.runner.intent.IntentMonitorRegistry;

import static androidx.test.espresso.intent.Intents.intending;
import static androidx.test.espresso.intent.matcher.IntentMatchers.hasAction;
import static androidx.test.platform.app.InstrumentationRegistry.getInstrumentation;

public class TakeImageFromCameraStub {

    public static void exec() {
        // Camera handling by stub ----------------------------------------------------------------

        final Intent resultData = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        IntentCallback intentCallback = new IntentCallback() {
            @Override
            public void onIntentSent(Intent intent) {
                if (intent.getAction().equals(Intent.ACTION_CHOOSER)) {
                    if (intent.hasExtra(Intent.EXTRA_INITIAL_INTENTS)) {
                        try {
                            Intent takePictureIntent = (Intent) intent.getParcelableArrayExtra(Intent.EXTRA_INITIAL_INTENTS)[0];

                            Uri imageUri = takePictureIntent.getParcelableExtra(MediaStore.EXTRA_OUTPUT);

                            // Converting Uri (FileProvider Uri) to String
//                            String[] filePathColumn = {MediaStore.Images.ImageColumns.DATA};
//
//                            Cursor cursor = getInstrumentation().getTargetContext().getContentResolver().query(imageUri, filePathColumn, null, null, null);
//                            cursor.moveToFirst();
//
//                            int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
//                            String imagePath = cursor.getString(columnIndex);
//
//                            Log.i("TEST_LOG", imagePath + " ");if (true) return;
                            // End converting

                            Bitmap icon = BitmapFactory.decodeResource(
                                    getInstrumentation().getTargetContext().getResources(),
                                    R.mipmap.ic_launcher);

                            OutputStream out = getInstrumentation().getTargetContext().getContentResolver().openOutputStream(imageUri);

                            icon.compress(Bitmap.CompressFormat.JPEG, 100, out);

                            out.flush();
                            out.close();
                        } catch (IOException e) {
                            Log.e("TAG_LOG", e.toString());
                        }
                    }
                }
            }
        };
        IntentMonitorRegistry.getInstance().addIntentCallback(intentCallback);

        // Stubbing
        intending(hasAction(Intent.ACTION_CHOOSER)).respondWith(
                new Instrumentation.ActivityResult(Activity.RESULT_OK, resultData));

        // End camera stub ------------------------------------------------------------------------
    }
}
