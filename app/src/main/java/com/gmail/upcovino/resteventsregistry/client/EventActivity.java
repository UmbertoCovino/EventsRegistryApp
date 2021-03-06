package com.gmail.upcovino.resteventsregistry.client;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.gmail.upcovino.resteventsregistry.BuildConfig;
import com.gmail.upcovino.resteventsregistry.R;
import com.gmail.upcovino.resteventsregistry.commons.Event;
import com.gmail.upcovino.resteventsregistry.commons.User;
import com.gmail.upcovino.resteventsregistry.commons.exceptions.ErrorCodes;
import com.gmail.upcovino.resteventsregistry.commons.exceptions.GenericSQLException;
import com.gmail.upcovino.resteventsregistry.commons.exceptions.InvalidEventIdException;
import com.gmail.upcovino.resteventsregistry.commons.exceptions.InvalidUserEmailException;
import com.gmail.upcovino.resteventsregistry.commons.exceptions.JsonParsingException;
import com.gmail.upcovino.resteventsregistry.commons.exceptions.UnauthorizedUserException;
import com.gmail.upcovino.resteventsregistry.commons.exceptions.VoidClassFieldException;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.restlet.data.ChallengeScheme;
import org.restlet.data.MediaType;
import org.restlet.representation.StringRepresentation;
import org.restlet.resource.ClientResource;
import org.restlet.resource.ResourceException;

import java.io.File;
import java.io.IOException;
import java.util.Date;

public class EventActivity extends AppCompatActivity {
    private Gson gson;
    private User userLogged;
    private Event event;
    private String toastMessage;
    private String storageDirectory;

    private boolean eventChanged;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event);

        Toolbar toolbar = (Toolbar) findViewById(R.id.aev_toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        ((Button) findViewById(R.id.aev_subscribe_button)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int id = event.getId();
                new SubscribePostTask().execute("events/" + id, userLogged.getEmail(), userLogged.getPassword());
            }
        });

        //gson = new Gson();
        gson = new GsonBuilder()
                .registerTypeAdapter(Date.class, new Constants.DateTypeAdapter())
                .create();

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        String userJson = preferences.getString(Constants.USER, null);
        if (userJson != null)
            userLogged = gson.fromJson(userJson, User.class);

        event = gson.fromJson(getIntent().getStringExtra(Constants.EVENT_EXTRA), Event.class);
        storageDirectory = getIntent().getStringExtra(Constants.STORAGE_DIRECTORY_EXTRA);

        initializeEventLayout(event);
    }

    private void initializeEventLayout(final Event event) {
        ((CollapsingToolbarLayout) findViewById(R.id.aev_collapsingToolbarLayout)).setTitle(event.getTitle());

        ImageView eventPhotoImageView = ((ImageView) findViewById(R.id.aev_eventImageView));
        if (event.getPhotoPath() != null) {
            Bitmap bitmap = BitmapFactory.decodeFile(storageDirectory + "/" + event.getPhotoPath());
            eventPhotoImageView.setImageBitmap(bitmap);

            eventPhotoImageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent();
                    intent.setAction(Intent.ACTION_VIEW);
                    intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    intent.setDataAndType(FileProvider.getUriForFile(getApplicationContext(), BuildConfig.APPLICATION_ID + ".provider", new File(storageDirectory + "/" + event.getPhotoPath())), "image/*");
                    startActivity(intent);
                }
            });
        } else
            eventPhotoImageView.setImageResource(R.drawable.default_event_icon);

//        ((TextView) findViewById(R.id.aev_titleTextView)).setText(event.getTitle());
        ((TextView) findViewById(R.id.aev_whenTextView)).setText(getApplicationContext().getString(R.string.when_the) + getApplicationContext().getString(R.string.from) + Event.DATETIME_SDF.format(event.getStartDate()) + getApplicationContext().getString(R.string.to) + Event.DATETIME_SDF.format(event.getEndDate()));
        ((TextView) findViewById(R.id.aev_descriptionTextView)).setText(event.getDescription());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (event.getOwner().getEmail().equals(userLogged.getEmail())) {
            getMenuInflater().inflate(R.menu.activity_event_user_menu, menu);
            return true;
        }
        return false;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.aev_menu_remove:
                confirmRemoveEventAlertDialog(event.getId()+"");
                break;
            case R.id.aev_menu_edit:
                Intent intent = new Intent(getApplicationContext(), ModifyEventActivity.class);
                intent.putExtra(Constants.EVENT_EXTRA, gson.toJson(event));
                intent.putExtra(Constants.STORAGE_DIRECTORY_EXTRA, storageDirectory);
                startActivityForResult(intent, 1);
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case 1:
                if (resultCode == Activity.RESULT_OK) {
                    event = gson.fromJson(data.getStringExtra(Constants.EVENT_EXTRA), Event.class);
                    initializeEventLayout(event);
                    eventChanged = true;
                } else if (resultCode == Activity.RESULT_CANCELED) {
                    // Write your code if there's no result
                }
                break;
        }
    }

    private void confirmRemoveEventAlertDialog(final String id) {
        AlertDialog.Builder builder;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
            builder = new AlertDialog.Builder(this, android.R.style.Theme_Material_Light_Dialog_Alert);
        else
            builder = new AlertDialog.Builder(this);

        builder.setTitle(R.string.alert_dialog_title_confirm)
                .setMessage(R.string.alert_dialog_message_confirm_event_removal)
                .setPositiveButton(R.string.alert_dialog_positive_button, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        removeEvent(id);
                    }
                })
                .setNegativeButton(R.string.alert_dialog_negative_button, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // do nothing
                    }
                })
                .show();
    }

    private void removeEvent(String id) {
        new EventDeleteTask().execute("events/" + id, userLogged.getEmail(), userLogged.getPassword());
    }

    private void removeEventCallback(boolean isEventRemoved) {
        if (isEventRemoved) {
            Toast.makeText(this, R.string.event_removed_successfully, Toast.LENGTH_LONG).show();

            setResult(Activity.RESULT_OK, new Intent());
            finish();
        } else
            Toast.makeText(this, toastMessage, Toast.LENGTH_LONG).show();
    }

    public void onBackPressed() {
        if (eventChanged) {
            Intent intent = new Intent();
            setResult(Activity.RESULT_OK, intent);
        }

        finish();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putString(Constants.EVENT, gson.toJson(event, Event.class));
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        event = gson.fromJson(savedInstanceState.getString(Constants.EVENT), Event.class);
        initializeEventLayout(event);
        eventChanged = true;
    }

    public class SubscribePostTask extends AsyncTask<String, Void, Boolean>{

        @Override
        protected Boolean doInBackground(String... params) {
            // params[1] == email; params[2] == password
            ClientResource cr = new ClientResource(Constants.BASE_URI + params[0] + "/subscribers");
            cr.setChallengeResponse(ChallengeScheme.HTTP_BASIC, params[1], params[2]);
            String jsonResponse = null;
            toastMessage = null;

            boolean isSubscribed = false;

            try {
                StringRepresentation sr = new StringRepresentation(params[1], MediaType.APPLICATION_JSON);
                jsonResponse = cr.post(sr).getText();

                if (cr.getStatus().getCode() == ErrorCodes.INVALID_EVENT_ID)
                    throw gson.fromJson(jsonResponse, InvalidEventIdException.class);
                if (cr.getStatus().getCode() == ErrorCodes.INVALID_USER_EMAIL)
                    throw gson.fromJson(jsonResponse, InvalidUserEmailException.class);
                else if (cr.getStatus().getCode() == ErrorCodes.UNAUTHORIZED_USER)
                    throw gson.fromJson(jsonResponse, UnauthorizedUserException.class);
                else if (cr.getStatus().getCode() == ErrorCodes.VOID_CLASS_FIELD)
                    throw gson.fromJson(jsonResponse, VoidClassFieldException.class);
                else if (cr.getStatus().getCode() == ErrorCodes.GENERIC_SQL)
                    throw gson.fromJson(jsonResponse, GenericSQLException.class);

                isSubscribed = gson.fromJson(jsonResponse, boolean.class);
            } catch (ResourceException | IOException e1) {
                String text = "Error: " + cr.getStatus().getCode() + " - " + cr.getStatus().getDescription() + " - " + cr.getStatus().getReasonPhrase();
                Log.e(Constants.TAG, text);
                toastMessage = text;
            } catch (InvalidEventIdException e2) {
                String text = "Error: " + cr.getStatus().getCode() + " - " + e2.getMessage();
                Log.e(Constants.TAG, text);
                toastMessage = text;
            } catch (UnauthorizedUserException e3) {
                String text = "Error: " + cr.getStatus().getCode() + " - " + e3.getMessage();
                Log.e(Constants.TAG, text);
                toastMessage = text;
            } catch (InvalidUserEmailException e2) {
                String text = "Error: " + cr.getStatus().getCode() + " - " + e2.getMessage();
                Log.e(Constants.TAG, text);
                toastMessage = text;
            } catch (VoidClassFieldException e2) {
                String text = "Error: " + cr.getStatus().getCode() + " - " + e2.getMessage();
                Log.e(Constants.TAG, text);
                toastMessage = text;
            } catch (GenericSQLException e4) {
                String text = "Error: " + cr.getStatus().getCode() + " - " + e4.getMessage();
                Log.e(Constants.TAG, text);
                toastMessage = text;
            }

            return isSubscribed;
        }

        @Override
        protected void onPostExecute(Boolean isSubscribed) {
            ((Button) findViewById(R.id.aev_subscribe_button)).setClickable(false);
        }
    }

    public class EventDeleteTask extends AsyncTask<String, Void, Boolean> {

        @Override
        protected Boolean doInBackground(String... params) {
            ClientResource cr = new ClientResource(Constants.BASE_URI + params[0]);
            cr.setChallengeResponse(ChallengeScheme.HTTP_BASIC, params[1], params[2]);
            String jsonResponse = null;
            toastMessage = null;

            boolean isEventRemoved = false;

            try {
                jsonResponse = cr.delete().getText();

                if (cr.getStatus().getCode() == ErrorCodes.INVALID_EVENT_ID)
                    throw gson.fromJson(jsonResponse, InvalidEventIdException.class);
                else if (cr.getStatus().getCode() == ErrorCodes.UNAUTHORIZED_USER)
                    throw gson.fromJson(jsonResponse, UnauthorizedUserException.class);
                else if (cr.getStatus().getCode() == ErrorCodes.GENERIC_SQL)
                    throw gson.fromJson(jsonResponse, GenericSQLException.class);

                isEventRemoved = gson.fromJson(jsonResponse, boolean.class);
            } catch (ResourceException | IOException e1) {
                String text = "Error: " + cr.getStatus().getCode() + " - " + cr.getStatus().getDescription() + " - " + cr.getStatus().getReasonPhrase();
                Log.e(Constants.TAG, text);
                toastMessage = text;
            } catch (InvalidEventIdException e2) {
                String text = "Error: " + cr.getStatus().getCode() + " - " + e2.getMessage();
                Log.e(Constants.TAG, text);
                toastMessage = text;
            } catch (UnauthorizedUserException e3) {
                String text = "Error: " + cr.getStatus().getCode() + " - " + e3.getMessage();
                Log.e(Constants.TAG, text);
                toastMessage = text;
            } catch (GenericSQLException e4) {
                String text = "Error: " + cr.getStatus().getCode() + " - " + e4.getMessage();
                Log.e(Constants.TAG, text);
                toastMessage = text;
            }

            return isEventRemoved;
        }

        @Override
        protected void onPostExecute(Boolean isEventRemoved) {
            removeEventCallback(isEventRemoved);
        }
    }
}
