package com.erimac2.soundstreamingapp;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.deezer.sdk.model.Artist;
import com.deezer.sdk.model.PlayableEntity;
import com.deezer.sdk.model.Track;
import com.deezer.sdk.network.connect.SessionStore;
import com.deezer.sdk.network.request.DeezerRequest;
import com.deezer.sdk.network.request.DeezerRequestFactory;
import com.deezer.sdk.network.request.event.DeezerError;
import com.deezer.sdk.network.request.event.JsonRequestListener;
import com.deezer.sdk.network.request.event.RequestListener;
import com.deezer.sdk.player.ArtistRadioPlayer;
import com.deezer.sdk.player.event.RadioPlayerListener;
import com.deezer.sdk.player.exception.TooManyPlayersExceptions;
import com.deezer.sdk.player.networkcheck.NetworkStateCheckerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class ArtistActivity extends PlayerActivity implements RadioPlayerListener {

    private List<Artist> artistList = new ArrayList<Artist>();
    Boolean ascending = true;

    private ArtistRadioPlayer player;

    private EditText filter;
    private ListView listView;
    private ListAdapter adapter;
    private final ArrayList<ListItem> items = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        new SessionStore().restore(deezerConnect, this);

        setContentView(R.layout.activity_artist);


        listView = findViewById(R.id.list);
        filter = findViewById(R.id.Filter);

        Filter();

        setupPlayerUI();
        createPlayer();

        getUserArtists();
    }
    private void setupPlayerUI()
    {
        setPlayerVisible(false);
        setButtonEnabled(buttonPlayerSeekBackward, false);
        setButtonEnabled(buttonPlayerSeekForward, false);
        setButtonEnabled(buttonPlayerSkipBackward, false);
        setButtonEnabled(buttonPlayerRepeat, false);
    }
    private void createPlayer()
    {
        try
        {
            player = new ArtistRadioPlayer(getApplication(), deezerConnect, NetworkStateCheckerFactory.wifiAndMobile());
            player.addPlayerListener(this);
            setAttachedPlayer(player);
        }
        catch(TooManyPlayersExceptions e)
        {
            handleError(e);
        }
        catch (DeezerError e)
        {
            handleError(e);
        }
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
    private void Filter()
    {
        filter.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                adapter.getFilter().filter(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
    }
    private void sortArrayList(Boolean ascending)
    {
        if (ascending) {
            Collections.sort(items, new Comparator<ListItem>() {
                @Override
                public int compare(ListItem item1, ListItem item2) {
                    return item1.getTitle().compareTo(item2.getTitle());
                }
            });
        }
        else
        {
            Collections.reverse(items);
        }
        adapter.notifyDataSetChanged();
    }
    private void getUserArtists() {
        RequestListener listener = new JsonRequestListener() {

            public void onResult(Object result, Object requestId) {
                artistList = (ArrayList<Artist>) result;
                for (Artist artist : artistList) {
                    items.add(new ListItem(artist.getName(), artist.getBigImageUrl(), artist.getId()));
                }
                adapter = new ListAdapter(ArtistActivity.this, items);
                listView.setTextFilterEnabled(true);
                listView.setAdapter(adapter);
                listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        ListItem item = (ListItem) parent.getItemAtPosition(position);
                        player.playArtistRadio(item.getId());
                        setPlayerVisible(true);
                    }
                });
                listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
                    @Override
                    public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                        Artist artist = artistList.get(position);
                        Intent shareIntent = new Intent(Intent.ACTION_SEND);
                        shareIntent.setType("text/plain");
                        shareIntent.putExtra(Intent.EXTRA_SUBJECT, "My application name");
                        String shareMessage= "\nDoes this work?\n\n";
                        shareMessage = shareMessage + artist.getLink() + BuildConfig.APPLICATION_ID +"\n\n";
                        shareIntent.putExtra(Intent.EXTRA_TEXT, shareMessage);
                        startActivity(Intent.createChooser(shareIntent, "Share"));
                        return true;
                    }
                });
            }

            public void onUnparsedResult(String requestResponse, Object requestId) {
                handleError(new DeezerError("Unparsed response"));
            }

            public void onException(Exception e, Object requestId) {
                handleError(e);
            }
        };

        DeezerRequest request = DeezerRequestFactory.requestCurrentUserArtists();
        deezerConnect.requestAsync(request, listener);
    }
    @Override
    protected void skipToNextTrack()
    {
        player.skipToNextTrack();
    }
    @Override
    public void onTooManySkipsException() {
        Toast.makeText(this, "Too many skips", Toast.LENGTH_LONG).show();
    }

    @Override
    public void onAllTracksEnded() {

    }

    @Override
    public void onPlayTrack(PlayableEntity playableEntity) {
        displayTrack((Track)playableEntity);
    }
    @Override
    public void onTrackEnded(PlayableEntity playableEntity) {
    }
    @Override
    public void onRequestException(Exception e, Object o) {
        handleError(e);
    }
}
