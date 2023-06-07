package com.pupu.pu1611.Activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.iid.FirebaseInstanceIdReceiver;
import com.google.firebase.iid.internal.FirebaseInstanceIdInternal;
import com.google.firebase.messaging.FirebaseMessaging;
import com.pupu.pu1611.R;
import com.pupu.pu1611.databinding.ActivityOutgoingInvitationBinding;
import com.pupu.pu1611.models.User;
import com.pupu.pu1611.network.ApiClient;
import com.pupu.pu1611.network.ApiService;
import com.pupu.pu1611.utilities.Constants;
import com.pupu.pu1611.utilities.PreferenceManager;
import com.squareup.picasso.Picasso;

import org.jitsi.meet.sdk.JitsiMeetActivity;
import org.jitsi.meet.sdk.JitsiMeetConferenceOptions;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.URL;
import java.util.UUID;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class OutgoingInvitationActivity extends AppCompatActivity {
    private ActivityOutgoingInvitationBinding binding;
    private PreferenceManager preferenceManager;
    private String inviterToken = null;
    private String meetingRoom = null;
    private String meetingType = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityOutgoingInvitationBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        preferenceManager = new PreferenceManager(getApplicationContext());


        meetingType = getIntent().getStringExtra("type");
        if(meetingType != null){
            if(meetingType.equals("video")){
                binding.imageMeetingType.setImageResource(R.drawable.ic_round_videocam_24);
            }else{
                binding.imageMeetingType.setImageResource(R.drawable.ic_round_call_24);
            }

        }

        User user = (User) getIntent().getSerializableExtra("user");
        if(user != null){
            Picasso.get().load(user.getImage()).placeholder(R.drawable.teest).into(binding.imageProfile);
            binding.textUserName.setText(user.getFirstName()+user.getLastName());

        }

        binding.imageStopinvitation.setOnClickListener(v -> {
            if (user != null){
                cancelInvitation(user.getToken());
                finish();
            }
        });


        if(meetingType != null && user != null){
            initiateMeeting(meetingType,user.getToken());
        }


    }



    private void initiateMeeting(String meetingType ,String receiverToken){
        FirebaseMessaging.getInstance().getToken().addOnCompleteListener(task -> {
            if(task.isSuccessful() && task.getResult() != null){
                inviterToken = task.getResult();
            }

        try {

            JSONArray tokens = new JSONArray();
            tokens.put(receiverToken);

            JSONObject body = new JSONObject();
            JSONObject data = new JSONObject();

            data.put(Constants.REMOTE_MSG_TYPE,Constants.REMOTE_MSG_INVITATION);
            data.put(Constants.REMOTE_MSG_MEETING_TYPE, meetingType);
            data.put(Constants.KEY_FIRST_NAME,preferenceManager.getString(Constants.KEY_FIRST_NAME));
            data.put(Constants.KEY_LAST_NAME,preferenceManager.getString(Constants.KEY_LAST_NAME));
            data.put(Constants.KEY_EMAIL,preferenceManager.getString(Constants.KEY_EMAIL));
            data.put(Constants.KEY_IMAGE,preferenceManager.getString(Constants.KEY_IMAGE));
            data.put(Constants.REMOTE_SMG_INVITER_TOKEN,inviterToken);

            meetingRoom = preferenceManager.getString(Constants.KEY_USER_ID) +"_"+
                    UUID.randomUUID().toString().substring(0,5);
            data.put(Constants.REMOTE_MSG_MEETING_ROOM,meetingRoom);

            body.put(Constants.REMOTE_MSG_DATA, data);
            body.put(Constants.REMOTE_MSG_REGISTRATION_IDS,tokens);
            sendRemoteMessage(body.toString(),Constants.REMOTE_MSG_INVITATION);

        }catch (Exception e){
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
            finish();
        }
        });
    }

    private void sendRemoteMessage(String remoteMessageBody,String type){
        ApiClient.getClient().create(ApiService.class).sendRemoteMessage(
                Constants.getRemoteMessageHeaders(),remoteMessageBody
        ).enqueue(new Callback<String>() {
            @Override
            public void onResponse(@NonNull Call<String> call,@NonNull Response<String> response) {
                if(response.isSuccessful()){
                    //if(type.equals(Constants.REMOTE_MSG_INVITATION)){
                        //Toast.makeText(OutgoingInvitationActivity.this, "gui thanh cong", Toast.LENGTH_SHORT).show();
                   // }
                    if(response.body() != null){
                        try {
                            JSONObject responseJson = new JSONObject(response.body());
                            JSONArray results = responseJson.getJSONArray("results");
                            if(responseJson.getInt("failure") == 1){
                                JSONObject error = (JSONObject) results.get(0);
                                Toast.makeText(OutgoingInvitationActivity.this,error.getString("error"), Toast.LENGTH_SHORT).show();
                                return;
                            }

                        }catch (JSONException e){
                            e.printStackTrace();
                        }
                        Toast.makeText(OutgoingInvitationActivity.this, "gui thanh cong", Toast.LENGTH_SHORT).show();
                    }

                }else{
                    Toast.makeText(OutgoingInvitationActivity.this, response.message(), Toast.LENGTH_SHORT).show();
                    finish();
                }
            }

            @Override
            public void onFailure(@NonNull Call<String> call,@NonNull Throwable t) {
                Toast.makeText(OutgoingInvitationActivity.this, t.getMessage(), Toast.LENGTH_SHORT).show();
                finish();
            }
        });

    }
    private void cancelInvitation(String receiverToken){
        try {
            JSONArray tokens = new JSONArray();
            tokens.put(receiverToken);

            JSONObject body = new JSONObject();
            JSONObject data = new JSONObject();

            data.put(Constants.REMOTE_MSG_TYPE,Constants.REMOTE_MSG_INVITATION_RESPONSE);
            data.put(Constants.REMOTE_MSG_INVITATION_RESPONSE,Constants.REMOTE_MSG_INVITATION_CANCELLED);

            body.put(Constants.REMOTE_MSG_DATA,data);
            body.put(Constants.REMOTE_MSG_REGISTRATION_IDS,tokens);

            sendRemoteMessage(body.toString(),Constants.REMOTE_MSG_INVITATION_CANCELLED);

        }catch (Exception e){
            Toast.makeText(this,e.getMessage(), Toast.LENGTH_SHORT).show();
            finish();
        }

    }

    private BroadcastReceiver invitationResponseReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String type = intent.getStringExtra(Constants.REMOTE_MSG_INVITATION_RESPONSE);
            if(type != null){
                if(type.equals(Constants.REMOTE_MSG_INVITATION_ACCEPTED)){
                   try {
                       URL serverURL = new URL("https://meet.jit.si");
                       JitsiMeetConferenceOptions.Builder builder = new JitsiMeetConferenceOptions.Builder();
                       builder.setServerURL(serverURL);
                       builder.setWelcomePageEnabled(false);
                       builder.setRoom(meetingRoom);
                       if(meetingType.equals("audio")){
                           builder.setVideoMuted(true);
                       }
                       JitsiMeetActivity.launch(OutgoingInvitationActivity.this,builder.build());
                       finish();

                   }catch (Exception exception){
                       Toast.makeText(context, exception.getMessage(), Toast.LENGTH_SHORT).show();
                       finish();
                   }
                }else if(type.equals(Constants.REMOTE_MSG_INVITATION_REJECTED)){
                    Toast.makeText(context, "tu choi cuoc goi", Toast.LENGTH_SHORT).show();
                    finish();
                }
            }
        }
    };

    @Override
    protected void onStart() {
        super.onStart();
        LocalBroadcastManager.getInstance(getApplicationContext()).registerReceiver(
                invitationResponseReceiver,
                new IntentFilter(Constants.REMOTE_MSG_INVITATION_RESPONSE)
        );
    }

    @Override
    protected void onStop() {
        super.onStop();
        LocalBroadcastManager.getInstance(getApplicationContext()).unregisterReceiver(
                invitationResponseReceiver
        );
    }
}