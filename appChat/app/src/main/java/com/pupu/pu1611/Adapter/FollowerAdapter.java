package com.pupu.pu1611.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.pupu.pu1611.R;
import com.pupu.pu1611.databinding.LayoutFriendSampleBinding;
import com.pupu.pu1611.models.Follower;
import com.pupu.pu1611.models.User;
import com.pupu.pu1611.utilities.Constants;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class FollowerAdapter extends RecyclerView.Adapter<FollowerAdapter.viewHolder> {
    private ArrayList<Follower> list;
    private Context context;
    private FirebaseFirestore database;

    public FollowerAdapter(ArrayList<Follower> list, Context context) {
        this.list = list;
        this.context = context;
    }

    @NonNull
    @Override
    public FollowerAdapter.viewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.layout_friend_sample,parent,false);
        database = FirebaseFirestore.getInstance();
        return new FollowerAdapter.viewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FollowerAdapter.viewHolder holder, int position) {
        Follower follower = list.get(position);
        // Tạo một đối tượng CollectionReference để truy cập vào collection "users"
        CollectionReference usersRef = database.collection(Constants.KEY_COLLECTION_USERS);

        // Truy cập vào Document tương ứng với userId và lấy thông tin người dùng
                usersRef.document(follower.getFollowedBy())
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        User user = documentSnapshot.toObject(User.class);
                        // Xử lý thông tin người dùng ở đây
                        Picasso.get().load(user.getImage()).placeholder(R.drawable.teest).into(holder.binding.profileImage);
                    } else {
                        // Thông báo người dùng không tồn tại
                    }
                })
                .addOnFailureListener(e -> {
                    // Xử lý khi lấy dữ liệu thất bại
                });


    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public class viewHolder extends RecyclerView.ViewHolder{
        LayoutFriendSampleBinding binding;
        public viewHolder(@NonNull View itemView) {
            super(itemView);
            binding = LayoutFriendSampleBinding.bind(itemView);
        }
    }

}