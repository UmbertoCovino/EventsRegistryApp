package com.gmail.upcovino.resteventsregistry.client;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v4.content.FileProvider;
import android.support.v4.view.GravityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.gmail.upcovino.resteventsregistry.BuildConfig;
import com.gmail.upcovino.resteventsregistry.R;
import com.gmail.upcovino.resteventsregistry.commons.User;
import com.google.gson.Gson;

import java.io.File;

public class UserActivity extends AppCompatActivity {
    private Gson gson;
    private User userLogged;
    private User user;

    private SharedPreferences preferences;
    private String storageDirectory;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user);

        Toolbar toolbar = (Toolbar) findViewById(R.id.au_toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        gson = new Gson();

        preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        String userJson = preferences.getString(Constants.USER, null);
        if (userJson != null)
            userLogged = gson.fromJson(userJson, User.class);

        user = gson.fromJson(getIntent().getStringExtra(Constants.USER_EXTRA), User.class);
        storageDirectory = getIntent().getStringExtra(Constants.STORAGE_DIRECTORY_EXTRA);

        if (user.getEmail().equals(userLogged.getEmail()))
            ((TextView) findViewById(R.id.au_userEmailTextView)).setText(user.getEmail());

        initializeUserLayout(user);
    }

    private void initializeUserLayout(final User user) {
        ((CollapsingToolbarLayout) findViewById(R.id.au_collapsingToolbarLayout)).setTitle(user.getName() + " " + user.getSurname());

        ImageView userPhotoImageView = ((ImageView) findViewById(R.id.au_userImageView));
        if (user.getPhoto() != null) {
            Bitmap bitmap = BitmapFactory.decodeFile(storageDirectory + "/" + user.getPhoto());
            userPhotoImageView.setImageBitmap(bitmap);

            userPhotoImageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent();
                    intent.setAction(Intent.ACTION_VIEW);
                    intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    intent.setDataAndType(FileProvider.getUriForFile(getApplicationContext(), BuildConfig.APPLICATION_ID + ".provider", new File(storageDirectory + "/" + user.getPhoto())), "image/*");
                    startActivity(intent);
                }
            });
        } else
            userPhotoImageView.setImageResource(R.drawable.default_user_icon);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (user.getEmail().equals(userLogged.getEmail())) {
            getMenuInflater().inflate(R.menu.activity_event_user_menu, menu);
            menu.getItem(0).setVisible(false);
            return true;
        }
        return false;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.aev_menu_edit:
                Intent intent = new Intent(getApplicationContext(), ModifyUserActivity.class);
                user.setPassword(userLogged.getPassword());
                intent.putExtra(Constants.USER_LOGGED, gson.toJson(user));
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
                    user = gson.fromJson(data.getStringExtra(Constants.USER_EXTRA), User.class);
                    initializeUserLayout(user);
                } else if (resultCode == Activity.RESULT_CANCELED) {
                    // Write your code if there's no result
                }
                break;
        }
    }

    public void onBackPressed() {
        if (user.getEmail().equals(userLogged.getEmail())) {
            Intent intent = new Intent();
            intent.putExtra(Constants.USER_EXTRA, gson.toJson(user, User.class));
            setResult(Activity.RESULT_OK, intent);
        }

        finish();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putString(Constants.USER, gson.toJson(user, User.class));
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        user = gson.fromJson(savedInstanceState.getString(Constants.USER), User.class);
        initializeUserLayout(user);
    }
}
