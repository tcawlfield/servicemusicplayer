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
import android.text.Layout;
import android.util.Log;
import android.view.View;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

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

    public abstract boolean play(Context ct);

    public abstract void stop();
}
