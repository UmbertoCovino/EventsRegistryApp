package com.gmail.upcovino.resteventsregistry.client;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Parcelable;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.gmail.upcovino.resteventsregistry.BuildConfig;
import com.gmail.upcovino.resteventsregistry.R;
import com.gmail.upcovino.resteventsregistry.commons.User;
import com.gmail.upcovino.resteventsregistry.commons.exceptions.ErrorCodes;
import com.gmail.upcovino.resteventsregistry.commons.exceptions.InvalidUserEmailException;
import com.gmail.upcovino.resteventsregistry.commons.exceptions.UnauthorizedUserException;
import com.google.gson.Gson;

import org.restlet.data.ChallengeScheme;
import org.restlet.data.MediaType;
import org.restlet.representation.FileRepresentation;
import org.restlet.representation.StringRepresentation;
import org.restlet.resource.ClientResource;
import org.restlet.resource.ResourceException;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class ModifyUserActivity extends AppCompatActivity {
    private Gson gson;
    private String toastMessage;
    private User userLogged;
    private String storageDirectory;

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
        ((CollapsingToolbarLayout) findViewById(R.id.ar_collapsingToolbarLayout)).setTitle(getResources().getString(R.string.modify_your_profile));

        gson = new Gson();

        userLogged = gson.fromJson(getIntent().getStringExtra(Constants.USER_LOGGED), User.class);
        storageDirectory = getIntent().getStringExtra(Constants.STORAGE_DIRECTORY_EXTRA);



        nameEditText = (EditText) findViewById(R.id.ar_nameEditText);
        surnameEditText = (EditText) findViewById(R.id.ar_surnameEditText);
        emailEditText = (EditText) findViewById(R.id.ar_emailEditText);
        passwordEditText = (EditText) findViewById(R.id.ar_passwordEditText);
        userImageView = (ImageView) findViewById(R.id.ar_userImageView);

        nameEditText.setText(userLogged.getName());
        surnameEditText.setText(userLogged.getSurname());
        emailEditText.setText(userLogged.getEmail());
        emailEditText.setEnabled(false);
        passwordEditText.setText(userLogged.getPassword());

        if (userLogged.getPhoto() != null) {
            Bitmap bitmap = BitmapFactory.decodeFile(storageDirectory + "/" + userLogged.getPhoto());
            userImageView.setImageBitmap(bitmap);

            userImageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent();
                    intent.setAction(Intent.ACTION_VIEW);
                    intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    intent.setDataAndType(FileProvider.getUriForFile(getApplicationContext(), BuildConfig.APPLICATION_ID + ".provider", new File(storageDirectory + "/" + userLogged.getPhoto())), "image/*");
                    startActivity(intent);
                }
            });
        } else
            userImageView.setImageResource(R.drawable.default_user_icon);



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
                else if (passwordEditText.getText().toString().isEmpty())
                    Toast.makeText(getApplicationContext(), R.string.empty_password_field_message, Toast.LENGTH_LONG).show();
                else {
                    confirmModifyUserAlertDialog();
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

    private void confirmModifyUserAlertDialog() {
        AlertDialog.Builder builder;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
            builder = new AlertDialog.Builder(this, android.R.style.Theme_Material_Light_Dialog_Alert);
        else
            builder = new AlertDialog.Builder(this);

        builder.setTitle(R.string.alert_dialog_title_confirm)
                .setMessage(R.string.alert_dialog_message_confirm_modify_user)
                .setPositiveButton(R.string.alert_dialog_positive_button, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        modifyUser();
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

    private void modifyUser() {
        User user = new User(nameEditText.getText().toString(), surnameEditText.getText().toString(), emailEditText.getText().toString(), passwordEditText.getText().toString());
        new UsersRegistryPutTask().execute("users", userLogged.getEmail(), userLogged.getPassword(), gson.toJson(user));
    }

    private void modifyUserCallback(boolean isUserModified) {
        if (isUserModified) {
            userLogged.setName(nameEditText.getText().toString());
            userLogged.setSurname(surnameEditText.getText().toString());
            userLogged.setPassword(passwordEditText.getText().toString());

            if (userImageViewPath == null) {
                userSuccessfullyModified();
            } else {
                uploadUserPhoto();
            }
        } else
            Toast.makeText(getApplicationContext(), toastMessage, Toast.LENGTH_LONG).show();
    }

    private void uploadUserPhoto() {
        new UserPhotoPutTask().execute("users/" + userLogged.getEmail() + "/photo", userLogged.getEmail(), userLogged.getPassword(), userImageViewPath);
    }

    private void uploadUserPhotoCallback(boolean isPhotoModified) {
        try {
            if (isPhotoModified) {
                if (userLogged.getPhoto() == null)
                    userLogged.setPhoto(userLogged.getEmail() + ".jpg");
                Constants.copyFile(new File(userImageViewPath), new File(storageDirectory + "/" + userLogged.getPhoto()));
                userSuccessfullyModified();
            } else
                Toast.makeText(getApplicationContext(), toastMessage, Toast.LENGTH_LONG).show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void userSuccessfullyModified() {
        Toast.makeText(getApplicationContext(), userLogged.getName() + " " + userLogged.getSurname() + getString(R.string.successful_modified_user), Toast.LENGTH_LONG).show();

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(Constants.USER, gson.toJson(userLogged, User.class));
        editor.apply();

        Intent intent = new Intent();
        intent.putExtra(Constants.USER_EXTRA, gson.toJson(userLogged, User.class));
        setResult(Activity.RESULT_OK, intent);

        finish();
    }

    public void onBackPressed() {
        AlertDialog.Builder builder;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
            builder = new AlertDialog.Builder(this, android.R.style.Theme_Material_Light_Dialog_Alert);
        else
            builder = new AlertDialog.Builder(this);

        builder.setTitle(R.string.alert_dialog_title_warning)
                .setMessage(R.string.alert_dialog_message_user_editing)
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





    public class UsersRegistryPutTask extends AsyncTask<String, Void, Boolean> {

        @Override
        protected Boolean doInBackground(String... params) {
            ClientResource cr = new ClientResource(Constants.BASE_URI + params[0]);
            cr.setChallengeResponse(ChallengeScheme.HTTP_BASIC, params[1], params[2]);
            String jsonResponse = null;

            boolean isUserModified = false;

            try {
                StringRepresentation sr = new StringRepresentation(params[3], MediaType.APPLICATION_JSON);
                jsonResponse = cr.put(sr).getText();

                if (cr.getStatus().getCode() == ErrorCodes.INVALID_USER_EMAIL)
                    throw gson.fromJson(jsonResponse, InvalidUserEmailException.class);

                isUserModified = gson.fromJson(jsonResponse, boolean.class);
            } catch (ResourceException | IOException e1) {
                String text = "Error: " + cr.getStatus().getCode() + " - " + cr.getStatus().getDescription() + " - " + cr.getStatus().getReasonPhrase();
                Log.e(Constants.TAG, text);
                toastMessage = text;
            } catch (InvalidUserEmailException e2) {
                String text = "Error: " + cr.getStatus().getCode() + " - " + e2.getMessage();
                Log.e(Constants.TAG, text);
                toastMessage = getResources().getString(R.string.duplicated_email_message);
            }

            return isUserModified;
        }

        @Override
        protected void onPostExecute(Boolean isUserModified) {
            modifyUserCallback(isUserModified);
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
