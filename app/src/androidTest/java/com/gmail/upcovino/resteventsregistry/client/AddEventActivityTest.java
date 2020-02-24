package com.gmail.upcovino.resteventsregistry.client;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.DatePicker;

import com.gmail.upcovino.resteventsregistry.R;
import com.gmail.upcovino.resteventsregistry.commons.User;
import com.google.gson.Gson;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.Matchers;
import org.hamcrest.TypeSafeMatcher;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.restlet.data.ChallengeScheme;
import org.restlet.data.MediaType;
import org.restlet.representation.StringRepresentation;
import org.restlet.resource.ClientResource;

import java.io.IOException;
import java.util.Calendar;
import java.util.Date;

import androidx.test.espresso.ViewAction;
import androidx.test.espresso.ViewInteraction;
import androidx.test.espresso.contrib.PickerActions;
import androidx.test.espresso.intent.rule.IntentsTestRule;
import androidx.test.filters.LargeTest;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.rule.ActivityTestRule;
import androidx.test.rule.GrantPermissionRule;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.replaceText;
import static androidx.test.espresso.action.ViewActions.scrollTo;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withClassName;
import static androidx.test.espresso.matcher.ViewMatchers.withContentDescription;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static androidx.test.platform.app.InstrumentationRegistry.getInstrumentation;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.is;

@LargeTest
@RunWith(AndroidJUnit4.class)
public class AddEventActivityTest {

    private static String title = "TITLE_ETEST";
    private static String description = "DESCRIPTION_ETEST";

    private static String name = "LOGIN_NAME_ETEST";
    private static String surname = "LOGIN_SURNAME_ETEST";
    private static String email = "LOGIN_ETEST@gmail.com";
    private static String password = "PASSWORD_ETEST";

    private static Gson gson = new Gson();
    private static User user;

    @Rule
    public ActivityTestRule<LoginActivity> mActivityTestRule = new ActivityTestRule<>(LoginActivity.class);

    @Rule
    public GrantPermissionRule mGrantPermissionRule =
            GrantPermissionRule.grant(
                    "android.permission.WRITE_EXTERNAL_STORAGE");

    // BEFORE/AFTER ALL ----------------------------------------------------------------------------

    @BeforeClass
    public static void resetSharedPref(){
        Context appContext = getInstrumentation().getTargetContext();

        SharedPreferences preferences =
                PreferenceManager.getDefaultSharedPreferences(appContext);

        SharedPreferences.Editor editor = preferences.edit();
        editor.clear();
        editor.commit();
    }

    @BeforeClass
    public static void userRegistration() throws IOException {
        user = new User(name, surname, email, password);
        ClientResource cr = new ClientResource(Constants.BASE_URI + "users");
        String jsonResponse = null;

        StringRepresentation sr = new StringRepresentation(gson.toJson(user),
                MediaType.APPLICATION_JSON);
        jsonResponse = cr.post(sr).getText();
    }

    @BeforeClass
    @AfterClass
    public static void deleteEvents() throws IOException {
        ClientResource cr = new ClientResource(Constants.BASE_URI + "events");
        cr.setChallengeResponse(ChallengeScheme.HTTP_BASIC, email, password);
        String jsonResponse = cr.delete().getText();
    }

    @AfterClass
    public static void deleteUser() throws IOException {
        ClientResource cr = new ClientResource(Constants.BASE_URI + "users/"+user.getEmail());
        String jsonResponse = null;
        cr.setChallengeResponse(ChallengeScheme.HTTP_BASIC, user.getEmail(), user.getPassword());

        jsonResponse = cr.delete().getText();
    }

    // BEFORE/AFTER EACH ---------------------------------------------------------------------------

    @After
    public void after(){
        resetSharedPref();
    }

    // TEST ----------------------------------------------------------------------------------------

    @Test
    public void addEvent() {
        ViewInteraction appCompatEditText = onView(
                allOf(withId(R.id.al_emailEditText),
                        childAtPosition(
                                childAtPosition(
                                        withClassName(is("android.widget.ScrollView")),
                                        0),
                                0)));
        appCompatEditText.perform(scrollTo(), click());

        ViewInteraction appCompatEditText2 = onView(
                allOf(withId(R.id.al_emailEditText),
                        childAtPosition(
                                childAtPosition(
                                        withClassName(is("android.widget.ScrollView")),
                                        0),
                                0)));
        appCompatEditText2.perform(scrollTo(), replaceText(email), closeSoftKeyboard());

        ViewInteraction appCompatEditText3 = onView(
                allOf(withId(R.id.al_passwordEditText),
                        childAtPosition(
                                childAtPosition(
                                        withClassName(is("android.widget.ScrollView")),
                                        0),
                                1)));
        appCompatEditText3.perform(scrollTo(), replaceText(password), closeSoftKeyboard());

        ViewInteraction appCompatButton = onView(
                allOf(withId(R.id.al_loginButton), withText("Login"),
                        childAtPosition(
                                childAtPosition(
                                        withClassName(is("android.widget.ScrollView")),
                                        0),
                                2)));
        appCompatButton.perform(scrollTo(), click());

        ViewInteraction floatingActionButton = onView(
                allOf(withId(R.id.ae_floatingActionButton),
                        childAtPosition(
                                childAtPosition(
                                        withId(R.id.ae_drawer_layout),
                                        0),
                                2),
                        isDisplayed()));
        floatingActionButton.perform(click());

        ViewInteraction appCompatEditText4 = onView(
                allOf(withId(R.id.ade_titleEditText),
                        childAtPosition(
                                childAtPosition(
                                        withClassName(is("android.support.v4.widget.NestedScrollView")),
                                        0),
                                0),
                        isDisplayed()));
        appCompatEditText4.perform(replaceText(title), closeSoftKeyboard());

        ViewInteraction appCompatEditText5 = onView(
                allOf(withId(R.id.ade_descriptionEditText),
                        childAtPosition(
                                childAtPosition(
                                        withClassName(is("android.support.v4.widget.NestedScrollView")),
                                        0),
                                1),
                        isDisplayed()));
        appCompatEditText5.perform(replaceText(description), closeSoftKeyboard());

        ViewInteraction appCompatEditText6 = onView(
                allOf(withId(R.id.ade_dateStartEditText),
                        childAtPosition(
                                childAtPosition(
                                        withClassName(is("android.support.v4.widget.NestedScrollView")),
                                        0),
                                2),
                        isDisplayed()));
        appCompatEditText6.perform(click());

        ViewInteraction appCompatImageButton = onView(
                allOf(withClassName(is("android.support.v7.widget.AppCompatImageButton")), withContentDescription("Next month"),
                        childAtPosition(
                                allOf(withClassName(is("android.widget.DayPickerView")),
                                        childAtPosition(
                                                withClassName(is("com.android.internal.widget.DialogViewAnimator")),
                                                0)),
                                2),
                        isDisplayed()));
        appCompatImageButton.perform(click());

        Date date = new Date();
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        int startDateDay = cal.get(Calendar.DATE) + 1;
        int startDateMonth = cal.get(Calendar.MONTH) + 1;
        int startDateYear = cal.get(Calendar.YEAR);

        cal.add(Calendar.DATE, 1);
        int endDateDay = cal.get(Calendar.DATE);
        int endDateMonth = cal.get(Calendar.MONTH) + 1;
        int endDateYear = cal.get(Calendar.YEAR);

        Log.e("TAG", startDateDay+"-"+startDateMonth+"-"+startDateYear+" -> "+endDateDay+"-"+endDateMonth+"-"+endDateYear);


        // to set Date in DayPicker
        onView(withClassName(Matchers.equalTo(DatePicker.class.getName()))).perform(PickerActions.setDate(startDateYear, startDateMonth, startDateDay));

        ViewInteraction appCompatButton2 = onView(
                allOf(withId(android.R.id.button1), withText("OK"),
                        childAtPosition(
                                childAtPosition(
                                        withClassName(is("android.widget.ScrollView")),
                                        0),
                                3)));
        appCompatButton2.perform(scrollTo(), click());

        ViewInteraction appCompatEditText7 = onView(
                allOf(withId(R.id.ade_startTimeEditText),
                        childAtPosition(
                                childAtPosition(
                                        withClassName(is("android.support.v4.widget.NestedScrollView")),
                                        0),
                                4),
                        isDisplayed()));
        appCompatEditText7.perform(click());

        ViewInteraction appCompatButton3 = onView(
                allOf(withId(android.R.id.button1), withText("OK"),
                        childAtPosition(
                                childAtPosition(
                                        withClassName(is("android.widget.ScrollView")),
                                        0),
                                3)));
        appCompatButton3.perform(scrollTo(), click());

        ViewInteraction appCompatEditText8 = onView(
                allOf(withId(R.id.ade_dateEndEditText),
                        childAtPosition(
                                childAtPosition(
                                        withClassName(is("android.support.v4.widget.NestedScrollView")),
                                        0),
                                3),
                        isDisplayed()));
        appCompatEditText8.perform(click());

        ViewInteraction appCompatImageButton2 = onView(
                allOf(withClassName(is("android.support.v7.widget.AppCompatImageButton")), withContentDescription("Next month"),
                        childAtPosition(
                                allOf(withClassName(is("android.widget.DayPickerView")),
                                        childAtPosition(
                                                withClassName(is("com.android.internal.widget.DialogViewAnimator")),
                                                0)),
                                2),
                        isDisplayed()));
        appCompatImageButton2.perform(click());

        // to set Date in DayPicker
        onView(withClassName(Matchers.equalTo(DatePicker.class.getName()))).perform((ViewAction) PickerActions.setDate(endDateYear, endDateMonth, endDateDay));

        ViewInteraction appCompatButton4 = onView(
                allOf(withId(android.R.id.button1), withText("OK"),
                        childAtPosition(
                                childAtPosition(
                                        withClassName(is("android.widget.ScrollView")),
                                        0),
                                3)));
        appCompatButton4.perform(scrollTo(), click());

        ViewInteraction appCompatEditText9 = onView(
                allOf(withId(R.id.ade_endTimeEditText),
                        childAtPosition(
                                childAtPosition(
                                        withClassName(is("android.support.v4.widget.NestedScrollView")),
                                        0),
                                5),
                        isDisplayed()));
        appCompatEditText9.perform(click());

        ViewInteraction appCompatButton5 = onView(
                allOf(withId(android.R.id.button1), withText("OK"),
                        childAtPosition(
                                childAtPosition(
                                        withClassName(is("android.widget.ScrollView")),
                                        0),
                                3)));
        appCompatButton5.perform(scrollTo(), click());

        ViewInteraction actionMenuItemView = onView(
                allOf(withId(R.id.menu_done), withText("Done"),
                        childAtPosition(
                                childAtPosition(
                                        withId(R.id.ade_toolbar),
                                        3),
                                0),
                        isDisplayed()));
        actionMenuItemView.perform(click());

        ViewInteraction appCompatButton6 = onView(
                allOf(withId(android.R.id.button1), withText("Yes"),
                        childAtPosition(
                                childAtPosition(
                                        withClassName(is("android.widget.ScrollView")),
                                        0),
                                3)));
        appCompatButton6.perform(scrollTo(), click());

        ViewInteraction textView = onView(
                allOf(withId(R.id.aeli_titleTextView), withText(title),
                        childAtPosition(
                                childAtPosition(
                                        withId(R.id.ae_eventsListView),
                                        0),
                                0),
                        isDisplayed()));
        textView.check(matches(withText(title)));
    }

    private static Matcher<View> childAtPosition(
            final Matcher<View> parentMatcher, final int position) {

        return new TypeSafeMatcher<View>() {
            @Override
            public void describeTo(Description description) {
                description.appendText("Child at position " + position + " in parent ");
                parentMatcher.describeTo(description);
            }

            @Override
            public boolean matchesSafely(View view) {
                ViewParent parent = view.getParent();
                return parent instanceof ViewGroup && parentMatcher.matches(parent)
                        && view.equals(((ViewGroup) parent).getChildAt(position));
            }
        };
    }
}
