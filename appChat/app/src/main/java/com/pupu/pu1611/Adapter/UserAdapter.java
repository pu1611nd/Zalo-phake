package com.pupu.pu1611.Adapter;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;


import com.makeramen.roundedimageview.RoundedImageView;
import com.pupu.pu1611.R;
import com.pupu.pu1611.listeners.UserListener;
import com.pupu.pu1611.models.User;
import com.pupu.pu1611.utilities.Constants;
import com.squareup.picasso.Picasso;

import java.util.List;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.ViewHolder>{
    private List<User> users;
    private UserListener userListener;


    public UserAdapter(List<User> users, UserListener userListener) {
        this.users = users;
        this.userListener = userListener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(
                R.layout.item_container_user,parent,false
        ));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.setUserData(users.get(position));
    }

    @Override
    public int getItemCount() {
        return users.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{
        TextView txtUserName;
        RoundedImageView imageView;
        ImageView imgAudioMeeting, imgVideoMeeting;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.imageProfile);
            txtUserName = itemView.findViewById(R.id.textName);
            imgAudioMeeting = itemView.findViewById(R.id.imageviewCall);
            imgVideoMeeting = itemView.findViewById(R.id.imageViewVideoCall);

        }
        void setUserData(User user){
            Picasso.get().load(user.getImage()).placeholder(R.drawable.teest).into(imageView);
            txtUserName.setText(user.getFirstName()+" "+user.getLastName());

            imgAudioMeeting.setOnClickListener(v -> userListener.initiateAudioMeeting(user));
            imgVideoMeeting.setOnClickListener(v -> userListener.initiateVideoMeeting(user));
        }


    }
}
