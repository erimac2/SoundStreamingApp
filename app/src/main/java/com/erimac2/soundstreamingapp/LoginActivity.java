package com.erimac2.soundstreamingapp;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.deezer.sdk.model.Permissions;
import com.deezer.sdk.network.connect.SessionStore;
import com.deezer.sdk.network.connect.event.DialogListener;

public class LoginActivity extends BaseActivity {

    protected static final String[] permissions = new String[]{
            Permissions.BASIC_ACCESS,
            Permissions.MANAGE_LIBRARY,
            Permissions.LISTENING_HISTORY
    };

    private Button loginButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        loginButton = findViewById(R.id.Login);
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                connectToDeezer();
            }
        });

        SessionStore sessionStore = new SessionStore();



        if (sessionStore.restore(deezerConnect, this)) {

            Toast.makeText(this, "Already logged in !", Toast.LENGTH_LONG).show();

            Intent intent = new Intent(LoginActivity.this, HomeActivity.class);

            startActivity(intent);

        }
}
private void connectToDeezer(){
        deezerConnect.authorize(this, permissions, deezerDialogListener);
    }

    private DialogListener deezerDialogListener = new DialogListener() {
        @Override
        public void onComplete(final Bundle bundle) {

            SessionStore sessionStore = new SessionStore();
            sessionStore.save(deezerConnect, LoginActivity.this);

            Log.i("Deezer Login", "Succesfully logged in");

            Intent intent = new Intent(LoginActivity.this, HomeActivity.class);
            startActivity(intent);
        }

        @Override
        public void onCancel() {
            Log.i("Deezer Login", "Login cancelled");

        }

        @Override
        public void onException(final Exception e) {
            Toast.makeText(LoginActivity.this, R.string.loginError, Toast.LENGTH_LONG).show();
            Log.i("Deezer Login", "Login error");
        }
    };
}

