package com.erimac2.soundstreamingapp;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;


import com.deezer.sdk.model.Album;
import com.deezer.sdk.model.PlayableEntity;
import com.deezer.sdk.model.Track;
import com.deezer.sdk.network.connect.SessionStore;
import com.deezer.sdk.network.request.AsyncDeezerTask;
import com.deezer.sdk.network.request.DeezerRequest;
import com.deezer.sdk.network.request.DeezerRequestFactory;
import com.deezer.sdk.network.request.event.DeezerError;
import com.deezer.sdk.network.request.event.JsonRequestListener;
import com.deezer.sdk.player.TrackPlayer;
import com.deezer.sdk.player.event.PlayerWrapperListener;
import com.deezer.sdk.player.exception.TooManyPlayersExceptions;
import com.deezer.sdk.player.networkcheck.NetworkStateCheckerFactory;
public class TrackActivity extends PlayerActivity implements PlayerWrapperListener {


    private List<Track> tracksList = new ArrayList<>();

    private ArrayAdapter<Track> tracksAdapter;

    private TrackPlayer trackPlayer;

    Boolean ascending = true;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        new SessionStore().restore(deezerConnect, this);

        setContentView(R.layout.activity_tracks);
        setupTracksList();
        setupPlayerUI();

        createPlayer();

        getUserTracks();

    }
    private void setupPlayerUI() {

        setPlayerVisible(false);
        setButtonEnabled(buttonPlayerSkipBackward, false);
        setButtonEnabled(buttonPlayerSkipForward, false);
    }
    private void setupTracksList() {

        tracksAdapter = new ArrayAdapter<Track>(this, android.R.layout.simple_list_item_1, tracksList) {

            @Override
            public View getView(final int position, final View convertView, final ViewGroup parent) {

                Track track = getItem(position);

                View view = convertView;

                if (view == null) {

                    LayoutInflater vi = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                    view = vi.inflate(android.R.layout.simple_list_item_1, null);

                }

                TextView textView = view.findViewById(android.R.id.text1);

                textView.setText(track.getTitle());

                return view;
            }
        };
        ListView listview = findViewById(android.R.id.list);

        listview.setAdapter(tracksAdapter);

        listview.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(final AdapterView<?> parent, final View view, final int position, final long id) {

                Track track = tracksList.get(position);

                trackPlayer.playTrack(track.getId());

                setPlayerVisible(true);
            }
        });
        listview.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                Track track = tracksList.get(position);
                Intent shareIntent = new Intent(Intent.ACTION_SEND);
                shareIntent.setType("text/plain");
                shareIntent.putExtra(Intent.EXTRA_SUBJECT, "My application name");
                String shareMessage= "\nShare?\n\n";
                shareMessage = shareMessage + track.getLink() + BuildConfig.APPLICATION_ID +"\n\n";
                shareIntent.putExtra(Intent.EXTRA_TEXT, shareMessage);
                startActivity(Intent.createChooser(shareIntent, "Share"));
                return true;
            }
        });
    }
    @Override
    public boolean onCreateOptionsMenu(final Menu menu)
    {
        super.onCreateOptionsMenu(menu);

        getMenuInflater().inflate(R.menu.sort, menu);

        return true;
    }
    @Override
    public boolean onOptionsItemSelected(final MenuItem item)
    {
        boolean res = true;

        switch (item.getItemId())
        {
            case R.id.sort:
                sortArrayList(ascending);
                ascending = !ascending;
                break;
            default:
                res = super.onOptionsItemSelected(item);
                break;
        }
        return res;
    }
    private void sortArrayList(Boolean ascending)
    {
        if (ascending) {
            Collections.sort(tracksList, new Comparator<Track>() {
                @Override
                public int compare(Track item1, Track item2) {
                    return item1.getTitle().compareTo(item2.getTitle());
                }
            });
        }
        else
        {
            Collections.reverse(tracksList);
        }
        tracksAdapter.notifyDataSetChanged();
    }
    private void createPlayer() {

        try {

            trackPlayer = new TrackPlayer(getApplication(), deezerConnect, NetworkStateCheckerFactory.wifiAndMobile());

            trackPlayer.addPlayerListener(this);

            setAttachedPlayer(trackPlayer);

        }
        catch (TooManyPlayersExceptions e) {

            handleError(e);
        }
        catch (DeezerError e) {

            handleError(e);
        }

    }
    private void getUserTracks() {

        DeezerRequest request = DeezerRequestFactory.requestCurrentUserCharts();

        AsyncDeezerTask task = new AsyncDeezerTask(deezerConnect, new JsonRequestListener() {

                    @Override
                    public void onResult(final Object result, final Object requestId) {

                        tracksList.clear();

                        try {

                            tracksList.addAll((List<Track>) result);
                        }
                        catch (ClassCastException e) {

                            handleError(e);
                        }
                        if (tracksList.isEmpty()) {
                            Toast.makeText(TrackActivity.this, "No results", Toast.LENGTH_LONG).show();

                        }
                        tracksAdapter.notifyDataSetChanged();
                    }
                    @Override
                    public void onUnparsedResult(final String response, final Object requestId) {
                        handleError(new DeezerError("Unparsed reponse"));
                    }
                    @Override
                    public void onException(final Exception exception, final Object requestId) {
                        handleError(exception);
                    }
                });
        task.execute(request);
    }
    @Override
    public void onPlayTrack(final PlayableEntity playableEntity) {
        displayTrack((Track)playableEntity);
    }
    @Override
    public void onTrackEnded(final PlayableEntity playableEntity) {
    }
    @Override
    public void onAllTracksEnded() {
    }
    @Override
    public void onRequestException(final Exception e, final Object requestId) {
        handleError(e);
    }
}