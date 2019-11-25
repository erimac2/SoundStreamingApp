package com.erimac2.soundstreamingapp;

import android.os.Bundle;
import android.view.View;
import android.widget.ExpandableListView;
import android.widget.Toast;

import com.deezer.sdk.model.PlayableEntity;
import com.deezer.sdk.model.Radio;
import com.deezer.sdk.model.RadioCategory;
import com.deezer.sdk.model.Track;
import com.deezer.sdk.network.connect.SessionStore;
import com.deezer.sdk.network.request.AsyncDeezerTask;
import com.deezer.sdk.network.request.DeezerRequest;
import com.deezer.sdk.network.request.DeezerRequestFactory;
import com.deezer.sdk.network.request.event.DeezerError;
import com.deezer.sdk.network.request.event.RadioCategoriesRequestListener;
import com.deezer.sdk.player.RadioPlayer;
import com.deezer.sdk.player.event.RadioPlayerListener;
import com.deezer.sdk.player.exception.TooManyPlayersExceptions;
import com.deezer.sdk.player.networkcheck.WifiAndMobileNetworkStateChecker;

import java.util.ArrayList;
import java.util.List;

public class RadioActivity extends PlayerActivity implements RadioPlayerListener {
    private List<RadioCategory> radioCategoryList = new ArrayList<>();
    private RadioAdapter radioCategoryAdapter = new RadioAdapter(this, radioCategoryList);


    private RadioPlayer radioPlayer;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        new SessionStore().restore(deezerConnect, this);
        setContentView(R.layout.activity_radio);

        setupPlayerUI();
        setupRadioList();
        createPlayer();
        searchAllRadioCategory();

    }
    private void createPlayer() {

        try { radioPlayer = new RadioPlayer(getApplication(), deezerConnect, new WifiAndMobileNetworkStateChecker());

            radioPlayer.addPlayerListener(this);

            setAttachedPlayer(radioPlayer);


        }
        catch (DeezerError e) {

            handleError(e);
        }
        catch (TooManyPlayersExceptions e) {

            handleError(e);
        }

    }
    @Override
    protected void skipToNextTrack() {

        super.skipToNextTrack();
        radioPlayer.skipToNextTrack();

    }
    private void setupPlayerUI() {


        setPlayerVisible(false);

        setButtonEnabled(buttonPlayerSeekBackward, false);

        setButtonEnabled(buttonPlayerSeekForward, false);

        setButtonEnabled(buttonPlayerSkipBackward, false);

        setButtonEnabled(buttonPlayerRepeat, false);

    }
    private void setupRadioList() {

        // setup the

        final ExpandableListView listViewRadioList = (ExpandableListView) findViewById(android.R.id.list);

        listViewRadioList.setAdapter(radioCategoryAdapter);

        listViewRadioList.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {



            @Override

            public boolean onChildClick(final ExpandableListView parent, final View v,

                                        final int groupPosition,

                                        final int childPosition, final long id) {

                Radio radio = radioCategoryAdapter.getChild(groupPosition, childPosition);



                radioPlayer.playRadio(RadioPlayer.RadioType.RADIO, radio.getId());

                setPlayerVisible(true);

                return true;

            }

        });

    }
    private void searchAllRadioCategory() {

        DeezerRequest request = DeezerRequestFactory.requestRadiosCategories();

        AsyncDeezerTask task = new AsyncDeezerTask(deezerConnect,

                new RadioCategoriesRequestListener() {

                    @Override
                    public void onResult(final Object result, final Object requestId) {



                        radioCategoryList.clear();



                        try {

                            radioCategoryList.addAll((List<RadioCategory>) result);

                        } catch (ClassCastException e) {

                            handleError(e);

                        }

                        if (radioCategoryList.isEmpty()) {

                            Toast.makeText(RadioActivity.this, "No result", Toast.LENGTH_LONG).show();

                        }



                        radioCategoryAdapter.notifyDataSetChanged();

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
    public void onPlayTrack(final PlayableEntity track) {

        displayTrack((Track)track);

    }
    @Override
    public void onTrackEnded(final PlayableEntity track) {

    }

    @Override
    public void onAllTracksEnded() {

    }





    @Override
    public void onRequestException(final Exception e, final Object requestId) {

        handleError(e);

    }



    @Override
    public void onTooManySkipsException()
    {
        Toast.makeText(this, "Too many skips", Toast.LENGTH_LONG).show();
    }
}
