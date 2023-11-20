package com.suresh.rsr.adapters;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;

import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.suresh.rsr.interfaces.OnRecyclerViewItemClickListener;
import com.suresh.rsr.model.Upload;
import com.suresh.rsr.R;

import java.util.List;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.ViewHolder> {

    private Context context;
    private List<Upload> uploads;
    private OnRecyclerViewItemClickListener listener;

    public UserAdapter(Context context, List<Upload> uploads) {
        this.context = context;
        this.uploads = uploads;

    }


    public void setOnItemClickListener(OnRecyclerViewItemClickListener listener)
    {
        this.listener = listener;
    }


    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.layout_user_images, parent, false);
        ViewHolder viewHolder = new ViewHolder(v);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {
        final Upload upload = uploads.get(position);
        holder.txtJobType.setText(upload.getJobType());
        holder.textViewName.setText(upload.getDescription());
        holder.txtDate.setText("Posted on : "+upload.getCurrentDate());

        Glide.with(context)
                .load(upload.getUrl())
                .apply(new RequestOptions().diskCacheStrategy(DiskCacheStrategy.NONE))
                .into(new CustomTarget<Drawable>() {
                    @Override
                    public void onResourceReady(@NonNull Drawable resource, @Nullable Transition<? super Drawable> transition) {
                        holder.progressBar.setVisibility(View.GONE);
                        holder.imageView.setImageDrawable(resource);

                    }

                    @Override
                    public void onLoadCleared(@Nullable Drawable placeholder) {

                        holder.progressBar.setVisibility(View.GONE);
                    }

                });

        holder.parentLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                listener.onRecyclerViewItemClicked(position, view.getId());
            }
        });


    }

    @Override
    public int getItemCount() {
        return uploads.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        public TextView txtJobType,textViewName,txtDate;
        public ImageView imageView,imgDelete;
        public LinearLayout parentLayout;
        ProgressBar progressBar;

        public ViewHolder(View itemView) {
            super(itemView);
            txtJobType = (TextView) itemView.findViewById(R.id.txtJobType);
            textViewName = (TextView) itemView.findViewById(R.id.textViewName);
            txtDate = (TextView) itemView.findViewById(R.id.currentDate);
            imageView = (ImageView) itemView.findViewById(R.id.imageView);
            imgDelete = (ImageView) itemView.findViewById(R.id.imgDelete);
            parentLayout = (LinearLayout)itemView.findViewById(R.id.parentLayout);
            progressBar = (ProgressBar)itemView.findViewById(R.id.progressBar);

        }
    }





}

