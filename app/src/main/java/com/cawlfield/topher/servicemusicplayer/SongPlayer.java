package com.cawlfield.topher.servicemusicplayer;

import android.app.Activity;
import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.SeekBar;

import java.io.IOException;

/**
 * Created by topher on 6/11/15.
 */
public class SongPlayer implements View.OnClickListener, SeekBar.OnSeekBarChangeListener {
    private static final String TAG = SongPlayer.class.getSimpleName();

    ImageButton playPauseBtn;
    SeekBar seekBar;
    Context ct;
    Song song;
    MediaPlayer mp;
    SeekBarUpdater sbu;

    @Override
    public void onProgressChanged(SeekBar sk, int progress, boolean fromUser) {
        if(fromUser && mp != null && state != State.OTHER) {
            mp.seekTo(progress);
        }
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
    }

    enum State {
        PLAYING,
        PAUSED,
        OTHER
    }
    State state;

    public SongPlayer(Context context, ImageButton playPause, SeekBar sb) {
        ct = context;
        playPauseBtn = playPause;
        seekBar = sb;

        playPause.setOnClickListener(this);
        seekBar.setOnSeekBarChangeListener(this);
    }

    public void setSong(Song song) {
        state = State.OTHER;
        playPauseBtn.setEnabled(false);
        playPauseBtn.setBackgroundResource(android.R.drawable.ic_media_play);
        seekBar.setEnabled(false);
        seekBar.setProgress(0);
        if (sbu != null) {
            sbu.cancel(true);
            sbu = null;
        }

        this.song = song;
        if (mp == null) {
            mp = new MediaPlayer();
        } else {
            if (mp.isPlaying()) {
                mp.stop();
            }
            mp.reset();
        }

        if (song == null) {
            // un-set the song
            mp.reset();
            return;
        }

        mp.setAudioStreamType(AudioManager.STREAM_MUSIC);
        try {
            mp.setDataSource(ct, song.getUri());
        } catch (IOException e) {
            Log.d(TAG, "Error finding data source: ", e);
            mp.reset();
            return;
        }
        mp.setOnPreparedListener(new MyOnPrepared());
        mp.setOnCompletionListener(new MyOnPlaybackFinished());
        mp.prepareAsync();

        seekBar.setMax(song.getTrackLengthMillis());
    }

    class MyOnPrepared implements MediaPlayer.OnPreparedListener {
        @Override
        public void onPrepared(MediaPlayer mp) {
            playPauseBtn.setEnabled(true);
            playPauseBtn.setBackgroundResource(android.R.drawable.ic_media_play);
            state = State.PAUSED;
            seekBar.setProgress(0);
            seekBar.setEnabled(true);
        }
    }

    class MyOnPlaybackFinished implements MediaPlayer.OnCompletionListener {
        @Override
        public void onCompletion(MediaPlayer mp) {
            //state = State.OTHER;
            //playPauseBtn.setEnabled(false);
            seekBar.setEnabled(false);
            if (sbu != null) { // better not be!
                sbu.cancel(true);
                sbu = null;
            }
            // Need to take state machine from PlaybackCompleted to
            // either Prepared or Started. Can't get to Prepared too easily.
            // So we go to Paused instead.
            mp.start();
            mp.pause();
            state = State.PAUSED;
            playPauseBtn.setEnabled(true);
            seekBar.setProgress(0);
            playPauseBtn.setBackgroundResource(android.R.drawable.ic_media_play);
        }
    }

    public void fragmentFinishing() {
        if (mp != null) {
            mp.reset();
            mp.release();
            mp = null;
        }
        state = State.OTHER;
    }

    public void fragmentStarting() {
        playPauseBtn.setEnabled(false);
        state = State.OTHER;
        seekBar.setProgress(0);
        seekBar.setEnabled(false);
    }

    @Override
    public void onClick(View view) {
        if (state == State.PAUSED) {
            mp.start();
            playPauseBtn.setBackgroundResource(android.R.drawable.ic_media_pause);
            state = State.PLAYING;
            if (sbu != null) {
                sbu.cancel(true);
            }
            sbu = new SeekBarUpdater();
            sbu.execute();
            // kick off async
        } else if (state == State.PLAYING) {
            mp.pause();
            state = State.PAUSED;
            playPauseBtn.setBackgroundResource(android.R.drawable.ic_media_play);
        }
    }

    class SeekBarUpdater extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... unused) {
            do {
                publishProgress();
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {}
            } while (! isCancelled());
            return null;
        }

        @Override
        protected void onProgressUpdate(Void... ignored) {
            if(mp != null){
            int mCurrentPosition = mp.getCurrentPosition();
                seekBar.setProgress(mCurrentPosition);
            }
        }
    }
}
