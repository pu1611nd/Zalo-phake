package com.pupu.pu1611.Adapter;

import android.graphics.Bitmap;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.pupu.pu1611.R;
import com.pupu.pu1611.databinding.ItemContainerReceivedMessageBinding;
import com.pupu.pu1611.databinding.ItemContainerSentMessageBinding;
import com.pupu.pu1611.models.ChatMessage;
import com.pupu.pu1611.utilities.Constants;
import com.squareup.picasso.Picasso;

import java.util.List;

public class ChatAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private final List<ChatMessage> chatMessages;
    private final String receiverProfileImage;
    private final String senderId;

    public static final int VIEW_TYPE_SENT = 1;
    public static final int VIEW_TYPE_RECEIVED = 2;

    public ChatAdapter(List<ChatMessage> chatMessages, String receiverProfileImage, String senderId) {
        this.chatMessages = chatMessages;
        this.receiverProfileImage = receiverProfileImage;
        this.senderId = senderId;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
      if(viewType == VIEW_TYPE_SENT) {
          return new SendMessageViewHolder(
               ItemContainerSentMessageBinding.inflate(
                       LayoutInflater.from(parent.getContext()),
                       parent,
                       false
               )
          );
      }else {
          return new ReceivedMessageViewHolder(
                  ItemContainerReceivedMessageBinding.inflate(
                          LayoutInflater.from(parent.getContext()),
                          parent,
                          false
                  )
          );
      }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if(getItemViewType(position) == VIEW_TYPE_SENT){
            ((SendMessageViewHolder)holder).setData(chatMessages.get(position));
        }else {
            ((ReceivedMessageViewHolder)holder).setData(chatMessages.get(position),receiverProfileImage);
        }
    }

    @Override
    public int getItemCount() {
        return chatMessages.size();
    }

    @Override
    public int getItemViewType(int position) {
        if(chatMessages.get(position).getSenderId().equals(senderId)){
            return VIEW_TYPE_SENT;
        }else {
            return VIEW_TYPE_RECEIVED;
        }
    }

    private class SendMessageViewHolder extends RecyclerView.ViewHolder{

        private final ItemContainerSentMessageBinding binding;
        SendMessageViewHolder(ItemContainerSentMessageBinding itemContainerSentMessageBinding){
            super(itemContainerSentMessageBinding.getRoot());
            binding = itemContainerSentMessageBinding;
        }
        void setData(ChatMessage chatMessage){
            binding.textMessage.setText(chatMessage.getMessage());
            binding.textDataTime.setText(chatMessage.getDateTime());
        }
    }

    private class ReceivedMessageViewHolder extends RecyclerView.ViewHolder{
        private final ItemContainerReceivedMessageBinding binding;

        ReceivedMessageViewHolder(ItemContainerReceivedMessageBinding itemContainerReceivedMessageBinding){
            super(itemContainerReceivedMessageBinding.getRoot());
            binding = itemContainerReceivedMessageBinding;
        }
        void setData(ChatMessage chatMessage, String receiverProfileImage){
            binding.textMessage.setText(chatMessage.getMessage());
            binding.textDataTime.setText(chatMessage.getDateTime());
            //binding.imageProfile.setImageBitmap(receiverProfileImage);
            Picasso.get().load(receiverProfileImage).placeholder(R.drawable.teest).into(binding.imageProfile);
        }
    }

}
