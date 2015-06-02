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

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;

import java.util.ArrayList;
import java.util.List;


public class MainActy extends Activity
    implements MainActyCallbacks {

    private static String TAG = "MainActy";
    private List<PlayListItemBase> playList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "in onCreate()");

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        playList = new ArrayList<PlayListItemBase>(10);
        playList.add(new HymnPlayListItem(this));
        playList.add(new HymnPlayListItem(this));
        playList.add(new HymnPlayListItem(this));
        //playList.add(new DummyPlayListItem(myUpNextSelected, songChoiceCallback));
        //playList.add(new DummyPlayListItem(myUpNextSelected, songChoiceCallback));
        //playList.add(new DummyPlayListItem(myUpNextSelected, songChoiceCallback));
        //playList.add(new DummyPlayListItem(myUpNextSelected, songChoiceCallback));
        //playList.add(new DummyPlayListItem(myUpNextSelected, songChoiceCallback));
        //playList.add(new DummyPlayListItem(myUpNextSelected, songChoiceCallback));
        //playList.add(new DummyPlayListItem(myUpNextSelected, songChoiceCallback));


        setContentView(R.layout.activity_overview_acty);
        if (savedInstanceState == null) {
            getFragmentManager().beginTransaction()
                    .add(R.id.container, new OverviewFrag())
                    .commit();
        }
        MusicCatalog cat = MusicCatalog.getInstance();
        cat.setContext(this);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.overview_acty, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onSongChoiceClick(PlayListItemBase playListItem) {
        int idx = playList.indexOf(playListItem);
        Fragment chooseSongFragment = playListItem.getSongChoiceFragment(idx);
        if (null == chooseSongFragment) {
            return;
        }
        FragmentTransaction transaction = getFragmentManager().beginTransaction();

        // Replace whatever is in the fragment_container view with this fragment,
        // and add the transaction to the back stack so the user can navigate back
        transaction.replace(R.id.container, chooseSongFragment);
        transaction.addToBackStack(null);

        // Commit the transaction
        transaction.commit();
    }

    @Override
    public void onSongChoiceDone() {
        getFragmentManager().popBackStack();
        InputMethodManager inputManager = (InputMethodManager)
            getSystemService(Context.INPUT_METHOD_SERVICE);

        inputManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(),
            InputMethodManager.HIDE_NOT_ALWAYS);
    }

    @Override
    public List<PlayListItemBase> getPlayList() {
        return playList;
    }
}
