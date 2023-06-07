package com.pupu.pu1611.Activity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.pupu.pu1611.R;
import com.pupu.pu1611.databinding.ActivityAddPostBinding;
import com.pupu.pu1611.models.Post;
import com.pupu.pu1611.utilities.Constants;
import com.pupu.pu1611.utilities.PreferenceManager;
import com.squareup.picasso.Picasso;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class AddPostActivity extends AppCompatActivity {

    private ActivityAddPostBinding binding;
    private Uri uri;
    private FirebaseFirestore database;
    private FirebaseStorage storage;
    private PreferenceManager preferenceManager;
    private ProgressDialog dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAddPostBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        preferenceManager = new PreferenceManager(getApplicationContext());

        database = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();

        dialog = new ProgressDialog(AddPostActivity.this);
        dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        dialog.setTitle("Post Uploading");
        dialog.setMessage("Please wait .....");
        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);

        Picasso.get().load(preferenceManager.getString(Constants.KEY_IMAGE)).placeholder(R.drawable.anh_test).into(binding.imageProfile);
        binding.textName.setText(preferenceManager.getString(Constants.KEY_FIRST_NAME)+" "+
                preferenceManager.getString(Constants.KEY_LAST_NAME));
        binding.textEmail.setText(preferenceManager.getString(Constants.KEY_EMAIL));

        binding.tvDercion.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                String description = binding.tvDercion.getText().toString().trim();
                if(!description.isEmpty()){
                    binding.buttonPost.setBackgroundDrawable(ContextCompat.getDrawable(getApplicationContext(), R.drawable.background_button));
                    binding.buttonPost.setTextColor(getApplicationContext().getResources().getColor(R.color.white));
                    binding.buttonPost.setEnabled(true);
                }else {
                    binding.buttonPost.setBackgroundDrawable(ContextCompat.getDrawable(getApplicationContext(), R.drawable.follow_active_btn));
                    binding.buttonPost.setTextColor(getApplicationContext().getResources().getColor(R.color.primary));
                    binding.buttonPost.setEnabled(false);
                }

            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        binding.imageAddImage.setOnClickListener(v->{
            Intent intent = new Intent();
            intent.setAction(Intent.ACTION_GET_CONTENT);
            intent.setType("image/*");
            startActivityForResult(intent,10);
        });

        binding.buttonPost.setOnClickListener(v->{
            dialog.show();
            final StorageReference reference = storage.getReference().child("posts")
                    .child(preferenceManager.getString(Constants.KEY_USER_ID))
                    .child(new Date().getTime()+"");
            reference.putFile(uri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    reference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            Post post = new Post();
                            post.setPostImage(uri.toString());
                            post.setPostedBy(preferenceManager.getString(Constants.KEY_USER_ID));
                            post.setPostDescription(binding.tvDercion.getText().toString().trim());
                            post.setPostedAt(new Date().getTime());
                            database.collection(Constants.KEY_COLLECTION_POST)
                                    .add(post)
                                    .addOnSuccessListener(documentReference -> {
                                        Map<String, Object> updates = new HashMap<>();
                                        updates.put("postId",documentReference.getId());
                                        database.collection(Constants.KEY_COLLECTION_POST)
                                                .document(documentReference.getId())
                                                .update(updates)
                                                .addOnSuccessListener(aVoid -> {
                                                    // Xử lý khi cập nhật thành công
                                                   dialog.dismiss();
                                                    onBackPressed();
                                                })
                                                .addOnFailureListener(e -> {
                                                    // Xử lý khi cập nhật thất bại
                                                });
                                    }).addOnFailureListener(e -> {});

                        }
                    });
                }
            });
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(data.getData() != null){
             uri = data.getData();
            binding.imagePost.setImageURI(uri);
            binding.imagePost.setVisibility(View.VISIBLE);
            binding.buttonPost.setBackgroundDrawable(ContextCompat.getDrawable(getApplicationContext(), R.drawable.background_button));
            binding.buttonPost.setTextColor(getApplicationContext().getResources().getColor(R.color.white));
            binding.buttonPost.setEnabled(true);
        }
    }
}