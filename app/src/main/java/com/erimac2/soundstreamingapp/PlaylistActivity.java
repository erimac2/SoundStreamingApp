package com.erimac2.soundstreamingapp;

import java.util.ArrayList;
import java.util.List;


import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;


import com.deezer.sdk.model.PlayableEntity;
import com.deezer.sdk.model.Playlist;
import com.deezer.sdk.model.Track;
import com.deezer.sdk.network.connect.SessionStore;
import com.deezer.sdk.network.request.AsyncDeezerTask;
import com.deezer.sdk.network.request.DeezerRequest;
import com.deezer.sdk.network.request.DeezerRequestFactory;
import com.deezer.sdk.network.request.event.DeezerError;
import com.deezer.sdk.network.request.event.JsonRequestListener;
import com.deezer.sdk.player.PlaylistPlayer;
import com.deezer.sdk.player.event.OnPlayerProgressListener;
import com.deezer.sdk.player.event.PlayerWrapperListener;
import com.deezer.sdk.player.exception.TooManyPlayersExceptions;
import com.deezer.sdk.player.networkcheck.NetworkStateCheckerFactory;
import com.koushikdutta.ion.Ion;


public class PlaylistActivity extends PlayerActivity implements PlayerWrapperListener, OnPlayerProgressListener {


    private List<Playlist> playlistList = new ArrayList<Playlist>();
    private ArrayAdapter<Playlist> playlistAdapter;
    private PlaylistPlayer playlistPlayer;



    private enum Option
    {
        none,
        fade_in_out,
    }

    private Option option;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        new SessionStore().restore(deezerConnect, this);
        setContentView(R.layout.activity_playlist);
        setupPlaylistsList();
        setPlayerVisible(false);

        createPlayer();
        getUserPlaylists();
        option = Option.none;

    }
    private void setupPlaylistsList() {

        playlistAdapter = new ArrayAdapter<Playlist>(this,

                R.layout.item_title_cover, playlistList)
        {

            @Override
            public View getView(final int position, final View convertView, final ViewGroup parent) {

                Playlist playlist = getItem(position);

                View view = convertView;

                if (view == null)
                {
                    LayoutInflater vi = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                    view = vi.inflate(R.layout.item_title_cover, null);
                }

                TextView textView = view.findViewById(android.R.id.text1);
                textView.setText(playlist.getTitle());
                ImageView imageView = view.findViewById(android.R.id.icon);
                Ion.with(PlaylistActivity.this).load(playlist.getPictureUrl()).intoImageView(imageView);

                return view;
            }
        };

        ListView listview = findViewById(android.R.id.list);
        listview.setAdapter(playlistAdapter);
        listview.setOnItemClickListener(new OnItemClickListener()
        {

            @Override
            public void onItemClick(final AdapterView<?> parent, final View view, final int position, final long id)
            {
                Playlist playlist = playlistList.get(position);
                playlistPlayer.playPlaylist(playlist.getId());
                setPlayerVisible(true);
            }
        });
    }
    private void createPlayer() {

        try {
            playlistPlayer = new PlaylistPlayer(getApplication(), deezerConnect, NetworkStateCheckerFactory.wifiAndMobile());
            playlistPlayer.addPlayerListener(this);
            playlistPlayer.addOnPlayerProgressListener(this);
            setAttachedPlayer(playlistPlayer);
        }
        catch (TooManyPlayersExceptions e)
        {
            handleError(e);
        }
        catch (DeezerError e)
        {
            handleError(e);
        }
    }
    private void getUserPlaylists() {

        DeezerRequest request = DeezerRequestFactory.requestCurrentUserPlaylists();

        AsyncDeezerTask task = new AsyncDeezerTask(deezerConnect, new JsonRequestListener()
        {
                    @Override
                    public void onResult(final Object result, final Object requestId) {

                        playlistList.clear();

                        try
                        {
                            playlistList.addAll((List<Playlist>) result);
                        }
                        catch (ClassCastException e)
                        {
                            handleError(e);
                        }
                        if (playlistList.isEmpty())
                        {
                            Toast.makeText(PlaylistActivity.this, "No results", Toast.LENGTH_LONG).show();
                        }

                        playlistAdapter.notifyDataSetChanged();
                    }
                    @Override
                    public void onUnparsedResult(final String response, final Object requestId)
                    {
                        handleError(new DeezerError("Unparsed reponse"));
                    }
                    @Override
                    public void onException(final Exception exception, final Object requestId)
                    {
                        handleError(exception);
                    }
                });
        task.execute(request);
    }
    @Override
    protected void skipToNextTrack() {
        playlistPlayer.skipToNextTrack();
    }
    @Override
    protected void skipToPreviousTrack() {
        playlistPlayer.skipToPreviousTrack();
    }
    @Override
    public void onAllTracksEnded()
    {
    }

    @Override
    public void onPlayTrack(PlayableEntity playableEntity) {
        displayTrack((Track)playableEntity);
    }

    @Override
    public void onTrackEnded(PlayableEntity playableEntity) {

    }
    @Override
    public void onRequestException(final Exception e, final Object requestId)
    {
        handleError(e);
    }
    @Override
    public void onPlayerProgress(final long timePosition) {
    }
}