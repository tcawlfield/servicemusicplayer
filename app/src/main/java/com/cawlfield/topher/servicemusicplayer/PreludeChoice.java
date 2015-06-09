package com.cawlfield.topher.servicemusicplayer;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.app.Fragment;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
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

/**
 * A simple {@link Fragment} subclass.
 *
 */
public class PreludeChoice extends Fragment {

    private static int MAX_HYMN_DIGITS = 3;
    private static String TAG = PreludeChoice.class.getSimpleName();

    TextView endTimeTV;
    ListView preludeLV;
    Button okayBtn;
    Button randomizeBtn;
    TextView upToTime;
    Button addBtn;
    Button delBtn;
    ImageButton mvUpBtn;
    ImageButton mvDownBtn;

    List<Song> songList;
    SongListArrayAdapter songListViewAdapter;
    Runnable updateListRunnable;
    Handler handler;
    PreludePLItem preludePLItem;
    MainActyCallbacks mainActyCallbacks;

    List<String> songListStr;

    public PreludeChoice() {
        super();
    }

    public static PreludeChoice newInstance(int index) {
        PreludeChoice hc = new PreludeChoice();

        // Supply index input as an argument.
        Bundle args = new Bundle();
        args.putInt("index", index);
        hc.setArguments(args);

        return hc;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        handler = new Handler(Looper.getMainLooper());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_prelude_choice, container, false);

        endTimeTV = (TextView) rootView.findViewById(R.id.end_time);
        preludeLV = (ListView) rootView.findViewById(R.id.prelude_list);
        okayBtn = (Button) rootView.findViewById(R.id.okay_button);
        randomizeBtn = (Button) rootView.findViewById(R.id.randomize_btn);
        upToTime = (TextView) rootView.findViewById(R.id.up_to_time);
        addBtn = (Button) rootView.findViewById(R.id.add_btn);
        delBtn = (Button) rootView.findViewById(R.id.remove_btn);
        mvUpBtn = (ImageButton) rootView.findViewById(R.id.move_up);
        mvDownBtn = (ImageButton) rootView.findViewById(R.id.move_down);

        songList = new ArrayList<Song>();

        MusicCatalog mc = MusicCatalog.getInstance();
        mc.refreshMusicCatalog();
        int max = 4;
        for (Song s : mc.allSongs) {
            songList.add(s);
            if (--max == 0) {
                break;
            }
        }

        songListViewAdapter = new SongListArrayAdapter(getActivity(), songList);
        preludeLV.setAdapter(songListViewAdapter);
        preludeLV.setClickable(true);

        updateListRunnable = new UpdateSongListRunnable();

        handler.removeCallbacks(updateListRunnable);
        handler.post(updateListRunnable);

        okayBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                done();
            }
        });

        List<PlayListItemBase> pl = mainActyCallbacks.getPlayList();
        preludePLItem = (PreludePLItem) pl.get(getArguments().getInt("index", 0));

        return rootView;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        mainActyCallbacks = (MainActyCallbacks) activity;
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    class UpdateSongListRunnable implements Runnable {
        @Override
        public void run() {
            Log.d(TAG, "in UpdateSongListRunnable.run() " + songList.size() + "items");
            songListViewAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    void done() {
        preludePLItem.setSongList(songList);
        mainActyCallbacks.onSongChoiceDone();
    }

    class SongListArrayAdapter extends ArrayAdapter<Song> {
        static final int LAYOUT_ID = R.layout.list_item_song;
        public SongListArrayAdapter(Context context, List<Song> objects) {
            super(context, LAYOUT_ID, R.id.text1, objects);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            Log.d(TAG, "getView for item at " + position);
            View v = convertView;
            if (v == null) {
                LayoutInflater li = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                v = li.inflate(LAYOUT_ID, null);
            }
            TextView line1 = (TextView) v.findViewById(R.id.text1);
            TextView line2 = (TextView) v.findViewById(R.id.text2);
            Song s = getItem(position);
            int duration = (s.getTrackLengthMillis() + 500) / 1000;
            String durationStr = (duration / 60) + ":" + (duration % 60);
            String l1 = s.title + " (" + durationStr + ")";
            line1.setText(l1);
            line2.setText(s.album);
            return v;
        }
    }
}
