package com.pupu.pu1611.Activity;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;

import com.pupu.pu1611.R;
import com.pupu.pu1611.databinding.ActivityUserProfileBinding;
import com.pupu.pu1611.models.User;
import com.pupu.pu1611.utilities.Constants;
import com.squareup.picasso.Picasso;

public class UserProfileActivity extends AppCompatActivity {
    private ActivityUserProfileBinding binding;
    private User user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityUserProfileBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        user = (User) getIntent().getSerializableExtra(Constants.KEY_USER);

        Picasso.get().load(user.getImage()).placeholder(R.drawable.image_svgrepo_com).into(binding.profileImage);
        binding.userNameP.setText(user.getFirstName()+" "+user.getLastName());
        Picasso.get().load(user.getImage_cover()).placeholder(R.drawable.image_svgrepo_com).into(binding.coverPhoto);
        binding.job.setText(user.getEmail());

        binding.commentView.setOnClickListener(v->{
            Intent intent = new Intent(getApplicationContext(),ChatActivity.class);
            intent.putExtra(Constants.KEY_USER, user);
            startActivity(intent);
        });

    }
}