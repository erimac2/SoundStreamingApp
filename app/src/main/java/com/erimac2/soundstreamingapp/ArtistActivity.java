package com.erimac2.soundstreamingapp;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

import com.deezer.sdk.model.Artist;
import com.deezer.sdk.network.connect.SessionStore;
import com.deezer.sdk.network.request.DeezerRequest;
import com.deezer.sdk.network.request.DeezerRequestFactory;
import com.deezer.sdk.network.request.event.JsonRequestListener;
import com.deezer.sdk.network.request.event.RequestListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class ArtistActivity extends MainActivity {

    public static final String EXTRA_TITLE = "com.erimac2.soundstreamingapp.EXTRA_TITLE";
    public static final String EXTRA_ID = "com.erimac2.soundstreamingapp.EXTRA_ID";
    public static final String EXTRA_LINK = "com.erimac2.soundstreamingapp.EXTRA_LINK";


    private List<Artist> artistList = new ArrayList<Artist>();
    Boolean ascending = true;

    private Button sortButton;
    private EditText filter;
    private ListView listView;
    private ArtistAdapter adapter;
    private final ArrayList<ArtistItem> items = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        new SessionStore().restore(deezerConnect, this);
        setContentView(R.layout.activity_artist);
        listView = findViewById(R.id.list);
        sortButton = findViewById(R.id.Button_sort);
        filter = findViewById(R.id.Filter);
        sortButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                sortArrayList(ascending);
                ascending = !ascending;
            }
        });

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

        getUserTracks();
    }
    private void sortArrayList(Boolean ascending)
    {
        if (ascending) {
            Collections.sort(items, new Comparator<ArtistItem>() {
                @Override
                public int compare(ArtistItem item1, ArtistItem item2) {
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
    private void getUserTracks() {
        RequestListener listener = new JsonRequestListener() {

            public void onResult(Object result, Object requestId) {
                artistList = (ArrayList<Artist>) result;
                for (Artist artist : artistList) {
                    Log.i("Artists", artist.getName());
                    items.add(new ArtistItem(artist.getName(), artist.getBigImageUrl(), artist.getId()));
                }

                adapter = new ArtistAdapter(ArtistActivity.this, items);
                listView.setTextFilterEnabled(true);
                listView.setAdapter(adapter);
                listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        ArtistItem item = (ArtistItem) parent.getItemAtPosition(position);

                        Intent intent = new Intent(ArtistActivity.this, PlayerActivity.class);
                        intent.putExtra(EXTRA_TITLE, item.getTitle());
                        intent.putExtra(EXTRA_LINK, item.getImageLink());
                        intent.putExtra(EXTRA_ID, item.getId());
                        startActivity(intent);
                    }
                });
            }

            public void onUnparsedResult(String requestResponse, Object requestId) {
            }

            public void onException(Exception e, Object requestId) {
            }
        };

        DeezerRequest request = DeezerRequestFactory.requestCurrentUserRecommendedArtists();
        deezerConnect.requestAsync(request, listener);
    }
}
