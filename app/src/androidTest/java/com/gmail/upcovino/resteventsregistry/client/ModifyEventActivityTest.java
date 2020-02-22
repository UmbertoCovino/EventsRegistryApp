package com.gmail.upcovino.resteventsregistry.client;

import android.content.Context;
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
    private String email = "a@gmail.com";
    private String password = "p";
    private String title = "TITLE_ETEST";
    private String description = "DESCRIPTION_ETEST";
    private Gson gson;

    @Rule
    public ActivityTestRule<LoginActivity> mActivityTestRule = new ActivityTestRule<>(LoginActivity.class);

    @Rule
    public GrantPermissionRule mGrantPermissionRule =
            GrantPermissionRule.grant(
                    "android.permission.WRITE_EXTERNAL_STORAGE");

    @Before
    public void before() throws IOException, ParseException {
        ClientResource cr = new ClientResource(Constants.BASE_URI + "events");
        cr.setChallengeResponse(ChallengeScheme.HTTP_BASIC, email, password);
        String jsonResponse = cr.delete().getText();

        gson = new GsonBuilder()
                .registerTypeAdapter(Date.class, new Constants.DateTypeAdapter())
                .create();
        Event event = new Event(title,
                Event.DATETIME_SDF.parse("2020-02-10"+ " "
                        + "12:00:00"),
                Event.DATETIME_SDF.parse("2020-02-11" + " "
                        + "12:00:00"), description);
        cr = new ClientResource(Constants.BASE_URI + "events");
        cr.setChallengeResponse(ChallengeScheme.HTTP_BASIC, email, password);
        StringRepresentation sr = new StringRepresentation(gson.toJson(event), MediaType.APPLICATION_JSON);
        jsonResponse = cr.post(sr).getText();
    }

    @After
    public void after() throws IOException {
        ClientResource cr = new ClientResource(Constants.BASE_URI + "events");
        cr.setChallengeResponse(ChallengeScheme.HTTP_BASIC, email, password);
        String jsonResponse = cr.delete().getText();
    }

    @BeforeClass
    @AfterClass
    public static void resetSharedPref(){
        Context appContext = getInstrumentation().getTargetContext();

        SharedPreferences preferences =
                PreferenceManager.getDefaultSharedPreferences(appContext);

        SharedPreferences.Editor editor = preferences.edit();
        editor.clear();
        editor.commit();
    }

    @Test
    public void modifyEvent_ActivityTest() {
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

        ViewInteraction appCompatTextView = onView(
                allOf(withId(R.id.aeli_titleTextView), withText("TITLE_ETEST"),
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

        ViewInteraction appCompatEditText4 = onView(
                allOf(withId(R.id.ade_titleEditText), withText("TITLE_ETEST"),
                        childAtPosition(
                                childAtPosition(
                                        withClassName(is("android.support.v4.widget.NestedScrollView")),
                                        0),
                                0),
                        isDisplayed()));
        appCompatEditText4.perform(click());

        ViewInteraction appCompatEditText5 = onView(
                allOf(withId(R.id.ade_titleEditText), withText("TITLE_ETEST"),
                        childAtPosition(
                                childAtPosition(
                                        withClassName(is("android.support.v4.widget.NestedScrollView")),
                                        0),
                                0),
                        isDisplayed()));
        appCompatEditText5.perform(replaceText("TITLE_ETEST_M"));

        ViewInteraction appCompatEditText6 = onView(
                allOf(withId(R.id.ade_titleEditText), withText("TITLE_ETEST_M"),
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
                allOf(withId(R.id.aev_descriptionTextView), withText("DESCRIPTION_ETEST")));
        textView.check(matches(withText("DESCRIPTION_ETEST")));
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