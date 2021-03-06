package com.demo.demochatapp.adapters;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.demo.demochatapp.R;
import com.demo.demochatapp.dbHelper.DBHelper;
import com.demo.demochatapp.models.ChatItemModel;
import com.demo.demochatapp.utilities.SharedPrefs;

import java.util.List;

public class ChatRoomRecyclerAdapter extends RecyclerView.Adapter {
    private Context context;
    private List<ChatItemModel> chatItemModelList;

    private static final int VIEW_TYPE_MESSAGE_SENT = 1;
    private static final int VIEW_TYPE_MESSAGE_RECEIVED = 2;
    private static final int VIEW_TYPE_ALERT = 3;

    private SharedPrefs sharedPrefs;
    private DBHelper dbHelper;
    private SQLiteDatabase db;


    public ChatRoomRecyclerAdapter(Context context, List<ChatItemModel> chatItemModelList) {
        this.context = context;
        this.chatItemModelList = chatItemModelList;
        sharedPrefs = new SharedPrefs(context);
        dbHelper = new DBHelper(context);
        db = dbHelper.getReadableDatabase();
    }

    @Override
    public int getItemViewType(int position) {
        ChatItemModel chatItemModel = chatItemModelList.get(position);

        if (chatItemModel.getContentType().equalsIgnoreCase("message")) {
            if (chatItemModel.getSender().equalsIgnoreCase(sharedPrefs.getUsername())) {
                return VIEW_TYPE_MESSAGE_SENT;
            }else {
                return VIEW_TYPE_MESSAGE_RECEIVED;
            }
        } else if (chatItemModel.getContentType().equalsIgnoreCase("alert")) {
            return VIEW_TYPE_ALERT;
        } else {
            return 0;
        }

    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view;

        if (viewType == VIEW_TYPE_MESSAGE_SENT) {
            view = LayoutInflater.from(context).inflate(R.layout.card_design_chat_item_sender, parent, false);
            return new ChatSentViewHolder(view);
        }else if(viewType == VIEW_TYPE_MESSAGE_RECEIVED){
            view = LayoutInflater.from(context).inflate(R.layout.card_design_chat_item_received, parent, false);
            return new ChatReceivedViewHolder(view);
        } else {
            view = LayoutInflater.from(context).inflate(R.layout.card_design_alert_messages, parent, false);
            return new ChatAlertVewHolder(view);
        }


//        View view = LayoutInflater.from(context).inflate(R.layout.card_design_chat_item, parent, false);
//        return new ChatViewHolder(view);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        ChatItemModel current = chatItemModelList.get(position);
//        Log.e("current", current.getMessage());

        switch (holder.getItemViewType()) {
            case VIEW_TYPE_MESSAGE_SENT:
                ((ChatSentViewHolder) holder).chatContent.setText(current.getMessage());
                ((ChatSentViewHolder) holder).sentDateTime.setText(current.getSentDateTime());

                Cursor cursor = db.rawQuery("SELECT * FROM " + ChatItemModel.TABLE_NAME + " WHERE "
                        + ChatItemModel.KEY_MESSAGE_ID + "=" + current.getMessageID(), null);
                cursor.moveToFirst();

                if (cursor.getInt(cursor.getColumnIndex(ChatItemModel.KEY_IS_MESSAGE_SENT_SUCCESSFULLY)) == 1) {
                    ((ChatSentViewHolder) holder).statusPending.setVisibility(View.GONE);
                    ((ChatSentViewHolder) holder).statusSent.setVisibility(View.VISIBLE);
                }else {
                    ((ChatSentViewHolder) holder).statusPending.setVisibility(View.VISIBLE);
                    ((ChatSentViewHolder) holder).statusSent.setVisibility(View.GONE);
                }

                break;

            case VIEW_TYPE_MESSAGE_RECEIVED:
                ((ChatReceivedViewHolder) holder).senderName.setText(current.getSender().substring(0, current.getSender().lastIndexOf("_")));
                ((ChatReceivedViewHolder) holder).chatContent.setText(current.getMessage());
                ((ChatReceivedViewHolder) holder).sentDateTime.setText(current.getSentDateTime());
                break;

            case VIEW_TYPE_ALERT:
                ((ChatAlertVewHolder) holder).alertMessage.setText(current.getMessage());
                break;
        }

    }


    @Override
    public int getItemCount() {
        return chatItemModelList.size();
    }


    public class ChatSentViewHolder extends RecyclerView.ViewHolder {
        TextView chatContent;
        TextView sentDateTime;

        ImageView statusPending;
        ImageView statusSent;

        public ChatSentViewHolder(View itemView) {
            super(itemView);

            chatContent = (TextView) itemView.findViewById(R.id.sentText);
            sentDateTime = (TextView) itemView.findViewById(R.id.sentDateTime);

            statusPending = (ImageView) itemView.findViewById(R.id.messageSentStatus_Awaited);
            statusSent = (ImageView) itemView.findViewById(R.id.messageSentStatus_Sent);
        }
    }

    public class ChatReceivedViewHolder extends RecyclerView.ViewHolder {

        TextView senderName;
        TextView chatContent;
        TextView sentDateTime;

        public ChatReceivedViewHolder(View itemView) {
            super(itemView);

            senderName = (TextView) itemView.findViewById(R.id.senderName);
            chatContent = (TextView) itemView.findViewById(R.id.sentText);
            sentDateTime = (TextView) itemView.findViewById(R.id.sentDateTime);
        }
    }

    public class ChatAlertVewHolder extends RecyclerView.ViewHolder {
        TextView alertMessage;

        public ChatAlertVewHolder(View itemView) {
            super(itemView);

            alertMessage = (TextView) itemView.findViewById(R.id.chatAlertMessage);
        }
    }



}
