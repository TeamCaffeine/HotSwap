package com.teamcaffeine.hotswap.messaging;

/*
 Copyright (C) 2017 : Rohit Agrawal

 Permission is hereby granted, free of charge, to any person obtaining a copy of this software and
 associated documentation files (the "Software"), to deal in the Software without restriction,
 including without limitation the rights to use, copy, modify, merge, publish, distribute,
 sublicense, and/or sell copies of the Software, and to permit persons to whom the Software
 is furnished to do so, subject to the following conditions:

 The above copyright notice and this permission notice shall be included in all copies or
 substantial portions of the Software.

 THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING
 BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
 DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
*/

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;
import com.stfalcon.chatkit.commons.ImageLoader;
import com.stfalcon.chatkit.messages.MessagesListAdapter;
import com.teamcaffeine.hotswap.R;
import com.teamcaffeine.hotswap.messaging.models.Message;
import com.teamcaffeine.hotswap.messaging.models.SimpleMessage;
import com.teamcaffeine.hotswap.messaging.models.User;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * @author agrawroh
 * @version v1.0
 */
public abstract class MessagesActivity extends AppCompatActivity
        implements MessagesListAdapter.SelectionListener,
        MessagesListAdapter.OnLoadMoreListener {
    protected static final String SENDER_ID = "0";
    protected static final String RECEIVER_ID = "1";
    protected ImageLoader imageLoader;
    protected MessagesListAdapter<Message> messagesAdapter;
    protected String channel;
    protected String subscription;
    public static List<Message> activeMessagesList;
    public Map<String, User> users = new HashMap<>();
    public static ValueEventListener activeListener;
    private Menu menu;
    private int selectionCount;
    private Date lastLoadedDate;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        imageLoader = new ImageLoader() {
            @Override
            public void loadImage(ImageView imageView, String url) {
                Picasso.with(MessagesActivity.this).load(url).into(imageView);
            }
        };
        activeMessagesList = new ArrayList<>();
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        this.menu = menu;
        getMenuInflater().inflate(R.menu.chat_actions_menu, menu);
        onSelectionChanged(0);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_delete:
                messagesAdapter.deleteSelectedMessages();
                break;
            case R.id.action_copy:
                messagesAdapter.copySelectedMessagesText(this, getMessageStringFormatter(), true);
//                AppUtils.showToast(this, R.string.copied_message, true);
                break;
        }
        return true;
    }

    @Override
    public void onBackPressed() {
        if (selectionCount == 0) {
            super.onBackPressed();
        } else {
            messagesAdapter.unselectAllItems();
        }
    }

    @Override
    public void onLoadMore(int page, int totalItemsCount) {
        if (messagesAdapter.getItemCount() >= 25 && null != lastLoadedDate) {
            loadMessages(subscription + "-" + channel, lastLoadedDate.getTime() - 1, false);
        }
    }

    @Override
    public void onSelectionChanged(int count) {
        this.selectionCount = count;
        menu.findItem(R.id.action_delete).setVisible(count > 0);
        menu.findItem(R.id.action_copy).setVisible(count > 0);
    }

    /**
     * Load Initial Messages
     */
    protected void loadInitialMessages() {
        FirebaseDatabase.getInstance().getReference(".info/serverTimeOffset").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                if (null != snapshot) {
                    loadMessages(subscription + "-" + channel, System.currentTimeMillis() + snapshot.getValue(Double.class), false);
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                /* Do Nothing */
            }
        });
    }

    /**
     * Load Messages
     */
    private void loadMessages(final String path, final double timestamp, final boolean isExhausted) {
        if (users.size() > 0) {
            FirebaseDatabase.getInstance()
                    .getReference().child("chats").child("delivered").child(path).orderByChild("timestamp").endAt(timestamp)
                    .limitToLast(25).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if (!dataSnapshot.exists()) {
                        if (isExhausted) {
                            /* Attach Active Listener */
                            if (null == activeListener) {
                                attachActiveListener();
                            }
                            /* Previous Messages Exhausted, Return */
                            return;
                        } else {
                            /* Search Other Path */
                            loadMessages(channel + "-" + subscription, timestamp, true);
                        }
                    }

                    /* Normal Processing */
                    List<Message> messages = new ArrayList<>();
                    for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                        SimpleMessage simpleMessage = postSnapshot.getValue(SimpleMessage.class);
                        Message message = new Message(String.valueOf(simpleMessage.getTimestamp()), users.get(simpleMessage.getUser()), simpleMessage.getMessage(), new Date(simpleMessage.getTimestamp()));
                        message.setStatus("Read");
                        message.getUser().setId(channel.equalsIgnoreCase(simpleMessage.getUser()) ? SENDER_ID : RECEIVER_ID);
                        messages.add(0, message);
                    }
                    if (messages.size() > 0) {
                        lastLoadedDate = messages.get(messages.size() - 1).getCreatedAt();
                        messagesAdapter.addToEnd(messages, false);
                        messagesAdapter.notifyDataSetChanged();
                    }

                    /* Attach Active Listener */
                    if (null == activeListener) {
                        /* Check For Empty Channel Path */
                        attachActiveListener();
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    /* Do Nothing */
                }
            });
        }
    }

    /**
     * Formatter
     */
    private MessagesListAdapter.Formatter<Message> getMessageStringFormatter() {
        return new MessagesListAdapter.Formatter<Message>() {
            @Override
            public String format(Message message) {
                String createdAt = new SimpleDateFormat("MMM d, EEE 'at' h:mm a", Locale.getDefault())
                        .format(message.getCreatedAt());

                String text = message.getText();
                if (text == null) text = "[attachment]";

                return String.format(Locale.getDefault(), "%s: %s (%s)",
                        message.getUser().getName(), text, createdAt);
            }
        };
    }

    /**
     * Fetch Recent Messages
     */
    private void fetchRecentMessages() {
        FirebaseDatabase.getInstance()
                .getReference().child("chats").child("active").child(channel).child(subscription)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()) {
                            for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                                if (!postSnapshot.exists()) {
                                    return;
                                }
                                SimpleMessage simpleMessage = postSnapshot.getValue(SimpleMessage.class);
                                Message message = new Message(String.valueOf(simpleMessage.getTimestamp()), users.get(simpleMessage.getUser()), simpleMessage.getMessage(), new Date(simpleMessage.getTimestamp()));
                                message.setStatus("Sent");
                                message.getUser().setId(channel.equalsIgnoreCase(simpleMessage.getUser()) ? SENDER_ID : RECEIVER_ID);
                                activeMessagesList.add(message);
                                messagesAdapter.addToStart(message, true);
                                messagesAdapter.notifyDataSetChanged();
                            }
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        /* Do Nothing */
                    }
                });
    }

    /**
     * Attach Active Listener
     */
    private void attachActiveListener() {
        activeListener = FirebaseDatabase.getInstance()
                .getReference().child("chats").child("active").child(subscription).child(channel)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(final DataSnapshot mainDataSnapshot) {
                        if (mainDataSnapshot.exists()) {
                            for (final DataSnapshot postSnapshot : mainDataSnapshot.getChildren()) {
                                if (!postSnapshot.exists()) {
                                    return;
                                }
                                FirebaseDatabase.getInstance()
                                        .getReference().child("chats").child("delivered").child(channel + "-" + subscription)
                                        .addListenerForSingleValueEvent(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(DataSnapshot dataSnapshot) {
                                                if (!dataSnapshot.exists()) {
                                                    FirebaseDatabase.getInstance()
                                                            .getReference().child("chats").child("delivered").child(subscription + "-" + channel)
                                                            .addListenerForSingleValueEvent(new ValueEventListener() {
                                                                @Override
                                                                public void onDataChange(DataSnapshot dataSnapshot) {
                                                                    SimpleMessage simpleMessage = postSnapshot.getValue(SimpleMessage.class);
                                                                    FirebaseDatabase.getInstance()
                                                                            .getReference().child("chats").child("delivered").child(subscription + "-" + channel)
                                                                            .push()
                                                                            .setValue(simpleMessage);
                                                                    Message message = new Message(String.valueOf(simpleMessage.getTimestamp()), users.get(simpleMessage.getUser()), simpleMessage.getMessage(), new Date(simpleMessage.getTimestamp()));
                                                                    message.setStatus("Read");
                                                                    message.getUser().setId(channel.equalsIgnoreCase(simpleMessage.getUser()) ? SENDER_ID : RECEIVER_ID);
                                                                    messagesAdapter.addToStart(message, true);
                                                                    messagesAdapter.notifyDataSetChanged();
                                                                    StyledMessagesActivity.messagesList.scrollToPosition(0);
                                                                    mainDataSnapshot.getRef().removeValue();
                                                                }

                                                                @Override
                                                                public void onCancelled(DatabaseError databaseError) {
                                                                    /* Do Nothing */
                                                                }
                                                            });
                                                } else {
                                                    SimpleMessage simpleMessage = postSnapshot.getValue(SimpleMessage.class);
                                                    FirebaseDatabase.getInstance()
                                                            .getReference().child("chats").child("delivered").child(channel + "-" + subscription)
                                                            .push()
                                                            .setValue(simpleMessage);
                                                    Message message = new Message(String.valueOf(simpleMessage.getTimestamp()), users.get(simpleMessage.getUser()), simpleMessage.getMessage(), new Date(simpleMessage.getTimestamp()));
                                                    message.setStatus("Read");
                                                    message.getUser().setId(channel.equalsIgnoreCase(simpleMessage.getUser()) ? SENDER_ID : RECEIVER_ID);
                                                    messagesAdapter.addToStart(message, true);
                                                    messagesAdapter.notifyDataSetChanged();
                                                    StyledMessagesActivity.messagesList.scrollToPosition(0);
                                                    mainDataSnapshot.getRef().removeValue();
                                                }
                                            }

                                            @Override
                                            public void onCancelled(DatabaseError databaseError) {
                                                /* Do Nothing */
                                            }
                                        });
                            }
                        }
                        /* Fetch Recently Sent Messages */
                        fetchRecentMessages();
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        /* Do Nothing */
                    }
                });
    }
}
