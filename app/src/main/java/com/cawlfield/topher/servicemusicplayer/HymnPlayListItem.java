package com.cawlfield.topher.servicemusicplayer;

import android.app.Fragment;
import android.content.ContentUris;
import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.PowerManager;
import android.util.Log;

import java.io.IOException;
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

public class HymnPlayListItem extends PlayListItemBase {
    private static int itemCount = 0;
    private int thisItemNum;
    private static String TAG = HymnPlayListItem.class.getSimpleName();
    private MusicCatalog.Hymn hymn;
    MediaPlayer mediaPlayer;

    public HymnPlayListItem(MainActyCallbacks callback) {
        super(callback);
        thisItemNum = ++itemCount;
        initNoSong();
    }

    @Override
    public void initNoSong() {
        setTitle("No Hymn Selected");
        setInfo("Tap to select a hymn number");
    }

    @Override
    public Fragment getSongChoiceFragment(int idx) {
        Fragment hc = HymnChoice.newInstance(idx);
        return hc;
    }

    public void setHymn(MusicCatalog.Hymn hymn) {
        this.hymn = hymn;
        setTitle(hymn.title);
        setInfo("Tap to change");
        //mainCallback.onSongChoiceDone();
    }

    @Override
    protected Song getSong() {
        return hymn;
    }
}
