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
import android.media.AudioManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.ProgressBar;
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
    static PlayListItemBase plUpNext = null;
    MySeqAdapter myseq;
    MyUpNextSelected myUpNextSelected = new MyUpNextSelected();
    MainActyCallbacks mainCallback;
    boolean musicPlaying = false;
    ProgressBar playbackProgress;
    ProgressBarUpdater progressBarUpdater;
    AudioManager audioManager;

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

        if (plUpNext == null) {
            plUpNext = playList.get(0);
        }
        myUpNextSelected.selectedSong(plUpNext); // select the first item

        lv = (ListView) rootView.findViewById(R.id.trackList);
        playStop = (Button) rootView.findViewById(R.id.go_button);
        playbackProgress = (ProgressBar) rootView.findViewById(R.id.progress_bar);

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
                int itemPosition = position;

                // ListView Clicked item value
                String itemValue = (String) lv.getItemAtPosition(position);

                // Show Alert
                Toast.makeText(getActivity().getApplicationContext(),
                        "Position :" + itemPosition + "  ListItem : " + itemValue, Toast.LENGTH_LONG)
                        .show();

                Log.d(TAG, "item click: Position :" + itemPosition);
            }
        });

        playStop.setOnClickListener(new PlayStopListener());

        ImageButton vu = (ImageButton) rootView.findViewById(R.id.volume_up);
        ImageButton vd = (ImageButton) rootView.findViewById(R.id.volume_down);
        vu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                audioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC, AudioManager.ADJUST_RAISE, 0);
            }
        });
        vd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                audioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC, AudioManager.ADJUST_LOWER, 0);
            }
        });

        musicPlaying = plUpNext.isPlaying();
        setPlayStopText();
        if (progressBarUpdater != null) {
            // should never happen because of onStop()
            progressBarUpdater.cancel(true);
        }
        if (musicPlaying) {
            int max = plUpNext.getDuration();
            Log.d(TAG, "Max progress: " + max);
            playbackProgress.setMax(max);
            progressBarUpdater = new ProgressBarUpdater();
            progressBarUpdater.execute(plUpNext);
        }

        return rootView;
    }

    @Override
    public void onStop() {
        super.onStop();
        if (progressBarUpdater != null) {
            progressBarUpdater.cancel(true);
            progressBarUpdater = null;
        }
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        mainCallback = (MainActyCallbacks) activity;
        playList = mainCallback.getPlayList();

        audioManager = (AudioManager)activity.getSystemService(Context.AUDIO_SERVICE);
    }

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
            if (progressBarUpdater != null) {
                progressBarUpdater.cancel(true);
                progressBarUpdater = null;
                playbackProgress.setProgress(0);
            }
        }

        @Override
        public void refreshPLI() {
            lv.invalidateViews();
        }

        @Override
        public void setProgressMax(int max) {
            playbackProgress.setMax(max);
            Log.d(TAG, "Max progress: " + max);
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
                if (progressBarUpdater != null) {
                    progressBarUpdater.cancel(true);
                    progressBarUpdater = null;
                    playbackProgress.setProgress(0);
                }
            } else {
                if (null != plUpNext) {
                    boolean success = plUpNext.play(getActivity());
                    if (success) {
                        musicPlaying = true;
                        setPlayStopText();
                        if (progressBarUpdater != null) {
                            progressBarUpdater.cancel(true);
                            playbackProgress.setProgress(0);
                        }
                        progressBarUpdater = new ProgressBarUpdater();
                        progressBarUpdater.execute(plUpNext);
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

    class ProgressBarUpdater extends AsyncTask<PlayListItemBase, Void, Void> {
        PlayListItemBase pli;
        @Override
        protected Void doInBackground(PlayListItemBase... pli) {
            this.pli = pli[0];
            do {
                publishProgress();
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {}
            } while (! isCancelled());
            return null;
        }

        @Override
        protected void onProgressUpdate(Void... unused) {
            int progress = pli.getProgress();
            playbackProgress.setProgress(progress);
            //Log.d(TAG, "progress = " + progress[0]);
        }
    }
}
