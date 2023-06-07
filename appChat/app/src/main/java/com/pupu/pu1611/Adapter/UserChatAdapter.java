package com.pupu.pu1611.Adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import com.pupu.pu1611.R;
import com.pupu.pu1611.databinding.ItemContainerUserBinding;
import com.pupu.pu1611.databinding.ItemContainerUserChatBinding;
import com.pupu.pu1611.listeners.UserListenerChat;
import com.pupu.pu1611.models.Follower;
import com.pupu.pu1611.models.Notification;
import com.pupu.pu1611.models.User;
import com.pupu.pu1611.utilities.Constants;
import com.pupu.pu1611.utilities.PreferenceManager;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class UserChatAdapter extends RecyclerView.Adapter<UserChatAdapter.UserViewHolder> {
    private final List<User> users;
    private ArrayList<Follower> list;
    private final UserListenerChat userListenerChat;
    private PreferenceManager preferenceManager;
    private FirebaseFirestore database;
    private Context context;


    public UserChatAdapter(List<User> users, UserListenerChat userListenerChat, Context context) {
        this.users = users;
        this.userListenerChat = userListenerChat;
        this.context = context;

        this.list = new ArrayList<>();
        database = FirebaseFirestore.getInstance();
    }


    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_container_user_chat, parent, false);
        return new UserViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {
        preferenceManager = new PreferenceManager(context);
        User user = users.get(position);
        holder.binding.textName.setText(user.getFirstName() + " " + user.getLastName());
        holder.binding.textEmail.setText(user.getEmail());
        Picasso.get().load(user.getImage()).placeholder(R.drawable.teest).into(holder.binding.imageProfile);
        holder.binding.getRoot().setOnClickListener(v -> userListenerChat.onUserClicked(user));

        setFollow(user,holder);
        holder.binding.buttonFollow.setOnClickListener(v -> {
            Follower follower = new Follower();
            follower.setFollowedBy(preferenceManager.getString(Constants.KEY_USER_ID));
            follower.setFollowedAt(new Date().getTime());
            Map<String, Object> updates = new HashMap<>();
            updates.put(Constants.KEY_FOLLOWER, FieldValue.arrayUnion(follower));
            database.collection(Constants.KEY_COLLECTION_USERS)
                    .document(user.getId())
                    .update(updates)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            // Xử lý khi cập nhật thành công
                            holder.binding.buttonFollow.setBackgroundDrawable(ContextCompat.getDrawable(context, R.drawable.follow_active_btn));
                            holder.binding.buttonFollow.setText("Following");
                            holder.binding.buttonFollow.setTextColor(context.getResources().getColor(R.color.primary));
                            holder.binding.buttonFollow.setEnabled(false);

                            Notification notification = new Notification();
                            notification.setNotificationBy(preferenceManager.getString(Constants.KEY_USER_ID));
                            notification.setNotificationAt(new Date().getTime());
                            notification.setType("follow");
                            UUID uuid = UUID.randomUUID();
                            Map<String, Object> updates = new HashMap<>();
                            updates.put(uuid.toString(),notification);
                            database.collection(Constants.KEY_COLLECTION_NOTIFICATION)
                                    .document(user.getId())
                                    .set(updates, SetOptions.merge())
                                    .addOnSuccessListener(documentReference -> {

                                    }).addOnFailureListener(e -> {});
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            // Xử lý khi cập nhật thất bại
                        }
                    });
        });

    }

    @Override
    public int getItemCount() {
        return users.size();
    }


    public class UserViewHolder extends RecyclerView.ViewHolder {
        private ItemContainerUserChatBinding binding;

        public UserViewHolder(@NonNull View itemView) {
            super(itemView);
            binding = ItemContainerUserChatBinding.bind(itemView);
        }
    }


    public void setFollow(User user,UserViewHolder holder) {
        DocumentReference userRef = database.collection(Constants.KEY_COLLECTION_USERS)
                .document(user.getId());
                // Lấy giá trị của trường followers trong tài liệu Firestore
                userRef.get()
                .addOnSuccessListener(documentSnapshot -> {
                    // Ép kiểu danh sách followers từ tài liệu Firestore
                    if (list != null) {
                        list.clear();
                    }
                    List<Map<String, Object>> followerData = (List<Map<String, Object>>) documentSnapshot.get(Constants.KEY_FOLLOWER);
                    if (followerData != null) {
                        for (Map<String, Object> data : followerData) {
                            Follower follower = new Follower();
                            follower.setFollowedBy((String) data.get("followedBy"));
                            follower.setFollowedAt((long) data.get("followedAt"));
                            list.add(follower);
                        }
                        for (Follower follower : list) {
                            if (follower.getFollowedBy().equals(preferenceManager.getString(Constants.KEY_USER_ID))) {
                                holder.binding.buttonFollow.setBackgroundDrawable(ContextCompat.getDrawable(context, R.drawable.follow_active_btn));
                                holder.binding.buttonFollow.setText("Following");
                                holder.binding.buttonFollow.setTextColor(context.getResources().getColor(R.color.primary));
                                holder.binding.buttonFollow.setEnabled(false);
                            }

                        }
                    }
                })
                .addOnFailureListener(e -> {
                    // Xử lý khi lấy dữ liệu thất bại
                });
    }


}
