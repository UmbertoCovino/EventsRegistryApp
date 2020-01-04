package com.gmail.upcovino.resteventsregistry.client;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.BitmapFactory;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Environment;
import android.os.Parcelable;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.gmail.upcovino.resteventsregistry.BuildConfig;
import com.gmail.upcovino.resteventsregistry.R;
import com.gmail.upcovino.resteventsregistry.commons.Event;
import com.gmail.upcovino.resteventsregistry.commons.User;
import com.gmail.upcovino.resteventsregistry.commons.exceptions.ErrorCodes;
import com.gmail.upcovino.resteventsregistry.commons.exceptions.GenericSQLException;
import com.gmail.upcovino.resteventsregistry.commons.exceptions.InvalidEventIdException;
import com.gmail.upcovino.resteventsregistry.commons.exceptions.JsonParsingException;
import com.gmail.upcovino.resteventsregistry.commons.exceptions.UnauthorizedUserException;
import com.gmail.upcovino.resteventsregistry.commons.exceptions.VoidClassFieldException;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.restlet.data.ChallengeScheme;
import org.restlet.data.MediaType;
import org.restlet.representation.FileRepresentation;
import org.restlet.representation.StringRepresentation;
import org.restlet.resource.ClientResource;
import org.restlet.resource.ResourceException;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.StringTokenizer;

public class AddEventActivity extends AppCompatActivity {
    private Gson gson;
    private String toastMessage;
    private User userLogged;

    private EditText titleEditText;
    private EditText descriptionEditText;
    private EditText dateStartEditText;
    private EditText dateEndEditText;
    private EditText startTimeEditText;
    private EditText endTimeEditText;
    private ImageView eventImageView;
    private String eventImageViewPath;
    private String cameraImageFilePath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_event);

        Toolbar toolbar = (Toolbar) findViewById(R.id.ade_toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
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

        titleEditText = (EditText) findViewById(R.id.ade_titleEditText);
        descriptionEditText = (EditText) findViewById(R.id.ade_descriptionEditText);
        dateStartEditText = (EditText) findViewById(R.id.ade_dateStartEditText);
        dateEndEditText = (EditText) findViewById(R.id.ade_dateEndEditText);
        startTimeEditText = (EditText) findViewById(R.id.ade_startTimeEditText);
        endTimeEditText = (EditText) findViewById(R.id.ade_endTimeEditText);
        eventImageView = (ImageView) findViewById(R.id.ade_eventImageView);

        dateStartEditText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Date todayDate = new Date();
                int dayOfMonth = Integer.parseInt(new SimpleDateFormat("dd").format(todayDate));
                int month = Integer.parseInt(new SimpleDateFormat("MM").format(todayDate)) - 1;
                int year = Integer.parseInt(new SimpleDateFormat("yyyy").format(todayDate));

                if (dateStartEditText.getText().length() != 0) {
                    StringTokenizer st = new StringTokenizer(dateStartEditText.getText().toString(), "-");
                    year       = Integer.parseInt(st.nextToken());
                    month      = Integer.parseInt(st.nextToken());
                    dayOfMonth = Integer.parseInt(st.nextToken()) - 1;
                }

                DatePickerDialog datePickerDialog = new DatePickerDialog(v.getContext(), new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                        dateStartEditText.setText(String.format("%04d-%02d-%02d", year, month + 1, dayOfMonth));
                    }
                }, year, month, dayOfMonth);

                datePickerDialog.show();
            }
        });

        dateEndEditText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Date todayDate = new Date();
                int dayOfMonth = Integer.parseInt(new SimpleDateFormat("dd").format(todayDate));
                int month = Integer.parseInt(new SimpleDateFormat("MM").format(todayDate)) - 1;
                int year = Integer.parseInt(new SimpleDateFormat("yyyy").format(todayDate));

                if (dateEndEditText.getText().length() != 0) {
                    StringTokenizer st = new StringTokenizer(dateStartEditText.getText().toString(), "-");
                    year       = Integer.parseInt(st.nextToken());
                    month      = Integer.parseInt(st.nextToken());
                    dayOfMonth = Integer.parseInt(st.nextToken()) - 1;
                }

                DatePickerDialog datePickerDialog = new DatePickerDialog(v.getContext(), new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                        dateEndEditText.setText(String.format("%04d-%02d-%02d", year, month + 1, dayOfMonth));
                    }
                }, year, month, dayOfMonth);

                datePickerDialog.show();
            }
        });


        startTimeEditText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int hourOfDay = 12, minute = 0;

                if (startTimeEditText.getText().length() != 0) {
                    hourOfDay = Integer.parseInt(startTimeEditText.getText().subSequence(0, 2).toString());
                    minute    = Integer.parseInt(startTimeEditText.getText().subSequence(3, 5).toString());
                }

                TimePickerDialog timePickerDialog = new TimePickerDialog(v.getContext(), new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                        startTimeEditText.setText(String.format("%02d:%02d", hourOfDay, minute));
                    }
                }, hourOfDay, minute, true);

                timePickerDialog.show();
            }
        });

        endTimeEditText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int hourOfDay = 12, minute = 0;

                if (endTimeEditText.getText().length() != 0) {
                    hourOfDay = Integer.parseInt(endTimeEditText.getText().subSequence(0, 2).toString());
                    minute    = Integer.parseInt(endTimeEditText.getText().subSequence(3, 5).toString());
                }

                TimePickerDialog timePickerDialog = new TimePickerDialog(v.getContext(), new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                        endTimeEditText.setText(String.format("%02d:%02d", hourOfDay, minute));
                    }
                }, hourOfDay, minute, true);

                timePickerDialog.show();
            }
        });

        ((FloatingActionButton) findViewById(R.id.ade_chooseImageFloatingActionButton)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

                String imageFileName = "IMG_" + new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
                File storageDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM + "/Camera");
                File imageFile = new File(storageDir + "/" + imageFileName + ".jpg");

                cameraImageFilePath = imageFile.getAbsolutePath();

                Uri outputUri = FileProvider.getUriForFile(getApplicationContext(), BuildConfig.APPLICATION_ID + ".provider", imageFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, outputUri);
                takePictureIntent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);



                Intent pickPhotoIntent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                pickPhotoIntent.setType("image/jpeg");



                List<Intent> intentList = new ArrayList<>();
                intentList.add(takePictureIntent);
                intentList.add(pickPhotoIntent);

                Intent chooserIntent = Intent.createChooser(intentList.remove(intentList.size() - 1), getString(R.string.pick_image_intent));
                chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, intentList.toArray(new Parcelable[]{}));

                startActivityForResult(chooserIntent, Constants.IMAGE_PICKER);
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.done_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_done:
                Date eventDate = null, eventStartTime = null,
                     eventEndTime = null;
                try {
                    Calendar date = Calendar.getInstance();
                    date.setTime(Event.DATE_SDF.parse(dateStartEditText.getText().toString()));
                    Calendar startTime = Calendar.getInstance();
                    startTime.setTime(Event.TIME_SDF.parse(startTimeEditText.getText().toString()));
                    date.set(Calendar.HOUR_OF_DAY, startTime.get(Calendar.HOUR_OF_DAY));
                    date.set(Calendar.MINUTE, startTime.get(Calendar.MINUTE));
                    eventDate = date.getTime();

                    eventStartTime = Event.DATETIME_SDF.parse(dateStartEditText.getText().toString() + " " +
                            startTimeEditText.getText().toString() + ":00");
                    eventEndTime = Event.DATETIME_SDF.parse(dateEndEditText.getText().toString() + " " +
                            endTimeEditText.getText().toString()+":00");
                } catch (ParseException e) {
                    e.printStackTrace();
                }

                if (titleEditText.getText().toString().isEmpty())
                    Toast.makeText(getApplicationContext(), R.string.empty_title_field_message, Toast.LENGTH_LONG).show();
                else if (descriptionEditText.getText().toString().isEmpty())
                    Toast.makeText(getApplicationContext(), R.string.empty_description_field_message, Toast.LENGTH_LONG).show();
                else if (dateStartEditText.getText().toString().isEmpty())
                    Toast.makeText(getApplicationContext(), R.string.empty_date_field_message, Toast.LENGTH_LONG).show();
                else if (dateEndEditText.getText().toString().isEmpty())
                    Toast.makeText(getApplicationContext(), R.string.empty_date_field_message, Toast.LENGTH_LONG).show();
                else if (startTimeEditText.getText().toString().isEmpty())
                    Toast.makeText(getApplicationContext(), R.string.empty_from_time_field_message, Toast.LENGTH_LONG).show();
                else if (endTimeEditText.getText().toString().isEmpty())
                    Toast.makeText(getApplicationContext(), R.string.empty_to_time_field_message, Toast.LENGTH_LONG).show();
                else if (eventDate.before(new Date()))
                    Toast.makeText(getApplicationContext(), R.string.invalid_date_field_message, Toast.LENGTH_LONG).show();
                else if (eventStartTime.after(eventEndTime))
                    Toast.makeText(getApplicationContext(), R.string.invalid_end_date_field_message, Toast.LENGTH_LONG).show();
                else
                    confirmAddEventAlertDialog();
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == Constants.IMAGE_PICKER && resultCode == RESULT_OK && data != null) {
            if (data.getData() != null) { // se la foto proviene dalla galleria
                Uri uri = data.getData();
                String[] filePathColumn = {MediaStore.Images.Media.DATA};

                Cursor cursor = getContentResolver().query(uri, filePathColumn, null, null, null);
                cursor.moveToFirst();

                int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
                eventImageViewPath = cursor.getString(columnIndex);
                cursor.close();
            } else { // o è stata scattata
                eventImageViewPath = cameraImageFilePath;
                MediaScannerConnection.scanFile(this, new String[] { eventImageViewPath }, new String[] { "image/jpeg" }, null); // per aggiornare la galleria
            }

            eventImageView.setImageBitmap(BitmapFactory.decodeFile(eventImageViewPath));

            eventImageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent();
                    intent.setAction(Intent.ACTION_VIEW);
                    intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    intent.setDataAndType(FileProvider.getUriForFile(getApplicationContext(), BuildConfig.APPLICATION_ID + ".provider", new File(eventImageViewPath)), "image/*");
                    startActivity(intent);
                }
            });
        }
    }

    private void confirmAddEventAlertDialog() {
        AlertDialog.Builder builder;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
            builder = new AlertDialog.Builder(this, android.R.style.Theme_Material_Light_Dialog_Alert);
        else
            builder = new AlertDialog.Builder(this);

        builder.setTitle(R.string.alert_dialog_title_confirm)
                .setMessage(R.string.alert_dialog_message_confirm_event_addition)
                .setPositiveButton(R.string.alert_dialog_positive_button, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        addEvent();
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

    private void addEvent() {
        try {
            Event event = new Event(titleEditText.getText().toString(),
                    Event.DATETIME_SDF.parse(dateStartEditText.getText().toString() + " "
                            + startTimeEditText.getText().toString()+":00"),
                    Event.DATETIME_SDF.parse(dateEndEditText.getText().toString() + " "
                            + endTimeEditText.getText().toString()+":00"), descriptionEditText.getText().toString());
            new EventsRegistryPostTask().execute("events", userLogged.getEmail(), userLogged.getPassword(), gson.toJson(event));
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    private void addEventCallback(String eventAddedId) {
        if (eventAddedId != null) {
            if (eventImageViewPath == null) {
                eventSuccessfullyAdded();
            } else {
                uploadEventPhoto(eventAddedId);
            }
        } else
            Toast.makeText(getApplicationContext(), toastMessage, Toast.LENGTH_LONG).show();
    }

    private void uploadEventPhoto(String id) {
        new EventPhotoPutTask().execute("events/" + id + "/photo", userLogged.getEmail(), userLogged.getPassword(), eventImageViewPath);
    }

    private void uploadEventPhotoCallback(boolean isPhotoAdded) {
        if (isPhotoAdded) {
            eventSuccessfullyAdded();
        } else
            Toast.makeText(getApplicationContext(), toastMessage, Toast.LENGTH_LONG).show();
    }

    private void eventSuccessfullyAdded() {
        Toast.makeText(getApplicationContext(), getString(R.string.successful_add_event), Toast.LENGTH_LONG).show();

        setResult(Activity.RESULT_OK, new Intent());
        finish();
    }

    public void onBackPressed() {
        AlertDialog.Builder builder;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
            builder = new AlertDialog.Builder(this, android.R.style.Theme_Material_Light_Dialog_Alert);
        else
            builder = new AlertDialog.Builder(this);

        builder.setTitle(R.string.alert_dialog_title_warning)
                .setMessage(R.string.alert_dialog_message_event_addition)
                .setPositiveButton(R.string.alert_dialog_positive_button, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
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

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putString(Constants.CAMERA_IMAGE_FILE_PATH, cameraImageFilePath);
        outState.putString(Constants.EVENT_IMAGE_VIEW_PATH, eventImageViewPath);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        cameraImageFilePath = savedInstanceState.getString(Constants.CAMERA_IMAGE_FILE_PATH);
        eventImageViewPath = savedInstanceState.getString(Constants.EVENT_IMAGE_VIEW_PATH);

        if (eventImageViewPath != null) {
            eventImageView.setImageBitmap(BitmapFactory.decodeFile(eventImageViewPath));
            eventImageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent();
                    intent.setAction(Intent.ACTION_VIEW);
                    intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    intent.setDataAndType(FileProvider.getUriForFile(getApplicationContext(), BuildConfig.APPLICATION_ID + ".provider", new File(eventImageViewPath)), "image/*");
                    startActivity(intent);
                }
            });
        }
    }

    public class EventsRegistryPostTask extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... params) {
            ClientResource cr = new ClientResource(Constants.BASE_URI + params[0]);
            cr.setChallengeResponse(ChallengeScheme.HTTP_BASIC, params[1], params[2]);
            String jsonResponse = null;

            String eventAddedId = null;

            try {
                StringRepresentation sr = new StringRepresentation(params[3], MediaType.APPLICATION_JSON);
                jsonResponse = cr.post(sr).getText();

                if (cr.getStatus().getCode() == ErrorCodes.INVALID_EVENT_ID)
                    throw gson.fromJson(jsonResponse, InvalidEventIdException.class);
                else if (cr.getStatus().getCode() == ErrorCodes.VOID_CLASS_FIELD)
                    throw gson.fromJson(jsonResponse, VoidClassFieldException.class);
                else if (cr.getStatus().getCode() == ErrorCodes.JSON_PARSING)
                    throw gson.fromJson(jsonResponse, JsonParsingException.class);
                else if (cr.getStatus().getCode() == ErrorCodes.GENERIC_SQL)
                    throw gson.fromJson(jsonResponse, GenericSQLException.class);

                eventAddedId = gson.fromJson(jsonResponse, String.class);
            } catch (ResourceException | IOException e1) {
                String text = "Error: " + cr.getStatus().getCode() + " - " + cr.getStatus().getDescription() + " - " + cr.getStatus().getReasonPhrase();
                Log.e(Constants.TAG, text);
                toastMessage = text;
            } catch (InvalidEventIdException e2) {
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
            } catch (JsonParsingException e5) {
                String text = "Error: " + cr.getStatus().getCode() + " - " + e5.getMessage();
                Log.e(Constants.TAG, text);
                toastMessage = text;
            }

            return eventAddedId;
        }

        @Override
        protected void onPostExecute(String eventAddedId) {
            addEventCallback(eventAddedId);
        }
    }

    public class EventPhotoPutTask extends AsyncTask<String, Void, Boolean> {

        @Override
        protected Boolean doInBackground(String... params) {
            ClientResource cr = new ClientResource(Constants.BASE_URI + params[0]);
            cr.setChallengeResponse(ChallengeScheme.HTTP_BASIC, params[1], params[2]);
            String jsonResponse = null;

            boolean isEventPhotoUploaded = false;

            FileRepresentation payload = new FileRepresentation(new File(params[3]), MediaType.IMAGE_JPEG);

            try {
                jsonResponse = cr.put(payload).getText();

                if (cr.getStatus().getCode() == ErrorCodes.INVALID_EVENT_ID)
                    throw gson.fromJson(jsonResponse, InvalidEventIdException.class);
                else if (cr.getStatus().getCode() == ErrorCodes.UNAUTHORIZED_USER)
                    throw gson.fromJson(jsonResponse, UnauthorizedUserException.class);
                else if (cr.getStatus().getCode() == ErrorCodes.GENERIC_SQL)
                    throw gson.fromJson(jsonResponse, GenericSQLException.class);

                isEventPhotoUploaded = gson.fromJson(jsonResponse, boolean.class);
            } catch (ResourceException | IOException e1) {
                if (org.restlet.data.Status.CLIENT_ERROR_UNAUTHORIZED.equals(cr.getStatus())) {
                    String text = getString(R.string.access_unauthorized_by_the_server);
                    Log.e(Constants.TAG, text);
                    toastMessage = text;
                } else {
                    String text = "Error: " + cr.getStatus().getCode() + " - " + cr.getStatus().getDescription() + " - " + cr.getStatus().getReasonPhrase();
                    Log.e(Constants.TAG, text);
                    toastMessage = text;
                }
            } catch (InvalidEventIdException e2) {
                String text = "Error: " + cr.getStatus().getCode() + " - " + e2.getMessage();
                Log.e(Constants.TAG, text);
                toastMessage = text;
            } catch (UnauthorizedUserException e2) {
                String text = "Error: " + cr.getStatus().getCode() + " - " + e2.getMessage();
                Log.e(Constants.TAG, text);
                toastMessage = text;
            } catch (GenericSQLException e4) {
                String text = "Error: " + cr.getStatus().getCode() + " - " + e4.getMessage();
                Log.e(Constants.TAG, text);
                toastMessage = text;
            }

            return isEventPhotoUploaded;
        }

        @Override
        protected void onPostExecute(Boolean isEventPhotoUploaded) {
            uploadEventPhotoCallback(isEventPhotoUploaded);
        }
    }
}
