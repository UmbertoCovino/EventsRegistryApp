package com.gmail.upcovino.resteventsregistry.client;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;

import com.gmail.upcovino.resteventsregistry.R;
import com.gmail.upcovino.resteventsregistry.commons.Event;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import org.hamcrest.core.IsInstanceOf;
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
import java.text.ParseException;
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
import static androidx.test.espresso.matcher.ViewMatchers.withParent;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static androidx.test.platform.app.InstrumentationRegistry.getInstrumentation;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.is;

@LargeTest
@RunWith(AndroidJUnit4.class)
public class ModifyEventActivityTest {

    @Rule
    public ActivityTestRule<LoginActivity> mActivityTestRule = new ActivityTestRule<>(LoginActivity.class);

    @Rule
    public GrantPermissionRule mGrantPermissionRule =
            GrantPermissionRule.grant(
                    "android.permission.WRITE_EXTERNAL_STORAGE");

    // BEFORE/AFTER ALL ----------------------------------------------------------------------------

    @BeforeClass
    public static void beforeClass() throws IOException {
        EspressoTestUtils.resetSharedPref();
        EspressoTestUtils.userRegistration();
    }

    @AfterClass
    public static void afterClass() throws IOException {
        EspressoTestUtils.deleteAllEvents();
        EspressoTestUtils.deleteUser();
    }

    // BEFORE/AFTER EACH ---------------------------------------------------------------------------

    @Before
    public void before() throws IOException, ParseException {
        EspressoTestUtils.deleteAllEvents();

        Event event = new Event(EspressoTestUtils.TEST_EVENT_TITLE,
                Event.DATETIME_SDF.parse(EspressoTestUtils.START_DATE_YEAR+"-"+EspressoTestUtils.START_DATE_MONTH+"-"+EspressoTestUtils.START_DATE_DAY+ " "
                        + "12:00:00"),
                Event.DATETIME_SDF.parse(EspressoTestUtils.END_DATE_YEAR+"-"+EspressoTestUtils.END_DATE_MONTH+"-"+EspressoTestUtils.END_DATE_DAY+ " "
                        + "12:00:00"), EspressoTestUtils.TEST_EVENT_DESCRIPTION);
        EspressoTestUtils.addEvent(event);

        EspressoTestUtils.initIntent(); // IntentsTestRule does not work, so manage myself Intents init() & release()
    }

    @After
    public void after() {
        EspressoTestUtils.resetSharedPref();

        EspressoTestUtils.releaseIntent(); // IntentsTestRule does not work, so manage myself Intents init() & release()
    }

    // TEST ----------------------------------------------------------------------------------------

    @Test
    public void modifyEventUsingGallery() {
        modifyEventUsing("Gallery");
    }

    @Test
    public void modifyEventUsingCamera() {
        modifyEventUsing("Camera");
    }

    private void modifyEventUsing(String usingWhat) {
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
        appCompatEditText2.perform(scrollTo(), replaceText(EspressoTestUtils.TEST_USER_EMAIL), closeSoftKeyboard());

        ViewInteraction appCompatEditText3 = onView(
                allOf(withId(R.id.al_passwordEditText),
                        childAtPosition(
                                childAtPosition(
                                        withClassName(is("android.widget.ScrollView")),
                                        0),
                                1)));
        appCompatEditText3.perform(scrollTo(), replaceText(EspressoTestUtils.TEST_USER_PASSWORD), closeSoftKeyboard());

        ViewInteraction appCompatButton = onView(
                allOf(withId(R.id.al_loginButton), withText("Login"),
                        childAtPosition(
                                childAtPosition(
                                        withClassName(is("android.widget.ScrollView")),
                                        0),
                                2)));
        appCompatButton.perform(scrollTo(), click());

        ViewInteraction appCompatTextView = onView(
                allOf(withId(R.id.aeli_titleTextView), withText(EspressoTestUtils.TEST_EVENT_TITLE),
                        childAtPosition(
                                withParent(withId(R.id.ae_eventsListView)),
                                0),
                        isDisplayed()));
        appCompatTextView.perform(click());

        ViewInteraction actionMenuItemView = onView(
                allOf(withId(R.id.aev_menu_edit), withContentDescription("Modify"),
                        childAtPosition(
                                childAtPosition(
                                        withId(R.id.aev_toolbar),
                                        3),
                                1),
                        isDisplayed()));
        actionMenuItemView.perform(click());



        // Call stub
        if (!usingWhat.isEmpty()) {
            if (usingWhat.equals("Gallery"))
                TakeImageFromGalleryStub.exec();
            else if (usingWhat.equals("Camera"))
                TakeImageFromCameraStub.exec();

            // Perform action on photo button
            onView(withId(R.id.ade_chooseImageFloatingActionButton)).perform(click());
        }



        ViewInteraction appCompatEditText4 = onView(
                allOf(withId(R.id.ade_titleEditText), withText(EspressoTestUtils.TEST_EVENT_TITLE),
                        childAtPosition(
                                childAtPosition(
                                        withClassName(is("android.support.v4.widget.NestedScrollView")),
                                        0),
                                0),
                        isDisplayed()));
        appCompatEditText4.perform(click());

        ViewInteraction appCompatEditText5 = onView(
                allOf(withId(R.id.ade_titleEditText), withText(EspressoTestUtils.TEST_EVENT_TITLE),
                        childAtPosition(
                                childAtPosition(
                                        withClassName(is("android.support.v4.widget.NestedScrollView")),
                                        0),
                                0),
                        isDisplayed()));
        appCompatEditText5.perform(replaceText(EspressoTestUtils.TEST_EVENT_TITLE + "_M"));

        ViewInteraction appCompatEditText6 = onView(
                allOf(withId(R.id.ade_titleEditText), withText(EspressoTestUtils.TEST_EVENT_TITLE + "_M"),
                        childAtPosition(
                                childAtPosition(
                                        withClassName(is("android.support.v4.widget.NestedScrollView")),
                                        0),
                                0),
                        isDisplayed()));
        appCompatEditText6.perform(closeSoftKeyboard());

        ViewInteraction actionMenuItemView2 = onView(
                allOf(withId(R.id.menu_done), withText("Done"),
                        childAtPosition(
                                childAtPosition(
                                        withId(R.id.ade_toolbar),
                                        3),
                                0),
                        isDisplayed()));
        actionMenuItemView2.perform(click());

        ViewInteraction appCompatButton2 = onView(
                allOf(withId(android.R.id.button1), withText("Yes"),
                        childAtPosition(
                                childAtPosition(
                                        withClassName(is("android.widget.ScrollView")),
                                        0),
                                3)));
        appCompatButton2.perform(scrollTo(), click());

        ViewInteraction textView = onView(
                allOf(withId(R.id.aev_descriptionTextView), withText(EspressoTestUtils.TEST_EVENT_DESCRIPTION)));
        textView.check(matches(withText(EspressoTestUtils.TEST_EVENT_DESCRIPTION)));
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
