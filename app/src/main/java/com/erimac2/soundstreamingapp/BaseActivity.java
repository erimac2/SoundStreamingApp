package com.erimac2.soundstreamingapp;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.deezer.sdk.network.connect.DeezerConnect;
import com.deezer.sdk.network.connect.SessionStore;
import com.erimac2.soundstreamingapp.Lab1.FirstActivity;
import com.erimac2.soundstreamingapp.Lab2.Lab2Activity;

public class BaseActivity extends AppCompatActivity {
    protected DeezerConnect deezerConnect = null;




    public static final String applicationID = "372944";



    @Override
    protected void onCreate(final Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setLogo(R.drawable.appicon);
        getSupportActionBar().setDisplayUseLogoEnabled(true);
        deezerConnect = new DeezerConnect(this, applicationID);

    }



    @Override

    public boolean onCreateOptionsMenu(final Menu menu) {

        if (deezerConnect.isSessionValid()) {

            new MenuInflater(this).inflate(R.menu.menu, menu);

        }

        return true;

    }
    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {

        if (item.getItemId() == R.id.lab1) {

            Intent intent = new Intent(this, FirstActivity.class);
            startActivity(intent);
        }
        else if(item.getItemId() == R.id.lab2)
        {
            Intent intent = new Intent(this, Lab2Activity.class);
            startActivity(intent);
        }
        else if(item.getItemId() == R.id.logout)
        {
            disconnectFromDeezer();

            Intent intent = new Intent(this, LoginActivity.class);

            startActivity(intent);
        }

        return super.onOptionsItemSelected(item);

    }



    private final void disconnectFromDeezer() {


        if (deezerConnect != null) {

            deezerConnect.logout(this);

        }

        new SessionStore().clear(this);

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
