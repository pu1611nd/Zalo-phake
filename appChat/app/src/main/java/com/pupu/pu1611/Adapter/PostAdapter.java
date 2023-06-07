package com.pupu.pu1611.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.github.marlonlom.utilities.timeago.TimeAgo;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import com.pupu.pu1611.R;
import com.pupu.pu1611.databinding.LayoutDasboardBinding;
import com.pupu.pu1611.models.Notification;
import com.pupu.pu1611.models.Post;
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

public class PostAdapter extends RecyclerView.Adapter<PostAdapter.viewHolder> {
    private ArrayList<Post> list;
    private Context context;
    private FirebaseFirestore database;
    private PreferenceManager preferenceManager;
    private Boolean like = false;
    private int postLike;

    public PostAdapter(ArrayList<Post> list, Context context) {
        this.list = list;
        this.context = context;
        this.database = FirebaseFirestore.getInstance();
        this.preferenceManager = new PreferenceManager(context);
    }
    public interface OnCommentClickListener {
        void onCommentClick(Post post);
    }
    private OnCommentClickListener onCommentClickListener;

    public void setOnEditClickListener(OnCommentClickListener listener) {
        this.onCommentClickListener = listener;
    }

    @NonNull
    @Override
    public PostAdapter.viewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.layout_dasboard,parent,false);
        return new PostAdapter.viewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PostAdapter.viewHolder holder, int position) {
        Post post = list.get(position);
        Picasso.get().load(post.getPostImage()).placeholder(R.drawable.image_svgrepo_com).into(holder.binding.posImage);
        String timeAgo = TimeAgo.using(post.getPostedAt());
        holder.binding.about.setText(timeAgo);
        holder.binding.comment.setOnClickListener(v->{
            onCommentClickListener.onCommentClick(post);
        });
        String description = post.getPostDescription();
        if(description.equals("")){
            holder.binding.textViewDesripsion.setVisibility(View.GONE);
        }else {
            holder.binding.textViewDesripsion.setText(post.getPostDescription());
        }
        postLike = post.getPostLike();
        holder.binding.like.setText(postLike+"");
        DocumentReference userRef = database.collection(Constants.KEY_COLLECTION_USERS)
                .document(post.getPostedBy());

        userRef.addSnapshotListener((value, error) -> {
            if (error != null) {
                // Xử lý lỗi
                return;
            }

            // Lấy dữ liệu từ tài liệu Firestore
            User user = new User();
            user.setFirstName(value.getString(Constants.KEY_FIRST_NAME));
            user.setLastName(value.getString(Constants.KEY_LAST_NAME));
            user.setEmail(value.getString(Constants.KEY_EMAIL));
            user.setImage(value.getString(Constants.KEY_IMAGE));

            // Cập nhật giao diện người dùng với dữ liệu mới
            Picasso.get().load(user.getImage()).placeholder(R.drawable.image_svgrepo_com).into(holder.binding.profileImage);
            holder.binding.userName.setText(user.getFirstName() + " " + user.getLastName());
        });

        checkLike(post,holder);

        holder.binding.like.setOnClickListener(v->{
            Map<String, Object> updates = new HashMap<>();
            Map<String, Object> updates1 = new HashMap<>();
            if(like){
                postLike --;
                updates.put(preferenceManager.getString(Constants.KEY_USER_ID),false);
                database.collection(Constants.KEY_COLLECTION_POST)
                        .document(post.getPostId())
                        .update("likes",updates)
                        .addOnSuccessListener(aVoid -> {
                            // Xử lý khi cập nhật thành công
                            updates1.put("postLike",postLike);
                            database.collection(Constants.KEY_COLLECTION_POST)
                                    .document(post.getPostId())
                                    .update(updates1)
                                    .addOnSuccessListener(aVoid1 -> {
                                        // Xử lý khi cập nhật thành công
                                        holder.binding.like.setCompoundDrawablesRelativeWithIntrinsicBounds(R.drawable.heart_svgrepo_com__1_,0,0,0);
                                    })
                                    .addOnFailureListener(e -> {
                                        // Xử lý khi cập nhật thất bại
                                    });
                        })
                        .addOnFailureListener(e -> {
                            // Xử lý khi cập nhật thất bại
                        });
            }else {
                postLike ++;
                updates.put(preferenceManager.getString(Constants.KEY_USER_ID),true);
                database.collection(Constants.KEY_COLLECTION_POST)
                        .document(post.getPostId())
                        .update("likes",updates)
                        .addOnSuccessListener(aVoid -> {
                            // Xử lý khi cập nhật thành công
                            updates1.put("postLike",postLike);
                            database.collection(Constants.KEY_COLLECTION_POST)
                                    .document(post.getPostId())
                                    .update(updates1)
                                    .addOnSuccessListener(aVoid1 -> {
                                        // Xử lý khi cập nhật thành công
                                        holder.binding.like.setCompoundDrawablesRelativeWithIntrinsicBounds(R.drawable.heart_svgrepo_com,0,0,0);
                                        Notification notification = new Notification();
                                        notification.setNotificationBy(preferenceManager.getString(Constants.KEY_USER_ID));
                                        notification.setNotificationAt(new Date().getTime());
                                        notification.setPostId(post.getPostId());
                                        notification.setPostedBy(post.getPostedBy());
                                        notification.setType("like");
                                        UUID uuid = UUID.randomUUID();
                                        Map<String, Object> update1 = new HashMap<>();
                                        update1.put(uuid.toString(),notification);
                                        database.collection(Constants.KEY_COLLECTION_NOTIFICATION)
                                                .document(post.getPostedBy())
                                                .set(update1, SetOptions.merge())
                                                .addOnSuccessListener(documentReference -> {

                                                }).addOnFailureListener(e -> {});
                                    })
                                    .addOnFailureListener(e -> {
                                        // Xử lý khi cập nhật thất bại
                                    });
                        })
                        .addOnFailureListener(e -> {
                            // Xử lý khi cập nhật thất bại
                        });


            }
        });

    }

    private void checkLike(Post post,PostAdapter.viewHolder holder){
        DocumentReference postRef = database.collection(Constants.KEY_COLLECTION_POST)
                .document(post.getPostId());

        postRef.addSnapshotListener((documentSnapshot, error) -> {
            if (error != null) {
                // Xử lý lỗi
                return;
            }

            if (documentSnapshot != null && documentSnapshot.exists()) {
                Map<String, Object> likes = (Map<String, Object>) documentSnapshot.getData().get("likes");
                if (likes != null && likes.get(preferenceManager.getString(Constants.KEY_USER_ID)) != null &&
                        (Boolean) likes.get(preferenceManager.getString(Constants.KEY_USER_ID))) {
                    // Người dùng đã thích bài viết này
                    like = true;
                    holder.binding.like.setCompoundDrawablesRelativeWithIntrinsicBounds(R.drawable.heart_svgrepo_com,0,0,0);
                } else {
                    // Người dùng chưa thích bài viết này
                    like = false;
                }
                List<HashMap<String, Object>> commentData = (List<HashMap<String, Object>>) documentSnapshot.get("comment");
                if(commentData != null){
                    holder.binding.comment.setText(commentData.size()+"");
                } else {
                    holder.binding.comment.setText("0");
                }
            } else {
                // Xử lý trường hợp không tìm thấy tài liệu được yêu cầu
            }


        });

    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public class viewHolder extends RecyclerView.ViewHolder{
        private LayoutDasboardBinding binding;
        public viewHolder(@NonNull View itemView) {
            super(itemView);
            binding = LayoutDasboardBinding.bind(itemView);
        }
    }



}
