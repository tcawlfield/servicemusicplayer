package com.cawlfield.topher.servicemusicplayer;

import android.app.Fragment;
import android.content.Context;
import android.util.Log;

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

public class DummyPlayListItem extends PlayListItemBase {
    private static int itemCount = 0;
    private int thisItemNum;
    private static String TAG = "DummyPlayListItem";

    public DummyPlayListItem(MainActyCallbacks callback) {
        super(callback);
        thisItemNum = ++itemCount;
        initNoSong();
    }

    @Override
    public void chooseSong() {
        Log.d(TAG, "Choosing song for item " + thisItemNum);
        super.chooseSong();
    }

    @Override
    public Fragment getSongChoiceFragment(int idx) {
        return null;
    }

    @Override
    public boolean play(Context ct) {
        return false;
    }

    @Override
    public void stop() {
    }

    @Override
    public void initNoSong() {
        setTitle("Dummy Song " + thisItemNum);
        setInfo("Meaningless info");
    }
}
