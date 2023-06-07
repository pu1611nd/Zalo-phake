package com.pupu.pu1611.Fragment;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.messaging.FirebaseMessaging;
import com.pupu.pu1611.Activity.OutgoingInvitationActivity;
import com.pupu.pu1611.Adapter.UserAdapter;
import com.pupu.pu1611.R;
import com.pupu.pu1611.listeners.UserListener;
import com.pupu.pu1611.models.User;
import com.pupu.pu1611.utilities.Constants;
import com.pupu.pu1611.utilities.PreferenceManager;

import java.util.ArrayList;
import java.util.List;

public class Fragment_Phone_Book extends Fragment implements UserListener {

    private RecyclerView recyclerView;

    private PreferenceManager preferenceManager;
    private List<User> users;
    private UserAdapter adapter;

    View view;
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_phone_book,container,false);

        preferenceManager = new PreferenceManager(getContext());
        recyclerView = view.findViewById(R.id.recyclerViewPhoneBook);
        getDataUser();
        return view;
    }
    private void showMessage (String message){
        Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
    }

    private void getDataUser() {
        users = new ArrayList<>();
        adapter = new UserAdapter(users,this);
        getUsers();
        recyclerView.setAdapter(adapter);

    }

    private  void getUsers(){
        FirebaseFirestore database = FirebaseFirestore.getInstance();
        database.collection(Constants.KEY_COLLECTION_USERS)
                .get()
                .addOnCompleteListener(task -> {
                    String myUserId = preferenceManager.getString(Constants.KEY_USER_ID);
                    if(task.isSuccessful() && task.getResult() !=null){
                        for(QueryDocumentSnapshot documentSnapshot : task.getResult()){
                            if(myUserId.equals(documentSnapshot.getId())){
                                continue;
                            }
                            User user = new User();
                            user.setImage(documentSnapshot.getString(Constants.KEY_IMAGE));
                            user.setImage_cover(documentSnapshot.getString(Constants.KEY_IMAGE_COVER));
                            user.setFirstName(documentSnapshot.getString(Constants.KEY_FIRST_NAME));
                            user.setLastName(documentSnapshot.getString(Constants.KEY_LAST_NAME));
                            user.setImage(documentSnapshot.getString(Constants.KEY_EMAIL));
                            user.setToken(documentSnapshot.getString(Constants.KEY_FCM_TOKEN));
                            users.add(user);
                        }
                        if(users.size() > 0){
                            adapter.notifyDataSetChanged();
                        }else{
                            showMessage("khong co ai");
                        }
                    }else{
                        showMessage("khong co ai");
                    }
                });
    }



    @Override
    public void initiateVideoMeeting(User user) {
        if(user.getToken() == null || user.getToken().trim().isEmpty()){
            showMessage("aaaa");
        }else{
            Intent intent = new Intent(getContext(), OutgoingInvitationActivity.class);
            intent.putExtra("user",user);
            intent.putExtra("type","video");
            startActivity(intent);
        }
    }

    @Override
    public void initiateAudioMeeting(User user) {
        if(user.getToken() == null || user.getToken().trim().isEmpty()){
            showMessage("bbbb");
        }else{
            Intent intent = new Intent(getContext(), OutgoingInvitationActivity.class);
            intent.putExtra("user",user);
            intent.putExtra("type","audio");
            startActivity(intent);
        }
    }



}
