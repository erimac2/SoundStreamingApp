package com.erimac2.soundstreamingapp;

import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.deezer.sdk.model.Track;
import com.deezer.sdk.player.PlayerWrapper;
import com.deezer.sdk.player.event.BufferState;
import com.deezer.sdk.player.event.OnBufferErrorListener;
import com.deezer.sdk.player.event.OnBufferProgressListener;
import com.deezer.sdk.player.event.OnBufferStateChangeListener;
import com.deezer.sdk.player.event.OnPlayerErrorListener;
import com.deezer.sdk.player.event.OnPlayerProgressListener;
import com.deezer.sdk.player.event.OnPlayerStateChangeListener;
import com.deezer.sdk.player.event.PlayerState;
import com.deezer.sdk.player.exception.NotAllowedToPlayThatSongException;
import com.deezer.sdk.player.exception.StreamLimitationException;

public class PlayerActivity extends BaseActivity {
    private PlayerHandler playerHandler = new PlayerHandler();
    private OnClickHandler onClickHandler = new OnClickHandler();

    protected ImageButton buttonPlayerStop;
    protected ImageButton buttonPlayerPause;
    protected ImageButton buttonPlayerSkipForward;
    protected ImageButton buttonPlayerSkipBackward;
    protected ImageButton buttonPlayerSeekBackward;
    protected ImageButton buttonPlayerSeekForward;

    protected ImageButton buttonPlayerRepeat;

    private SeekBar seekBar;
    private boolean isUserSeeking = false;
    private TextView textTime;
    private TextView textLength;

    private TextView textArtist;
    private TextView textTrack;

    private PlayerWrapper player;

    @Override
    public void setContentView(final int layoutResID)
    {
        super.setContentView(layoutResID);

        buttonPlayerPause = findViewById(R.id.button_pause);
        buttonPlayerStop = findViewById(R.id.button_stop);
        buttonPlayerSkipForward = findViewById(R.id.button_skip_forward);
        buttonPlayerSkipBackward = findViewById(R.id.button_skip_backward);
        buttonPlayerSeekForward = findViewById(R.id.button_seek_forward);
        buttonPlayerSeekBackward = findViewById(R.id.button_seek_backward);
        buttonPlayerRepeat = findViewById(R.id.button_repeat);

        seekBar = findViewById(R.id.seek_progress);
        textTime = findViewById(R.id.text_time);
        textLength = findViewById(R.id.text_length);

        textArtist = findViewById(R.id.text_artist);
        textTrack = findViewById(R.id.text_track);

        buttonPlayerPause.setOnClickListener(onClickHandler);
        buttonPlayerStop.setOnClickListener(onClickHandler);
        buttonPlayerSkipForward.setOnClickListener(onClickHandler);
        buttonPlayerSkipBackward.setOnClickListener(onClickHandler);
        buttonPlayerSeekForward.setOnClickListener(onClickHandler);
        buttonPlayerSeekBackward.setOnClickListener(onClickHandler);
        buttonPlayerRepeat.setOnClickListener(onClickHandler);
    }

    @Override
    protected void onDestroy()
    {
        destroyPlayer();
        super.onDestroy();
    }

    protected void destroyPlayer()
    {
        if(player == null)
        {
            return;
        }
        if(player.getPlayerState() == PlayerState.RELEASED)
        {
            return;
        }
        if(player.getPlayerState() != PlayerState.STOPPED)
        {
            player.stop();
        }
        player.release();
    }
    protected void setAttachedPlayer(final PlayerWrapper player)
    {
        this.player = player;
        player.addOnBufferErrorListener(playerHandler);
        player.addOnBufferStateChangeListener(playerHandler);
        player.addOnBufferProgressListener(playerHandler);

        player.addOnPlayerErrorListener(playerHandler);
        player.addOnBufferStateChangeListener(playerHandler);
        player.addOnPlayerProgressListener(playerHandler);

        if(player.isAllowedToSeek())
        {
            seekBar.setEnabled(true);
        }
    }
    protected void displayTrack(final Track track)
    {
        if((track.getArtist() == null) || (track.getArtist().getName() == null))
        {
            textArtist.setVisibility(View.GONE);
        }
        else
        {
            textArtist.setVisibility(View.VISIBLE);
            textArtist.setText(track.getArtist().getName());
        }

        if(track.getTitle() == null)
        {
            textTrack.setVisibility(View.GONE);
        }
        else
        {
            textTrack.setVisibility(View.VISIBLE);
            textTrack.setText(track.getTitle());
            UserDB userDB = new UserDB(track.getId(), track.getTitle());
            database.userDao().insert(userDB);
        }
    }
    protected void setPlayerVisible(final boolean visible)
    {
        if(visible)
        {
            findViewById(R.id.player).setVisibility(View.VISIBLE);
        }
        else
        {
            findViewById(R.id.player).setVisibility(View.GONE);
        }
    }
    protected void setButtonEnabled(final View button, final boolean enabled)
    {
        if(enabled)
        {
            button.setVisibility(View.VISIBLE);
        }
        else
        {
            button.setVisibility(View.GONE);
        }
        button.setEnabled(enabled);
    }
    public void showPlayerProgress(final long timePosition)
    {
        if(!isUserSeeking)
        {
            seekBar.setProgress((int)timePosition / 1000);
            String text = formatTime(timePosition);
            textTime.setText(text);
        }

        seekBar.setEnabled(false);
    }
    public void showPlayerState(final PlayerState state)
    {
        seekBar.setEnabled(true);
        buttonPlayerPause.setEnabled(true);
        buttonPlayerStop.setEnabled(true);

        Log.i("STATE_INCOMING", state.toString());
        switch(state)
        {
            case STARTED:
                buttonPlayerPause.setEnabled(true);
                buttonPlayerPause.setImageResource(R.drawable.ic_action_play);
                break;
            case INITIALIZING:
                buttonPlayerPause.setEnabled(true);
                buttonPlayerPause.setImageResource(R.drawable.ic_action_play);
                break;
            case READY:
                buttonPlayerPause.setEnabled(true);
                buttonPlayerPause.setImageResource(R.drawable.ic_action_play);
                showPlayerProgress(0);
                break;
            case PLAYING:
                buttonPlayerStop.setEnabled(true);
                buttonPlayerPause.setEnabled(true);
                buttonPlayerPause.setImageResource(R.drawable.ic_action_pause);
                break;
            case PAUSED:
                buttonPlayerPause.setEnabled(true);
                buttonPlayerPause.setImageResource(R.drawable.ic_action_play);
            case PLAYBACK_COMPLETED:
                buttonPlayerPause.setEnabled(true);
                buttonPlayerPause.setImageResource(R.drawable.ic_action_play);
                break;
            case WAITING_FOR_DATA:
                buttonPlayerPause.setEnabled(false);
                break;
            case STOPPED:
                seekBar.setEnabled(false);
                showPlayerProgress(0);
                showBufferProgress(0);
                buttonPlayerPause.setImageResource(R.drawable.ic_action_play);
                buttonPlayerStop.setEnabled(false);
                break;
                default:
                    break;
        }
    }
    public void showBufferProgress(final int position)
    {
        synchronized (this)
        {
            if(player != null)
            {
                if(position > 0)
                {
                    showTrackDuration(player.getTrackDuration());
                }
                long progress = (position * player.getTrackDuration()) / 100;
                seekBar.setSecondaryProgress((int)progress / 1000);
            }
        }
    }
    public void showTrackDuration(final long trackLength)
    {
        String text = formatTime(trackLength);
        textLength.setText(text);
        seekBar.setMax((int)trackLength / 1000);
    }
    private static String formatTime(long time)
    {
        time /= 1000;
        long seconds = time % 60;
        time /= 60;
        long minutes = time % 60;
        time /= 60;
        long hours = time;
        StringBuilder builder = new StringBuilder(8);
        doubleDigit(builder, seconds);
        builder.insert(0, ':');
        if(hours == 0)
        {
            builder.insert(0, minutes);
        }
        else
        {
            doubleDigit(builder, minutes);
            builder.insert(0, ':');
            builder.insert(0, hours);
        }
        return builder.toString();
    }
    private static void doubleDigit(final StringBuilder builder, final long value)
    {
        builder.insert(0, value);
        if(value < 10)
        {
            builder.insert(0, '0');
        }
    }

    private class OnClickHandler implements View.OnClickListener
    {
        @Override
        public void onClick(final View v)
        {
            if(v == buttonPlayerPause)
            {
                if(player.getPlayerState() == PlayerState.PLAYING)
                {
                    player.pause();
                    showPlayerState(player.getPlayerState());
                }
                else
                {
                    player.play();
                    showPlayerState(player.getPlayerState());
                }
            }
            else if(v == buttonPlayerStop)
            {
                player.stop();
                showPlayerState(player.getPlayerState());
                showPlayerProgress(0);
                showBufferProgress(0);
                setPlayerVisible(false);
            }
            else if(v == buttonPlayerSkipForward)
            {
                skipToNextTrack();
            }
            else if(v == buttonPlayerSkipBackward)
            {
                skipToPreviousTrack();
            }
            else if(v == buttonPlayerSeekBackward)
            {
                try
                {
                    player.seek(player.getPosition() - (10 * 1000));
                }
                catch(Exception e)
                {
                    handleError(e);
                }
            }
            else if(v == buttonPlayerSeekForward)
            {
                try
                {
                    player.seek(player.getPosition() + (10 * 1000));
                }
                catch(Exception e)
                {
                    handleError(e);
                }
            }
            else if(v == buttonPlayerRepeat)
            {
                switchRepeatMode();
            }
        }
    }
    protected void skipToNextTrack()
    {
    }
    protected void skipToPreviousTrack()
    {
    }
    protected  void switchRepeatMode()
    {
        PlayerWrapper.RepeatMode current = player.getRepeatMode();
        PlayerWrapper.RepeatMode next;
        String toast;

        switch (current)
        {
            case NONE:
                next = PlayerWrapper.RepeatMode.ONE;
                toast = "Repeat One";
                break;
            case ONE:
                next = PlayerWrapper.RepeatMode.ALL;
                toast = "Repeat All";
                break;
            case ALL:
                default:
                    next = PlayerWrapper.RepeatMode.NONE;
                    toast = "No Repeat";
                    break;
        }
        player.setRepeatMode(next);
        Toast.makeText(this, toast, Toast.LENGTH_SHORT).show();
    }

    private class PlayerHandler implements
            OnPlayerProgressListener,
            OnBufferProgressListener,
            OnPlayerStateChangeListener,
            OnBufferStateChangeListener,
            OnPlayerErrorListener,
            OnBufferErrorListener
    {
        @Override
        public void onBufferError(final Exception ex, final double percent)
        {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    handleError(ex);
                }
            });
        }
        @Override
        public void onBufferStateChange(final BufferState state, final double percent)
        {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    showBufferProgress((int)Math.round(percent));
                }
            });
        }
        @Override
        public void onPlayerError(final Exception ex, final long timePosition)
        {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    handleError(ex);
                    if(ex instanceof NotAllowedToPlayThatSongException)
                    {
                        player.skipToNextTrack();
                    }
                    else if(ex instanceof StreamLimitationException)
                    {

                    }
                    else
                    {
                        finish();
                    }
                }
            });
        }
        @Override
        public void onPlayerStateChange(final PlayerState state, final long timePosition)
        {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    showPlayerState(state);
                    showPlayerProgress(timePosition);
                }
            });
        }
        @Override
        public void onBufferProgress(final double percent)
        {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    showBufferProgress((int)Math.round(percent));
                }
            });
        }
        @Override
        public void onPlayerProgress(final long timePosition)
        {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    showPlayerProgress(timePosition);
                }
            });
        }
    }
}
