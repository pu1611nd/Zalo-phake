package com.pupu.pu1611.Fragment;

import androidx.fragment.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;
import com.pupu.pu1611.Adapter.NotificationAdapter;
import com.pupu.pu1611.R;
import com.pupu.pu1611.models.Notification;
import com.pupu.pu1611.utilities.Constants;
import com.pupu.pu1611.utilities.PreferenceManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Fragment_Notification extends Fragment{
    private RecyclerView rcList;
    private ArrayList<Notification> list;
    private NotificationAdapter adapter;
    private FirebaseFirestore database;
    private PreferenceManager preferenceManager;

    private View view;
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_notification,container,false);
        rcList = view.findViewById(R.id.rc_notification);
        list = new ArrayList<>();
        adapter = new NotificationAdapter(list,getContext());
        preferenceManager = new PreferenceManager(getContext());
        database = FirebaseFirestore.getInstance();
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext(),LinearLayoutManager.VERTICAL,false);
        rcList.setNestedScrollingEnabled(false);
        rcList.setLayoutManager(linearLayoutManager);
        rcList.setAdapter(adapter);

        database.collection(Constants.KEY_COLLECTION_NOTIFICATION)
                .document(preferenceManager.getString(Constants.KEY_USER_ID))
                .addSnapshotListener((value, error) -> {
                    if (error != null) {
                        // Xử lý lỗi
                        return;
                    }

                    if (value.exists()) {
                        list.clear();
                        Map<String, Object> notificationData = value.getData();

                        // Lặp qua các thông báo và chuyển đổi sang đối tượng Notification
                        for (Map.Entry<String, Object> entry : notificationData.entrySet()) {
                            Notification notification = new Notification();
                            Map<String, Object> notificationMap = (Map<String, Object>) entry.getValue();
                            notification.setNotificationBy((String) notificationMap.get("notificationBy"));
                            notification.setNotificationAt((Long) notificationMap.get("notificationAt"));
                            notification.setPostId((String) notificationMap.get("postId"));
                            notification.setPostedBy((String) notificationMap.get("postedBy"));
                            notification.setType((String) notificationMap.get("type"));
                            list.add(notification);
                        }

                        // Cập nhật RecyclerView với danh sách thông báo mới
                        Log.d("AAAAA",list.size()+"");
                        adapter.notifyDataSetChanged();
                    } else {
                        // Không tìm thấy tài liệu
                    }
                });


        return view;

    }

}
