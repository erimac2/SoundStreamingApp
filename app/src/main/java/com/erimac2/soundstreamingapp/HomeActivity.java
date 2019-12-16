package com.erimac2.soundstreamingapp;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.deezer.sdk.model.AImageOwner;
import com.deezer.sdk.model.User;
import com.deezer.sdk.network.connect.SessionStore;
import com.deezer.sdk.network.request.AsyncDeezerTask;
import com.deezer.sdk.network.request.DeezerRequest;
import com.deezer.sdk.network.request.DeezerRequestFactory;
import com.deezer.sdk.network.request.event.DeezerError;
import com.deezer.sdk.network.request.event.JsonRequestListener;
import com.deezer.sdk.network.request.event.RequestListener;
import com.koushikdutta.ion.Ion;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class HomeActivity extends BaseActivity {
     ImageView image;

    @Override
    protected void  onCreate(final Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_home);

        new SessionStore().restore(deezerConnect, this);

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, getResources().getStringArray(R.array.user_data));

        ListView list = findViewById(android.R.id.list);
        image = findViewById(R.id.user_picture);
        list.setAdapter(adapter);
        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                userNavigate(position);
            }
        });

        fetchUserInfo();
    }

    @Override
    public boolean onCreateOptionsMenu(final Menu menu)
    {
        super.onCreateOptionsMenu(menu);

        getMenuInflater().inflate(R.menu.notifications, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item)
    {
        boolean res = true;

        switch (item.getItemId())
        {
            case R.id.action_notification:
                promptUserNotification();
                break;
            default:
                res = super.onOptionsItemSelected(item);
                break;
        }
        return res;
    }

    private void displayUserInfo(final User user)
    {
        ((TextView)findViewById(R.id.name)).setText(user.getLastName());
        ((TextView) findViewById(R.id.first_name)).setText(user.getFirstName());

        Date currentTime = Calendar.getInstance().getTime();

        SimpleDateFormat format = new SimpleDateFormat("yyyy, MMMM dd");

        ((TextView) findViewById(R.id.date)).setText(format.format(currentTime));

        setTitle(getString(R.string.activity_home, user.getName()));

        Ion.with(this).load(user.getImageUrl(AImageOwner.ImageSize.medium)).intoImageView(image);

    }
    private void fetchUserInfo()
    {
        DeezerRequest request = DeezerRequestFactory.requestCurrentUser();
        AsyncDeezerTask task = new AsyncDeezerTask(deezerConnect, new JsonRequestListener() {
            @Override
            public void onResult(final Object result, final Object requestId) {
                if(result instanceof User)
                {
                    displayUserInfo((User)result);
                }
                else
                {
                    handleError(new IllegalArgumentException());
                }
            }

            @Override
            public void onUnparsedResult(final String response, final Object requestId) {
                handleError(new DeezerError("Unparsed response"));
            }

            @Override
            public void onException(final Exception exception, final Object requestId) {
                handleError(exception);
            }
        });

        task.execute(request);
    }
    private static final int PLAYLISTS = 0;
    private static final int ALBUMS = 1;
    private static final int ARTISTS = 2;
    private static final int RADIOS = 3;
    private static final int TRACKS = 4;

    private void userNavigate(final int selection)
    {
        Intent intent = null;

        switch(selection)
        {
            case PLAYLISTS:
                intent = new Intent(this, PlaylistActivity.class);
                break;
            case ALBUMS:
                intent = new Intent(this, AlbumActivity.class);
                break;
            case ARTISTS:
                intent = new Intent(this, ArtistActivity.class);
                break;
            case RADIOS:
                intent = new Intent(this, RadioActivity.class);
                break;
            case TRACKS:
                intent = new Intent(this, TrackActivity.class);
                break;
        }

        if(intent != null)
        {
            startActivity(intent);
        }
    }

    private void promptUserNotification()
    {
        AlertDialog.Builder alert = new AlertDialog.Builder(this);

        alert.setTitle("Send notification");

        final EditText input = new EditText(this);
        alert.setView(input);

        alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String value = input.getText().toString();
                sendNotification(value);
            }
        });
        alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });

        alert.show();
    }
    private void sendNotification(final String notification)
    {
        DeezerRequest request = DeezerRequestFactory.requestCurrentUserSendNotification(notification);
        deezerConnect.requestAsync(request, new RequestListener() {
            @Override
            public void onComplete(final String response, final Object requestId) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(getBaseContext(), "Notification sent", Toast.LENGTH_SHORT).show();
                    }
                });
            }
            @Override
            public void onException(final Exception exception, final Object requestId) {
                handleError(exception);
            }
        });
    }
}
