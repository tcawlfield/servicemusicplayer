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
        mediaPlayer.release();
        return duration;
    }
}
