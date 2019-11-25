package com.erimac2.soundstreamingapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.deezer.sdk.network.connect.SessionStore;
import com.deezer.sdk.player.ArtistRadioPlayer;
import com.deezer.sdk.player.networkcheck.NetworkStateCheckerFactory;
import com.koushikdutta.ion.Ion;

public class ArtistPlayerActivity extends BaseActivity {

    Button playButton;
    Button stopButton;
    ImageView playerImage;
    TextView playerText;

    long Id;

    Boolean playing = false;

    private ArtistRadioPlayer artistRadioPlayer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.artist_activity_player);
        new SessionStore().restore(deezerConnect, this);

        Intent intent = getIntent();

        playButton = findViewById(R.id.Play_Button);
        stopButton = findViewById(R.id.Stop_Button);
        playerImage = findViewById(R.id.Player_Image);
        playerText = findViewById(R.id.Player_Name);

        String Title = intent.getStringExtra(ArtistActivity.EXTRA_TITLE);
        String Link = intent.getStringExtra(ArtistActivity.EXTRA_LINK);
        Id = intent.getLongExtra(ArtistActivity.EXTRA_ID, 0);

        playerText.setText(Title);

        Ion.with(playerImage)
                .placeholder(android.R.drawable.btn_default_small)
                .error(android.R.drawable.ic_dialog_alert)
                .load(Link);

        playButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!playing) {
                    playTrack();
                    playing = true;
                }
            }
        });
        stopButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(playing)
                {
                    artistRadioPlayer.stop();
                    Toast.makeText(ArtistPlayerActivity.this, "Player state:" + artistRadioPlayer.getPlayerState(), Toast.LENGTH_LONG).show();
                    artistRadioPlayer.release();
                    playing = false;
                }
            }
        });
    }
//    @Override
//    protected void onPause() {
//        super.onPause();
//        if(playing)
//        {
//            artistRadioPlayer.stop();
//            artistRadioPlayer.release();
//            playing = false;
//        }
//    }
    @Override
    protected void onDestroy()
    {
        super.onDestroy();
        if(playing)
        {
            artistRadioPlayer.stop();
            artistRadioPlayer.release();
            playing = false;
        }
    }
    private void playTrack() {
        try {
            artistRadioPlayer = new ArtistRadioPlayer(getApplication(), deezerConnect, NetworkStateCheckerFactory.wifiAndMobile());
            artistRadioPlayer.playArtistRadio(Id);
            Toast.makeText(ArtistPlayerActivity.this, "Player state:" + artistRadioPlayer.getPlayerState(), Toast.LENGTH_LONG).show();
        } catch (Exception e) {
            handleError(e);
        }
    }
}
