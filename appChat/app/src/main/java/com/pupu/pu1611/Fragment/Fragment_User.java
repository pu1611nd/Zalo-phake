package com.pupu.pu1611.Fragment;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.pupu.pu1611.Activity.LognInActivity;
import com.pupu.pu1611.Adapter.FollowerAdapter;
import com.pupu.pu1611.R;
import com.pupu.pu1611.models.Follower;
import com.pupu.pu1611.models.User;
import com.pupu.pu1611.utilities.Constants;
import com.pupu.pu1611.utilities.PreferenceManager;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Fragment_User extends Fragment {
    private RecyclerView recyclerView;
    private ArrayList<Follower> list;
    private FollowerAdapter adapter;
    private ImageView imageViewChanePhoto,coverPhoto,imageCheck,imageProfile,imageSetting;
    private TextView name,email,follower,friend,photo;

    private FirebaseStorage storage;
    private FirebaseFirestore database;
    private PreferenceManager preferenceManager;
    View view;
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_setting_user,container,false);
        recyclerView = view.findViewById(R.id.rcListFriend);
        list = new ArrayList<>();
        adapter = new FollowerAdapter(list,getContext());
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext(),LinearLayoutManager.HORIZONTAL,false);
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setAdapter(adapter);

        storage = FirebaseStorage.getInstance();
        database = FirebaseFirestore.getInstance();
        preferenceManager = new PreferenceManager(getContext());

        coverPhoto = view.findViewById(R.id.coverPhoto);
        Picasso.get().load(preferenceManager.getString(Constants.KEY_IMAGE_COVER)).placeholder(R.drawable.image_svgrepo_com).into(coverPhoto);

        imageProfile = view.findViewById(R.id.profile_image);
        Picasso.get().load(preferenceManager.getString(Constants.KEY_IMAGE)).placeholder(R.drawable.image_svgrepo_com).into(imageProfile);

        imageViewChanePhoto = view.findViewById(R.id.chaneCoverPhoto);
        imageViewChanePhoto.setOnClickListener(v->{
            Intent intent = new Intent();
            intent.setAction(Intent.ACTION_GET_CONTENT);
            intent.setType("image/*");
            startActivityForResult(intent,11);
        });
        imageCheck = view.findViewById(R.id.imageCheck);
        imageCheck.setOnClickListener(v->{
            Intent intent = new Intent();
            intent.setAction(Intent.ACTION_GET_CONTENT);
            intent.setType("image/*");
            startActivityForResult(intent,22);
        });


        name = view.findViewById(R.id.userNameP);
        name.setText(preferenceManager.getString(Constants.KEY_FIRST_NAME)+" "+
                preferenceManager.getString(Constants.KEY_LAST_NAME));
        email = view.findViewById(R.id.job);
        email.setText(preferenceManager.getString(Constants.KEY_EMAIL));
        follower = view.findViewById(R.id.tv_follower);

        getFollower();
        return view;
    }


    private void getFollower(){
        // Tạo một đối tượng DatabaseReference để truy cập vào tài liệu Firestore
        DocumentReference userRef = database.collection(Constants.KEY_COLLECTION_USERS)
                .document(preferenceManager.getString(Constants.KEY_USER_ID));

        // Lấy giá trị của trường followers trong tài liệu Firestore
                userRef.get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        // Ép kiểu danh sách followers từ tài liệu Firestore
                        if(list != null){
                            list.clear();
                        }
                        List<Map<String, Object>> followerData = (List<Map<String, Object>>) documentSnapshot.get(Constants.KEY_FOLLOWER);
                        if(followerData != null){
                            for (Map<String, Object> data : followerData) {
                                Follower follower = new Follower();
                                follower.setFollowedBy((String) data.get("followedBy"));
                                follower.setFollowedAt((long) data.get("followedAt"));
                                list.add(follower);
                            }
                            adapter.notifyDataSetChanged();
                        }
                        follower.setText(list.size()+"");

                        // Xử lý danh sách followers ở đây
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        // Xử lý khi lấy dữ liệu thất bại
                    }
                });

    }

    private void signOut(){

        Toast.makeText(getContext(), "Signing out ....", Toast.LENGTH_SHORT).show();
            FirebaseFirestore database = FirebaseFirestore.getInstance();
            DocumentReference documentReference =
                    database.collection(Constants.KEY_COLLECTION_USERS).document(
                            preferenceManager.getString(Constants.KEY_USER_ID)
                    );
            HashMap<String,Object> update = new HashMap<>();
            update.put(Constants.KEY_FCM_TOKEN, FieldValue.delete());
            documentReference.update(update)
                    .addOnSuccessListener(unused -> {
                        preferenceManager.clear();
                        startActivity(new Intent(getContext(), LognInActivity.class));
                        getActivity().finish();
                    })
                    .addOnFailureListener(e -> Toast.makeText(getContext(), "loi dang xuat", Toast.LENGTH_SHORT).show());

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == 11){
            if (data.getData() != null){
                Uri uri = data.getData();
                coverPhoto.setImageURI(uri);
                final StorageReference reference = storage.getReference().child("cover_photo").child(preferenceManager.getString(Constants.KEY_USER_ID));
                reference.putFile(uri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        Toast.makeText(getContext(), "Luu thanh cong", Toast.LENGTH_SHORT).show();
                        reference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {
                                Map<String, Object> updates = new HashMap<>();
                                updates.put(Constants.KEY_IMAGE_COVER, uri.toString());

                                database.collection(Constants.KEY_COLLECTION_USERS)
                                        .document(preferenceManager.getString(Constants.KEY_USER_ID))
                                        .update(updates)
                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {
                                                // Xử lý khi cập nhật thành công
                                                preferenceManager.putString(Constants.KEY_IMAGE_COVER,uri.toString());
                                            }
                                        })
                                        .addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                // Xử lý khi cập nhật thất bại
                                            }
                                        });

                            }
                        });
                    }
                });
            }
        }else {
            if (data.getData() != null){
                Uri uri = data.getData();
                imageProfile.setImageURI(uri);
                final StorageReference reference = storage.getReference().child("profile_image").child(preferenceManager.getString(Constants.KEY_USER_ID));
                reference.putFile(uri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        Toast.makeText(getContext(), "Luu thanh cong", Toast.LENGTH_SHORT).show();

                    }
                });
            }
        }

    }
}
