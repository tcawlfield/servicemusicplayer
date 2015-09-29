package com.cawlfield.topher.servicemusicplayer;

import android.app.Activity;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.app.Fragment;
import android.os.Handler;
import android.os.Looper;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.TimePicker;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
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

/**
 * A simple {@link Fragment} subclass.
 *
 */
public class PreludeChoice extends Fragment implements View.OnClickListener, AdapterView.OnItemClickListener {

    private static int MAX_HYMN_DIGITS = 3;
    private static String TAG = PreludeChoice.class.getSimpleName();

    TextView endTimeTV;
    ListView preludeLV;
    Button okayBtn;
    //Button randomizeBtn;
    //TextView upToTime;
    Button addBtn;
    Button delBtn;
    ImageButton mvUpBtn;
    ImageButton mvDownBtn;
    TextView totalTime;

    List<Song> songList;
    SongListArrayAdapter songListViewAdapter;
    Runnable updateListRunnable;
    Handler handler;
    PreludePLItem preludePLItem;
    MainActyCallbacks mainActyCallbacks;
    SongPlayer songPlayer;

    int endHour;
    int endMinute;
    int targetDurSeconds = 10 * 60;

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
        //randomizeBtn = (Button) rootView.findViewById(R.id.randomize_btn);
        //upToTime = (TextView) rootView.findViewById(R.id.up_to_time);
        addBtn = (Button) rootView.findViewById(R.id.add_btn);
        delBtn = (Button) rootView.findViewById(R.id.remove_btn);
        mvUpBtn = (ImageButton) rootView.findViewById(R.id.move_up);
        mvDownBtn = (ImageButton) rootView.findViewById(R.id.move_down);
        totalTime = (TextView) rootView.findViewById(R.id.total_time);

        songListViewAdapter = new SongListArrayAdapter(getActivity(), songList);
        preludeLV.setAdapter(songListViewAdapter);
        preludeLV.setClickable(true);
        preludeLV.setOnItemClickListener(this);

        endTimeTV.setOnClickListener(this);
        okayBtn.setOnClickListener(this);
        //randomizeBtn.setOnClickListener(this);
        //upToTime.setOnClickListener(this);
        addBtn.setOnClickListener(this);
        delBtn.setOnClickListener(this);
        mvUpBtn.setOnClickListener(this);
        mvDownBtn.setOnClickListener(this);

        ImageButton playPause = (ImageButton) rootView.findViewById(R.id.play_pause);
        SeekBar sb = (SeekBar) rootView.findViewById(R.id.seek_bar);
        songPlayer = new SongPlayer(getActivity(), playPause, sb);

        return rootView;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        mainActyCallbacks = (MainActyCallbacks) activity;

        List<PlayListItemBase> pl = mainActyCallbacks.getPlayList();
        preludePLItem = (PreludePLItem) pl.get(getArguments().getInt("index", 0));
        songList = preludePLItem.songList;
        if (songList == null) {
            songList = new ArrayList<Song>();
        }
        endHour = preludePLItem.endHour;
        endMinute = preludePLItem.endMinute;
    }

    @Override
    public void onResume() {
        super.onResume();

        handler.removeCallbacks(updateListRunnable);
        handler.post(updateListRunnable);

        updateStartTime();
        //updateUpToTime();
        updateTotalTime();
        songPlayer.fragmentStarting();
    }

    @Override
    public void onClick(View view) {
        if (view == endTimeTV) {
            showEndTimePickerDialog();
        } else if (view == okayBtn) {
            done();
        } else if (view == addBtn) {
            addASong();
        } else if (view == delBtn) {
            delSong();
        } else if (view == mvUpBtn) {
            moveSong(true);
        } else if (view == mvDownBtn) {
            moveSong(false);
        //} else if (view == upToTime) {
        //    showUpToTimePickerDialog();
        }
    }

    @Override
    public void onDetach() {
        songPlayer.fragmentFinishing();
        super.onDetach();
    }

    void done() {
        preludePLItem.setSongList(songList);
        preludePLItem.endHour = endHour;
        preludePLItem.endMinute = endMinute;
        mainActyCallbacks.onSongChoiceDone();
    }

    void addASong() {
        AddSongFragment nextFrag = AddSongFragment.newInstance(this);
        nextFrag.show(getFragmentManager(), "addSong");
    }

    void delSong() {
        Log.d(TAG, "delSong()");
        if (songList != null && songList.size() > 0) {
            int pos = preludeLV.getCheckedItemPosition();
            if (pos != AdapterView.INVALID_POSITION) {
                songList.remove(pos);
                songListViewAdapter.notifyDataSetChanged();
                updateTotalTime();
                //preludeLV.dispatchSetSelected(false);
            }
        }
    }

    void moveSong(boolean moveUp) {
        Log.d(TAG, "moveSong(moveUp="+moveUp+")");
        if (songList != null && songList.size() > 1) {
            int pos = preludeLV.getCheckedItemPosition();
            if (pos != AdapterView.INVALID_POSITION) {
                int newPos;
                if (moveUp && pos > 0) {
                    newPos = pos - 1;
                } else if (! moveUp && pos+1 < songList.size()) {
                    newPos = pos + 1;
                } else {
                    return;
                }
                Song moving = songList.get(pos);
                songList.remove(pos);
                songList.add(newPos, moving);
                songListViewAdapter.notifyDataSetChanged();
                updateTotalTime();
                preludeLV.setItemChecked(pos, false);
                preludeLV.setItemChecked(newPos, true);
            }
        }
    }

    public void showEndTimePickerDialog() {
        EndTimePickerFragment newFragment = new EndTimePickerFragment();
        newFragment.setPlC(this);
        newFragment.show(getFragmentManager(), "timePicker");
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        // An item in the prelude list was clicked.
        Song selectedSong = songListViewAdapter.getItem(position);
        songPlayer.setSong(selectedSong);
    }

    public static class EndTimePickerFragment extends DialogFragment
            implements TimePickerDialog.OnTimeSetListener {
        private PreludeChoice plc;

        public void setPlC(PreludeChoice plc) {
            this.plc = plc;
        }

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            // Use the current time as the default values for the picker
            //final Calendar c = Calendar.getInstance();
            int hour = plc.endHour;
            int minute = plc.endMinute;

            // Create a new instance of TimePickerDialog and return it
            return new TimePickerDialog(getActivity(), this, hour, minute,
                    DateFormat.is24HourFormat(getActivity()));
        }

        public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
            plc.endHour = hourOfDay;
            plc.endMinute = minute;
            plc.updateStartTime();
        }
    }

    void updateStartTime() {
        java.text.DateFormat df;
        if (DateFormat.is24HourFormat(getActivity())) {
            df = new SimpleDateFormat("HH:mm");
        } else {
            df = new SimpleDateFormat("KK:mm a");
        }
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, endHour);
        cal.set(Calendar.MINUTE, endMinute);
        endTimeTV.setText(df.format(cal.getTime()));
    }

    //void updateUpToTime() {
    //    upToTime.setText(MinSec.toString(targetDurSeconds * 1000));
    //}

    void updateTotalTime() {
        int millis = 0;
        for (Song s : songList) {
            millis += s.getTrackLengthMillis();
        }
        totalTime.setText(MinSec.toString(millis));
    }

    //void showUpToTimePickerDialog() {

    //}

    void addThisSong(Song song) {
        if (songList == null) {
            Log.e(TAG, "sond list is null!");
        }
        int insertPos = 0;
        if (songList.size() > 0) {
            insertPos = preludeLV.getCheckedItemPosition();
            if (insertPos == AdapterView.INVALID_POSITION) {
                insertPos = songList.size();
            } else {
                insertPos += 1;
            }
        }
        Log.d(TAG, "Adding song " + song.title + " to position " + insertPos);
        songList.add(insertPos, song);
        Log.d(TAG, "Now we have " + songList.size() + " songs!");
        songListViewAdapter.notifyDataSetChanged();
        updateTotalTime();
    }
}
