package com.gmail.upcovino.resteventsregistry.client;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;

import com.gmail.upcovino.resteventsregistry.R;
import com.gmail.upcovino.resteventsregistry.commons.User;
import com.google.gson.Gson;

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

import androidx.test.espresso.ViewInteraction;
import androidx.test.espresso.intent.rule.IntentsTestRule;
import androidx.test.filters.LargeTest;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.rule.GrantPermissionRule;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.replaceText;
import static androidx.test.espresso.action.ViewActions.scrollTo;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withClassName;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static androidx.test.platform.app.InstrumentationRegistry.getInstrumentation;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.is;

@LargeTest
@RunWith(AndroidJUnit4.class)
public class RegistrationActivityTest {
    private String name = "SIGNIN_NAME_ETEST";
    private String surname = "SIGNIN_SURNAME_ETEST";
    private String email = "SIGNIN_ETEST@gmail.com";
    private String password = "PASSWORD_ETEST";
    private Gson gson = new Gson();
    private User user;

//    @Rule
//    public ActivityTestRule<LoginActivity> mActivityTestRule = new ActivityTestRule<>(LoginActivity.class);
    @Rule
    public IntentsTestRule<LoginActivity> intentsRule = new IntentsTestRule<>(LoginActivity.class, true, false);

    @Rule
    public GrantPermissionRule mGrantPermissionRule =
            GrantPermissionRule.grant(
                    "android.permission.WRITE_EXTERNAL_STORAGE");

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

    @After
    public void deleteUser() throws IOException {
        user = new User(name, surname, email, password);
        ClientResource cr = new ClientResource(Constants.BASE_URI + "users/"+user.getEmail());
        String jsonResponse = null;
        cr.setChallengeResponse(ChallengeScheme.HTTP_BASIC, user.getEmail(), user.getPassword());

        jsonResponse = cr.delete().getText();
    }

    @Test
    public void signinActivityTestWithGallery() {
        Intent intent = new Intent();
        intent.putExtra("LoginActivity", "LoginActivity");
        intentsRule.launchActivity(intent);


        ViewInteraction appCompatTextView = onView(
                allOf(withId(R.id.al_signInTextView), withText("Sign in now!"),
                        childAtPosition(
                                allOf(withId(R.id.linearLayout),
                                        childAtPosition(
                                                withClassName(is("android.support.constraint.ConstraintLayout")),
                                                5)),
                                1)));
        appCompatTextView.perform(scrollTo(), click());


        // Call stub
        TakeImageFromGalleryStub.exec();
//        TakeImageFromCameraStub.exec();

        // Perform action on photo button
        onView(withId(R.id.ar_chooseImageFloatingActionButton)).perform(click());



        ViewInteraction appCompatEditText = onView(
                allOf(withId(R.id.ar_nameEditText),
                        childAtPosition(
                                childAtPosition(
                                        withClassName(is("android.support.v4.widget.NestedScrollView")),
                                        0),
                                0),
                        isDisplayed()));
        appCompatEditText.perform(replaceText(name), closeSoftKeyboard());

        ViewInteraction appCompatEditText2 = onView(
                allOf(withId(R.id.ar_surnameEditText),
                        childAtPosition(
                                childAtPosition(
                                        withClassName(is("android.support.v4.widget.NestedScrollView")),
                                        0),
                                1),
                        isDisplayed()));
        appCompatEditText2.perform(replaceText(surname), closeSoftKeyboard());

        ViewInteraction appCompatEditText3 = onView(
                allOf(withId(R.id.ar_emailEditText),
                        childAtPosition(
                                childAtPosition(
                                        withClassName(is("android.support.v4.widget.NestedScrollView")),
                                        0),
                                2),
                        isDisplayed()));
        appCompatEditText3.perform(replaceText(email), closeSoftKeyboard());

        ViewInteraction appCompatEditText4 = onView(
                allOf(withId(R.id.ar_passwordEditText),
                        childAtPosition(
                                childAtPosition(
                                        withClassName(is("android.support.v4.widget.NestedScrollView")),
                                        0),
                                3),
                        isDisplayed()));
        appCompatEditText4.perform(replaceText(password), closeSoftKeyboard());

        ViewInteraction actionMenuItemView = onView(
                allOf(withId(R.id.menu_done), withText("Done"),
                        childAtPosition(
                                childAtPosition(
                                        withId(R.id.ar_toolbar),
                                        3),
                                0),
                        isDisplayed()));
        actionMenuItemView.perform(click());

        ViewInteraction appCompatButton = onView(
                allOf(withId(android.R.id.button1), withText("Yes"),
                        childAtPosition(
                                childAtPosition(
                                        withClassName(is("android.widget.ScrollView")),
                                        0),
                                3)));
        appCompatButton.perform(scrollTo(), click());

        ViewInteraction textView = onView(
                allOf(withId(android.R.id.message), withText("Do you want enable telegram notifications?")));
        textView.check(matches(withText("Do you want enable telegram notifications?")));
    }

    @Test
    public void signinActivityTestWithCamera() {
        Intent intent = new Intent();
        intent.putExtra("LoginActivity", "LoginActivity");
        intentsRule.launchActivity(intent);


        ViewInteraction appCompatTextView = onView(
                allOf(withId(R.id.al_signInTextView), withText("Sign in now!"),
                        childAtPosition(
                                allOf(withId(R.id.linearLayout),
                                        childAtPosition(
                                                withClassName(is("android.support.constraint.ConstraintLayout")),
                                                5)),
                                1)));
        appCompatTextView.perform(scrollTo(), click());


        // Call stub
//        TakeImageFromGalleryStub.exec();
        TakeImageFromCameraStub.exec();

        // Perform action on photo button
        onView(withId(R.id.ar_chooseImageFloatingActionButton)).perform(click());



        ViewInteraction appCompatEditText = onView(
                allOf(withId(R.id.ar_nameEditText),
                        childAtPosition(
                                childAtPosition(
                                        withClassName(is("android.support.v4.widget.NestedScrollView")),
                                        0),
                                0),
                        isDisplayed()));
        appCompatEditText.perform(replaceText(name), closeSoftKeyboard());

        ViewInteraction appCompatEditText2 = onView(
                allOf(withId(R.id.ar_surnameEditText),
                        childAtPosition(
                                childAtPosition(
                                        withClassName(is("android.support.v4.widget.NestedScrollView")),
                                        0),
                                1),
                        isDisplayed()));
        appCompatEditText2.perform(replaceText(surname), closeSoftKeyboard());

        ViewInteraction appCompatEditText3 = onView(
                allOf(withId(R.id.ar_emailEditText),
                        childAtPosition(
                                childAtPosition(
                                        withClassName(is("android.support.v4.widget.NestedScrollView")),
                                        0),
                                2),
                        isDisplayed()));
        appCompatEditText3.perform(replaceText(email), closeSoftKeyboard());

        ViewInteraction appCompatEditText4 = onView(
                allOf(withId(R.id.ar_passwordEditText),
                        childAtPosition(
                                childAtPosition(
                                        withClassName(is("android.support.v4.widget.NestedScrollView")),
                                        0),
                                3),
                        isDisplayed()));
        appCompatEditText4.perform(replaceText(password), closeSoftKeyboard());

        ViewInteraction actionMenuItemView = onView(
                allOf(withId(R.id.menu_done), withText("Done"),
                        childAtPosition(
                                childAtPosition(
                                        withId(R.id.ar_toolbar),
                                        3),
                                0),
                        isDisplayed()));
        actionMenuItemView.perform(click());

        ViewInteraction appCompatButton = onView(
                allOf(withId(android.R.id.button1), withText("Yes"),
                        childAtPosition(
                                childAtPosition(
                                        withClassName(is("android.widget.ScrollView")),
                                        0),
                                3)));
        appCompatButton.perform(scrollTo(), click());

        ViewInteraction textView = onView(
                allOf(withId(android.R.id.message), withText("Do you want enable telegram notifications?")));
        textView.check(matches(withText("Do you want enable telegram notifications?")));
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