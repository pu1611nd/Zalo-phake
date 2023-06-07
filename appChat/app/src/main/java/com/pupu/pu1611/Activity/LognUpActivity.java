package com.pupu.pu1611.Activity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Patterns;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.pupu.pu1611.MainActivity;
import com.pupu.pu1611.R;
import com.pupu.pu1611.databinding.ActivityLognUpBinding;
import com.pupu.pu1611.utilities.Constants;
import com.pupu.pu1611.utilities.PreferenceManager;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

public class LognUpActivity extends AppCompatActivity {

    private ActivityLognUpBinding binding;
    private PreferenceManager preferenceManager;
    private FirebaseStorage storage;
    private String encodeImage;
    private Uri uriImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLognUpBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        preferenceManager = new PreferenceManager(getApplicationContext());
        findViewById(R.id.textSignIn).setOnClickListener(v -> onBackPressed());

        storage = FirebaseStorage.getInstance();

        binding.layoutImage.setOnClickListener(v ->{
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            intent.addFlags(Intent.FLAG_GRANT_PREFIX_URI_PERMISSION);
            pickImage.launch(intent);
        });

        binding.buttonSignUp.setOnClickListener(v ->{
            if(binding.textInputfirstName.getText().toString().trim().isEmpty()){
                showMessage("Nhap ho");
            }else if(binding.textLastName.getText().toString().trim().isEmpty()){
                showMessage("Nhap ten");
            } else if (binding.textInputEmail.getText().toString().trim().isEmpty()) {
                showMessage("Nhap email");
            } else if(!Patterns.EMAIL_ADDRESS.matcher(binding.textInputEmail.getText().toString()).matches()){
                showMessage("nhap dung dinh dang email");
            }else if(binding.textInputPassword.getText().toString().trim().isEmpty()){
                showMessage("nhap password");
            }else if(binding.textInputComformPassword.getText().toString().trim().isEmpty()){
                showMessage("nhap lai password");
            }else if (!binding.textInputComformPassword.getText().toString().equals(binding.textInputPassword.getText().toString())){
                showMessage("hai mk phai trung nhau");
            }else{
                checkEmail(binding.textInputEmail.getText().toString().trim());
            }
        });
    }
    private void checkEmail(String email){
        FirebaseFirestore database = FirebaseFirestore.getInstance();
        database.collection(Constants.KEY_COLLECTION_USERS)
                .whereEqualTo(Constants.KEY_EMAIL,email)
                .get()
                .addOnCompleteListener(task -> {
                    if(task.isSuccessful() && task.getResult() != null && task.getResult().getDocuments().size() > 0){
                       showMessage("email da duoc dang ky vui long chon email khac");
                    }else{
                        signUp();
                    }
                });
    }

    private void showMessage (String message){
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
    }
    private void signUp(){
        FirebaseFirestore database = FirebaseFirestore.getInstance();
        HashMap<String, Object> user = new HashMap<>();
        user.put(Constants.KEY_FIRST_NAME, binding.textInputfirstName.getText().toString());
        user.put(Constants.KEY_LAST_NAME,binding.textLastName.getText().toString());
        user.put(Constants.KEY_EMAIL,binding.textInputEmail.getText().toString());
        user.put(Constants.KEY_PASSWORD,binding.textInputPassword.getText().toString());
        user.put(Constants.KEY_IMAGE,"");
        user.put(Constants.KEY_IMAGE_COVER,"");

        database.collection(Constants.KEY_COLLECTION_USERS)
                .add(user)
                .addOnSuccessListener(documentReference -> {
                    preferenceManager.putBoolean(Constants.KEY_IS_SIGNED_IN,true);
                    preferenceManager.putString(Constants.KEY_USER_ID,documentReference.getId());
                    preferenceManager.putString(Constants.KEY_FIRST_NAME,binding.textInputfirstName.getText().toString());
                    preferenceManager.putString(Constants.KEY_LAST_NAME,binding.textLastName.getText().toString());
                    preferenceManager.putString(Constants.KEY_EMAIL,binding.textInputEmail.getText().toString());
                    final StorageReference reference = storage.getReference().child("profile_image").child(documentReference.getId());
                    reference.putFile(uriImage).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            reference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
                                    Map<String, Object> updates = new HashMap<>();
                                    updates.put(Constants.KEY_IMAGE, uri.toString());
                                    updates.put(Constants.KEY_IMAGE_COVER,uri.toString());
                                    database.collection(Constants.KEY_COLLECTION_USERS)
                                            .document(preferenceManager.getString(Constants.KEY_USER_ID))
                                            .update(updates)
                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @Override
                                                public void onSuccess(Void aVoid) {
                                                    // Xử lý khi cập nhật thành công
                                                    preferenceManager.putString(Constants.KEY_IMAGE,uri.toString());
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
                    preferenceManager.putString(Constants.KEY_IMAGE,encodeImage);
                    Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);

                })
                .addOnFailureListener(e -> showMessage("loi"+e.getMessage()));
    }
    private String encodeImage(Bitmap bitmap){
        int previewWidth = 150;
        int previewHeight = bitmap.getHeight() * previewWidth / bitmap.getWidth();
        Bitmap previewBitmap = Bitmap.createScaledBitmap(bitmap,previewWidth,previewHeight,false);
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        previewBitmap.compress(Bitmap.CompressFormat.JPEG,50,byteArrayOutputStream);
        byte[] bytes = byteArrayOutputStream.toByteArray();
        return Base64.encodeToString(bytes,Base64.DEFAULT);
    }
    private  final ActivityResultLauncher<Intent> pickImage = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if(result.getResultCode() == RESULT_OK){
                    if(result.getData() != null){
                        Uri imageUri = result.getData().getData();
                        uriImage = imageUri;
                        try {
                            InputStream inputStream = getContentResolver().openInputStream(imageUri);
                            Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                            binding.imageProfile.setImageBitmap(bitmap);
                            encodeImage = encodeImage(bitmap);

                        }catch (FileNotFoundException e){
                            e.printStackTrace();
                        }
                    }
                }
            }
    );

}