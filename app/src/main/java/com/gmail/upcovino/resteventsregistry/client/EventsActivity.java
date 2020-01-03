package com.gmail.upcovino.resteventsregistry.client;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaScannerConnection;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.content.FileProvider;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.SpannableString;
import android.text.style.UnderlineSpan;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.gmail.upcovino.resteventsregistry.BuildConfig;
import com.gmail.upcovino.resteventsregistry.R;
import com.gmail.upcovino.resteventsregistry.commons.Event;
import com.gmail.upcovino.resteventsregistry.commons.User;
import com.gmail.upcovino.resteventsregistry.commons.exceptions.ErrorCodes;
import com.gmail.upcovino.resteventsregistry.commons.exceptions.InvalidEventIdException;
import com.gmail.upcovino.resteventsregistry.commons.exceptions.UnauthorizedUserException;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.restlet.data.ChallengeScheme;
import org.restlet.representation.Representation;
import org.restlet.resource.ClientResource;
import org.restlet.resource.ResourceException;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.StringTokenizer;

public class EventsActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    private Gson gson;
    private String toastMessage;
    private SharedPreferences preferences;
    private NavigationView navigationView;
    private SwipeRefreshLayout swipeRefreshLayout;

    private ArrayList<Event> events;
    private User userLogged;
    private File storageDirectory;
    private int eventsListViewSelectedItemPosition;

    private DrawerLayout drawer;
    private final int EVENT_ACTIVITY_RESULT_NUMBER = 1,
                      ADD_EVENT_ACTIVITY_RESULT_NUMBER = 2,
                      MODIFY_EVENT_ACTIVITY_RESULT_NUMBER = 3,
                      USER_ACTIVITY_RESULT_NUMBER = 4;
    private String eventsRegistryGetUri;

    private String filterFromDate;
    private String filterFromTime;
    private String filterToDate;
    private String filterToTime;

    private int itemCheckedId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_events);

        Toolbar toolbar = (Toolbar) findViewById(R.id.ae_toolbar);
        setSupportActionBar(toolbar);

        drawer = (DrawerLayout) findViewById(R.id.ae_drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        navigationView = (NavigationView) findViewById(R.id.ae_navigationView);
        navigationView.setNavigationItemSelectedListener(this);

        //gson = new Gson();
        gson = new GsonBuilder()
                .registerTypeAdapter(Date.class, new Constants.DateTypeAdapter())
                .create();
        storageDirectory = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).getAbsolutePath() + "/eventsRegistry"); //getExternalCacheDir();
        if (!storageDirectory.exists())
            storageDirectory.mkdir();

        preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        String userJson = preferences.getString(Constants.USER, null);
        if (userJson != null)
            userLogged = gson.fromJson(userJson, User.class);

        if (userLogged != null)
            ((FloatingActionButton) findViewById(R.id.ae_floatingActionButton)).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    openAddEventActivity();
                }
            });
        else
            ((FloatingActionButton) findViewById(R.id.ae_floatingActionButton)).setVisibility(View.INVISIBLE);

        swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.ae_swipeRefresh);
        swipeRefreshLayout.setColorSchemeResources(R.color.colorPrimary);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refreshEvents();
            }
        });

        ((TextView) findViewById(R.id.ae_filterTextView)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openFilterInputDialog(null);
            }
        });

        if (savedInstanceState != null) {
            // do nothing
        } else {
            eventsRegistryGetUri = "events/after/" + Event.DATETIME_SDF.format(new Date());

            refreshEvents();
        }
    }

    private void refreshEvents() {
        swipeRefreshLayout.setRefreshing(true);

        File[] files = storageDirectory.listFiles();
        if (files != null) // some JVMs return null for empty dirs
            for (File file: files) {
                file.delete();
                getContentResolver().delete(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, MediaStore.Images.Media.DATA + "=?", new String[]{ file.getAbsolutePath() } ); // per aggiornare la galleria
            }


        initializeNavigationView();

        StringTokenizer st = new StringTokenizer(eventsRegistryGetUri, "/");
        String token1 = st.nextToken();
        String token2 = st.nextToken();
        if (token2.equals("before") || token2.equals("after"))
            eventsRegistryGetUri = token1 + "/" + token2 + "/" + Event.DATETIME_SDF.format(new Date());

        new EventsRegistryGetTask().execute(eventsRegistryGetUri);
    }

    private void refreshEventsCallback(Event[] eventsArray) {
        if (eventsArray != null) {
            events = new ArrayList<Event>(Arrays.asList(eventsArray));

            final ListView eventsListView = (ListView) findViewById(R.id.ae_eventsListView);

            if (events.size() != 0) {
                EventsAdapter eventsListViewAdapter = new EventsAdapter(this, events);
                eventsListView.setAdapter(eventsListViewAdapter);
                if (userLogged != null) {
                    eventsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                        @Override
                        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                            openEventActivity(position);
                        }
                    });

                    eventsListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
                        @Override
                        public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                            eventsListViewSelectedItemPosition = position;
                            return false;
                        }
                    });

                    registerForContextMenu(eventsListView);
                }
            } else {
                eventsListView.setAdapter(null);
                Toast.makeText(getApplicationContext(), R.string.no_events_to_view, Toast.LENGTH_LONG).show();
            }
        } else
            Toast.makeText(getApplicationContext(), toastMessage, Toast.LENGTH_LONG).show();

        swipeRefreshLayout.setRefreshing(false);
    }

    private void initializeNavigationView() {
        if (userLogged != null) {
            setPhotoOfUserWithEmail(userLogged, (ImageView) navigationView.getHeaderView(0).findViewById(R.id.ae_userPhotoImageView));
            ((ImageView) navigationView.getHeaderView(0).findViewById(R.id.ae_userPhotoImageView)).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    drawer.closeDrawer(GravityCompat.START);
                    openUserActivity(userLogged);
                }
            });

            ((TextView) navigationView.getHeaderView(0).findViewById(R.id.ae_userNameTextView)).setText(userLogged.getName() + " " + userLogged.getSurname());
            ((TextView) navigationView.getHeaderView(0).findViewById(R.id.ae_userNameTextView)).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    drawer.closeDrawer(GravityCompat.START);
                    openUserActivity(userLogged);
                }
            });

            ((TextView) navigationView.getHeaderView(0).findViewById(R.id.ae_userEmailTextView)).setText(userLogged.getEmail());
            navigationView.getMenu().setGroupVisible(R.id.ae_navigation_group3, false);
        } else {
            navigationView.getMenu().setGroupVisible(R.id.ae_navigation_group1, false);
            navigationView.getMenu().setGroupVisible(R.id.ae_navigation_group2, false);
        }
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        int id = item.getItemId();

        switch (item.getItemId()) {
            case R.id.ae_navigation_add_event:
                openAddEventActivity();
                break;
            case R.id.ae_navigation_settings:
                //openSettingsActivity();
                break;
            case R.id.ae_navigation_profile:
                openUserActivity(userLogged);
                break;
            case R.id.ae_navigation_logout:
                startActivity(new Intent(getApplicationContext(), LoginActivity.class));

                SharedPreferences.Editor editor = preferences.edit();
                editor.remove(Constants.USER);
                editor.apply();

                Toast.makeText(getApplicationContext(), getString(R.string.successful_logout), Toast.LENGTH_LONG).show();
                finish();
                break;
            case R.id.ae_navigation_login:
                startActivity(new Intent(getApplicationContext(), LoginActivity.class));
                finish();
                break;
        }

        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_events_menu, menu);

        if (itemCheckedId != 0)
            menu.findItem(itemCheckedId).setChecked(true);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.ae_menu_refresh:
                refreshEvents();
                break;
            case R.id.ae_menu_past_events:
                item.setChecked(true);
                itemCheckedId = item.getItemId();

                eventsRegistryGetUri = "events/before/" + Event.DATETIME_SDF.format(new Date());
                refreshEvents();
                getSupportActionBar().setTitle(R.string.past_events);
                ((TextView) findViewById(R.id.ae_filterTextView)).setVisibility(View.GONE);
                break;
            case R.id.ae_menu_upcoming_events:
                item.setChecked(true);
                itemCheckedId = item.getItemId();

                eventsRegistryGetUri = "events/after/" + Event.DATETIME_SDF.format(new Date());
                refreshEvents();
                getSupportActionBar().setTitle(R.string.upcoming_events);
                ((TextView) findViewById(R.id.ae_filterTextView)).setVisibility(View.GONE);
                break;
            case R.id.ae_menu_between_dates_events:
                openFilterInputDialog(item);
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    private void openFilterInputDialog(final MenuItem item) {
        LayoutInflater inflater = getLayoutInflater();

        View alertDialogLayout = inflater.inflate(R.layout.custom_input_dialog, null);

        final EditText fromDateEditText = (EditText) alertDialogLayout.findViewById(R.id.cid_fromDateEditText);
        final EditText fromTimeEditText = (EditText) alertDialogLayout.findViewById(R.id.cid_fromTimeEditText);
        final EditText toDateEditText = (EditText) alertDialogLayout.findViewById(R.id.cid_toDateEditText);
        final EditText toTimeEditText = (EditText) alertDialogLayout.findViewById(R.id.cid_toTimeEditText);

        if (filterFromDate != null)
            fromDateEditText.setText(filterFromDate);

        if (filterFromTime != null)
            fromTimeEditText.setText(filterFromTime);

        if (filterToDate != null)
            toDateEditText.setText(filterToDate);

        if (filterToTime != null)
            toTimeEditText.setText(filterToTime);


        final AlertDialog dialog = new AlertDialog.Builder(EventsActivity.this)
                .setView(alertDialogLayout)
                .setPositiveButton(R.string.apply, null)
                .setNegativeButton(R.string.cancel, null)
                .create();

        dialog.setOnShowListener(new DialogInterface.OnShowListener() {

            @Override
            public void onShow(DialogInterface dialogInterface) {
                ((AlertDialog) dialog).getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View view) {
                        Date fromDate = null;
                        Date toDate = null;
                        try {
                            Calendar _fromDate = Calendar.getInstance();
                            _fromDate.setTime(Event.DATETIME_SDF.parse(fromDateEditText.getText().toString()));
                            Calendar fromTime = Calendar.getInstance();
                            fromTime.setTime(Event.DATETIME_SDF.parse(fromTimeEditText.getText().toString()));
                            _fromDate.set(Calendar.HOUR_OF_DAY, fromTime.get(Calendar.HOUR_OF_DAY));
                            _fromDate.set(Calendar.MINUTE, fromTime.get(Calendar.MINUTE));
                            fromDate = _fromDate.getTime();

                            Calendar _toDate = Calendar.getInstance();
                            _toDate.setTime(Event.DATETIME_SDF.parse(toDateEditText.getText().toString()));
                            Calendar toTime = Calendar.getInstance();
                            toTime.setTime(Event.DATETIME_SDF.parse(toTimeEditText.getText().toString()));
                            _toDate.set(Calendar.HOUR_OF_DAY, toTime.get(Calendar.HOUR_OF_DAY));
                            _toDate.set(Calendar.MINUTE, toTime.get(Calendar.MINUTE));
                            toDate = _toDate.getTime();
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }

                        if (fromDateEditText.getText().toString().isEmpty())
                            Toast.makeText(getApplicationContext(), R.string.empty_from_date_field_message, Toast.LENGTH_LONG).show();
                        else if (fromTimeEditText.getText().toString().isEmpty())
                            Toast.makeText(getApplicationContext(), R.string.empty_from_time_field_message, Toast.LENGTH_LONG).show();
                        else if (toDateEditText.getText().toString().isEmpty())
                            Toast.makeText(getApplicationContext(), R.string.empty_to_date_field_message, Toast.LENGTH_LONG).show();
                        else if (toTimeEditText.getText().toString().isEmpty())
                            Toast.makeText(getApplicationContext(), R.string.empty_to_time_field_message, Toast.LENGTH_LONG).show();
                        else if (fromDate.after(toDate))
                            Toast.makeText(getApplicationContext(), R.string.invalid_from_date_field_message, Toast.LENGTH_LONG).show();
                        else {
                            filterFromDate = fromDateEditText.getText().toString();
                            filterFromTime = fromTimeEditText.getText().toString();
                            filterToDate = toDateEditText.getText().toString();
                            filterToTime = toTimeEditText.getText().toString();

                            eventsRegistryGetUri = "events/between/" + Event.DATETIME_SDF.format(fromDate) + "/" + Event.DATETIME_SDF.format(toDate);
                            refreshEvents();

                            if (item != null) {
                                item.setChecked(true);
                                itemCheckedId = item.getItemId();
                                getSupportActionBar().setTitle(R.string.between_dates_events);
                                ((TextView) findViewById(R.id.ae_filterTextView)).setVisibility(View.VISIBLE);
                            }
                            ((TextView) findViewById(R.id.ae_filterTextView)).setText(getResources().getString(R.string._from_date) + filterFromDate + getResources().getString(R.string.to) + filterFromTime + getResources().getString(R.string._to_date) + filterToDate + getResources().getString(R.string.to) + filterToTime);

                            dialog.dismiss();
                        }
                    }
                });
            }
        });

        dialog.show();



        fromDateEditText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Date todayDate = new Date();
                int dayOfMonth = Integer.parseInt(new SimpleDateFormat("dd").format(todayDate));
                int month = Integer.parseInt(new SimpleDateFormat("MM").format(todayDate)) - 1;
                int year = Integer.parseInt(new SimpleDateFormat("yyyy").format(todayDate));

                if (fromDateEditText.getText().length() != 0) {
                    StringTokenizer st = new StringTokenizer(fromDateEditText.getText().toString(), ".");
                    dayOfMonth = Integer.parseInt(st.nextToken());
                    month      = Integer.parseInt(st.nextToken()) - 1;
                    year       = Integer.parseInt(st.nextToken());
                }

                DatePickerDialog datePickerDialog = new DatePickerDialog(v.getContext(), new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                        fromDateEditText.setText(String.format("%02d.%02d.%04d", dayOfMonth, month + 1, year));
                    }
                }, year, month, dayOfMonth);

                datePickerDialog.show();
            }
        });

        fromTimeEditText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int hourOfDay = 12, minute = 0;

                if (fromTimeEditText.getText().length() != 0) {
                    hourOfDay = Integer.parseInt(fromTimeEditText.getText().subSequence(0, 2).toString());
                    minute    = Integer.parseInt(fromTimeEditText.getText().subSequence(3, 5).toString());
                }

                TimePickerDialog timePickerDialog = new TimePickerDialog(v.getContext(), new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                        fromTimeEditText.setText(String.format("%02d:%02d", hourOfDay, minute));
                    }
                }, hourOfDay, minute, true);

                timePickerDialog.show();
            }
        });

        toDateEditText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Date todayDate = new Date();
                int dayOfMonth = Integer.parseInt(new SimpleDateFormat("dd").format(todayDate));
                int month = Integer.parseInt(new SimpleDateFormat("MM").format(todayDate)) - 1;
                int year = Integer.parseInt(new SimpleDateFormat("yyyy").format(todayDate));

                if (toDateEditText.getText().length() != 0) {
                    StringTokenizer st = new StringTokenizer(toDateEditText.getText().toString(), ".");
                    dayOfMonth = Integer.parseInt(st.nextToken());
                    month      = Integer.parseInt(st.nextToken()) - 1;
                    year       = Integer.parseInt(st.nextToken());
                }

                DatePickerDialog datePickerDialog = new DatePickerDialog(v.getContext(), new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                        toDateEditText.setText(String.format("%02d.%02d.%04d", dayOfMonth, month + 1, year));
                    }
                }, year, month, dayOfMonth);

                datePickerDialog.show();
            }
        });

        toTimeEditText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int hourOfDay = 12, minute = 0;

                if (toTimeEditText.getText().length() != 0) {
                    hourOfDay = Integer.parseInt(toTimeEditText.getText().subSequence(0, 2).toString());
                    minute    = Integer.parseInt(toTimeEditText.getText().subSequence(3, 5).toString());
                }

                TimePickerDialog timePickerDialog = new TimePickerDialog(v.getContext(), new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                        toTimeEditText.setText(String.format("%02d:%02d", hourOfDay, minute));
                    }
                }, hourOfDay, minute, true);

                timePickerDialog.show();
            }
        });
    }

    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        if (userLogged != null && events.get(eventsListViewSelectedItemPosition).getOwner().getEmail().equals(userLogged.getEmail())) {
            super.onCreateContextMenu(menu, v, menuInfo);
            MenuInflater inflater = getMenuInflater();
            inflater.inflate(R.menu.activity_events_context_menu, menu);
        }
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo listItemInfo = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();

        switch (item.getItemId()) {
            case R.id.ae_modify_event:
                Intent intent = new Intent(getApplicationContext(), ModifyEventActivity.class);
                intent.putExtra(Constants.EVENT_EXTRA, gson.toJson(events.get(listItemInfo.position)));
                intent.putExtra(Constants.STORAGE_DIRECTORY_EXTRA, storageDirectory.getAbsolutePath());
                startActivityForResult(intent, MODIFY_EVENT_ACTIVITY_RESULT_NUMBER);
                return true;
            case R.id.ae_remove_event:
                confirmRemoveEventAlertDialog(events.get(listItemInfo.position).getId()+"");
                return true;
        }
        return false;
    }

    public void onBackPressed() {
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            //super.onBackPressed();
            finish();
        }
    }

    private void openAddEventActivity() {
        Intent intent = new Intent(getApplicationContext(), AddEventActivity.class);
        startActivityForResult(intent, ADD_EVENT_ACTIVITY_RESULT_NUMBER);
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
            refreshEvents();
        } else
            Toast.makeText(this, toastMessage, Toast.LENGTH_LONG).show();
    }

    private void openEventActivity(int position) {
        if (userLogged != null) {
            Intent intent = new Intent(getApplicationContext(), EventActivity.class);
            intent.putExtra(Constants.EVENT_EXTRA, gson.toJson(events.get(position), Event.class));
            intent.putExtra(Constants.STORAGE_DIRECTORY_EXTRA, storageDirectory.getAbsolutePath());
            startActivityForResult(intent, EVENT_ACTIVITY_RESULT_NUMBER);
        } else
            Toast.makeText(getApplicationContext(), R.string.authentication_required, Toast.LENGTH_LONG).show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case ADD_EVENT_ACTIVITY_RESULT_NUMBER:
            case EVENT_ACTIVITY_RESULT_NUMBER:
            case MODIFY_EVENT_ACTIVITY_RESULT_NUMBER:
                if (resultCode == Activity.RESULT_OK) {
                    refreshEvents();
                } else if (resultCode == Activity.RESULT_CANCELED) {
                    // Write your code if there's no result
                }
                break;
            case USER_ACTIVITY_RESULT_NUMBER:
                if (resultCode == Activity.RESULT_OK) {
                    userLogged = gson.fromJson(data.getStringExtra(Constants.USER_EXTRA), User.class);

                    refreshEvents();
                } else if (resultCode == Activity.RESULT_CANCELED) {
                    // Write your code if there's no result
                }
                break;
        }
    }

    private void openUserActivity(User user) {
        if (userLogged != null) {
            Intent intent = new Intent(getApplicationContext(), UserActivity.class);
            intent.putExtra(Constants.USER_EXTRA, gson.toJson(user, User.class));
            intent.putExtra(Constants.STORAGE_DIRECTORY_EXTRA, storageDirectory.getAbsolutePath());
            startActivityForResult(intent, USER_ACTIVITY_RESULT_NUMBER);
        } else
            Toast.makeText(getApplicationContext(), R.string.authentication_required, Toast.LENGTH_LONG).show();
    }

    private void setPhotoOfEventWithId(Event event, final ImageView eventPhotoImageView) {
        if (event.getPhotoPath() != null) {
            File eventPhotoFile = new File(storageDirectory + "/" + event.getPhotoPath());

            if (!eventPhotoFile.exists()) {
                //Log.e(Constants.TAG, "RECUPERO " + event.getId());
                new EventPhotoGetTask(eventPhotoFile, event, eventPhotoImageView).execute("events/" + event.getId() + "/photo");
            } else
                eventPhotoImageView.setImageBitmap(BitmapFactory.decodeFile(eventPhotoFile.getAbsolutePath()));
        } else
            eventPhotoImageView.setImageResource(R.drawable.default_event_icon);
    }

    private void setPhotoOfEventWithIdCallback(Bitmap bitmap, Event event, ImageView eventPhotoImageView) {
        if (bitmap != null) {
            eventPhotoImageView.setImageBitmap(bitmap);
            MediaScannerConnection.scanFile(this, new String[] {storageDirectory + "/" + event.getPhotoPath()}, new String[] {"image/jpeg"}, null); // per aggiornare la galleria
        } else {
            eventPhotoImageView.setImageResource(R.drawable.default_event_icon);
            event.setPhotoPath(null);
        }
    }

    private void setPhotoOfUserWithEmail(User user, final ImageView userPhotoImageView) {
        if (user.getPhoto() != null) {
            File userPhotoFile = new File(storageDirectory + "/" + user.getPhoto());

            if (!userPhotoFile.exists()) {
                //Log.e(Constants.TAG, "RECUPERO " + user.getEmail());
                new UserPhotoGetTask(userPhotoFile, user, userPhotoImageView).execute("users/" + user.getEmail() + "/photo");
            } else
                userPhotoImageView.setImageBitmap(BitmapFactory.decodeFile(userPhotoFile.getAbsolutePath()));
        } else
            userPhotoImageView.setImageResource(R.drawable.default_user_icon);
    }

    private void setPhotoOfUserWithEmailCallback(Bitmap bitmap, User user, ImageView userPhotoImageView) {
        if (bitmap != null) {
            userPhotoImageView.setImageBitmap(bitmap);
            MediaScannerConnection.scanFile(this, new String[]{storageDirectory + "/" + user.getPhoto()}, new String[]{"image/jpeg"}, null); // per aggiornare la galleria
        } else {
            userPhotoImageView.setImageResource(R.drawable.default_user_icon);
            user.setPhoto(null);
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        if (events != null) {
            outState.putString(Constants.EVENTS_ARRAY, gson.toJson(events.toArray(new Event[events.size()]), Event[].class));

            outState.putString(Constants.EVENTS_REGISTRY_GET_URI, eventsRegistryGetUri);

            outState.putString(Constants.SUPPORT_ACTION_BAR_TITLE, getSupportActionBar().getTitle().toString());
            outState.putInt(Constants.CHECKED_MENU_ITEM_ID, itemCheckedId);

            outState.putInt(Constants.FILTER_TEXT_VIEW_VISIBILITY, ((TextView) findViewById(R.id.ae_filterTextView)).getVisibility());
            outState.putString(Constants.FILTER_TEXT_VIEW_CONTENT, ((TextView) findViewById(R.id.ae_filterTextView)).getText().toString());

            outState.putString(Constants.FILTER_FROM_DATE, filterFromDate);
            outState.putString(Constants.FILTER_FROM_TIME, filterFromTime);
            outState.putString(Constants.FILTER_TO_DATE, filterToDate);
            outState.putString(Constants.FILTER_TO_TIME, filterToTime);
        }
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        if (savedInstanceState.getString(Constants.EVENTS_ARRAY, null) != null) {
            eventsRegistryGetUri = savedInstanceState.getString(Constants.EVENTS_REGISTRY_GET_URI);

            getSupportActionBar().setTitle(savedInstanceState.getString(Constants.SUPPORT_ACTION_BAR_TITLE));
            itemCheckedId = savedInstanceState.getInt(Constants.CHECKED_MENU_ITEM_ID);

            ((TextView) findViewById(R.id.ae_filterTextView)).setVisibility(savedInstanceState.getInt(Constants.FILTER_TEXT_VIEW_VISIBILITY));
            ((TextView) findViewById(R.id.ae_filterTextView)).setText(savedInstanceState.getString(Constants.FILTER_TEXT_VIEW_CONTENT));

            filterFromDate = savedInstanceState.getString(Constants.FILTER_FROM_DATE);
            filterFromTime = savedInstanceState.getString(Constants.FILTER_FROM_TIME);
            filterToDate = savedInstanceState.getString(Constants.FILTER_TO_DATE);
            filterToTime = savedInstanceState.getString(Constants.FILTER_TO_TIME);

            initializeNavigationView();
            refreshEventsCallback(gson.fromJson(savedInstanceState.getString(Constants.EVENTS_ARRAY), Event[].class));
        }
    }

    public class EventsAdapter extends ArrayAdapter<Event> {
        private Context context;
        private ArrayList<Event> events;
        private HashMap<String, User> eventsUsers; // OPZIONALE: per non fare GET inutili su foto dello stesso utente per utenti che non hanno la foto

        public EventsAdapter(Context context, ArrayList<Event> events) {
            super(context, R.layout.activity_events_list_item, events);
            this.context = context;
            this.events = events;
            eventsUsers = new HashMap<String, User>(); // OPZIONALE
        }

        @Override
        public int getCount() {
            return events.size();
        }

        @Override
        public Event getItem(int position) {
            return events.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            LayoutInflater inflater = ((Activity) context).getLayoutInflater();

            View listViewItem = inflater.inflate(R.layout.activity_events_list_item, parent, false);

            TextView titleTextView  = (TextView) listViewItem.findViewById(R.id.aeli_titleTextView);
            TextView whenTextView  = (TextView) listViewItem.findViewById(R.id.aeli_whenTextView);
            TextView descriptionTextView   = (TextView) listViewItem.findViewById(R.id.aeli_descriptionTextView);
            final ImageView eventPhotoImageView = (ImageView) listViewItem.findViewById(R.id.aeli_eventPhotoImageView);
            ImageView userPhotoImageView = (ImageView) listViewItem.findViewById(R.id.aeli_userPhotoImageView);
            TextView userNameTextView   = (TextView) listViewItem.findViewById(R.id.aeli_userNameTextView);

            final Event event = events.get(position);
            if (!eventsUsers.containsKey(event.getOwner().getEmail()))         // OPZIONALE
                eventsUsers.put(event.getOwner().getEmail(), event.getOwner()); // OPZIONALE

            SpannableString content = new SpannableString(event.getTitle());
            content.setSpan(new UnderlineSpan(), 0, content.length(), 0);
            titleTextView.setText(content);
            titleTextView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    openEventActivity(position);
                }
            });

            whenTextView.setText(getContext().getString(R.string.when_the) + getContext().getString(R.string.from) + Event.DATETIME_SDF.format(event.getStartDate()) + getContext().getString(R.string.to) + Event.DATETIME_SDF.format(event.getEndDate()));
            descriptionTextView.setText(event.getDescription());

            eventPhotoImageView.setPadding(0, 0, 0, 0);
            eventPhotoImageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
            setPhotoOfEventWithId(event, eventPhotoImageView);
            eventPhotoImageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (event.getPhotoPath() != null) {
                        Intent intent = new Intent();
                        intent.setAction(Intent.ACTION_VIEW);
                        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                        intent.setDataAndType(FileProvider.getUriForFile(getContext(), BuildConfig.APPLICATION_ID + ".provider", new File(storageDirectory + "/" + event.getPhotoPath())), "image/*");
                        startActivity(intent);
                    } else {
                        eventPhotoImageView.setFocusable(false);
                        eventPhotoImageView.setClickable(false);
                        eventPhotoImageView.setFocusableInTouchMode(false);
                    }
                }
            });

            userPhotoImageView.setPadding(0, 0, 0, 0);
            userPhotoImageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
            //setPhotoOfUserWithEmail(event.getUser(), userPhotoImageView);                           // alternativa all'OPZIONALE
            setPhotoOfUserWithEmail(eventsUsers.get(event.getOwner().getEmail()), userPhotoImageView); // OPZIONALE
            userPhotoImageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    openUserActivity(eventsUsers.get(event.getOwner().getEmail()));
                }
            });

            content = new SpannableString(event.getOwner().getName() + " " + event.getOwner().getSurname());
            content.setSpan(new UnderlineSpan(), 0, content.length(), 0);
            userNameTextView.setText(content);
            userNameTextView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    openUserActivity(eventsUsers.get(event.getOwner().getEmail()));
                }
            });

            return listViewItem;
        }
    }

    public class EventsRegistryGetTask extends AsyncTask<String, Void, Event[]> {

        @Override
        protected Event[] doInBackground(String... params) {
            ClientResource cr = new ClientResource(Constants.BASE_URI + params[0]);
            String jsonResponse = null;

            Event[] events = null;

            try {
                jsonResponse = cr.get().getText();

                if (cr.getStatus().getCode() == ErrorCodes.INVALID_EVENT_ID)
                    throw gson.fromJson(jsonResponse, InvalidEventIdException.class);

                events = gson.fromJson(jsonResponse, Event[].class);
            } catch (ResourceException | IOException e1) {
                String text = "Error: " + cr.getStatus().getCode() + " - " + cr.getStatus().getDescription() + " - " + cr.getStatus().getReasonPhrase();
                Log.e(Constants.TAG, text);
                toastMessage = text;
            } catch (InvalidEventIdException e2) {
                String text = "Error: " + cr.getStatus().getCode() + " - " + e2.getMessage();
                Log.e(Constants.TAG, text);
                toastMessage = text;
            }

            return events;
        }

        @Override
        protected void onPostExecute(Event[] events) {
            refreshEventsCallback(events);
        }
    }

    public class EventDeleteTask extends AsyncTask<String, Void, Boolean> {

        @Override
        protected Boolean doInBackground(String... params) {
            ClientResource cr = new ClientResource(Constants.BASE_URI + params[0]);
            cr.setChallengeResponse(ChallengeScheme.HTTP_BASIC, params[1], params[2]);
            String jsonResponse = null;

            boolean isEventRemoved = false;

            try {
                jsonResponse = cr.delete().getText();

                if (cr.getStatus().getCode() == ErrorCodes.INVALID_EVENT_ID)
                    throw gson.fromJson(jsonResponse, InvalidEventIdException.class);
                else if (cr.getStatus().getCode() == ErrorCodes.UNAUTHORIZED_USER)
                    throw gson.fromJson(jsonResponse, UnauthorizedUserException.class);

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
            }

            return isEventRemoved;
        }

        @Override
        protected void onPostExecute(Boolean isEventRemoved) {
            removeEventCallback(isEventRemoved);
        }
    }

    public class EventPhotoGetTask extends AsyncTask<String, Void, Bitmap> {
        private File eventPhotoFile;
        private Event event;
        private ImageView eventPhotoImageView;

        public EventPhotoGetTask(File eventPhotoFile, Event event, ImageView eventPhotoImageView) {
            super();
            this.eventPhotoFile = eventPhotoFile;
            this.event = event;
            this.eventPhotoImageView = eventPhotoImageView;
        }

        @Override
        protected Bitmap doInBackground(String... params) {
            ClientResource cr = new ClientResource(Constants.BASE_URI + params[0]);
            Representation eventPhoto;

            Bitmap eventPhotoBitmap = null;

            try {
                eventPhoto = cr.get();

                if (cr.getStatus().getCode() == 204 /* No Content */)
                    eventPhotoBitmap = null;
                else {
                    eventPhoto.write(new FileOutputStream(eventPhotoFile));
                    eventPhotoBitmap = BitmapFactory.decodeFile(eventPhotoFile.getAbsolutePath());
                }
            } catch (ResourceException e1) {
                String text = "Error: " + cr.getStatus().getCode() + " - " + cr.getStatus().getDescription() + " - " + cr.getStatus().getReasonPhrase();
                Log.e(Constants.TAG, text);
                toastMessage = text;
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

            return eventPhotoBitmap;
        }

        @Override
        protected void onPostExecute(Bitmap eventPhotoBitmap) {
            setPhotoOfEventWithIdCallback(eventPhotoBitmap, event, eventPhotoImageView);
        }
    }

    public class UserPhotoGetTask extends AsyncTask<String, Void, Bitmap> {
        private File userPhotoFile;
        private User user;
        private ImageView userPhotoImageView;

        public UserPhotoGetTask(File userPhotoFile, User user, ImageView userPhotoImageView) {
            super();
            this.userPhotoFile = userPhotoFile;
            this.user = user;
            this.userPhotoImageView = userPhotoImageView;
        }

        @Override
        protected Bitmap doInBackground(String... params) {
            ClientResource cr = new ClientResource(Constants.BASE_URI + params[0]);
            Representation userPhoto;

            Bitmap userPhotoBitmap = null;

            try {
                userPhoto = cr.get();

                if (cr.getStatus().getCode() == 204 /* No Content */)
                    userPhotoBitmap = null;
                else {
                    userPhoto.write(new FileOutputStream(userPhotoFile));
                    userPhotoBitmap = BitmapFactory.decodeFile(userPhotoFile.getAbsolutePath());
                }
            } catch (ResourceException e1) {
                String text = "Error: " + cr.getStatus().getCode() + " - " + cr.getStatus().getDescription() + " - " + cr.getStatus().getReasonPhrase();
                Log.e(Constants.TAG, text);
                toastMessage = text;
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

            return userPhotoBitmap;
        }

        @Override
        protected void onPostExecute(Bitmap userPhotoBitmap) {
            setPhotoOfUserWithEmailCallback(userPhotoBitmap, user, userPhotoImageView);
        }
    }
}



