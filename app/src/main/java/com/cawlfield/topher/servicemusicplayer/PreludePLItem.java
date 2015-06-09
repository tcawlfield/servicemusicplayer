package com.cawlfield.topher.servicemusicplayer;

import android.app.Fragment;
import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.util.Log;

import java.io.IOException;
import java.util.Calendar;
import java.util.List;

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

public class PreludePLItem extends PlayListItemBase {
    private static String TAG = PreludePLItem.class.getSimpleName();
    private List<Song> songList;
    private int durationTotal = 0;
    private Calendar timeToFinish;
    MediaPlayer mediaPlayer;

    public PreludePLItem(MainActyCallbacks callback) {
        super(callback);
        initNoSong();
    }

    @Override
    public void initNoSong() {
        setTitle("No Preludes Selected");
        setInfo("Tap to select preludes");
    }

    @Override
    public Fragment getSongChoiceFragment(int idx) {
        Fragment pc = PreludeChoice.newInstance(idx);
        return pc;
    }

    public void setSongList(List<Song> sl) {
        songList = sl;
        if (sl == null || sl.size() < 1) {
            setTitle("No Preludes Selected");
            setInfo("Tap to select preludes");
            durationTotal = 0;
        } else {
            setTitle(sl.size() + " songs");
            durationTotal = 0;
            for (Song s : songList) {
                durationTotal += s.getTrackLengthMillis();
            }
            int secs = (durationTotal + 500) / 1000;
            setInfo("total time " + (secs/60) + ":" + (secs % 60));
        }
        // TODO: info could show song length
        //mainCallback.onSongChoiceDone();
    }

    public boolean play(Context ct) {
        if (null == songList || songList.size() < 1) {
            return false;
        }
        Song song = songList.get(0);
        if (null == mediaPlayer) {
            mediaPlayer = new MediaPlayer();
        }
        if (mediaPlayer.isPlaying()) {
            Log.e(TAG, "in play() method but MediaPlayer is already playing something.");
            return false;
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

    class MyOnPrepared implements MediaPlayer.OnPreparedListener {
        @Override
        public void onPrepared(MediaPlayer mp) {
            mp.start();
        }
    }

    class MyOnPlaybackFinished implements MediaPlayer.OnCompletionListener {
        @Override
        public void onCompletion(MediaPlayer mp) {
            mp.release();
            if (null != mediaPlayer) { // possible race condition
                mediaPlayer = null;
            }
            upNextCallback.songFinished();
        }
    }

    public void stop() {
        if (null == mediaPlayer) {
            return;
        }
        if (mediaPlayer.isPlaying()) {
            mediaPlayer.stop();
        }
        mediaPlayer.release();
        mediaPlayer = null;
    }
}
