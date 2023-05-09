// Generated by view binder compiler. Do not edit!
package com.pupu.pu1611.databinding;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewbinding.ViewBinding;
import androidx.viewbinding.ViewBindings;
import com.pupu.pu1611.R;
import java.lang.NullPointerException;
import java.lang.Override;
import java.lang.String;

public final class FragmentCommentBinding implements ViewBinding {
  @NonNull
  private final ConstraintLayout rootView;

  @NonNull
  public final ImageView imgExit;

  @NonNull
  public final EditText inputMessage;

  @NonNull
  public final FrameLayout layoutSend;

  @NonNull
  public final RecyclerView rcComment;

  @NonNull
  public final TextView textView8;

  @NonNull
  public final View view15;

  @NonNull
  public final View view9;

  private FragmentCommentBinding(@NonNull ConstraintLayout rootView, @NonNull ImageView imgExit,
      @NonNull EditText inputMessage, @NonNull FrameLayout layoutSend,
      @NonNull RecyclerView rcComment, @NonNull TextView textView8, @NonNull View view15,
      @NonNull View view9) {
    this.rootView = rootView;
    this.imgExit = imgExit;
    this.inputMessage = inputMessage;
    this.layoutSend = layoutSend;
    this.rcComment = rcComment;
    this.textView8 = textView8;
    this.view15 = view15;
    this.view9 = view9;
  }

  @Override
  @NonNull
  public ConstraintLayout getRoot() {
    return rootView;
  }

  @NonNull
  public static FragmentCommentBinding inflate(@NonNull LayoutInflater inflater) {
    return inflate(inflater, null, false);
  }

  @NonNull
  public static FragmentCommentBinding inflate(@NonNull LayoutInflater inflater,
      @Nullable ViewGroup parent, boolean attachToParent) {
    View root = inflater.inflate(R.layout.fragment_comment, parent, false);
    if (attachToParent) {
      parent.addView(root);
    }
    return bind(root);
  }

  @NonNull
  public static FragmentCommentBinding bind(@NonNull View rootView) {
    // The body of this method is generated in a way you would not otherwise write.
    // This is done to optimize the compiled bytecode for size and performance.
    int id;
    missingId: {
      id = R.id.imgExit;
      ImageView imgExit = ViewBindings.findChildViewById(rootView, id);
      if (imgExit == null) {
        break missingId;
      }

      id = R.id.inputMessage;
      EditText inputMessage = ViewBindings.findChildViewById(rootView, id);
      if (inputMessage == null) {
        break missingId;
      }

      id = R.id.layoutSend;
      FrameLayout layoutSend = ViewBindings.findChildViewById(rootView, id);
      if (layoutSend == null) {
        break missingId;
      }

      id = R.id.rc_comment;
      RecyclerView rcComment = ViewBindings.findChildViewById(rootView, id);
      if (rcComment == null) {
        break missingId;
      }

      id = R.id.textView8;
      TextView textView8 = ViewBindings.findChildViewById(rootView, id);
      if (textView8 == null) {
        break missingId;
      }

      id = R.id.view15;
      View view15 = ViewBindings.findChildViewById(rootView, id);
      if (view15 == null) {
        break missingId;
      }

      id = R.id.view9;
      View view9 = ViewBindings.findChildViewById(rootView, id);
      if (view9 == null) {
        break missingId;
      }

      return new FragmentCommentBinding((ConstraintLayout) rootView, imgExit, inputMessage,
          layoutSend, rcComment, textView8, view15, view9);
    }
    String missingId = rootView.getResources().getResourceName(id);
    throw new NullPointerException("Missing required view with ID: ".concat(missingId));
  }
}
