package com.suresh.rsr.adapters;



import android.content.Context;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.suresh.rsr.Constants;
import com.suresh.rsr.model.Upload;
import com.suresh.rsr.R;

import java.util.List;

import static androidx.constraintlayout.motion.utils.Oscillator.TAG;

/**
 * Created by Belal on 2/23/2017.
 */

public class AdminAdapter extends RecyclerView.Adapter<AdminAdapter.ViewHolder> {

    private Context context;
    private List<Upload> uploads;

    public AdminAdapter(Context context, List<Upload> uploads) {
        this.uploads = uploads;
        this.context = context;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.layout_admin_images, parent, false);
        ViewHolder viewHolder = new ViewHolder(v);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {
        final Upload upload = uploads.get(position);
        holder.txtJobType.setText(upload.getJobType());
        holder.textViewName.setText(upload.getDescription());
        holder.txtDate.setText("Posted on : "+upload.getCurrentDate());

        if (upload.count != null) {
            holder.txtViewsCount.setText("" + upload.getCount());
        }else {
            holder.txtViewsCount.setText("0");
        }

        Glide.with(context)
                .load(upload.getUrl())
                .apply(new RequestOptions().diskCacheStrategy(DiskCacheStrategy.NONE))
                .into(new CustomTarget<Drawable>() {
                    @Override
                    public void onResourceReady(@NonNull Drawable resource, @Nullable Transition<? super Drawable> transition) {
                        holder.progressBar.setVisibility(View.GONE);
                        holder.imageView.setImageDrawable(resource);
                        Log.d(TAG, "onResourceReady: "+resource);

                    }

                    @Override
                    public void onLoadCleared(@Nullable Drawable placeholder) {

                        holder.progressBar.setVisibility(View.GONE);
                    }

                });


        holder.imgDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                deleteArtist(upload.getId());
                deleteImageFromStorage(upload.getKey(),upload.getUrl());
                uploads.remove(position);
                notifyDataSetChanged();

            }
        });
    }

    @Override
    public int getItemCount() {
        return uploads.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        public TextView txtJobType,textViewName,txtDate,txtViewsCount;
        public ImageView imageView,imgDelete;
        ProgressBar progressBar;

        public ViewHolder(View itemView) {
            super(itemView);
            txtJobType = (TextView) itemView.findViewById(R.id.txtJobType);
            textViewName = (TextView) itemView.findViewById(R.id.textViewName);
            txtDate = (TextView) itemView.findViewById(R.id.currentDate);
            txtViewsCount = (TextView) itemView.findViewById(R.id.txtViewsCount);
            imageView = (ImageView) itemView.findViewById(R.id.imageView);
            imgDelete = (ImageView) itemView.findViewById(R.id.imgDelete);
            progressBar = (ProgressBar)itemView.findViewById(R.id.progressBar);

        }
    }

    private boolean deleteArtist(String id) {
        //getting the specified artist reference
        DatabaseReference dR = FirebaseDatabase.getInstance().getReference(Constants.DATABASE_PATH_UPLOADS).child(id);

        //removing artist
        dR.removeValue();

        /*//getting the tracks reference for the specified artist
        DatabaseReference drTracks = FirebaseDatabase.getInstance().getReference("tracks").child(id);

        //removing all tracks
        drTracks.removeValue();*/

        Toast.makeText(context, "Post Deleted", Toast.LENGTH_LONG).show();

        return true;
    }

    private void deleteImageFromStorage(final String key, String imageUrl){

        FirebaseStorage mStorage = FirebaseStorage.getInstance();
        final DatabaseReference mDatabaseRef = FirebaseDatabase.getInstance().getReference(Constants.DATABASE_PATH_UPLOADS);

        StorageReference imageRef = mStorage.getReferenceFromUrl(imageUrl);
        imageRef.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
               // mDatabaseRef.child(key).removeValue();
                Toast.makeText(context, "Post deleted", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
