package com.gmail.upcovino.resteventsregistry.client;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.gmail.upcovino.resteventsregistry.R;
import com.gmail.upcovino.resteventsregistry.commons.ErrorCodes;
import com.gmail.upcovino.resteventsregistry.commons.InvalidUserEmailException;
import com.gmail.upcovino.resteventsregistry.commons.User;
import com.google.gson.Gson;

import org.restlet.resource.ClientResource;
import org.restlet.resource.ResourceException;

import java.io.IOException;
import java.util.concurrent.ExecutionException;

public class LoginActivity extends AppCompatActivity {
    private static final int PERMISSION_WRITE_EXTERNAL_STORAGE_REQUEST_CODE = 2;
    private Gson gson;
    private String toastMessage;

    private EditText emailEditText;
    private EditText passwordEditText;

    private SharedPreferences preferences;
    private User userFromPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        Toolbar toolbar = (Toolbar) findViewById(R.id.al_toolbar);
        setSupportActionBar(toolbar);

        gson = new Gson();

        // Richiedo tutti i permessi
        if (!(ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED)) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, PERMISSION_WRITE_EXTERNAL_STORAGE_REQUEST_CODE);
        }

        // Controllo nelle SharedPreferences se sono già presenti delle credenziali salvate precedentemente; in caso positivo effettuo il login direttamente
        preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        String userJson = preferences.getString(Constants.USER, null);
        if (userJson != null) {
            userFromPreferences = gson.fromJson(userJson, User.class);
            tryLoginThroughPreferencesData();
        }

        emailEditText = (EditText) findViewById(R.id.al_emailEditText);
        passwordEditText = (EditText) findViewById(R.id.al_passwordEditText);

        ((Button) findViewById(R.id.al_loginButton)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (emailEditText.getText().toString().isEmpty())
                    Toast.makeText(getApplicationContext(), R.string.empty_email_field_message, Toast.LENGTH_LONG).show();
                else if (!Patterns.EMAIL_ADDRESS.matcher(emailEditText.getText().toString()).matches())
                    Toast.makeText(getApplicationContext(), R.string.invalid_email_field_message, Toast.LENGTH_LONG).show();
                else if (passwordEditText.getText().toString().isEmpty())
                    Toast.makeText(getApplicationContext(), R.string.empty_password_field_message, Toast.LENGTH_LONG).show();
                else {
                    tryLogin();
                }
            }
        });

        ((TextView) findViewById(R.id.al_signInTextView)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(), RegistrationActivity.class));
            }
        });

        ((TextView) findViewById(R.id.al_continueWithoutLoginTextView)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(), EventsActivity.class));

                SharedPreferences.Editor editor = preferences.edit();
                editor.remove(Constants.USER);
                editor.apply();

                finish();
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_WRITE_EXTERNAL_STORAGE_REQUEST_CODE: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(getApplicationContext(), R.string.permission_granted, Toast.LENGTH_LONG).show();
                } else {
                    if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.WRITE_EXTERNAL_STORAGE))
                        showPermissionAlertDialog();
                }
            }
        }
    }

    private void showPermissionAlertDialog() {
        AlertDialog.Builder builder;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
            builder = new AlertDialog.Builder(this, android.R.style.Theme_Material_Light_Dialog_Alert);
        else
            builder = new AlertDialog.Builder(this);

        builder.setTitle(R.string.alert_dialog_title_warning)
                .setMessage(R.string.alert_dialog_message_permission_not_granted)
                .setPositiveButton(R.string.alert_dialog_positive_button, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Toast.makeText(getApplicationContext(), R.string.permission_not_granted, Toast.LENGTH_LONG).show();
                    }
                })
                .setNegativeButton(R.string.alert_dialog_negative_button, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        ActivityCompat.requestPermissions(LoginActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, PERMISSION_WRITE_EXTERNAL_STORAGE_REQUEST_CODE);
                    }
                })
                .show();
    }

    private void tryLoginThroughPreferencesData() {
        new UserPostTask().execute("users/" + userFromPreferences.getEmail(), userFromPreferences.getPassword());
    }

    private void tryLoginThroughPreferencesDataCallback(User user) {
        if (user != null) {
            startActivity(new Intent(getApplicationContext(), EventsActivity.class));
            Toast.makeText(getApplicationContext(), getString(R.string.authenticated_as) + user.getName() + " " + user.getSurname(), Toast.LENGTH_LONG).show();
            finish();
        } else
            Toast.makeText(getApplicationContext(), toastMessage, Toast.LENGTH_LONG).show();
    }

    private void tryLogin() {
        new UserPostTask().execute("users/" + emailEditText.getText().toString(), passwordEditText.getText().toString());
    }

    private void tryLoginCallback(User user) {
        if (user != null) {
            user.setPassword(passwordEditText.getText().toString());

            SharedPreferences.Editor editor = preferences.edit();
            editor.putString(Constants.USER, gson.toJson(user, User.class));
            editor.apply();

            startActivity(new Intent(getApplicationContext(), EventsActivity.class));

            Toast.makeText(getApplicationContext(), getString(R.string.authenticated_as) + user.getName() + " " + user.getSurname(), Toast.LENGTH_LONG).show();
            finish();
        } else
            Toast.makeText(getApplicationContext(), toastMessage, Toast.LENGTH_LONG).show();
    }

    public void onBackPressed() {
            finish();
    }





    public class UserPostTask extends AsyncTask<String, Void, User> {

        @Override
        protected User doInBackground(String... params) {
            ClientResource cr = new ClientResource(Constants.BASE_URI + params[0]);
            String jsonResponse = null;

            User user = null;

            try {
                jsonResponse = cr.post(gson.toJson(params[1], String.class)).getText();

                if (cr.getStatus().getCode() == ErrorCodes.INVALID_USER_EMAIL)
                    throw gson.fromJson(jsonResponse, InvalidUserEmailException.class);

                user = gson.fromJson(jsonResponse, User.class);

                if (user == null)
                    toastMessage = getResources().getString(R.string.incorrect_password_message);
            } catch (ResourceException | IOException e1) {
                String text = "Error: " + cr.getStatus().getCode() + " - " + cr.getStatus().getDescription() + " - " + cr.getStatus().getReasonPhrase();
                Log.e(Constants.TAG, text);
                toastMessage = text;
            } catch (InvalidUserEmailException e2) {
                String text = "Error: " + cr.getStatus().getCode() + " - " + e2.getMessage();
                Log.e(Constants.TAG, text);
                toastMessage = getResources().getString(R.string.incorrect_email_message);
            }

            return user;
        }

        @Override
        protected void onPostExecute(User user) {
            if (userFromPreferences != null)
                tryLoginThroughPreferencesDataCallback(user);
            else
                tryLoginCallback(user);
        }
    }
}
