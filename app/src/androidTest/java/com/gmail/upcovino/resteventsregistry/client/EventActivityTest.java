package com.gmail.upcovino.resteventsregistry.client;

import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;

import com.gmail.upcovino.resteventsregistry.R;
import com.gmail.upcovino.resteventsregistry.commons.Event;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.restlet.data.ChallengeScheme;
import org.restlet.resource.ClientResource;

import java.io.IOException;
import java.text.ParseException;

import androidx.test.espresso.ViewInteraction;
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
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.is;

@LargeTest
@RunWith(AndroidJUnit4.class)
public class EventActivityTest {

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

        Event event = new Event(EspressoTestUtils.TEST_EVENT_TITLE,
                Event.DATETIME_SDF.parse(EspressoTestUtils.START_DATE_YEAR+"-"+EspressoTestUtils.START_DATE_MONTH+"-"+EspressoTestUtils.START_DATE_DAY+ " "
                        + "12:00:00"),
                Event.DATETIME_SDF.parse(EspressoTestUtils.END_DATE_YEAR+"-"+EspressoTestUtils.END_DATE_MONTH+"-"+EspressoTestUtils.END_DATE_DAY+ " "
                        + "12:00:00"), EspressoTestUtils.TEST_EVENT_DESCRIPTION);
        EspressoTestUtils.addEvent(event);
    }

    @AfterClass
    public static void afterClass() throws IOException {
        ClientResource cr = new ClientResource(Constants.BASE_URI + "events");
        cr.setChallengeResponse(ChallengeScheme.HTTP_BASIC, EspressoTestUtils.TEST_USER_EMAIL, EspressoTestUtils.TEST_USER_PASSWORD);
        String jsonResponse = cr.get().getText();
        Event[] events = EspressoTestUtils.GSON.fromJson(jsonResponse, Event[].class);

        cr = new ClientResource(Constants.BASE_URI + "events/" + events[0].getId() + "/subscribers");
        cr.setChallengeResponse(ChallengeScheme.HTTP_BASIC, EspressoTestUtils.TEST_USER_EMAIL, EspressoTestUtils.TEST_USER_PASSWORD);
        cr.delete();

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
    public void subscribeEvent() {
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

        ViewInteraction appCompatButton2 = onView(
                allOf(withId(R.id.aev_subscribe_button), withText("parteciperò"),
                        childAtPosition(
                                childAtPosition(
                                        withClassName(is("android.support.v4.widget.NestedScrollView")),
                                        0),
                                2),
                        isDisplayed()));
        appCompatButton2.perform(click());

        ViewInteraction button = onView(
                allOf(withId(R.id.aev_subscribe_button)));
        button.check(matches(isDisplayed()));
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
