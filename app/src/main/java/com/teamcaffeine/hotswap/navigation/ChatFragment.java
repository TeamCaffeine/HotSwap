package com.teamcaffeine.hotswap.navigation;

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

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;
import com.stfalcon.chatkit.commons.ImageLoader;
import com.stfalcon.chatkit.dialogs.DialogsList;
import com.stfalcon.chatkit.dialogs.DialogsListAdapter;
import com.stfalcon.chatkit.utils.DateFormatter;
import com.teamcaffeine.hotswap.R;
import com.teamcaffeine.hotswap.messaging.StyledMessagesActivity;
import com.teamcaffeine.hotswap.messaging.holder.dialogs.CustomDialogViewHolder;
import com.teamcaffeine.hotswap.messaging.models.Channel;
import com.teamcaffeine.hotswap.messaging.models.Dialog;
import com.teamcaffeine.hotswap.messaging.models.Message;
import com.teamcaffeine.hotswap.messaging.models.SimpleMessage;
import com.teamcaffeine.hotswap.messaging.models.Subscriptions;
import com.teamcaffeine.hotswap.messaging.models.User;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

/**
 * @author agrawroh
 * @version v1.0
 */
public class ChatFragment extends Fragment implements DialogsListAdapter.OnDialogClickListener<Dialog>,
        DialogsListAdapter.OnDialogLongClickListener<Dialog>, DateFormatter.Formatter {
    private static final String CLASS_TAG = "Chat";
    private ImageLoader imageLoader;
    private DialogsListAdapter<Dialog> dialogsAdapter;
    private List<Dialog> dialogs = new ArrayList<>();
    private ProgressDialog progressDialog;
    private DatabaseReference userRef;
    private Handler mHandler;
    private DialogsList dialogsList;
    private ChatFragmentListener CFL;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_styled_dialogs, container, false);
        dialogsList = (DialogsList) view.findViewById(R.id.dialogsList);

      /* Load Animation */
//        progressDialog = new ProgressDialog(getActivity().getApplicationContext());
//        progressDialog.setMessage("Loading Chat(s)...");
//        progressDialog.show();

        /* Load Image */
        imageLoader = new ImageLoader() {
            @Override
            public void loadImage(ImageView imageView, String url) {
                Picasso.with(getActivity().getApplicationContext()).load(url).into(imageView);
            }
        };

        /* Set Online */
        userRef = FirebaseDatabase.getInstance().getReference().child("presence")
                .child(FirebaseAuth.getInstance().getCurrentUser().getEmail().replace(".", "|"));
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

        /* Load Dialogs */
        initAdapter();

        return view;
    }

    @Override
    public void onDialogLongClick(Dialog dialog) {
//        AppUtils.showToast(App.getAppContext(), dialog.getDialogName(), false);
    }

    @Override
    public void onDestroy() {
        killSilently();
        super.onDestroy();
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
            for (int i = 0; i < dialogs.size(); i++) {
                final Dialog dialog = dialogs.get(i);
                FirebaseDatabase.getInstance().getReference().child("presence").child(dialog.getUsers().get(0).getId().replace(".", "|")).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()) {
                            dialog.getUsers().get(0).setOnline(true);
                        } else {
                            dialog.getUsers().get(0).setOnline(false);
                        }
                        getDialog(dialog.getUsers().get(0).getId(), dialog);
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
    public void onDialogClick(Dialog dialog) {
        Intent intent = new Intent(getActivity().getApplicationContext(), StyledMessagesActivity.class);
        intent.putExtra("channel", FirebaseAuth.getInstance().getCurrentUser().getEmail());
        intent.putExtra("subscription", dialog.getUsers().get(0).getId());
        startActivity(intent);
    }

    @Override
    public String format(Date date) {
        if (DateFormatter.isToday(date)) {
            return DateFormatter.format(date, DateFormatter.Template.TIME);
        } else if (DateFormatter.isYesterday(date)) {
            return "Yesterday"; //TODO: getString
        } else if (DateFormatter.isCurrentYear(date)) {
            return DateFormatter.format(date, DateFormatter.Template.STRING_DAY_MONTH);
        } else {
            return DateFormatter.format(date, DateFormatter.Template.STRING_DAY_MONTH_YEAR);
        }
    }

    /**
     * Initialize Adapter
     */
    private void initAdapter() {
        dialogsAdapter = new DialogsListAdapter<>(R.layout.item_custom_dialog_view_holder,
                CustomDialogViewHolder.class, imageLoader);
        getDialogs();
        dialogsAdapter.setOnDialogClickListener(this);
        dialogsAdapter.setOnDialogLongClickListener(this);
        dialogsAdapter.setDatesFormatter(this);
        dialogsList.setAdapter(dialogsAdapter);
    }

    /**
     * Get Dialogs
     */
    private void getDialogs() {
        FirebaseDatabase.getInstance()
                .getReference().child("channels").orderByChild("channel").equalTo(FirebaseAuth.getInstance().getCurrentUser().getEmail())
                .limitToLast(1).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                    Channel channel = postSnapshot.getValue(Channel.class);
                    Subscriptions subscriptions = channel.getSubscriptions();
                    for (final String subscriptionChannel : subscriptions.getChannel()) {
                        getDialog(subscriptionChannel, null);
                    }
                    /* Update Online Status */
                    mHandler = new Handler();
                    mStatusChecker.run();

                    /* Dismiss Progress Dialog */
//                    progressDialog.dismiss();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                /* Do Nothing */
            }
        });
    }

    /**
     * Get Dialog
     * .equalTo(subscriptionChannel) TODO: Remove this reminder
     * @param subscriptionChannel Subscription Channel ID
     */
    private void getDialog(final String subscriptionChannel, final Dialog originalDialog) {
        FirebaseDatabase.getInstance()
                .getReference().child("users").orderByChild("id").equalTo(subscriptionChannel)
                .limitToLast(1).addListenerForSingleValueEvent(new ValueEventListener()
        {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                    final User currUser = postSnapshot.getValue(User.class);
                    String testingSomething = FirebaseAuth.getInstance().getCurrentUser().getEmail();
                    FirebaseDatabase.getInstance()
                            .getReference().child("chats").child("active").child(subscriptionChannel.replace(".", "|")).child(FirebaseAuth.getInstance().getCurrentUser().getEmail().replace(".", "|"))
                            .orderByChild("timestamp").addListenerForSingleValueEvent(new ValueEventListener() {
                        final User currentUser = new User(currUser);

                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            int count = 0;
                            String lastMessage = "No History";
                            Date lastMessageDate = new Date(0);
                            if (dataSnapshot.exists()) {
                                for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                                    ++count;
                                    SimpleMessage simpleMessage = postSnapshot.getValue(SimpleMessage.class);
                                    lastMessage = simpleMessage.getMessage();
                                    lastMessageDate = new Date(simpleMessage.getTimestamp());
                                }
                                Dialog dialog;
                                if (null == originalDialog) {
                                    dialog = new Dialog(
                                            currentUser.getId(),
                                            currentUser.getName(),
                                            currentUser.getAvatar(),
                                            new ArrayList<>(Arrays.asList(currentUser)),
                                            new Message(String.valueOf(new Date().getTime()) + "_" + String.valueOf(lastMessageDate.getTime()), currentUser, lastMessage, lastMessageDate),
                                            count);
                                    dialogs.add(dialog);
                                    dialogsAdapter.addItem(dialog);
                                } else {
                                    dialog = originalDialog;
                                    dialog.setLastMessage(new Message(String.valueOf(new Date().getTime()) + "_" + String.valueOf(lastMessageDate.getTime()), currentUser, lastMessage, lastMessageDate));
                                    dialog.setUnreadCount(count);
                                    dialogsAdapter.updateItemById(dialog);
                                }
                                dialogsAdapter.sortByLastMessageDate();
                                dialogsAdapter.notifyDataSetChanged();
                            } else {
                                FirebaseDatabase.getInstance()
                                        .getReference().child("chats").child("active").child(FirebaseAuth.getInstance().getCurrentUser().getEmail().replace(".", "|")).child(subscriptionChannel.replace(".", "|"))
                                        .orderByChild("timestamp").addListenerForSingleValueEvent(new ValueEventListener() {
                                    final User currentUser = new User(currUser);

                                    @Override
                                    public void onDataChange(DataSnapshot dataSnapshot) {
                                        String lastMessage = "No History";
                                        Date lastMessageDate = new Date(0);
                                        if (dataSnapshot.exists()) {
                                            for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                                                SimpleMessage simpleMessage = postSnapshot.getValue(SimpleMessage.class);
                                                lastMessage = simpleMessage.getMessage();
                                                lastMessageDate = new Date(simpleMessage.getTimestamp());
                                            }
                                            Dialog dialog;
                                            if (null == originalDialog) {
                                                dialog = new Dialog(
                                                        currentUser.getId(),
                                                        currentUser.getName(),
                                                        currentUser.getAvatar(),
                                                        new ArrayList<>(Arrays.asList(currentUser)),
                                                        new Message(String.valueOf(new Date().getTime()) + "_" + String.valueOf(lastMessageDate.getTime()), currentUser, lastMessage, lastMessageDate),
                                                        0);
                                                dialogs.add(dialog);
                                                dialogsAdapter.addItem(dialog);
                                            } else {
                                                dialog = originalDialog;
                                                dialog.setLastMessage(new Message(String.valueOf(new Date().getTime()) + "_" + String.valueOf(lastMessageDate.getTime()), currentUser, lastMessage, lastMessageDate));
                                                dialog.setUnreadCount(0);
                                                dialogsAdapter.updateItemById(dialog);
                                            }
                                            dialogsAdapter.sortByLastMessageDate();
                                            dialogsAdapter.notifyDataSetChanged();
                                        } else {
                                            final String subChannel = subscriptionChannel.replace(".", "|");
                                            final String channel = FirebaseAuth.getInstance().getCurrentUser().getEmail().replace(".", "|");

                                            /* Look For Path */
                                            final String channelPath = subChannel + "-" + channel;
                                            FirebaseDatabase.getInstance()
                                                    .getReference().child("chats").child("delivered").child(channelPath).addListenerForSingleValueEvent(new ValueEventListener() {

                                                @Override
                                                public void onDataChange(DataSnapshot dataSnapshot) {
                                                    if (!dataSnapshot.exists()) {
                                                        final String channelPath = channel + "-" + subChannel;
                                                        FirebaseDatabase.getInstance()
                                                                .getReference().child("chats").child("delivered").child(channelPath).addListenerForSingleValueEvent(new ValueEventListener() {

                                                            @Override
                                                            public void onDataChange(DataSnapshot dataSnapshot) {
                                                                if (dataSnapshot.exists()) {
                                                                    /* Get Last Delivered Text */
                                                                    getLastDeliveredText(channelPath, originalDialog, currUser);
                                                                } else {
                                                                    if (null == originalDialog) {
                                                                        String lastMessage = "No History";
                                                                        Date lastMessageDate = new Date(0);
                                                                        Dialog dialog = new Dialog(
                                                                                currentUser.getId(),
                                                                                currentUser.getName(),
                                                                                currentUser.getAvatar(),
                                                                                new ArrayList<>(Arrays.asList(currentUser)),
                                                                                new Message(String.valueOf(new Date().getTime()) + "_" + String.valueOf(lastMessageDate.getTime()), currentUser, lastMessage, lastMessageDate),
                                                                                0);
                                                                        dialogs.add(dialog);
                                                                        dialogsAdapter.addItem(dialog);
                                                                    }
                                                                    dialogsAdapter.sortByLastMessageDate();
                                                                    dialogsAdapter.notifyDataSetChanged();
                                                                }
                                                            }

                                                            @Override
                                                            public void onCancelled(DatabaseError databaseError) {
                                                                /* Do Nothing */
                                                            }
                                                        });
                                                    } else {
                                                        /* Get Last Delivered Text */
                                                        getLastDeliveredText(channelPath, originalDialog, currUser);
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

    /**
     * Get Last Delivered Text
     *
     * @param channelPath    Channel Path
     * @param originalDialog Original Dialog
     * @param currUser       Current User
     */
    private void getLastDeliveredText(final String channelPath, final Dialog originalDialog, final User currUser) {
        FirebaseDatabase.getInstance()
                .getReference().child("chats").child("delivered").child(channelPath)
                .orderByChild("timestamp").limitToLast(1).addListenerForSingleValueEvent(new ValueEventListener() {
            final User currentUser = new User(currUser);

            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String lastMessage = "No History";
                Date lastMessageDate = new Date(0);
                if (dataSnapshot.exists()) {
                    for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                        SimpleMessage simpleMessage = postSnapshot.getValue(SimpleMessage.class);
                        if (null != simpleMessage) {
                            lastMessage = simpleMessage.getMessage();
                            lastMessageDate = new Date(simpleMessage.getTimestamp());
                        }
                    }
                    Dialog dialog;
                    if (null == originalDialog) {
                        dialog = new Dialog(
                                currentUser.getId(),
                                currentUser.getName(),
                                currentUser.getAvatar(),
                                new ArrayList<>(Arrays.asList(currentUser)),
                                new Message(String.valueOf(new Date().getTime()) + "_" + String.valueOf(lastMessageDate.getTime()), currentUser, lastMessage, lastMessageDate),
                                0);
                        dialogs.add(dialog);
                        dialogsAdapter.addItem(dialog);
                    } else {
                        dialog = originalDialog;
                        dialog.setLastMessage(new Message(String.valueOf(new Date().getTime()) + "_" + String.valueOf(lastMessageDate.getTime()), currentUser, lastMessage, lastMessageDate));
                        dialog.setUnreadCount(0);
                        dialogsAdapter.updateItemById(dialog);
                    }
                    dialogsAdapter.sortByLastMessageDate();
                    dialogsAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                /* Do Nothing */
            }
        });
    }

    public interface ChatFragmentListener {
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        CFL = (ChatFragment.ChatFragmentListener) context;
    }
}
