package com.pupu.pu1611.Adapter;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.pupu.pu1611.R;
import com.pupu.pu1611.databinding.LayoutStoryBinding;
import com.pupu.pu1611.models.Follower;
import com.pupu.pu1611.models.Story;
import com.pupu.pu1611.models.User;
import com.pupu.pu1611.models.UserStories;
import com.pupu.pu1611.utilities.Constants;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import omari.hamza.storyview.StoryView;
import omari.hamza.storyview.callback.StoryClickListeners;
import omari.hamza.storyview.model.MyStory;

public class StoryAdapter extends RecyclerView.Adapter<StoryAdapter.viewHolder>{

    private ArrayList<Story> list;
    private Context context;
    private FirebaseFirestore database;

    public StoryAdapter(ArrayList<Story> list, Context context) {
        this.list = list;
        this.context = context;
        this.database = FirebaseFirestore.getInstance();
    }

    @NonNull
    @Override
    public viewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.layout_story,parent,false);

        return new viewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull viewHolder holder, int position) {
        Story story = list.get(position);
        getUserStory(story.getStoryBy(),holder);
        Log.d("FFFFF",story.getStoryBy());


    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public class viewHolder extends RecyclerView.ViewHolder{
        private LayoutStoryBinding binding;
        public viewHolder(@NonNull View itemView) {
            super(itemView);
            binding = LayoutStoryBinding.bind(itemView);

        }
    }

    private void getUserStory( String storyId,viewHolder holder){
        User user = new User();
        DocumentReference userRef = database.collection(Constants.KEY_COLLECTION_STORY)
                .document(storyId);
        // Lấy giá trị của trường followers trong tài liệu Firestore
        userRef.get()
                .addOnSuccessListener(documentSnapshot1 -> {
                    // Ép kiểu danh sách followers từ tài liệu Firestore
                    ArrayList<UserStories> listStory = new ArrayList<>();
                    List<Map<String, Object>> storyData = (List<Map<String, Object>>) documentSnapshot1.get("userStories");
                    if (storyData != null) {
                        for (Map<String, Object> data : storyData) {
                            UserStories userStories = new UserStories();
                            userStories.setImage((String) data.get("image"));
                            userStories.setStoryAt((long) data.get("storyAt"));
                            listStory.add(userStories);
                        }
                        Picasso.get().load(listStory.get(listStory.size()-1).getImage()).into(holder.binding.story);
                        holder.binding.profileImage.setPortionsCount(listStory.size());
                        database.collection(Constants.KEY_COLLECTION_USERS)
                                .document(storyId)
                                .get()
                                .addOnSuccessListener(documentSnapshot -> {
                                    // danh sách từ tài liệu Firestore
                                    user.setFirstName(documentSnapshot.getString(Constants.KEY_FIRST_NAME));
                                    user.setLastName(documentSnapshot.getString(Constants.KEY_LAST_NAME));
                                    user.setEmail(documentSnapshot.getString(Constants.KEY_EMAIL));
                                    user.setImage(documentSnapshot.getString(Constants.KEY_IMAGE));
                                   // Picasso.get().load(user.getImage()).placeholder(R.drawable.image_svgrepo_com).into( holder.binding.profileImage);
                                    holder.binding.name.setText(user.getFirstName() + " " + user.getLastName());

                                })
                                .addOnFailureListener(e -> {
                                    // Xử lý khi lấy dữ liệu thất bại
                                });
                        holder.binding.story.setOnClickListener(v->{
                            ArrayList<MyStory> myStories = new ArrayList<>();
                            for(UserStories userStories: listStory){
                                myStories.add(new MyStory(
                                        userStories.getImage()
                                ));
                            }
                            new StoryView.Builder(((AppCompatActivity) context).getSupportFragmentManager())
                                    .setStoriesList(myStories) // Required
                                    .setStoryDuration(5000) // Default is 2000 Millis (2 Seconds)
                                    .setTitleText(user.getFirstName()+" "+user.getLastName()) // Default is Hidden
                                    .setSubtitleText("") // Default is Hidden
                                    .setTitleLogoUrl(user.getImage()) // Default is Hidden
                                    .setStoryClickListeners(new StoryClickListeners() {
                                        @Override
                                        public void onDescriptionClickListener(int position) {
                                            //your action
                                        }

                                        @Override
                                        public void onTitleIconClickListener(int position) {
                                            //your action
                                        }
                                    }) // Optional Listeners
                                    .build() // Must be called before calling show method
                                    .show();
                        });

                    }
                })
                .addOnFailureListener(e -> {
                    // Xử lý khi lấy dữ liệu thất bại
                });
    }
}

