package com.pupu.pu1611.Fragment;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.SetOptions;
import com.pupu.pu1611.Adapter.CommentAdapter;
import com.pupu.pu1611.R;
import com.pupu.pu1611.models.Comment;
import com.pupu.pu1611.models.Follower;
import com.pupu.pu1611.models.Notification;
import com.pupu.pu1611.models.Post;
import com.pupu.pu1611.models.Story;
import com.pupu.pu1611.utilities.Constants;
import com.pupu.pu1611.utilities.PreferenceManager;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class Fragment_Comment extends DialogFragment {

    private ImageView imgExit;
    private RecyclerView rcListComment;
    private EditText inputComment;
    private FrameLayout layoutSend;

    private ArrayList<Comment> list;
    private CommentAdapter adapter;
    private FirebaseFirestore database;
    private PreferenceManager preferenceManager;

    private String postId;
    private String postBy;

    public static Fragment_Comment newInstance(String postId, String postBy ) {
        Fragment_Comment fragment = new Fragment_Comment();
        Bundle args = new Bundle();
        args.putString("postId", postId);
        args.putString("postBy", postBy);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NORMAL, android.R.style.Theme_Black_NoTitleBar_Fullscreen);
        Bundle args = getArguments();
        if (args != null) {
            postId = args.getString("postId");
            postBy = args.getString("postBy");
        }
    }


    private View view;
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_comment,container,false);
        imgExit = view.findViewById(R.id.imgExit);
        rcListComment = view.findViewById(R.id.rc_comment);
        inputComment = view.findViewById(R.id.inputMessage);
        layoutSend = view.findViewById(R.id.layoutSend);
        preferenceManager = new PreferenceManager(getContext());
        list = new ArrayList<>();
        adapter = new CommentAdapter(list,getContext());
        database = FirebaseFirestore.getInstance();
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext(),LinearLayoutManager.VERTICAL,false);
        rcListComment.setLayoutManager(linearLayoutManager);
        rcListComment.setNestedScrollingEnabled(false);
        rcListComment.setAdapter(adapter);

        imgExit.setOnClickListener(v->{
            dismiss();
        });
        layoutSend.setOnClickListener(v->{
            Comment comment = new Comment();
            comment.setCommentBody(inputComment.getText().toString().trim());
            comment.setCommentedAt(new Date().getTime());
            comment.setCommentedBy(preferenceManager.getString(Constants.KEY_USER_ID));
            Map<String, Object> updates = new HashMap<>();
            updates.put("comment", FieldValue.arrayUnion(comment));
            database.collection(Constants.KEY_COLLECTION_POST)
                    .document(postId)
                    .update(updates)
                    .addOnSuccessListener(aVoid -> {
                        // Xử lý khi cập nhật thành công
                        Notification notification = new Notification();
                        notification.setNotificationBy(preferenceManager.getString(Constants.KEY_USER_ID));
                        notification.setNotificationAt(new Date().getTime());
                        notification.setPostId(postId);
                        notification.setPostedBy(postBy);
                        notification.setType("comment");

                        UUID uuid = UUID.randomUUID();
                        Map<String, Object> update = new HashMap<>();
                        update.put(uuid.toString(),notification);
                        database.collection(Constants.KEY_COLLECTION_NOTIFICATION)
                                .document(postBy)
                                .set(update, SetOptions.merge())
                                .addOnSuccessListener(documentReference -> {

                                }).addOnFailureListener(e -> {});
                    })
                    .addOnFailureListener(e -> {
                        // Xử lý khi cập nhật thất bại
                    });
        });

        getComment();

        return view;

    }

    private void getComment() {
        DocumentReference postRef = database.collection(Constants.KEY_COLLECTION_POST)
                .document(postId);

        postRef.addSnapshotListener((value, error) -> {
            if (error != null) {
                // Xử lý lỗi
                return;
            }

            List<HashMap<String, Object>> commentData = (List<HashMap<String, Object>>) value.get("comment");
            List<Comment> comments = new ArrayList<>();
            if(commentData != null){
                for (HashMap<String, Object> commentHashMap : commentData) {
                    Comment comment = new Comment();
                    comment.setCommentBody(((String) commentHashMap.get("commentBody")));
                    comment.setCommentedBy(((String) commentHashMap.get("commentedBy")));
                    comment.setCommentedAt(((long) commentHashMap.get("commentedAt")));
                    comments.add(comment);
                }


                // Xóa list hiện tại và thêm danh sách comment vào list
                list.clear();
                list.addAll(comments);

                // Báo cho adapter biết rằng dữ liệu đã thay đổi
                adapter.notifyDataSetChanged();
            }

        });



    }
}
