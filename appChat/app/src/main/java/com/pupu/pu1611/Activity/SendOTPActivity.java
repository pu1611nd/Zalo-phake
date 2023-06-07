package com.pupu.pu1611.Activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.google.firebase.FirebaseException;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
import com.pupu.pu1611.R;
import com.pupu.pu1611.databinding.ActivitySendOtpactivityBinding;

import java.util.concurrent.TimeUnit;

public class SendOTPActivity extends AppCompatActivity {
    private ActivitySendOtpactivityBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySendOtpactivityBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.buttonGetOTP.setOnClickListener(v -> {
            if(binding.inputMobile.getText().toString().trim().isEmpty()){
                Toast.makeText(this, "Enter phone number", Toast.LENGTH_SHORT).show();
                return;
            }else {
                binding.progressBar.setVisibility(View.VISIBLE);
                binding.buttonGetOTP.setVisibility(View.INVISIBLE);
                PhoneAuthProvider.getInstance().verifyPhoneNumber(
                        "+84"+binding.inputMobile.getText().toString(),
                                    60,
                                    TimeUnit.SECONDS,
                                    SendOTPActivity.this,
                                    new PhoneAuthProvider.OnVerificationStateChangedCallbacks(){
                                        @Override
                                        public void onVerificationCompleted(@NonNull PhoneAuthCredential phoneAuthCredential) {
                                            binding.progressBar.setVisibility(View.GONE);
                                            binding.buttonGetOTP.setVisibility(View.VISIBLE);
                                        }

                                        @Override
                                        public void onVerificationFailed(@NonNull FirebaseException e) {
                                            binding.progressBar.setVisibility(View.GONE);
                                            binding.buttonGetOTP.setVisibility(View.VISIBLE);
                                            Toast.makeText(SendOTPActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                                        }

                                        @Override
                                        public void onCodeSent(@NonNull String verificationId, @NonNull PhoneAuthProvider.ForceResendingToken forceResendingToken) {
                                            binding.progressBar.setVisibility(View.GONE);
                                            binding.buttonGetOTP.setVisibility(View.VISIBLE);
                                            Intent intent = new Intent(getApplicationContext(),VerityOTPActivity.class);
                                            intent.putExtra("mobile",binding.inputMobile.getText().toString());
                                            intent.putExtra("verificationId",verificationId);
                                            startActivity(intent);
                                        }
                                    }

                );

            }
        });

    }
}