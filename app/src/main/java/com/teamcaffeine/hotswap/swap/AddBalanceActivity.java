package com.teamcaffeine.hotswap.swap;

import android.content.Intent;
import android.os.Bundle;
import android.renderscript.Sampler;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.okhttp.Callback;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;
import com.stripe.android.Stripe;
import com.stripe.android.TokenCallback;
import com.stripe.android.model.Card;
import com.stripe.android.model.Token;
import com.stripe.android.view.CardInputWidget;
import com.teamcaffeine.hotswap.R;
import com.teamcaffeine.hotswap.login.User;

import java.io.IOException;

public class AddBalanceActivity extends AppCompatActivity {

    private TextView balanceText;
    private EditText editTransferAmount;
    private CardInputWidget cardInputWidget;
    private Button transferButton;

    private FirebaseUser firebaseUser;
    private FirebaseDatabase database;
    private DatabaseReference users;
    private String userTable = "users";

    private Stripe stripe;
    private String TAG = "AddBalanceActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_balance);

        balanceText = (TextView) findViewById(R.id.balanceText);
        editTransferAmount = (EditText) findViewById(R.id.editTransferAmount);
        cardInputWidget = (CardInputWidget) findViewById(R.id.cardInputWidget);
        transferButton = (Button) findViewById(R.id.transferButton);

        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        database = FirebaseDatabase.getInstance();
        users = database.getReference().child(userTable);

        stripe = new Stripe(getApplicationContext(), getString(R.string.stripe_debug_key));

        users.child(firebaseUser.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    User user = dataSnapshot.getValue(User.class);
                    balanceText.setText(getString(R.string.current_balance) + " $" + Double.toString(user.getBalance()));
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e("AddBalanceActivity", "Users database connection terminated");
            }
        });

        transferButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                double toAdd;
                try {
                    toAdd = Double.parseDouble(editTransferAmount.getText().toString());
                } catch (NumberFormatException e) {
                    Log.e(TAG, "NumberFormatException: ", e);
                    Toast.makeText(getApplicationContext(), "Please add a value of at least $5 or more.", Toast.LENGTH_SHORT).show();
                    return;
                }
                final double valueToAdd = toAdd;
                if (valueToAdd < 5) {
                    Toast.makeText(getApplicationContext(), "Please add a value of at least $5 or more.", Toast.LENGTH_SHORT).show();
                    return;
                }

                Card card = cardInputWidget.getCard();
                if (card == null) {
                    Toast.makeText(getApplicationContext(), "Invalid card information.", Toast.LENGTH_SHORT).show();
                    return;
                }

                stripe.createToken(
                        card,
                        new TokenCallback() {
                            public void onSuccess(Token token) {
                                try {
                                    chargeUser(token.getId(), firebaseUser.getEmail(), valueToAdd);
                                    Toast.makeText(getApplicationContext(), "Charge successfully submitted.", Toast.LENGTH_SHORT).show();

                                    users.child(firebaseUser.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(DataSnapshot dataSnapshot) {
                                            User user = dataSnapshot.getValue(User.class);
                                            user.addBalance(valueToAdd);

                                            users.child(firebaseUser.getUid()).updateChildren(user.toMap());

                                            finish();
                                        }

                                        @Override
                                        public void onCancelled(DatabaseError databaseError) {
                                            Log.e("AddBalanceActivity", "Users database connection terminated");
                                        }
                                    });
                                } catch (IOException e) {
                                    e.printStackTrace();
                                    Toast.makeText(getApplicationContext(), "An unexpected error occurred.", Toast.LENGTH_SHORT).show();
                                }
                            }
                            public void onError(Exception error) {
                                // Show localized error message
                                Toast.makeText(getApplicationContext(),
                                        error.getLocalizedMessage(),
                                        Toast.LENGTH_SHORT
                                ).show();
                            }
                        }
                );
            }
        });
    }

    private void chargeUser(String token, String email, double amount) throws IOException {
        if (token.isEmpty()) {
            return;
        }

        int cents = (int) (amount * 100.0);

        OkHttpClient client = new OkHttpClient();

        Request request = new Request.Builder()
                .url("http://hotswap.glitch.me/charge?stripeToken=" + token + "&email=" + email + "&amount=" + cents)
                .get()
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Request request, IOException e) {
                // do nothing, POC
            }

            @Override
            public void onResponse(Response response) throws IOException {
                // do nothing, POC
            }
        });
    }

}
