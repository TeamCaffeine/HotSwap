package com.teamcaffeine.hotswap;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.google.android.gms.common.SignInButton;

public class LoginActivity extends AppCompatActivity {
//        implements AdapterView.OnItemClickListener {

//    private static final Class[] CLASSES = new Class[]{
//            GoogleSignInActivity.class,
//            FacebookLoginActivity.class,
//            EmailPasswordActivity.class
//    };
//
//    private static final int[] DESCRIPTION_IDS = new int[] {
//            R.string.desc_google_sign_in,
//            R.string.desc_facebook_login,
//            R.string.desc_emailpassword,
//    };

    SignInButton btnGoogleSignIn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        btnGoogleSignIn = (SignInButton) findViewById(R.id.google_sign_in_button);

        btnGoogleSignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(LoginActivity.this, GoogleSignInActivity.class);
                startActivity(intent);
            }
        });

//        // Set up ListView and Adapter
//        ListView listView = findViewById(R.id.list_view);
//
//        MyArrayAdapter adapter = new MyArrayAdapter(this, android.R.layout.simple_list_item_2, CLASSES);
//        adapter.setDescriptionIds(DESCRIPTION_IDS);
//
//        listView.setAdapter(adapter);
//        listView.setOnItemClickListener(this);
    }

//    @Override
//    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
//        Class clicked = CLASSES[position];
//        startActivity(new Intent(this, clicked));
//    }

//    public static class MyArrayAdapter extends ArrayAdapter<Class> {
//
//        private Context mContext;
//        private Class[] mClasses;
//        private int[] mDescriptionIds;
//
//        public MyArrayAdapter(Context context, int resource, Class[] objects) {
//            super(context, resource, objects);
//
//            mContext = context;
//            mClasses = objects;
//        }
//
//        @Override
//        public View getView(int position, View convertView, ViewGroup parent) {
//            View view = convertView;
//
//            if (convertView == null) {
//                LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(LAYOUT_INFLATER_SERVICE);
//                view = inflater.inflate(android.R.layout.simple_list_item_2, null);
//            }
//
//            ((TextView) view.findViewById(android.R.id.text1)).setText(mClasses[position].getSimpleName());
//            ((TextView) view.findViewById(android.R.id.text2)).setText(mDescriptionIds[position]);
//
//            return view;
//        }
//
//        public void setDescriptionIds(int[] descriptionIds) {
//            mDescriptionIds = descriptionIds;
//        }
//    }
}





















//import android.content.Intent;
//import android.support.v7.app.AppCompatActivity;
//import android.os.Bundle;
//
//import com.firebase.ui.auth.AuthUI;
//import com.firebase.ui.auth.IdpResponse;
//import com.firebase.ui.auth.ResultCodes;
//import com.google.firebase.auth.FirebaseAuth;
//import com.google.firebase.auth.FirebaseUser;
//
//import java.util.Arrays;
//import java.util.List;
//
//public class LoginActivity extends AppCompatActivity {
//
//    private static final int RC_SIGN_IN = 123;
//    private FirebaseAuth mAuth;
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//
//        mAuth = FirebaseAuth.getInstance();
//
//        // Choose authentication providers
//        List<AuthUI.IdpConfig> providers = Arrays.asList(
//                new AuthUI.IdpConfig.Builder(AuthUI.EMAIL_PROVIDER).build(),
//                new AuthUI.IdpConfig.Builder(AuthUI.GOOGLE_PROVIDER).build(),
//                new AuthUI.IdpConfig.Builder(AuthUI.FACEBOOK_PROVIDER).build());
//
//        // Create and launch sign-in intent
//        startActivityForResult(
//                AuthUI.getInstance()
//                        .createSignInIntentBuilder()
//                        .setAvailableProviders(providers)
//                        .build(),
//                RC_SIGN_IN);
//    }
//
//    @Override
//    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
//        super.onActivityResult(requestCode, resultCode, data);
//
//        if (requestCode == RC_SIGN_IN) {
//            IdpResponse response = IdpResponse.fromResultIntent(data);
//
//            if (resultCode == ResultCodes.OK) {
//                // Successfully signed in
//                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
//                // ...
//            } else {
//                // Sign in failed, check response for error code
//                // ...
//            }
//        }
//    }
//}
