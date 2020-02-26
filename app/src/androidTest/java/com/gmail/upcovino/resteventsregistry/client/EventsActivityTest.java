package com.gmail.upcovino.resteventsregistry.client;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.DatePicker;

import com.gmail.upcovino.resteventsregistry.R;
import com.gmail.upcovino.resteventsregistry.commons.Event;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.Matchers;
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
import androidx.test.espresso.matcher.ViewMatchers;
import androidx.test.filters.LargeTest;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.rule.ActivityTestRule;
import androidx.test.rule.GrantPermissionRule;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.Espresso.openActionBarOverflowOrOptionsMenu;
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
public class EventsActivityTest {

    @Rule
    public ActivityTestRule<LoginActivity> mActivityTestRule = new ActivityTestRule<>(LoginActivity.class);

    @Rule
    public GrantPermissionRule mGrantPermissionRule =
            GrantPermissionRule.grant(
                    "android.permission.WRITE_EXTERNAL_STORAGE");

    // BEFORE/AFTER ALL ----------------------------------------------------------------------------

    @BeforeClass
    public static void beforeClass() throws IOException, ParseException {
        EspressoTestUtils.resetSharedPref();
        EspressoTestUtils.userRegistration();

        EspressoTestUtils.deleteAllEvents();

        Event event1 = new Event(EspressoTestUtils.TEST_EVENT_TITLE + "1",
                Event.DATETIME_SDF.parse(EspressoTestUtils.START_DATE_YEAR+"-"+EspressoTestUtils.START_DATE_MONTH+"-"+EspressoTestUtils.START_DATE_DAY+ " "
                        + "12:00:00"),
                Event.DATETIME_SDF.parse(EspressoTestUtils.END_DATE_YEAR+"-"+EspressoTestUtils.END_DATE_MONTH+"-"+EspressoTestUtils.END_DATE_DAY+ " "
                        + "12:00:00"), EspressoTestUtils.TEST_EVENT_DESCRIPTION + "1");
        EspressoTestUtils.addEvent(event1);

        Event event2 = new Event(EspressoTestUtils.TEST_EVENT_TITLE + "2",
                Event.DATETIME_SDF.parse(EspressoTestUtils.START_DATE_YEAR+"-"+EspressoTestUtils.START_DATE_MONTH+"-"+EspressoTestUtils.START_DATE_DAY+ " "
                        + "13:00:00"),
                Event.DATETIME_SDF.parse(EspressoTestUtils.END_DATE_YEAR+"-"+EspressoTestUtils.END_DATE_MONTH+"-"+EspressoTestUtils.END_DATE_DAY+ " "
                        + "12:00:00"), EspressoTestUtils.TEST_EVENT_DESCRIPTION + "2");
        EspressoTestUtils.addEvent(event2);

        Event event3 = new Event(EspressoTestUtils.TEST_EVENT_TITLE + "3",
                Event.DATETIME_SDF.parse(EspressoTestUtils.START_DATE_YEAR+"-"+EspressoTestUtils.START_DATE_MONTH+"-"+EspressoTestUtils.START_DATE_DAY+ " "
                        + "14:00:00"),
                Event.DATETIME_SDF.parse(EspressoTestUtils.END_DATE_YEAR+"-"+EspressoTestUtils.END_DATE_MONTH+"-"+EspressoTestUtils.END_DATE_DAY+ " "
                        + "12:00:00"), EspressoTestUtils.TEST_EVENT_DESCRIPTION + "3");
        EspressoTestUtils.addEvent(event3);

        Event event4 = new Event(EspressoTestUtils.TEST_EVENT_TITLE + "4",
                Event.DATETIME_SDF.parse(EspressoTestUtils.START_DATE_YEAR+"-"+EspressoTestUtils.START_DATE_MONTH+"-"+EspressoTestUtils.START_DATE_DAY+ " "
                        + "15:00:00"),
                Event.DATETIME_SDF.parse(EspressoTestUtils.END_DATE_YEAR+"-"+EspressoTestUtils.END_DATE_MONTH+"-"+EspressoTestUtils.END_DATE_DAY+ " "
                        + "12:00:00"), EspressoTestUtils.TEST_EVENT_DESCRIPTION + "4");
        EspressoTestUtils.addEvent(event4);
    }

    @AfterClass
    public static void afterClass() throws IOException {
        EspressoTestUtils.deleteAllEvents();
        EspressoTestUtils.deleteUser();
    }

    // BEFORE/AFTER EACH ---------------------------------------------------------------------------

    @After
    public void after(){
        EspressoTestUtils.resetSharedPref();
    }

    // TEST ----------------------------------------------------------------------------------------

    @Test
    public void deleteEvent() {
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
                allOf(withId(R.id.aeli_titleTextView), withText(EspressoTestUtils.TEST_EVENT_TITLE + "1"),
                        childAtPosition(
                                withParent(withId(R.id.ae_eventsListView)),
                                0),
                        isDisplayed()));
        appCompatTextView.perform(click());

        ViewInteraction actionMenuItemView = onView(
                allOf(withId(R.id.aev_menu_remove), withContentDescription("Remove"),
                        childAtPosition(
                                childAtPosition(
                                        withId(R.id.aev_toolbar),
                                        3),
                                0),
                        isDisplayed()));
        actionMenuItemView.perform(click());

        ViewInteraction appCompatButton2 = onView(
                allOf(withId(android.R.id.button1), withText("Yes"),
                        childAtPosition(
                                childAtPosition(
                                        withClassName(is("android.widget.ScrollView")),
                                        0),
                                3)));
        appCompatButton2.perform(scrollTo(), click());

        ViewInteraction textView = onView(
                allOf(withText("Upcoming events")));
        textView.check(matches(withText("Upcoming events")));
    }

    @Test
    public void eventsBetweenTwoDates() throws IOException {
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

        openActionBarOverflowOrOptionsMenu(getInstrumentation().getTargetContext());

        ViewInteraction appCompatTextView = onView(
                allOf(withId(R.id.title), withText("Events to show"),
                        childAtPosition(
                                childAtPosition(
                                        withId(R.id.content),//withClassName(is("android.support.v7.view.menu.ListMenuItemView")),
                                        0),
                                0),
                        isDisplayed()));
        appCompatTextView.perform(click());

        // wait 500 ms to avoid the exception:
        // "com.google.android.apps.common.testing.ui.espresso.PerformException: Error performing 'single click' on view [...]
        //      Caused by: java.lang.RuntimeException: Action will not be performed because the target view does not match one or more of the following constraints: at least 90 percent of the view's area is displayed to the user."
        EspressoTestUtils.waitFor(500);

        ViewInteraction appCompatTextView2 = onView(
                allOf(withId(R.id.title), withText("Events between two dates"),
                        childAtPosition(
                                childAtPosition(
                                        withId(R.id.content),//withClassName(is("android.support.v7.view.menu.ListMenuItemView")),
                                        0),
                                0),
                        isDisplayed()));
        appCompatTextView2.perform(click());

        ViewInteraction appCompatEditText4 = onView(
                allOf(withId(R.id.cid_fromDateEditText),
                        childAtPosition(
                                childAtPosition(
                                        withClassName(is("android.support.design.widget.CoordinatorLayout")),
                                        1),
                                0),
                        isDisplayed()));
        appCompatEditText4.perform(click());

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

        // to set Date in DayPicker
        onView(withClassName(Matchers.equalTo(DatePicker.class.getName()))).perform(PickerActions.setDate(EspressoTestUtils.START_DATE_YEAR, EspressoTestUtils.START_DATE_MONTH, EspressoTestUtils.START_DATE_DAY));

        ViewInteraction appCompatButton2 = onView(
                allOf(withId(android.R.id.button1), withText("OK"),
                        childAtPosition(
                                childAtPosition(
                                        withClassName(is("android.widget.ScrollView")),
                                        0),
                                3)));
        appCompatButton2.perform(scrollTo(), click());

        ViewInteraction appCompatEditText5 = onView(
                allOf(withId(R.id.cid_fromTimeEditText),
                        childAtPosition(
                                childAtPosition(
                                        withClassName(is("android.support.design.widget.CoordinatorLayout")),
                                        1),
                                1),
                        isDisplayed()));
        appCompatEditText5.perform(click());

        ViewInteraction appCompatButton3 = onView(
                allOf(withId(android.R.id.button1), withText("OK"),
                        childAtPosition(
                                childAtPosition(
                                        withClassName(is("android.widget.ScrollView")),
                                        0),
                                3)));
        appCompatButton3.perform(scrollTo(), click());

        ViewInteraction appCompatEditText6 = onView(
                allOf(withId(R.id.cid_toDateEditText),
                        childAtPosition(
                                childAtPosition(
                                        withClassName(is("android.support.design.widget.CoordinatorLayout")),
                                        1),
                                2),
                        isDisplayed()));
        appCompatEditText6.perform(click());

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

        ViewInteraction appCompatImageButton3 = onView(
                allOf(withClassName(is("android.support.v7.widget.AppCompatImageButton")), withContentDescription("Next month"),
                        childAtPosition(
                                allOf(withClassName(is("android.widget.DayPickerView")),
                                        childAtPosition(
                                                withClassName(is("com.android.internal.widget.DialogViewAnimator")),
                                                0)),
                                2),
                        isDisplayed()));
        appCompatImageButton3.perform(click());

        // to set Date in DayPicker
        onView(withClassName(Matchers.equalTo(DatePicker.class.getName()))).perform(PickerActions.setDate(EspressoTestUtils.END_DATE_YEAR, EspressoTestUtils.END_DATE_MONTH, EspressoTestUtils.END_DATE_DAY));

        ViewInteraction appCompatButton4 = onView(
                allOf(withId(android.R.id.button1), withText("OK"),
                        childAtPosition(
                                childAtPosition(
                                        withClassName(is("android.widget.ScrollView")),
                                        0),
                                3)));
        appCompatButton4.perform(scrollTo(), click());

        ViewInteraction appCompatEditText7 = onView(
                allOf(withId(R.id.cid_toTimeEditText),
                        childAtPosition(
                                childAtPosition(
                                        withClassName(is("android.support.design.widget.CoordinatorLayout")),
                                        1),
                                3),
                        isDisplayed()));
        appCompatEditText7.perform(click());

        ViewInteraction appCompatButton5 = onView(
                allOf(withId(android.R.id.button1), withText("OK"),
                        childAtPosition(
                                childAtPosition(
                                        withClassName(is("android.widget.ScrollView")),
                                        0),
                                3)));
        appCompatButton5.perform(scrollTo(), click());

        ViewInteraction appCompatButton6 = onView(
                allOf(withId(android.R.id.button1), withText("Apply"),
                        childAtPosition(
                                childAtPosition(
                                        withClassName(is("android.widget.ScrollView")),
                                        0),
                                3)));
        appCompatButton6.perform(scrollTo(), click());

        ViewInteraction textView = onView(
                allOf(withId(R.id.ae_filterTextView), withText("from " + EspressoTestUtils.START_DATE_YEAR + "-" + EspressoTestUtils.prependZeroToMonth(EspressoTestUtils.START_DATE_MONTH) + "-" + EspressoTestUtils.START_DATE_DAY + " to 12:00 " +
                        "to " + EspressoTestUtils.END_DATE_YEAR + "-" + EspressoTestUtils.prependZeroToMonth(EspressoTestUtils.END_DATE_MONTH) + "-" + EspressoTestUtils.END_DATE_DAY + " to 12:00")));
        textView.check(matches(withText("from " + EspressoTestUtils.START_DATE_YEAR + "-" + EspressoTestUtils.prependZeroToMonth(EspressoTestUtils.START_DATE_MONTH) + "-" + EspressoTestUtils.START_DATE_DAY + " to 12:00 " +
                "to "+ EspressoTestUtils.END_DATE_YEAR + "-" + EspressoTestUtils.prependZeroToMonth(EspressoTestUtils.END_DATE_MONTH) + "-" + EspressoTestUtils.END_DATE_DAY + " to 12:00")));
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
