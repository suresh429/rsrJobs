package com.suresh.rsr;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Process;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;


import com.google.firebase.FirebaseApp;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.suresh.rsr.adapters.UserAdapter;
import com.suresh.rsr.admin.LoginActivity;
import com.suresh.rsr.interfaces.OnRecyclerViewItemClickListener;
import com.suresh.rsr.model.Upload;
import com.suresh.rsr.model.UserLeads;
import com.suresh.rsr.reciever.ConnectivityReceiver;
import com.suresh.rsr.singleton.AppController;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import butterknife.BindView;
import butterknife.ButterKnife;


public class HomeActivity extends AppCompatActivity implements ConnectivityReceiver.ConnectivityReceiverListener {

    boolean doubleBackToExitPressedOnce = false;
    @BindView(R.id.mainRecycler)
    RecyclerView mainRecycler;
    @BindView(R.id.txtError)
    TextView txtError;
    @BindView(R.id.simpleSwipeRefreshLayout)
    SwipeRefreshLayout simpleSwipeRefreshLayout;
    @BindView(R.id.callFab)
    FloatingActionButton callFab;


    //adapter object
    private UserAdapter adapter;

    //database reference
    private DatabaseReference mDatabase;


    //list to hold all the uploaded images
    private List<Upload> uploads = new ArrayList<>();


    EditText editTextName, editTextEmail, editTextPhone;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        ButterKnife.bind(this);
        Objects.requireNonNull(getSupportActionBar()).setTitle(getResources().getString(R.string.app_name));

        if (!FirebaseApp.getApps(this).isEmpty())
            FirebaseDatabase.getInstance().setPersistenceEnabled(true);

        // leftToRight.setAnimation(AnimationUtils.loadAnimation(HomeActivity.this, R.anim.flash_leave_now));

        callFab.setOnClickListener(v -> {
            Intent callIntent = new Intent(Intent.ACTION_DIAL);
            callIntent.setData(Uri.parse("tel:7702959468"));
            startActivity(callIntent);
        });


    }


    /**
     * Callback will be triggered when there is change in
     * network connection
     */
    @Override
    public void onNetworkConnectionChanged(boolean isConnected) {
        showSnack(isConnected);
    }

    // Showing the status in Snackbar
    private void showSnack(boolean isConnected) {

        simpleSwipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.simpleSwipeRefreshLayout);

        if (!isConnected) {
            Snackbar.make(mainRecycler, Html.fromHtml("<font color=\"" + Color.RED + "\">" + getResources().getString(R.string.Sorry_Notconnected_to_internet) + "</font>"), Snackbar.LENGTH_SHORT).show();
            txtError.setVisibility(View.VISIBLE);
            txtError.setText(R.string.Sorry_Notconnected_to_internet);
            mainRecycler.setAdapter(null);

        } else {

            txtError.setVisibility(View.GONE);

            simpleSwipeRefreshLayout.setColorSchemeResources(R.color.colorAccent, R.color.colorPrimary);
            simpleSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
                @Override
                public void onRefresh() {

                    prepareHomeData();
                    simpleSwipeRefreshLayout.setRefreshing(false);
                }
            });

            prepareHomeData();
        }

    }


    private void prepareHomeData() {

        simpleSwipeRefreshLayout.setRefreshing(true);

        mDatabase = FirebaseDatabase.getInstance().getReference(Constants.DATABASE_PATH_UPLOADS);

        //adding an event listener to fetch values
        mDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                //dismissing the progress dialog
                simpleSwipeRefreshLayout.setRefreshing(false);

                Log.e("SNAPSHOT", "" + snapshot.getValue());

                if (snapshot.getValue() != null) {
                    txtError.setVisibility(View.GONE);
                    //iterating through all the values in database
                    uploads.clear();
                    for (DataSnapshot postSnapshot : snapshot.getChildren()) {
                        Upload upload = postSnapshot.getValue(Upload.class);
                        uploads.add(upload);

                    }
                    //creating adapter
                    adapter = new UserAdapter(getApplicationContext(), uploads);
                    mainRecycler.setHasFixedSize(true);
                    LinearLayoutManager layoutManager = new LinearLayoutManager(HomeActivity.this, LinearLayoutManager.VERTICAL, true);

                    // mainRecycler.setLayoutManager(new LinearLayoutManager(HomeActivity.this, LinearLayoutManager.VERTICAL, true));
                    //adding adapter to recyclerview
                    layoutManager.setReverseLayout(true);
                    layoutManager.setStackFromEnd(true);
                    mainRecycler.setLayoutManager(layoutManager);
                    mainRecycler.setAdapter(adapter);

                    adapter.setOnItemClickListener((position, id) -> {
                        Upload upload = uploads.get(position);

                        int count = 0;
                        if (upload.count != null) {
                            count = Integer.parseInt(upload.count) + 1;
                        } else {
                            count = count + 1;
                        }
                        Log.d("TAG", "onDataChange: " + count);
                        Upload updateUpload = new Upload(upload.id, upload.currentDate, upload.jobType, upload.description, Objects.requireNonNull(upload.url).toString(), String.valueOf(count));
                        mDatabase.child(upload.getId()).setValue(updateUpload);

                        showBottomSheetDialog(upload);
                        Log.d("TAG", "onRecyclerViewItemClicked: " + position);

                        //  showLeadsDialog(upload.getCurrentDate(), upload.getJobType(), upload.description, upload.url);

                    });
                    adapter.notifyDataSetChanged();


                } else {

                    txtError.setVisibility(View.VISIBLE);
                    txtError.setText("No Posts Added Today");
                    mainRecycler.setAdapter(null);
                }


            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                simpleSwipeRefreshLayout.setRefreshing(false);
            }
        });
    }

    @Override
    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        // BottomSheetBehavior.from(myBottomSheet).setState(BottomSheetBehavior.STATE_HIDDEN);

    }

    private void showBottomSheetDialog(Upload upload) {

        final BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(HomeActivity.this);
        bottomSheetDialog.setContentView(R.layout.bottom_sheet_dialog);

        TextView txtJobType = bottomSheetDialog.findViewById(R.id.txtJobType);
        TextView textViewName = bottomSheetDialog.findViewById(R.id.textViewName);
        TextView txtDate = bottomSheetDialog.findViewById(R.id.currentDate);
        ImageView imageView = bottomSheetDialog.findViewById(R.id.imageView);
        ProgressBar progressBar = bottomSheetDialog.findViewById(R.id.progressBar);

        txtJobType.setText(upload.getJobType());
        textViewName.setText(upload.getDescription());
        txtDate.setText("Posted on : " + upload.getCurrentDate());

        Glide.with(HomeActivity.this)
                .load(upload.getUrl())
                .apply(new RequestOptions().diskCacheStrategy(DiskCacheStrategy.NONE))
                .into(new CustomTarget<Drawable>() {
                    @Override
                    public void onResourceReady(@NonNull Drawable resource, @Nullable Transition<? super Drawable> transition) {
                        progressBar.setVisibility(View.GONE);
                        imageView.setImageDrawable(resource);
                    }

                    @Override
                    public void onLoadCleared(@Nullable Drawable placeholder) {
                        progressBar.setVisibility(View.GONE);
                    }

                });

        bottomSheetDialog.show();
    }

    private void showLeadsDialog(final String date, final String jobType, final String description, final String image) {

        //  userLeads = new ArrayList<>();
        //getting the reference of artists node
        final DatabaseReference database = FirebaseDatabase.getInstance().getReference(Constants.DATABASE_PATH_LEADS);

        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        final View dialogView = inflater.inflate(R.layout.update_dialog, null);
        dialogBuilder.setView(dialogView);

        editTextName = (EditText) dialogView.findViewById(R.id.name);
        editTextEmail = (EditText) dialogView.findViewById(R.id.email);
        editTextPhone = (EditText) dialogView.findViewById(R.id.mobile);
        final Button buttonSubmit = (Button) dialogView.findViewById(R.id.btn_submit);
        final ProgressBar progressBar = (ProgressBar) dialogView.findViewById(R.id.progressBar);
        final TextView txtError = (TextView) dialogView.findViewById(R.id.txtError);

        dialogBuilder.setTitle("Submit Details");
        final AlertDialog b = dialogBuilder.create();
        b.show();


        buttonSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String name = editTextName.getText().toString().trim();
                String email = editTextEmail.getText().toString().trim();
                String phone = editTextPhone.getText().toString().trim();

                if ((!isValidName(name))) {
                    return;
                }
                if (!isValidEmail(email)) {
                    return;
                }

                if (!isValidPhoneNumber(phone)) {
                    return;
                }

                progressBar.setVisibility(View.VISIBLE);

                String leadsId = database.push().getKey();
                //creating an Artist Object
                UserLeads userLeads = new UserLeads(leadsId, date, jobType, description, image, name, email, phone, "");
                //Saving the Artist
                database.child(leadsId).setValue(userLeads);

                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {

                        progressBar.setVisibility(View.GONE);
                        b.hide();

                    }
                }, 2000);


            }
        });


    }


    // validate name
    private boolean isValidName(String name) {
        Pattern pattern = Pattern.compile("[a-zA-Z ]+");
        Matcher matcher = pattern.matcher(name);

        if (name.isEmpty()) {
            editTextName.setError("Enter name");
            editTextName.setFocusable(true);
            return false;
        } else if (!matcher.matches()) {
            editTextName.setError("Enter Alphabets Only");
            editTextName.setFocusable(true);
            return false;
        } else if (name.length() < 4 || name.length() > 20) {
            editTextName.setError("Name Should be 5 to 20 characters");
            editTextName.setFocusable(true);
            return false;
        } else {
            editTextName.setFocusable(false);
        }
        return matcher.matches();
    }


    // validate phone
    private boolean isValidPhoneNumber(String mobile) {
        Pattern pattern = Pattern.compile("^[9876]\\d{9}$");
        Matcher matcher = pattern.matcher(mobile);

        if (mobile.isEmpty()) {
            editTextPhone.setError("Phone no is required");
            editTextPhone.setFocusable(true);
            return false;
        } else if (!matcher.matches()) {
            editTextPhone.setError("Enter a valid mobile");
            editTextPhone.setFocusable(true);
            ;
            return false;
        } else {
            editTextPhone.setFocusable(false);
        }

        return matcher.matches();
    }


    // validate your email address
    private boolean isValidEmail(String email) {
        final String EMAIL_PATTERN = "^[_A-Za-z0-9-]+(\\.[_A-Za-z0-9-]+)*@[A-Za-z0-9]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$";
        Pattern pattern = Pattern.compile(EMAIL_PATTERN);
        Matcher matcher = pattern.matcher(email);

        if (email.isEmpty()) {
            editTextEmail.setError("Email is required");
            editTextEmail.setFocusable(true);
            return false;
        } else if (!matcher.matches()) {
            editTextEmail.setError("Enter a valid email");
            editTextEmail.setFocusable(true);
            return false;
        } else {
            editTextEmail.setFocusable(false);
        }


        return matcher.matches();
    }


    @Override
    protected void onResume() {
        super.onResume();
        // receiver function
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("android.net.conn.CONNECTIVITY_CHANGE");

        ConnectivityReceiver connectivityReceiver = new ConnectivityReceiver();
        registerReceiver(connectivityReceiver, intentFilter);

        AppController.getInstance().setConnectivityListener(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds countries to the action bar if it is present.
        getMenuInflater().inflate(R.menu.home_menu, menu);
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_admin) {

            Intent intent = new Intent(HomeActivity.this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);

            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    @Override
    public void onBackPressed() {
        if (doubleBackToExitPressedOnce) {
            moveTaskToBack(true);
            Process.killProcess(Process.myPid());
            System.exit(1);
            return;
        }

        this.doubleBackToExitPressedOnce = true;
        Toast.makeText(this, "Please click BACK again to exit", Toast.LENGTH_SHORT).show();

        new Handler().postDelayed(new Runnable() {

            @Override
            public void run() {
                doubleBackToExitPressedOnce = false;
            }
        }, 2000);
    }


}
