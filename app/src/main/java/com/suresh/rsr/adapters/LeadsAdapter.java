package com.suresh.rsr.adapters;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.suresh.rsr.interfaces.OnRecyclerViewItemClickListener;
import com.suresh.rsr.model.UserLeads;
import com.suresh.rsr.R;

import java.util.List;

public class LeadsAdapter extends RecyclerView.Adapter<LeadsAdapter.ViewHolder> {

    private Context context;
    private List<UserLeads> uploads;
    private OnRecyclerViewItemClickListener listener;

    public LeadsAdapter(Context context, List<UserLeads> uploads) {
        this.uploads = uploads;
        this.context = context;
    }

    public void setOnItemClickListener(OnRecyclerViewItemClickListener listener)
    {
        this.listener = listener;
    }


    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.leads_item, parent, false);
        ViewHolder viewHolder = new ViewHolder(v);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, final int position) {
        final UserLeads userLeads = uploads.get(position);

        holder.txtJobType.setText(userLeads.getJobType());
        holder.textViewName.setText(userLeads.getName());
        holder.txtEmail.setText(userLeads.getEmail());
        holder.txtPhone.setText(userLeads.getPhone());

        if (userLeads.getReviewMsg().equalsIgnoreCase("") || userLeads.getReviewMsg()==null){
            holder.txtreviewMsg.setVisibility(View.GONE);
        }else {
            holder.txtreviewMsg.setVisibility(View.VISIBLE);
        }
        holder.txtreviewMsg.setText("Review : "+userLeads.getReviewMsg());

        holder.parentLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                listener.onRecyclerViewItemClicked(position, view.getId());
            }
        });

        holder.callLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent = new Intent(Intent.ACTION_DIAL);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.setData(Uri.parse("tel:"+userLeads.getPhone()));
                context.startActivity(intent);

            }
        });

        holder.msgLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                final Intent intent = new Intent(Intent.ACTION_SEND);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.setType("plain/text");
                intent.putExtra(Intent.EXTRA_EMAIL, new String[]{userLeads.getEmail()});
                intent.putExtra(Intent.EXTRA_SUBJECT,"RSR Lead");
                intent.putExtra(Intent.EXTRA_TEXT, "");

                try {
                    context.startActivity(intent);
                } catch (android.content.ActivityNotFoundException ex) {
                    Toast.makeText(context, "There are no email clients installed.", Toast.LENGTH_SHORT).show();
                }
            }
        });

    }

    @Override
    public int getItemCount() {
        return uploads.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        public TextView txtJobType,textViewName,txtEmail,txtPhone,txtreviewMsg;
        public LinearLayout parentLayout,callLayout,msgLayout;

        public ViewHolder(View itemView) {
            super(itemView);
            txtJobType = (TextView) itemView.findViewById(R.id.txtJobType);
            textViewName = (TextView) itemView.findViewById(R.id.txtName);
            txtEmail = (TextView) itemView.findViewById(R.id.txtEmail);
            txtPhone = (TextView) itemView.findViewById(R.id.txtMobile);
            txtreviewMsg = (TextView) itemView.findViewById(R.id.txtReviewMsg);
            parentLayout = (LinearLayout) itemView.findViewById(R.id.parentLayout);
            callLayout = (LinearLayout) itemView.findViewById(R.id.call_Layout);
            msgLayout = (LinearLayout) itemView.findViewById(R.id.msg_Layout);


        }
    }


}

