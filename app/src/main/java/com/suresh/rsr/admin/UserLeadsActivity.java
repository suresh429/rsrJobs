package com.suresh.rsr.admin;

import android.app.AlertDialog;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.Bundle;

import com.google.android.material.snackbar.Snackbar;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.text.Html;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;


import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.suresh.rsr.adapters.LeadsAdapter;
import com.suresh.rsr.Constants;
import com.suresh.rsr.interfaces.OnRecyclerViewItemClickListener;
import com.suresh.rsr.model.UserLeads;
import com.suresh.rsr.R;
import com.suresh.rsr.reciever.ConnectivityReceiver;
import com.suresh.rsr.singleton.AppController;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class UserLeadsActivity extends AppCompatActivity implements ConnectivityReceiver.ConnectivityReceiverListener {
    @BindView(R.id.leadsRecycler)
    RecyclerView leadsRecycler;
    @BindView(R.id.txtError)
    TextView txtError;
    @BindView(R.id.simpleSwipeRefreshLayout)
    SwipeRefreshLayout simpleSwipeRefreshLayout;

    //adapter object
    private LeadsAdapter adapter;

    //database reference
    private DatabaseReference mDatabase;

    //list to hold all the uploaded images
    private List<UserLeads> userLeads ;

    EditText etReview;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_leads);
        ButterKnife.bind(this);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("User Leads");



    }



    private void homeData() {

        simpleSwipeRefreshLayout.setRefreshing(true);
        userLeads = new ArrayList<>();

        mDatabase = FirebaseDatabase.getInstance().getReference(Constants.DATABASE_PATH_LEADS);

        //adding an event listener to fetch values
        mDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                //dismissing the progress dialog
                simpleSwipeRefreshLayout.setRefreshing(false);

                if (snapshot.getValue() != null) {
                    txtError.setVisibility(View.GONE);
                    //iterating through all the values in database
                    userLeads.clear();
                    for (DataSnapshot postSnapshot : snapshot.getChildren()) {
                        UserLeads upload = postSnapshot.getValue(UserLeads.class);
                        userLeads.add(upload);

                    }
                    //creating adapter
                    adapter = new LeadsAdapter(getApplicationContext(), userLeads);
                    leadsRecycler.setHasFixedSize(true);
                    leadsRecycler.setLayoutManager(new LinearLayoutManager(UserLeadsActivity.this, LinearLayoutManager.VERTICAL, false));

                    //adding adapter to recyclerview
                    leadsRecycler.setAdapter(adapter);
                    adapter.notifyDataSetChanged();
                    adapter.setOnItemClickListener(new OnRecyclerViewItemClickListener() {
                        @Override
                        public void onRecyclerViewItemClicked(int position, int id) {

                            UserLeads upload = userLeads.get(position);
                            showLeadsDialog( upload.getId(),upload.getDate(),upload.getJobType(),upload.getDescription(),upload.getImage(),upload.getName(),upload.getEmail(),upload.getPhone(),upload.getReviewMsg());

                        }
                    });
                } else {
                    txtError.setVisibility(View.VISIBLE);
                    txtError.setText("No Leads Found");
                    leadsRecycler.setAdapter(null);
                }


            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                simpleSwipeRefreshLayout.setRefreshing(false);
            }
        });

    }

    private void showLeadsDialog(final String id, final String date,final String jobType, final String description, final String image, final String name, final String email, final String phone,final  String reviewMsg) {

        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        final View dialogView = inflater.inflate(R.layout.update_msg_dialog, null);
        dialogBuilder.setView(dialogView);

        etReview = (EditText) dialogView.findViewById(R.id.etReview);
        etReview.setText(reviewMsg);
        final Button buttonReview = (Button) dialogView.findViewById(R.id.btn_review);
        final Button buttonDelete = (Button) dialogView.findViewById(R.id.btn_delete);
        final ProgressBar progressBar =(ProgressBar)dialogView.findViewById(R.id.progressBar);
        final TextView txtError =(TextView)dialogView.findViewById(R.id.txtError);

        dialogBuilder.setTitle("Review / Delete ");
       // dialogBuilder.setTitle(artistName);
        final AlertDialog b = dialogBuilder.create();
        b.show();


        buttonReview.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String review = etReview.getText().toString().trim();

                if (!TextUtils.isEmpty(review)) {
                    updateArtist(id, date,jobType, description,image,name,email,phone,review);
                    b.dismiss();
                }

                /*progressBar.setVisibility(View.VISIBLE);

                String leadsId = mDatabase.push().getKey();
                //creating an Artist Object
                UserLeads userLeads = new UserLeads(leadsId, date, description, image, name, email, phone);
                //Saving the Artist
                mDatabase.child(leadsId).setValue(userLeads);
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {

                        progressBar.setVisibility(View.GONE);
                        b.hide();

                    }
                }, 2000);*/



            }
        });

        buttonDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                /*
                 * we will code this method to delete the artist
                 * */

                deleteArtist(id);
                b.dismiss();

            }
        });


    }

    private void updateArtist(final String id, final String date,final String jobType, final String description, final String image, final String name, final String email, final String phone,final String review) {
        //getting the specified artist reference
        DatabaseReference dR = FirebaseDatabase.getInstance().getReference("userLeads").child(id);

        //updating artist
        UserLeads userLeads = new UserLeads(id, date,jobType, description,image,name,email,phone,review);
        dR.setValue(userLeads);
        Toast.makeText(getApplicationContext(), "Lead Updated", Toast.LENGTH_LONG).show();

    }


    private void deleteArtist(String id) {
        //getting the specified artist reference
        DatabaseReference dR = FirebaseDatabase.getInstance().getReference("userLeads").child(id);

        //removing artist
        dR.removeValue();

        Toast.makeText(getApplicationContext(), "Lead Deleted", Toast.LENGTH_LONG).show();


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
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if (id == android.R.id.home) {
            onBackPressed();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    // Showing the status in Snackbar
    private void showSnack(boolean isConnected) {


        if (!isConnected) {
            Snackbar.make(leadsRecycler, Html.fromHtml("<font color=\"" + Color.RED + "\">" + getResources().getString(R.string.Sorry_Notconnected_to_internet) + "</font>"), Snackbar.LENGTH_SHORT).show();
            txtError.setVisibility(View.VISIBLE);
            txtError.setText(R.string.Sorry_Notconnected_to_internet);
            leadsRecycler.setAdapter(null);

        } else {

            txtError.setVisibility(View.GONE);
            simpleSwipeRefreshLayout.setColorSchemeResources(R.color.colorAccent, R.color.colorPrimary);
            simpleSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
                @Override
                public void onRefresh() {

                    homeData();
                    simpleSwipeRefreshLayout.setRefreshing(false);


                }
            });
            homeData();
        }

    }


    /**
     * Callback will be triggered when there is change in
     * network connection
     */
    @Override
    public void onNetworkConnectionChanged(boolean isConnected) {
        showSnack(isConnected);
    }

}
