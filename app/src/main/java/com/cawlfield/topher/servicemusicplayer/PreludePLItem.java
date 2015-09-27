package com.cawlfield.topher.servicemusicplayer;

import android.app.Fragment;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

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

public class PreludePLItem extends PlayListItemBase {
    private static String TAG = PreludePLItem.class.getSimpleName();
    List<Song> songList;
    private int durationTotal = 0;
    private int nextSongIdx = 0;
    int endHour = 19;
    int endMinute = 30;
    Calendar waitingStarted;
    Calendar finishWaitAt;
    Handler handler;
    String lastWaitingInfo = null;
    int waitMillis; // current duration to wait

    public PreludePLItem(MainActyCallbacks callback) {
        super(callback);
        initNoSong();
    }

    @Override
    public void initNoSong() {
        setTitle("No Preludes Selected");
        setInfo("Tap to select preludes");
        songList = new ArrayList<Song>();
        nextSongIdx = 0;
    }

    @Override
    public Fragment getSongChoiceFragment(int idx) {
        Fragment pc = PreludeChoice.newInstance(idx);
        return pc;
    }

    public void setSongList(List<Song> sl) {
        songList = sl;
        if (sl == null || sl.size() < 1) {
            setTitle("No Preludes Selected");
            setInfo("Tap to select preludes");
            durationTotal = 0;
        } else {
            setTitle(sl.size() + " songs");
            durationTotal = 0;
            for (Song s : songList) {
                durationTotal += s.getTrackLengthMillis();
            }
            setInfo("total time " + MinSec.toString(durationTotal));
        }
        // TODO: info could show song length
        //mainCallback.onSongChoiceDone();
        nextSongIdx = 0;
        stop();
    }

    @Override
    protected Song getSong() {
        Log.d(TAG, "nextSongIdx = " + nextSongIdx);
        if (nextSongIdx < songList.size()) {
            Song s = songList.get(nextSongIdx);
            nextSongIdx++;
            setInfo("Playing: " + s.title);
            upNextCallback.refreshPLI();
            return s;
        } else {
            // This should never happen because we rely on moreToPlay() instead.
            setInfo("total time " + MinSec.toString(durationTotal));
            upNextCallback.refreshPLI();
            return null;
        }
    }

    @Override
    protected boolean moreToPlay() {
        if (nextSongIdx < songList.size()) {
            return true;
        } else {
            setInfo("total time " + MinSec.toString(durationTotal));
            upNextCallback.refreshPLI();
            return false;
        }
    }

    @Override
    public boolean play(Context ct) {
        if (nextSongIdx == 0) {
            Log.d(TAG, "First song, might wait to start...");
            // We might wait to start.
            Calendar now = Calendar.getInstance();
            finishWaitAt = Calendar.getInstance();
            finishWaitAt.set(Calendar.HOUR_OF_DAY, endHour);
            finishWaitAt.set(Calendar.MINUTE, endMinute);
            finishWaitAt.set(Calendar.SECOND, 0);
            //Log.d(TAG, "finishWaitAt.compareTo(now) = " + finishWaitAt.compareTo(now));
            waitMillis = (int)(finishWaitAt.getTimeInMillis() - now.getTimeInMillis()) - durationTotal;
            Log.d(TAG, "waitMillis = " + waitMillis);
            if (waitMillis <= 0) {
                return super.play(ct);
            } else {
                waitingStarted = now;
                if (handler == null) {
                    handler = new Handler(Looper.getMainLooper());
                }
                waitIsOver = new WaitIsOver(ct);
                handler.postDelayed(waitIsOver, waitMillis);
                upNextCallback.setProgressMax(waitMillis);
                lastWaitingInfo = "Waiting to start (" + MinSec.toString(waitMillis) + ")";
                setInfo(lastWaitingInfo);
                upNextCallback.refreshPLI();
                return true;
            }
        } else {
            Log.d(TAG, "super.play()");
            return super.play(ct);
        }
    }

    class WaitIsOver implements Runnable {
        public WaitIsOver(Context ct) {
            playContext = ct;
        }
        private Context playContext;
        @Override
        public void run() {
            // This happens when it's time to play the first song.
            playNow(playContext);
        }
    }
    WaitIsOver waitIsOver;

    void playNow(Context ct) {
        waitingStarted = null;
        super.play(ct);
    }

    @Override
    public int getProgress() {
        if (waitingStarted != null) {
            Calendar now = Calendar.getInstance();
            int timeLeft = (int) (finishWaitAt.getTimeInMillis() - now.getTimeInMillis() - durationTotal);
            //Log.d(TAG, "timeLeft = " + timeLeft);
            String waitingInfo = "Waiting to start (" + MinSec.toString(timeLeft) + ")";
            int timeSpent = (int) (now.getTimeInMillis() - waitingStarted.getTimeInMillis());
            if (! waitingInfo.equals(lastWaitingInfo)) {
                setInfo(lastWaitingInfo);
                upNextCallback.refreshPLI();
                lastWaitingInfo = waitingInfo;
            }
            return timeSpent;
        } else {
            return super.getProgress();
        }
    }

    @Override
    public void stop() {
        if (waitingStarted != null && handler != null) {
            handler.removeCallbacks(waitIsOver);
            waitingStarted = null;
        } else {
            super.stop();
        }
    }

    @Override
    public boolean isPlaying() {
        if (waitingStarted != null) {
            return true;
        } else {
            return super.isPlaying();
        }
    }

    @Override
    public int getDuration() {
        if (waitingStarted != null) {
            return waitMillis;
        } else {
            return super.getDuration();
        }
    }
}
