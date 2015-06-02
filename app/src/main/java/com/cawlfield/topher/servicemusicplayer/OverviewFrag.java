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

/**
 * Created by ccawlfield on 8/19/14.
 */

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

/**
 * A fragment containing a simple view.
 */
public class OverviewFrag extends Fragment {
    private static String TAG = "OverviewFrag";

    ListView lv;
    Button playStop;

    List<PlayListItemBase> playList;
    PlayListItemBase plUpNext = null;
    MySeqAdapter myseq;
    MyUpNextSelected myUpNextSelected = new MyUpNextSelected();
    MainActyCallbacks mainCallback;
    boolean musicPlaying = false;

    public OverviewFrag() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "in onCreate()");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.d(TAG, "in onCreateView");
        View rootView = inflater.inflate(R.layout.fragment_overview, container, false);

        for (PlayListItemBase pli : playList) {
            pli.setFragInfo(myUpNextSelected);
        }

        myUpNextSelected.selectedSong(playList.get(0)); // select the first item

        lv = (ListView) rootView.findViewById(R.id.trackList);
        playStop = (Button) rootView.findViewById(R.id.go_button);

        myseq = new MySeqAdapter(getActivity(), playList);
        lv.setAdapter(myseq);
        lv.setDividerHeight(3);
        lv.setItemsCanFocus(false);
        lv.setFocusable(false);
        lv.setFocusableInTouchMode(false);
        lv.setClickable(false);
        //lv.canAnimate(true);

        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
              @Override
              public void onItemClick(AdapterView<?> parent, View view,
                 int position, long id) {

               // ListView Clicked item index
               int itemPosition     = position;

               // ListView Clicked item value
               String  itemValue    = (String) lv.getItemAtPosition(position);

               // Show Alert
               Toast.makeText(getActivity().getApplicationContext(),
                       "Position :" + itemPosition + "  ListItem : " + itemValue, Toast.LENGTH_LONG)
                 .show();

                  Log.d(TAG, "item click: Position :" + itemPosition);
              }
         });

        playStop.setOnClickListener(new PlayStopListener());

        return rootView;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        mainCallback = (MainActyCallbacks) activity;
        playList = mainCallback.getPlayList();
    }

//    @Override
//    public void onResume() {
//        super.onResume();
//        InputMethodManager inputManager = (InputMethodManager)
//            getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
//
//        inputManager.hideSoftInputFromWindow(getActivity().getCurrentFocus().getWindowToken(),
//            InputMethodManager.HIDE_NOT_ALWAYS);
//    }

    class MySeqAdapter extends ArrayAdapter<PlayListItemBase> {
        Context parentContext;

        public MySeqAdapter(Context ct, List<PlayListItemBase> playList) {
            super(ct, R.layout.play_list_item, playList);
            parentContext = ct;
        }

        @Override
        public View getView (int position, View convertView, ViewGroup parent) {
            //Log.d(TAG, "getView("+position+", ...)");
            return getItem(position).getView(parentContext, convertView, parent);
        }
    }

    class MyUpNextSelected implements UpNextSelectedCallback {
        @Override
        public void selectedSong(PlayListItemBase item) {
            plUpNext = null;
            for (PlayListItemBase ipli : playList) {
                if (ipli == item) {
                    plUpNext = item;
                    ipli.setIsUpNext(true);
                } else {
                    ipli.setIsUpNext(false);
                }
            }
            if (null != lv) {
                lv.invalidateViews();
            }
        }

        @Override
        public void songFinished() {
            musicPlaying = false;
            setPlayStopText();
            incrementUpNext();
        }
    }

    class PlayStopListener implements View.OnClickListener {
        @Override
        public void onClick(View view) {
            if (musicPlaying) {
                for (PlayListItemBase ipli : playList) {
                    ipli.stop(); // overkill
                }
                musicPlaying = false;
                setPlayStopText();
                incrementUpNext();
            } else {
                if (null != plUpNext) {
                    boolean success = plUpNext.play(getActivity());
                    if (success) {
                        musicPlaying = true;
                        setPlayStopText();
                    }
                }
            }
        }
    }

    void incrementUpNext() {
        boolean pickNext = false;
        int nextIdx = playList.indexOf(plUpNext) + 1;
        if (nextIdx >= playList.size()) {
            nextIdx = 0;
        }
        plUpNext.setIsUpNext(false);
        plUpNext = playList.get(nextIdx);
        plUpNext.setIsUpNext(true);
        if (null != lv) {
            lv.invalidateViews();
        }
    }

    private void setPlayStopText() {
        if (musicPlaying) {
            playStop.setText(R.string.stop);
        } else {
            playStop.setText(R.string.play);
        }
    }
}
