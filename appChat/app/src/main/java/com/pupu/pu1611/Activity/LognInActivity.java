package com.pupu.pu1611.Activity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.pupu.pu1611.MainActivity;
import com.pupu.pu1611.R;

import com.pupu.pu1611.databinding.ActivityLognInBinding;
import com.pupu.pu1611.utilities.Constants;
import com.pupu.pu1611.utilities.PreferenceManager;

public class LognInActivity extends AppCompatActivity {
    private ActivityLognInBinding binding;
    private PreferenceManager preferenceManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLognInBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        preferenceManager = new PreferenceManager(getApplicationContext());
        if(preferenceManager.getBoolean(Constants.KEY_IS_SIGNED_IN)) {
            Intent intent = new Intent(getApplicationContext(),MainActivity.class);
            startActivity(intent);
            finish();
        }

        findViewById(R.id.textSignUp).setOnClickListener(v ->
                startActivity(new Intent(getApplicationContext(),LognUpActivity.class)));
        binding.buttonSignIn.setOnClickListener(v -> {
            if(binding.textInputEmail.getText().toString().trim().isEmpty()){
                showMessage("nhap email");
            }else if(!Patterns.EMAIL_ADDRESS.matcher(binding.textInputEmail.getText().toString()).matches()){
                showMessage("nhap dung dinh dang email");
            }else if(binding.textInputPassword.getText().toString().trim().isEmpty()){
                showMessage("nhap password");
            }else {
                signIn();
            }
        });

        binding.buttonSignInOTP.setOnClickListener(v ->{
            Intent intent = new Intent(getApplicationContext(),SendOTPActivity.class);
            startActivity(intent);
        });

    }

    private void signIn() {
        FirebaseFirestore database = FirebaseFirestore.getInstance();
        database.collection(Constants.KEY_COLLECTION_USERS)
                .whereEqualTo(Constants.KEY_EMAIL,binding.textInputEmail.getText().toString())
                .whereEqualTo(Constants.KEY_PASSWORD,binding.textInputPassword.getText().toString())
                .get()
                .addOnCompleteListener(task -> {
                    if(task.isSuccessful() && task.getResult() != null && task.getResult().getDocuments().size() > 0){
                        DocumentSnapshot documentSnapshot = task.getResult().getDocuments().get(0);
                        preferenceManager.putBoolean(Constants.KEY_IS_SIGNED_IN,true);
                        preferenceManager.putString(Constants.KEY_USER_ID,documentSnapshot.getId());
                        preferenceManager.putString(Constants.KEY_FIRST_NAME,documentSnapshot.getString(Constants.KEY_FIRST_NAME));
                        preferenceManager.putString(Constants.KEY_LAST_NAME,documentSnapshot.getString(Constants.KEY_LAST_NAME));
                        preferenceManager.putString(Constants.KEY_EMAIL,documentSnapshot.getString(Constants.KEY_EMAIL));
                        preferenceManager.putString(Constants.KEY_IMAGE,documentSnapshot.getString(Constants.KEY_IMAGE));
                        preferenceManager.putString(Constants.KEY_IMAGE_COVER,documentSnapshot.getString(Constants.KEY_IMAGE_COVER));
                        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                    }else{
                        showMessage("loi dang nhap");
                    }
                });
    }

    private void showMessage (String message){
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
    }
}