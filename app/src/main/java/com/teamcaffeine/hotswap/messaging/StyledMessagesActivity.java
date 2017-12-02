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

import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.stfalcon.chatkit.messages.MessageHolders;
import com.stfalcon.chatkit.messages.MessageInput;
import com.stfalcon.chatkit.messages.MessagesList;
import com.stfalcon.chatkit.messages.MessagesListAdapter;
import com.stfalcon.chatkit.utils.DateFormatter;
import com.teamcaffeine.hotswap.R;
import com.teamcaffeine.hotswap.messaging.holder.messages.CustomIncomingTextMessageViewHolder;
import com.teamcaffeine.hotswap.messaging.holder.messages.CustomOutcomingTextMessageViewHolder;
import com.teamcaffeine.hotswap.messaging.models.Message;
import com.teamcaffeine.hotswap.messaging.models.SimpleMessage;
import com.teamcaffeine.hotswap.messaging.models.User;

import java.util.Date;

/**
 * @author agrawroh
 * @version v1.0
 */
public class StyledMessagesActivity extends MessagesActivity implements MessageInput.InputListener,
        MessageInput.AttachmentsListener, DateFormatter.Formatter {
    public static MessagesList messagesList;
    public static DatabaseReference userRef;
    private Handler mHandler;

    @Override
    public void onBackPressed() {
        killSilently();
        finish();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        killSilently();
        super.onSaveInstanceState(outState);
    }

    /**
     * Kill Silently
     */
    private void killSilently() {
        /* Remove Callbacks */
        mHandler.removeCallbacks(mStatusChecker);

        /* Online Status */
        if (null != userRef) {
            userRef.removeValue();
            userRef = null;
        }

        /* Active Listener */
        if (null != activeListener) {
            FirebaseDatabase.getInstance()
                    .getReference().child("chats").child("active").child(subscription)
                    .child(channel).removeEventListener(activeListener);
            activeListener = null;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_styled_messages);

        /* Set Online */
        userRef = FirebaseDatabase.getInstance().getReference().child("presence")
                .child("actual").child(FirebaseAuth.getInstance().getCurrentUser().getEmail().replace(".", "|"));
        FirebaseDatabase.getInstance().getReference(".info/connected")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()) {
                            if (null != userRef) {
                                userRef.onDisconnect().removeValue();
                                userRef.setValue(true);
                            }
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        /* Do Nothing */
                    }
                });

        /* Get Bundle Information */
        Bundle intentBundle = getIntent().getExtras();
        this.channel = intentBundle.getString("channel").replace(".", "|");
        this.subscription = intentBundle.getString("subscription").replace(".", "|");
        getUser(this.channel, this.subscription);

        /* Set Message Adapter */
        messagesList = findViewById(R.id.messagesList);
        initAdapter();

        /* Set Message Listener */
        MessageInput input = findViewById(R.id.input);
        input.setInputListener(this);
        input.setAttachmentsListener(this);

        /* Update Online Status */
        mHandler = new Handler();
        mStatusChecker.run();
    }

    /**
     * Retrieve User State
     */
    public class UpdateUserState extends AsyncTask<Void, Void, Void> {
        /**
         * Perform In Background
         *
         * @param voids Void
         */
        protected Void doInBackground(Void... voids) {
            if (users.size() > 0) {
                FirebaseDatabase.getInstance().getReference().child("presence").child("actual")
                        .child(StyledMessagesActivity.this.subscription)
                        .addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                if (dataSnapshot.exists()) {
                                    users.get(StyledMessagesActivity.this.subscription).setOnline(true);
                                    for (int i = 0; i < activeMessagesList.size(); i++) {
                                        Message message = activeMessagesList.remove(0);
                                        message.setStatus("Read");
                                        messagesAdapter.update(message);
                                    }
                                } else {
                                    FirebaseDatabase.getInstance().getReference().child("presence")
                                            .child(StyledMessagesActivity.this.subscription)
                                            .addListenerForSingleValueEvent(new ValueEventListener() {
                                                @Override
                                                public void onDataChange(DataSnapshot dataSnapshot) {
                                                    if (dataSnapshot.exists()) {
                                                        users.get(StyledMessagesActivity.this.subscription).setOnline(false);
                                                        for (int i = 0; i < activeMessagesList.size(); i++) {
                                                            Message message = activeMessagesList.get(i);
                                                            message.setStatus("Delivered");
                                                            messagesAdapter.update(message);
                                                        }
                                                    } else {
                                                        users.get(StyledMessagesActivity.this.subscription).setOnline(false);
                                                    }
                                                    messagesAdapter.notifyDataSetChanged();
                                                }

                                                @Override
                                                public void onCancelled(DatabaseError databaseError) {
                                                    /* Do Nothing */
                                                }
                                            });
                                }
                                messagesAdapter.notifyDataSetChanged();
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {
                                /* Do Nothing */
                            }
                        });
            }
            return null;
        }
    }

    /* Update Online Status */
    Runnable mStatusChecker = new Runnable() {
        @Override
        public void run() {
            try {
                new UpdateUserState().execute().get();
            } catch (Exception ex) {
                ex.printStackTrace();
            } finally {
                mHandler.postDelayed(mStatusChecker, 2500);
            }
        }


    };

    @Override
    public boolean onSubmit(CharSequence input) {
        storeMessage(input.toString());
        final Message message = new Message(String.valueOf(new Date().getTime()), users.get(this.channel), input.toString());
        message.setStatus(users.get(StyledMessagesActivity.this.subscription).isOnline() ? "Read" : "Sent");
        message.getUser().setId("0");
        if (!users.get(StyledMessagesActivity.this.subscription).isOnline()) {
            activeMessagesList.add(message);
        }
        messagesAdapter.addToStart(message, true);
        messagesAdapter.notifyDataSetChanged();
        return true;
    }

    /**
     * Store Message
     *
     * @param message
     */
    private void storeMessage(final String message) {
        Log.e("MESSAGE_POST", "Storing Message...");
        FirebaseDatabase.getInstance().getReference(".info/serverTimeOffset").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    FirebaseDatabase.getInstance()
                            .getReference().child("chats").child("active").child(channel).child(subscription)
                            .push()
                            .setValue(new SimpleMessage(message, channel, (long) (System.currentTimeMillis() + snapshot.getValue(Double.class))));
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                /* Do Nothing */
            }
        });
    }

    @Override
    public void onAddAttachments() {
        //messagesAdapter.addToStart(MessagesFixtures.getImageMessage(), true);
        //messagesAdapter.notifyDataSetChanged();
    }

    @Override
    public String format(Date date) {
        if (DateFormatter.isToday(date)) {
            return "Today"; //TODO: change back to get string
        } else if (DateFormatter.isYesterday(date)) {
            return "Yesterday";
        } else {
            return DateFormatter.format(date, DateFormatter.Template.STRING_DAY_MONTH_YEAR);
        }
    }

    /**
     * Initialize Adapter
     */
    private void initAdapter() {
        MessageHolders holdersConfig = new MessageHolders()
                .setIncomingTextConfig(
                        CustomIncomingTextMessageViewHolder.class,
                        R.layout.item_custom_incoming_text_message)
                .setOutcomingTextConfig(
                        CustomOutcomingTextMessageViewHolder.class,
                        R.layout.item_custom_outcoming_text_message);
        super.messagesAdapter = new MessagesListAdapter<>(SENDER_ID, holdersConfig, super.imageLoader);
        super.messagesAdapter.enableSelectionMode(this);
        super.messagesAdapter.setLoadMoreListener(this);
        messagesList.setAdapter(super.messagesAdapter);
    }

    /**
     * Get Users
     */
    private void getUser(final String channel, final String subscription) {
        FirebaseDatabase.getInstance()
                .getReference().child("users").orderByChild("id").equalTo(channel.replace("|", "."))
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                            users.put(channel, postSnapshot.getValue(User.class));
                            FirebaseDatabase.getInstance()
                                    .getReference().child("users").orderByChild("id").equalTo(subscription.replace("|", "."))
                                    .addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(DataSnapshot dataSnapshot) {
                                            for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                                                users.put(subscription, postSnapshot.getValue(User.class));

                                                /* Action Bar Title */
                                                if (null != getSupportActionBar()) {
                                                    getSupportActionBar().setTitle(users.get(subscription).getName());
                                                }

                                                /* Load Initial Messages */
                                                loadInitialMessages();
                                            }
                                        }

                                        @Override
                                        public void onCancelled(DatabaseError databaseError) {
                                            /* Do Nothing */
                                        }
                                    });
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        /* Do Nothing */
                    }
                });
    }
}
