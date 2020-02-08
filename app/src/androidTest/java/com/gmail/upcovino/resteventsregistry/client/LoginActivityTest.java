package com.gmail.upcovino.resteventsregistry.client;


import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.test.InstrumentationRegistry;
import android.support.test.espresso.ViewInteraction;
import android.support.test.filters.LargeTest;
import android.support.test.rule.ActivityTestRule;
import android.support.test.rule.GrantPermissionRule;
import android.support.test.runner.AndroidJUnit4;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;

import com.gmail.upcovino.resteventsregistry.R;
import com.gmail.upcovino.resteventsregistry.commons.User;
import com.google.gson.Gson;

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

import static android.content.Context.MODE_PRIVATE;
import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.closeSoftKeyboard;
import static android.support.test.espresso.action.ViewActions.replaceText;
import static android.support.test.espresso.action.ViewActions.scrollTo;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withClassName;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.is;

@LargeTest
@RunWith(AndroidJUnit4.class)
public class LoginActivityTest {

    private String name = "LOGIN_NAME_ETEST";
    private String surname = "LOGIN_SURNAME_ETEST";
    private String email = "LOGIN_ETEST@gmail.com";
    private String password = "PASSWORD_ETEST";
    private Gson gson = new Gson();
    private User user;

    @Rule
    public ActivityTestRule<LoginActivity> mActivityTestRule = new ActivityTestRule<>(LoginActivity.class);

    @Rule
    public GrantPermissionRule mGrantPermissionRule =
            GrantPermissionRule.grant(
                    "android.permission.WRITE_EXTERNAL_STORAGE");

    @Before
    public void userRegistration() throws IOException {
        user = new User(name, surname, email, password);
        ClientResource cr = new ClientResource(Constants.BASE_URI + "users");
        String jsonResponse = null;

        StringRepresentation sr = new StringRepresentation(gson.toJson(user),
                MediaType.APPLICATION_JSON);
        jsonResponse = cr.post(sr).getText();
    }

    @After
    public void deleteUser() throws IOException {
        ClientResource cr = new ClientResource(Constants.BASE_URI + "users/"+user.getEmail());
        String jsonResponse = null;
        cr.setChallengeResponse(ChallengeScheme.HTTP_BASIC, user.getEmail(), user.getPassword());

        jsonResponse = cr.delete().getText();
    }

    @BeforeClass
    @AfterClass
    public static void resetSharedPref(){
        Context appContext = InstrumentationRegistry.getTargetContext();

        SharedPreferences preferences =
                PreferenceManager.getDefaultSharedPreferences(appContext);

        SharedPreferences.Editor editor = preferences.edit();
        editor.clear();
        editor.commit();
    }

    @Test
    public void loginActivityTest() {
        ViewInteraction appCompatEditText = onView(
                allOf(withId(R.id.al_emailEditText),
                        childAtPosition(
                                childAtPosition(
                                        withClassName(is("android.widget.ScrollView")),
                                        0),
                                0)));
        appCompatEditText.perform(scrollTo(), replaceText(email), closeSoftKeyboard());

        ViewInteraction appCompatEditText5 = onView(
                allOf(withId(R.id.al_passwordEditText),
                        childAtPosition(
                                childAtPosition(
                                        withClassName(is("android.widget.ScrollView")),
                                        0),
                                1)));
        appCompatEditText5.perform(scrollTo(), replaceText(password), closeSoftKeyboard());

        ViewInteraction appCompatButton = onView(
                allOf(withId(R.id.al_loginButton), withText("Login"),
                        childAtPosition(
                                childAtPosition(
                                        withClassName(is("android.widget.ScrollView")),
                                        0),
                                2)));
        appCompatButton.perform(scrollTo(), click());

        ViewInteraction textView = onView(
                allOf(withText("Upcoming events")));
        textView.check(matches(withText("Upcoming events")));
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
