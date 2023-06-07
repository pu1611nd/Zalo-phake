package com.pupu.pu1611.Adapter;

import android.content.Context;
import android.graphics.Color;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;
import com.pupu.pu1611.R;
import com.pupu.pu1611.databinding.LayoutNotificationSampleBinding;
import com.pupu.pu1611.models.Notification;
import com.pupu.pu1611.models.User;
import com.pupu.pu1611.utilities.Constants;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.viewHolder>  {
    private ArrayList<Notification> list;
    private Context context;
    private FirebaseFirestore database;

    public NotificationAdapter(ArrayList<Notification> list, Context context) {
        this.list = list;
        this.context = context;
        this.database = FirebaseFirestore.getInstance();
    }

    @NonNull
    @Override
    public NotificationAdapter.viewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.layout_notification_sample,parent,false);

        return new NotificationAdapter.viewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull NotificationAdapter.viewHolder holder, int position) {
        Notification notification = list.get(position);
        String type = notification.getType();
        database.collection(Constants.KEY_COLLECTION_USERS)
                .document(notification.getNotificationBy())
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    // danh sách từ tài liệu Firestore
                    User user = new User();
                    user.setFirstName(documentSnapshot.getString(Constants.KEY_FIRST_NAME));
                    user.setLastName(documentSnapshot.getString(Constants.KEY_LAST_NAME));
                    user.setEmail(documentSnapshot.getString(Constants.KEY_EMAIL));
                    user.setImage(documentSnapshot.getString(Constants.KEY_IMAGE));
                    Picasso.get().load(user.getImage()).placeholder(R.drawable.anh_test).into(holder.binding.profileImage);
                    if(type.equals("like")){
                        holder.binding.notificationInfo.setText(Html.fromHtml("<b>"+user.getFirstName()+" "+user.getLastName()+"</b>"+" liked your post"));
                    }else if(type.equals("comment")){
                        holder.binding.notificationInfo.setText(Html.fromHtml("<b>"+user.getFirstName()+" "+user.getLastName()+" commented on your post"));
                    }else {
                        holder.binding.notificationInfo.setText(Html.fromHtml("<b>"+user.getFirstName()+" "+user.getLastName()+" start following you"));
                    }

                })
                .addOnFailureListener(e -> {
                    // Xử lý khi lấy dữ liệu thất bại
                });

        holder.binding.opentNotification.setOnClickListener(v->{
            if(!type.equals("follow")){
               holder.binding.opentNotification.setBackgroundColor(Color.parseColor("#FFFFFF"));

            }
        });


    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public class viewHolder extends RecyclerView.ViewHolder{
        private LayoutNotificationSampleBinding binding;
        public viewHolder(@NonNull View itemView) {
            super(itemView);
            binding = LayoutNotificationSampleBinding.bind(itemView);
        }
    }
}
