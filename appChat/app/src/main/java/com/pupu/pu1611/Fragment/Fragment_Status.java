package com.pupu.pu1611.Fragment;

import android.app.ProgressDialog;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.cooltechworks.views.shimmer.ShimmerRecyclerView;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.makeramen.roundedimageview.RoundedImageView;
import com.pupu.pu1611.Activity.AddPostActivity;
import com.pupu.pu1611.Adapter.PostAdapter;
import com.pupu.pu1611.Adapter.StoryAdapter;
import com.pupu.pu1611.R;
import com.pupu.pu1611.models.Post;
import com.pupu.pu1611.models.Story;
import com.pupu.pu1611.models.UserStories;
import com.pupu.pu1611.utilities.Constants;
import com.pupu.pu1611.utilities.PreferenceManager;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import de.hdodenhof.circleimageview.CircleImageView;

public class Fragment_Status extends Fragment {
    private RecyclerView storyRv;
    private ShimmerRecyclerView dasBoardR;
    private ArrayList<Story> list;
    private ArrayList<Post> listPos;
    private StoryAdapter adapter;
    private PostAdapter dasBoardAdapter;
    private RoundedImageView imageAddStory;
    private CircleImageView imageProfile;
    private FirebaseFirestore database;
    private FirebaseStorage storage;
    private PreferenceManager preferenceManager;
    private ActivityResultLauncher<String> galleryLauncher;
    private ProgressDialog dialog;

    View view;
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_status,container,false);
        database = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();
        preferenceManager = new PreferenceManager(getContext());


        dialog = new ProgressDialog(getContext());
        dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        dialog.setTitle("Story Uploading");
        dialog.setMessage("Please wait .....");
        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);

        storyRv = view.findViewById(R.id.storyRv);
        list = new ArrayList<>();
        adapter = new StoryAdapter(list,getContext());
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext(),LinearLayoutManager.HORIZONTAL,false);
        storyRv.setLayoutManager(linearLayoutManager);
        storyRv.setNestedScrollingEnabled(false);
        storyRv.setAdapter(adapter);

        dasBoardR = view.findViewById(R.id.dasBoardRv);
        dasBoardR.showShimmerAdapter();
        listPos = new ArrayList<>();
        dasBoardAdapter = new PostAdapter(listPos,getContext());
        LinearLayoutManager linearLayoutManager1 = new LinearLayoutManager(getContext(),LinearLayoutManager.VERTICAL,false);
        dasBoardR.setLayoutManager(linearLayoutManager1);
        dasBoardR.setNestedScrollingEnabled(false);
        dasBoardAdapter.setOnEditClickListener(new PostAdapter.OnCommentClickListener() {
            @Override
            public void onCommentClick(Post post) {
                FragmentManager fragmentManager = requireActivity().getSupportFragmentManager();
                Fragment_Comment dialogFragment = Fragment_Comment.newInstance(post.getPostId(), post.getPostedBy());
                dialogFragment.show(fragmentManager, "Fragment_Comment");
            }
        });
        getPostData();
        getStory();
        imageProfile = view.findViewById(R.id.profile_image);
        Picasso.get().load(preferenceManager.getString(Constants.KEY_IMAGE)).placeholder(R.drawable.image_svgrepo_com).into(imageProfile);
        imageAddStory = view.findViewById(R.id.story);
        Picasso.get().load(preferenceManager.getString(Constants.KEY_IMAGE)).placeholder(R.drawable.image_svgrepo_com).into(imageAddStory);
        imageAddStory.setOnClickListener(v->{
            galleryLauncher.launch("image/*");
        });
        galleryLauncher = registerForActivityResult(new ActivityResultContracts.GetContent(), new ActivityResultCallback<Uri>() {
            @Override
            public void onActivityResult(Uri result) {
                imageAddStory.setImageURI(result);
                dialog.show();
                final StorageReference reference = storage.getReference().child("stories").child(preferenceManager.getString(Constants.KEY_USER_ID))
                        .child(new Date().getTime()+"");
                reference.putFile(result).addOnSuccessListener(taskSnapshot -> {
                    reference.getDownloadUrl().addOnSuccessListener(uri -> {
                        UserStories userStories = new UserStories();
                        userStories.setImage(uri.toString());
                        userStories.setStoryAt(new Date().getTime());
                        Map<String, Object> updates = new HashMap<>();
                        updates.put("storyBy",preferenceManager.getString(Constants.KEY_USER_ID));
                        updates.put("userStories", FieldValue.arrayUnion(userStories));
                        database.collection(Constants.KEY_COLLECTION_STORY)
                                .document(preferenceManager.getString(Constants.KEY_USER_ID))
                                .set(updates,SetOptions.merge())
                                .addOnSuccessListener(documentReference1 -> {
                                    dialog.dismiss();
                                }).addOnFailureListener(e -> {});

                    });
                });
            }
        });


        return view;
    }



    private void getStory(){
        database.collection(Constants.KEY_COLLECTION_STORY)
                .addSnapshotListener((value, error) -> {
                    if (error != null) {
                        // Xử lý lỗi
                        return;
                    }
                    // Xóa listPos hiện tại
                    list.clear();
                    // Lấy các dòng dữ liệu mới nhất và thêm vào listPos
                    for (QueryDocumentSnapshot document : value) {
                        Story story = new Story();
                        story.setStoryBy(document.getId());
                        list.add(story);
                    }

                    // Báo cho adapter biết rằng dữ liệu đã thay đổi
                    adapter.notifyDataSetChanged();
                });
    }

    private void getPostData() {
        database.collection(Constants.KEY_COLLECTION_POST)
                .addSnapshotListener((value, error) -> {
                    if (error != null) {
                        // Xử lý lỗi
                        return;
                    }
                    // Xóa listPos hiện tại
                    listPos.clear();
                    // Lấy các dòng dữ liệu mới nhất và thêm vào listPos
                    for (QueryDocumentSnapshot document : value) {
                        Post post = document.toObject(Post.class);
                        listPos.add(post);
                    }

                    // Báo cho adapter biết rằng dữ liệu đã thay đổi
                    dasBoardR.hideShimmerAdapter();
                    dasBoardR.setAdapter(dasBoardAdapter);
                    dasBoardAdapter.notifyDataSetChanged();
                });

    }
}
