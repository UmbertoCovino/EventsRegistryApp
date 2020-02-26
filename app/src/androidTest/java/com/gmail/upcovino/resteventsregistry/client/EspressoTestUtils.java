package com.gmail.upcovino.resteventsregistry.client;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.view.View;

import com.gmail.upcovino.resteventsregistry.commons.Event;
import com.gmail.upcovino.resteventsregistry.commons.User;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.hamcrest.Matcher;
import org.restlet.data.ChallengeScheme;
import org.restlet.data.MediaType;
import org.restlet.representation.StringRepresentation;
import org.restlet.resource.ClientResource;

import java.io.IOException;
import java.util.Calendar;
import java.util.Date;

import androidx.test.espresso.UiController;
import androidx.test.espresso.ViewAction;
import androidx.test.espresso.intent.Intents;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.matcher.ViewMatchers.isRoot;
import static androidx.test.platform.app.InstrumentationRegistry.getInstrumentation;

public class EspressoTestUtils {
    public final static String TEST_USER_NAME = "LOGIN_NAME_ETEST";
    public final static String TEST_USER_SURNAME = "LOGIN_SURNAME_ETEST";
    public final static String TEST_USER_EMAIL = "LOGIN_ETEST@gmail.com";
    public final static String TEST_USER_PASSWORD = "PASSWORD_ETEST";

    public final static String TEST_EVENT_TITLE = "TITLE_ETEST";
    public final static String TEST_EVENT_DESCRIPTION = "DESCRIPTION_ETEST";

    public final static int START_DATE_DAY;
    public final static int START_DATE_MONTH;
    public final static int START_DATE_YEAR;
    public final static int END_DATE_DAY;
    public final static int END_DATE_MONTH;
    public final static int END_DATE_YEAR;

    static {
        Calendar cal = Calendar.getInstance();
        cal.setTime(new Date());
        cal.add(Calendar.DATE, 1); // tomorrow
        START_DATE_DAY = cal.get(Calendar.DATE);
        START_DATE_MONTH = cal.get(Calendar.MONTH) + 1;
        START_DATE_YEAR = cal.get(Calendar.YEAR);

        cal.add(Calendar.DATE, 1); // the day after tomorrow
        END_DATE_DAY = cal.get(Calendar.DATE);
        END_DATE_MONTH = cal.get(Calendar.MONTH) + 1;
        END_DATE_YEAR = cal.get(Calendar.YEAR);

        //Log.e("TAG", START_DATE_DAY+"-"+START_DATE_MONTH+"-"+START_DATE_YEAR+" -> "+END_DATE_DAY+"-"+END_DATE_MONTH+"-"+END_DATE_YEAR);
    }

    public final static Gson GSON = new GsonBuilder()
            .registerTypeAdapter(Date.class, new Constants.DateTypeAdapter())
            .create();
    private static User user;



    public static void resetSharedPref(){
        Context appContext = getInstrumentation().getTargetContext();

        SharedPreferences preferences =
                PreferenceManager.getDefaultSharedPreferences(appContext);

        SharedPreferences.Editor editor = preferences.edit();
        editor.clear();
        editor.commit();
    }

    public static void userRegistration() throws IOException {
        user = new User(TEST_USER_NAME, TEST_USER_SURNAME, TEST_USER_EMAIL, TEST_USER_PASSWORD);
        ClientResource cr = new ClientResource(Constants.BASE_URI + "users");
        String jsonResponse = null;

        StringRepresentation sr = new StringRepresentation(GSON.toJson(user),
                MediaType.APPLICATION_JSON);
        jsonResponse = cr.post(sr).getText();

        deleteAllEvents();
    }

    public static void deleteAllEvents() throws IOException {
        ClientResource cr = new ClientResource(Constants.BASE_URI + "events");
        cr.setChallengeResponse(ChallengeScheme.HTTP_BASIC, TEST_USER_EMAIL, TEST_USER_PASSWORD);
        String jsonResponse = cr.delete().getText();
    }

    public static void deleteUser() throws IOException {
        if (user == null)
            user = new User(TEST_USER_NAME, TEST_USER_SURNAME, TEST_USER_EMAIL, TEST_USER_PASSWORD);

        ClientResource cr = new ClientResource(Constants.BASE_URI + "users/" + user.getEmail());
        String jsonResponse = null;
        cr.setChallengeResponse(ChallengeScheme.HTTP_BASIC, user.getEmail(), user.getPassword());

        jsonResponse = cr.delete().getText();
    }

    public static void addEvent(Event e) throws IOException {
        ClientResource cr = new ClientResource(Constants.BASE_URI + "events");
        cr.setChallengeResponse(ChallengeScheme.HTTP_BASIC, TEST_USER_EMAIL, TEST_USER_PASSWORD);
        StringRepresentation sr = new StringRepresentation(GSON.toJson(e), MediaType.APPLICATION_JSON);
        String jsonResponse = cr.post(sr).getText();
    }

    public static void initIntent() {
        Intents.init();
    }

    public static void releaseIntent() {
        Intents.release();
    }

    public static String prependZeroToMonth(int monthNum) {
        if (monthNum < 10)
            return "0" + monthNum;
        else
            return "" + monthNum;
    }

    public static void waitFor(final long millis) {
        onView(isRoot()).perform(waitFor2(millis));
    }

    /**
     * Perform action of waiting for a specific time.
     *
     * Use it with subsequent code:
     *
     *      onView(isRoot()).perform(waitFor2(VALUE_IN_MILLIS));
     */
    private static ViewAction waitFor2(final long millis) {
        return new ViewAction() {
            @Override
            public Matcher<View> getConstraints() {
                return isRoot();
            }

            @Override
            public String getDescription() {
                return "Wait for " + millis + " milliseconds.";
            }

            @Override
            public void perform(UiController uiController, final View view) {
                uiController.loopMainThreadForAtLeast(millis);
            }
        };
    }
}
