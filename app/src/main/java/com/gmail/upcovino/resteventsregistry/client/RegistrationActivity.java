package com.gmail.upcovino.resteventsregistry.client;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.BitmapFactory;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Parcelable;
import android.os.PersistableBundle;
import android.provider.MediaStore;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.util.Patterns;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.gmail.upcovino.resteventsregistry.BuildConfig;
import com.gmail.upcovino.resteventsregistry.R;
import com.gmail.upcovino.resteventsregistry.commons.ErrorCodes;
import com.gmail.upcovino.resteventsregistry.commons.InvalidUserEmailException;
import com.gmail.upcovino.resteventsregistry.commons.UnauthorizedUserException;
import com.gmail.upcovino.resteventsregistry.commons.User;
import com.google.gson.Gson;

import org.restlet.data.ChallengeScheme;
import org.restlet.data.MediaType;
import org.restlet.representation.FileRepresentation;
import org.restlet.resource.ClientResource;
import org.restlet.resource.ResourceException;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class RegistrationActivity extends AppCompatActivity {
    private Gson gson;
    private String toastMessage;

    User user;

    private EditText nameEditText;
    private EditText surnameEditText;
    private EditText emailEditText;
    private EditText passwordEditText;
    private ImageView userImageView;
    private String userImageViewPath;
    private String cameraImageFilePath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration);

        Toolbar toolbar = (Toolbar) findViewById(R.id.ar_toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        gson = new Gson();

        nameEditText = (EditText) findViewById(R.id.ar_nameEditText);
        surnameEditText = (EditText) findViewById(R.id.ar_surnameEditText);
        emailEditText = (EditText) findViewById(R.id.ar_emailEditText);
        passwordEditText = (EditText) findViewById(R.id.ar_passwordEditText);
        userImageView = (ImageView) findViewById(R.id.ar_userImageView);

        ((FloatingActionButton) findViewById(R.id.ar_chooseImageFloatingActionButton)).setOnClickListener(new View.OnClickListener() {
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
                if (nameEditText.getText().toString().isEmpty())
                    Toast.makeText(getApplicationContext(), R.string.empty_name_field_message, Toast.LENGTH_LONG).show();
                else if (surnameEditText.getText().toString().isEmpty())
                    Toast.makeText(getApplicationContext(), R.string.empty_surname_field_message, Toast.LENGTH_LONG).show();
                else if (emailEditText.getText().toString().isEmpty())
                    Toast.makeText(getApplicationContext(), R.string.empty_email_field_message, Toast.LENGTH_LONG).show();
                else if (!Patterns.EMAIL_ADDRESS.matcher(emailEditText.getText().toString()).matches())
                    Toast.makeText(getApplicationContext(), R.string.invalid_email_field_message, Toast.LENGTH_LONG).show();
                else if (passwordEditText.getText().toString().isEmpty())
                    Toast.makeText(getApplicationContext(), R.string.empty_password_field_message, Toast.LENGTH_LONG).show();
                else {
                    confirmAddUserAlertDialog();
                }
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
                userImageViewPath = cursor.getString(columnIndex);
                cursor.close();
            } else { // o è stata scattata
                userImageViewPath = cameraImageFilePath;
                MediaScannerConnection.scanFile(this, new String[] { userImageViewPath }, new String[] { "image/jpeg" }, null); // per aggiornare la galleria
            }

            userImageView.setImageBitmap(BitmapFactory.decodeFile(userImageViewPath));

            userImageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent();
                    intent.setAction(Intent.ACTION_VIEW);
                    intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    intent.setDataAndType(FileProvider.getUriForFile(getApplicationContext(), BuildConfig.APPLICATION_ID + ".provider", new File(userImageViewPath)), "image/*");
                    startActivity(intent);
                }
            });
        }
    }

    private void confirmAddUserAlertDialog() {
        AlertDialog.Builder builder;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
            builder = new AlertDialog.Builder(this, android.R.style.Theme_Material_Light_Dialog_Alert);
        else
            builder = new AlertDialog.Builder(this);

        builder.setTitle(R.string.alert_dialog_title_confirm)
                .setMessage(R.string.alert_dialog_message_confirm_registration)
                .setPositiveButton(R.string.alert_dialog_positive_button, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        addUser();
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

    private void addUser() {
        user = new User(nameEditText.getText().toString(), surnameEditText.getText().toString(), emailEditText.getText().toString(), passwordEditText.getText().toString());
        new UsersRegistryPostTask().execute("users", gson.toJson(user));
    }

    private void addUserCallback(boolean isUserAdded) {
        if (isUserAdded) {
            if (userImageViewPath == null) {
                Toast.makeText(getApplicationContext(), user.getName() + " " + user.getSurname() + getString(R.string.successful_registration), Toast.LENGTH_LONG).show();
                finish();
            } else {
                uploadUserPhoto();
            }
        } else
            Toast.makeText(getApplicationContext(), toastMessage, Toast.LENGTH_LONG).show();
    }

    private void uploadUserPhoto() {
        new UserPhotoPutTask().execute("users/" + user.getEmail() + "/photo", user.getEmail(), user.getPassword(), userImageViewPath);
    }

    private void uploadUserPhotoCallback(boolean isPhotoAdded) {
        if (isPhotoAdded) {
            Toast.makeText(getApplicationContext(), user.getName() + " " + user.getSurname() + getString(R.string.successful_registration), Toast.LENGTH_LONG).show();
            finish();
        } else
            Toast.makeText(getApplicationContext(), toastMessage, Toast.LENGTH_LONG).show();
    }

    public void onBackPressed() {
        AlertDialog.Builder builder;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
            builder = new AlertDialog.Builder(this, android.R.style.Theme_Material_Light_Dialog_Alert);
        else
            builder = new AlertDialog.Builder(this);

        builder.setTitle(R.string.alert_dialog_title_warning)
                .setMessage(R.string.alert_dialog_message_registration)
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
        outState.putString(Constants.USER_IMAGE_VIEW_PATH, userImageViewPath);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        cameraImageFilePath = savedInstanceState.getString(Constants.CAMERA_IMAGE_FILE_PATH);
        userImageViewPath = savedInstanceState.getString(Constants.USER_IMAGE_VIEW_PATH);

        if (userImageViewPath != null) {
            userImageView.setImageBitmap(BitmapFactory.decodeFile(userImageViewPath));
            userImageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent();
                    intent.setAction(Intent.ACTION_VIEW);
                    intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    intent.setDataAndType(FileProvider.getUriForFile(getApplicationContext(), BuildConfig.APPLICATION_ID + ".provider", new File(userImageViewPath)), "image/*");
                    startActivity(intent);
                }
            });
        }
    }

//    @Override
//    protected void onPause() {
//        super.onPause();
//        Log.e(Constants.TAG, "onPause");
//    }
//
//    @Override
//    protected void onResume() {
//        super.onResume();
//        Log.e(Constants.TAG, "onResume");
//    }

    public class UsersRegistryPostTask extends AsyncTask<String, Void, Boolean> {

        @Override
        protected Boolean doInBackground(String... params) {
            ClientResource cr = new ClientResource(Constants.BASE_URI + params[0]);
            String jsonResponse = null;

            boolean isUserAdded = false;

            try {
                jsonResponse = cr.post(params[1]).getText();

                if (cr.getStatus().getCode() == ErrorCodes.INVALID_USER_EMAIL)
                    throw gson.fromJson(jsonResponse, InvalidUserEmailException.class);

                isUserAdded = gson.fromJson(jsonResponse, boolean.class);
            } catch (ResourceException | IOException e1) {
                String text = "Error: " + cr.getStatus().getCode() + " - " + cr.getStatus().getDescription() + " - " + cr.getStatus().getReasonPhrase();
                Log.e(Constants.TAG, text);
                toastMessage = text;
            } catch (InvalidUserEmailException e2) {
                String text = "Error: " + cr.getStatus().getCode() + " - " + e2.getMessage();
                Log.e(Constants.TAG, text);
                toastMessage = getResources().getString(R.string.duplicated_email_message);
            }

            return isUserAdded;
        }

        @Override
        protected void onPostExecute(Boolean isUserAdded) {
            addUserCallback(isUserAdded);
        }
    }

    public class UserPhotoPutTask extends AsyncTask<String, Void, Boolean> {

        @Override
        protected Boolean doInBackground(String... params) {
            ClientResource cr = new ClientResource(Constants.BASE_URI + params[0]);
            cr.setChallengeResponse(ChallengeScheme.HTTP_BASIC, params[1], params[2]);
            String jsonResponse = null;

            boolean isUserPhotoUploaded = false;

            FileRepresentation payload = new FileRepresentation(new File(params[3]), MediaType.IMAGE_JPEG);

            try {
                jsonResponse = cr.put(payload).getText();

                if (cr.getStatus().getCode() == ErrorCodes.INVALID_USER_EMAIL)
                    throw gson.fromJson(jsonResponse, InvalidUserEmailException.class);
                else if (cr.getStatus().getCode() == ErrorCodes.UNAUTHORIZED_USER)
                    throw gson.fromJson(jsonResponse, UnauthorizedUserException.class);

                isUserPhotoUploaded = gson.fromJson(jsonResponse, boolean.class);
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
            } catch (InvalidUserEmailException e2) {
                String text = "Error: " + cr.getStatus().getCode() + " - " + e2.getMessage();
                Log.e(Constants.TAG, text);
                toastMessage = text;
            } catch (UnauthorizedUserException e2) {
                String text = "Error: " + cr.getStatus().getCode() + " - " + e2.getMessage();
                Log.e(Constants.TAG, text);
                toastMessage = text;
            }

            return isUserPhotoUploaded;
        }

        @Override
        protected void onPostExecute(Boolean isUserPhotoUploaded) {
            uploadUserPhotoCallback(isUserPhotoUploaded);
        }
    }
}
