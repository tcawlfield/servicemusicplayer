/**
 *   Copyright (C) 2014-2015 Topher Cawlfield <tcawlfield_at_gmail_dot_com>
 * 
 *   This file is part of ServiceMusicPlayer.
 *
 *   ServiceMusicPlayer is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   ServiceMusicPlayer is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License for more details.
 *
 *   You should have received a copy of the GNU General Public License
 *   along with ServiceMusicPlayer.  If not, see <http://www.gnu.org/licenses/>.
 **/

package com.cawlfield.topher.servicemusicplayer;

import android.app.Fragment;
import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.text.Layout;
import android.util.Log;
import android.view.View;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.io.IOException;

/**
 * Created by ccawlfield on 8/25/14.
 */
public abstract class PlayListItemBase {
    private static String TAG = "PlayListItemBase";
    static LayoutInflater myInflater = null;
    View myView;
    CheckBox upNext;
    TextView mainText;
    TextView subText;
    String mainStr = null;
    String subStr = null;
    boolean isUpNext = false;
    UpNextSelectedCallback upNextCallback;
    MainActyCallbacks mainCallback;
    MediaPlayer mediaPlayer;
    private Context lastPlayContext;

    public PlayListItemBase(MainActyCallbacks callback) {
        mainCallback = callback;
    }

    View getView(Context ct, View convertView, ViewGroup parent) {
        //Log.d(TAG, "In getView");
        myView = convertView;
        if (null == myView) {
            if (null == myInflater) {
                myInflater = (LayoutInflater) ct.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            }
            myView = myInflater.inflate(R.layout.play_list_item, parent, false);
        }
        upNext = (CheckBox) myView.findViewById(R.id.upNext);
        mainText = (TextView) myView.findViewById(R.id.songTitle);
        subText = (TextView) myView.findViewById(R.id.songInfo);
        RelativeLayout clickBox = (RelativeLayout) myView.findViewById(R.id.songClickBox);
        clickBox.setOnClickListener(new MyOnClickListener());
        //upNext.setClickable(true);
        upNext.setOnClickListener(new CheckBox.OnClickListener() {
            public void onClick(View v) {
                Log.d(TAG, "clicked upNext for " + mainStr + " view " + System.identityHashCode(v));
                upNextCallback.selectedSong(PlayListItemBase.this);
            }
        });
        upNext.setChecked(isUpNext);
        if (mainStr != null) {
            mainText.setText(mainStr);
        }
        if (subStr != null) {
            subText.setText(subStr);
        }

        return myView;
    }
    
    public void setFragInfo(UpNextSelectedCallback cb) {
        upNextCallback = cb;
    }

    public void setTitle(String title) {
        mainStr = title;
        if (mainText != null) {
            mainText.setText(title);
        }
    }

    public void setInfo(String info) {
        subStr = info;
        if (subText != null) {
            subText.setText(info);
        }
    }

    public void setIsUpNext(boolean itIs) {
        isUpNext = itIs;
        if (null != upNext) {
            upNext.setChecked(isUpNext);
            //upNext.invalidate();
            //myView.invalidate();
        }
        //Log.d(TAG, "setIsUpNext("+itIs+") for " + mainStr + "(view "+System.identityHashCode(upNext)+")");
    }

    public boolean isUpNext() {
        return isUpNext;
    }

    // This is a callback for the clickbox.
    private class MyOnClickListener implements View.OnClickListener {
        public void onClick(View v) {
            chooseSong();
        }
    }

    public void chooseSong() {
        mainCallback.onSongChoiceClick(this);
    }

    public abstract void initNoSong();

    public abstract Fragment getSongChoiceFragment(int idx);

    // Implement this to return the Song to play.
    protected abstract Song getSong();

    public boolean play(Context ct) {
        lastPlayContext = ct;
        Song song = getSong();
        if (null == song) {
            return false;
        }

        if (null == mediaPlayer) {
            mediaPlayer = new MediaPlayer();
        } else if (mediaPlayer.isPlaying()) {
            Log.e(TAG, "in play() method but MediaPlayer is already playing something.");
            return false;
        } else {
            mediaPlayer.reset();
        }

        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        Uri contentUri = song.getUri();

        try {
            mediaPlayer.setDataSource(ct, contentUri);
        } catch (IOException e) {
            Log.d(TAG, "Error finding data source: ", e);
            return false;
        }
        //mediaPlayer.setWakeMode(ct, PowerManager.SCREEN_BRIGHT_WAKE_LOCK);
        mediaPlayer.setOnPreparedListener(new MyOnPrepared());
        mediaPlayer.setOnCompletionListener(new MyOnPlaybackFinished());
        mediaPlayer.prepareAsync();
        return true;
    }

    // override this if this playlistitem has multiple songs.
    protected boolean moreToPlay() {
        return false;
    }

    protected int getProgress() {
        if (mediaPlayer != null && mediaPlayer.isPlaying()) {
            return mediaPlayer.getCurrentPosition();
        } else {
            return 0;
        }
    }

    class MyOnPrepared implements MediaPlayer.OnPreparedListener {
        @Override
        public void onPrepared(MediaPlayer mp) {
            upNextCallback.setProgressMax(mp.getDuration());
            mp.start();
        }
    }

    class MyOnPlaybackFinished implements MediaPlayer.OnCompletionListener {
        @Override
        public void onCompletion(MediaPlayer mp) {
            if (moreToPlay()) {
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {}
                play(lastPlayContext);
            } else {
                mp.reset();
                mp.release();
                if (null != mediaPlayer) { // possible race condition
                    mediaPlayer = null;
                }
                upNextCallback.songFinished();
            }
        }
    }

    public void stop() {
        if (null == mediaPlayer) {
            return;
        }
        if (mediaPlayer.isPlaying()) {
            mediaPlayer.stop();
        }
        mediaPlayer.reset();
        mediaPlayer.release();
        mediaPlayer = null;
    }

    public boolean isPlaying() {
        if (mediaPlayer == null) {
            return false;
        }
        return mediaPlayer.isPlaying();
    }

    public int getDuration() {
        if (mediaPlayer == null) {
            return 100;
        } else {
            return mediaPlayer.getDuration();
        }
    }
}
