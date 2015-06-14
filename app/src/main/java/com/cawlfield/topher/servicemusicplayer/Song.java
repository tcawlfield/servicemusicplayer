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

import android.content.ContentUris;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.util.Log;

import java.io.IOException;

/**
 * Created by ccawlfield on 6/9/15.
 */
class Song {
    static final String TAG = Song.class.getSimpleName();
    String album;
    String title;
    String artist;
    int track;
    long id;
    private int duration = 0;

    Uri getUri() {
        return ContentUris.withAppendedId(
                android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, id);
    }

    int getTrackLengthMillis() {
        if (duration > 0) {
            return duration;
        }
        MediaPlayer mediaPlayer = new MediaPlayer();
        if (mediaPlayer.isPlaying()) {
            // How can this be? This is a fresh new player instance!
            Log.e(TAG, "in getTrackLengthMillis() method but MediaPlayer is already playing something.");
            return -1;
        }
        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);

        try {
            mediaPlayer.setDataSource(MainActy.getAppContext(), getUri());
        } catch (IOException e) {
            Log.d(TAG, "Error finding data source: ", e);
            return -1;
        }
        try {
            mediaPlayer.prepare();
            duration = mediaPlayer.getDuration();
        } catch (IOException e) {
            Log.e(TAG, "Must use prepareAsync for this media type -- probably streaming:" + title);
            duration = -1;
        }
        mediaPlayer.reset();
        mediaPlayer.release();
        mediaPlayer = null;
        return duration;
    }

    public String toString() { // for ArrayAdapter
        return title;
    }
}
