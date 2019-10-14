package com.erimac2.soundstreamingapp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.deezer.sdk.model.Permissions;
import com.deezer.sdk.network.connect.DeezerConnect;
import com.deezer.sdk.network.connect.SessionStore;
import com.deezer.sdk.network.connect.event.DialogListener;
import com.erimac2.soundstreamingapp.Lab1.FirstActivity;

public class MainActivity extends AppCompatActivity {

    protected static final String[] permissions = new String[]{
            Permissions.BASIC_ACCESS,
            Permissions.MANAGE_LIBRARY,
            Permissions.LISTENING_HISTORY
    };
    static String applicationID = "372944";
    static DeezerConnect deezerConnect;

    private Button labButton;
    private Button loginButton;
    private DialogListener deezerDialogListener = new DialogListener() {
        @Override
        public void onComplete(final Bundle bundle) {

            SessionStore sessionStore = new SessionStore();
            sessionStore.save(deezerConnect, MainActivity.this);

            Log.i("Deezer Login", "Succesfully logged in");

            Intent intent = new Intent(MainActivity.this, ArtistActivity.class);
            startActivity(intent);
        }

        @Override
        public void onCancel() {
            Log.i("Deezer Login", "Login cancelled");

        }

        @Override
        public void onException(final Exception e) {
            Toast.makeText(MainActivity.this, R.string.loginError, Toast.LENGTH_LONG).show();
            Log.i("Deezer Login", "Login error");
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        labButton = findViewById(R.id.First_Activity_Button);
        loginButton = findViewById(R.id.Login);
        deezerConnect = new DeezerConnect(this, applicationID);
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                connectToDeezer();
            }
        });
        labButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                runFirstActivity();
            }
        });
}
    public void runFirstActivity(){
        Intent intent = new Intent(MainActivity.this, FirstActivity.class);
        startActivity(intent);
    }

private void connectToDeezer(){
        deezerConnect.authorize(this, permissions, deezerDialogListener);
    }

    protected void handleError(final Exception exception) {

        String message = exception.getMessage();

        if (TextUtils.isEmpty(message)) {

            message = exception.getClass().getName();

        }



        Toast toast = Toast.makeText(this, message, Toast.LENGTH_LONG);

        ((TextView) toast.getView().findViewById(android.R.id.message)).setTextColor(Color.RED);

        toast.show();



        Log.e("BaseActivity", "Exception occured " + exception.getClass().getName(), exception);

    }
}

