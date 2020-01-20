package com.gmail.upcovino.resteventsregistry.client;

import android.util.Log;
import android.widget.Toast;

import com.gmail.upcovino.resteventsregistry.commons.Event;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.ParseException;
import java.util.Date;

public class Constants {
    public static final int IMAGE_PICKER = 1;
    public static final String BASE_URI = "http://10.0.0.2:8182/eventsRegistry/",
                               TAG = "EventsRegistry",
                               USER = "user",
                               EVENT = "event",
                               USER_EXTRA = "user",
                               USER_LOGGED = "userLogged",
                               EVENT_EXTRA = "event",
                               STORAGE_DIRECTORY_EXTRA = "storageDirectory",
                               USER_IMAGE_VIEW_PATH = "userImageViewPath",
                               EVENT_IMAGE_VIEW_PATH = "eventImageViewPath",
                               EVENTS_ARRAY = "eventsArray",
                               EVENTS_REGISTRY_GET_URI = "eventsRegistryGetUri",
                               FILTER_TEXT_VIEW_VISIBILITY = "filterTextViewVisibility",
                               FILTER_TEXT_VIEW_CONTENT = "filterTextViewContent",
                               FILTER_FROM_DATE = "filterFromDate",
                               FILTER_FROM_TIME = "filterFromTime",
                               FILTER_TO_DATE = "filterToDate",
                               FILTER_TO_TIME = "filterToTime",
                               SUPPORT_ACTION_BAR_TITLE = "supportActionBarTitle",
                               CHECKED_MENU_ITEM_ID = "checkedMenuItemId",
                               CAMERA_IMAGE_FILE_PATH = "cameraImageFilePath";

    public static void copyFile(File src, File dst) throws IOException {
        if (dst.exists()) {
            dst.delete();
            dst.createNewFile();
        }

        InputStream in = new FileInputStream(src);
        try {
            OutputStream out = new FileOutputStream(dst);
            try {
                // Transfer bytes from in to out
                byte[] buf = new byte[1024];
                int len;
                while ((len = in.read(buf)) > 0) {
                    out.write(buf, 0, len);
                }
            } finally {
                out.close();
            }
        } finally {
            in.close();
        }
    }

    static class DateTypeAdapter extends TypeAdapter<Date> {

        @Override
        public void write(JsonWriter out, Date value) throws IOException {
            if (value != null)
                out.value(Event.DATETIME_SDF.format(value));
            else
                out.nullValue();
        }

        @Override
        public Date read(JsonReader in) throws IOException {
            try {
                return Event.DATETIME_SDF.parse(in.nextString());
            } catch (ParseException e) {
                throw new IOException("Invalid format for datetime: correct one is 'yyyy-MM-dd HH:mm:ss'.");
            }
        }
    }
}
