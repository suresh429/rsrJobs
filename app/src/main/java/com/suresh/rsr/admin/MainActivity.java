package com.suresh.rsr.admin;

import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.Bundle;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.text.Html;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;


import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.suresh.rsr.adapters.AdminAdapter;
import com.suresh.rsr.Constants;
import com.suresh.rsr.model.Upload;
import com.suresh.rsr.R;
import com.suresh.rsr.reciever.ConnectivityReceiver;
import com.suresh.rsr.singleton.AppController;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class MainActivity extends AppCompatActivity implements ConnectivityReceiver.ConnectivityReceiverListener {

    @BindView(R.id.mainRecycler)
    RecyclerView mainRecycler;
    @BindView(R.id.addPost)
    FloatingActionButton addPost;
    @BindView(R.id.txtError)
    TextView txtError;
    @BindView(R.id.simpleSwipeRefreshLayout)
    SwipeRefreshLayout simpleSwipeRefreshLayout;

    //adapter object
    private AdminAdapter adapter;

    //database reference
    private DatabaseReference mDatabase;

    private FirebaseStorage mStorage;

    //list to hold all the uploaded images
    private List<Upload> uploads;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("ADMIN");


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

        uploads = new ArrayList<>();

        mStorage = FirebaseStorage.getInstance();
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
                        upload.setKey(postSnapshot.getKey());
                        uploads.add(upload);

                    }
                    //creating adapter
                    adapter = new AdminAdapter(getApplicationContext(), uploads);
                    mainRecycler.setHasFixedSize(true);
                    LinearLayoutManager layoutManager = new LinearLayoutManager(MainActivity.this, LinearLayoutManager.VERTICAL, true);

                    // mainRecycler.setLayoutManager(new LinearLayoutManager(HomeActivity.this, LinearLayoutManager.VERTICAL, true));
                    //adding adapter to recyclerview
                    layoutManager.setReverseLayout(true);
                    layoutManager.setStackFromEnd(true);
                    mainRecycler.setLayoutManager(layoutManager);
                    mainRecycler.setAdapter(adapter);


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
        getMenuInflater().inflate(R.menu.main_menu, menu);
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

            Intent intent = new Intent(MainActivity.this, UserLeadsActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);

            return true;
        } else if (id == R.id.action_logout) {

            FirebaseAuth.getInstance().signOut();
            Intent intent = new Intent(MainActivity.this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            return true;
        }else if (id == android.R.id.home) {
            onBackPressed();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    @OnClick(R.id.addPost)
    public void onViewClicked() {
        Intent intent = new Intent(getApplicationContext(), UploadActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }


   /* @Override
    protected void onDestroy() {
        super.onDestroy();
        mDatabase.removeEventListener(mDBListener);
    }*/
}
