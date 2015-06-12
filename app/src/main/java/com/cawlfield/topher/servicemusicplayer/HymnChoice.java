package com.cawlfield.topher.servicemusicplayer;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.app.Fragment;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.SeekBar;
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
public class HymnChoice extends Fragment implements AdapterView.OnItemClickListener {

    private static int MAX_HYMN_DIGITS = 3;
    private static String TAG = "HymnChoice";

    EditText hymnEntry;
    ListView hymnListView;
    Button doneButton;
    List<MusicCatalog.Hymn> hymnList;
    List<String> hymnListStr;
    ArrayAdapter hymnListViewAdapter;
    Runnable updateListRunnable;
    Handler handler;
    HymnPlayListItem hymnPlayListItem;
    MainActyCallbacks mainActyCallbacks;
    SongPlayer songPlayer;

    public HymnChoice() {
        super();
    }

    public static HymnChoice newInstance(int index) {
        HymnChoice hc = new HymnChoice();

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
        View rootView = inflater.inflate(R.layout.fragment_hymn_choice, container, false);

        hymnEntry = (EditText) rootView.findViewById(R.id.hymnNumber);
        hymnListView = (ListView) rootView.findViewById(R.id.hymnList);
        doneButton = (Button) rootView.findViewById(R.id.okay_button);

        hymnListStr = new ArrayList<String>(); // initially empty
        hymnListViewAdapter = new ArrayAdapter(getActivity(), android.R.layout.simple_list_item_activated_1, hymnListStr);
        hymnListView.setAdapter(hymnListViewAdapter);
        hymnListView.setClickable(true);
        hymnListView.setOnItemClickListener(this);

        updateListRunnable = new UpdateHymnListRunnable();

        ImageButton playPause = (ImageButton) rootView.findViewById(R.id.play_pause);
        SeekBar sb = (SeekBar) rootView.findViewById(R.id.seek_bar);
        songPlayer = new SongPlayer(getActivity(), playPause, sb);

        hymnEntry.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) { }

            @Override
            public void onTextChanged(CharSequence charSequence, int start, int before, int count) {
                Log.d(TAG, "in onTextChanged()");
                handler.removeCallbacks(updateListRunnable);
                if (charSequence.length() >= MAX_HYMN_DIGITS) {
                    handler.post(updateListRunnable);
                } else {
                    handler.postDelayed(updateListRunnable, 100);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) { }
        });

        hymnEntry.setOnEditorActionListener(
                new EditText.OnEditorActionListener() {
                    @Override
                    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                        if (actionId == EditorInfo.IME_ACTION_DONE) {
                            done();
                            return true;
                        }
                        return false;
                    }
                });

        doneButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                done();
            }
        });

        List<PlayListItemBase> pl = mainActyCallbacks.getPlayList();
        hymnPlayListItem = (HymnPlayListItem) pl.get(getArguments().getInt("index", 0));

        return rootView;
    }


    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        mainActyCallbacks = (MainActyCallbacks) activity;
    }

//    @Override
//    public void onAttach(Activity activity) {
//        super.onAttach(activity);
//        try {
//            mListener = (OnFragmentInteractionListener) activity;
//        } catch (ClassCastException e) {
//            throw new ClassCastException(activity.toString()
//                    + " must implement OnFragmentInteractionListener");
//        }
//    }

    @Override
    public void onResume() {
        super.onResume();
        InputMethodManager imm = (InputMethodManager) getActivity()
                .getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.showSoftInput(hymnEntry, InputMethodManager.SHOW_IMPLICIT);
        songPlayer.fragmentStarting();
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        // An item in the hymn list was clicked.
        //Song selectedSong = (Song) hymnListViewAdapter.getItem(position);
        Song selectedSong = (Song) hymnList.get(position);
        songPlayer.setSong(selectedSong);
    }

    class UpdateHymnListRunnable implements Runnable {
        @Override
        public void run() {
            Log.d(TAG, "in UpdateHymnListRunnable.run()");
            String hymnNumStr = hymnEntry.getText().toString();
            Log.d(TAG, "hymnNumStr = " + hymnNumStr);
            try {
                int hymnNum = Integer.parseInt(hymnNumStr);
                hymnList = MusicCatalog.getInstance().getHymns(hymnNum);
                if (hymnList.isEmpty()) {
                    // empty out hymn list
                    hymnListStr.clear();
                    hymnListViewAdapter.notifyDataSetChanged();
                } else {
                    hymnListStr.clear();
                    for (MusicCatalog.Hymn h : hymnList) {
                        Log.d(TAG, "Found hymn " + h.title);
                        hymnListStr.add(h.title);
                    }
                    hymnListViewAdapter.notifyDataSetChanged();
                    hymnListView.setItemChecked(0, true);
                }
            } catch (NumberFormatException e) {}
        }
    }

    @Override
    public void onDetach() {
        songPlayer.fragmentFinishing();
        super.onDetach();
        //mListener = null;
    }

    void done() {
        if (hymnList == null || hymnList.isEmpty()) {
            Log.d(TAG, "No matching hymns");
            return;
        }
        int chosenIdx = hymnListView.getCheckedItemPosition();
        Log.d(TAG, "Selected item position is "+ chosenIdx);
        if (chosenIdx >= 0 && chosenIdx < hymnList.size()) {
            MusicCatalog.Hymn chosen = hymnList.get(chosenIdx);
            hymnPlayListItem.setHymn(chosen);
            mainActyCallbacks.onSongChoiceDone();
        }
    }

    //TODO: preview and cancel button
}
