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

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.text.DecimalFormat;
import java.util.List;

/**
 * Created by topher on 6/10/15.
 */
class SongListArrayAdapter extends ArrayAdapter<Song> {
    private static final String TAG = SongListArrayAdapter.class.getSimpleName();
    static final int LAYOUT_ID = R.layout.list_item_song;

    public SongListArrayAdapter(Context context, List<Song> objects) {
        super(context, LAYOUT_ID, R.id.text1, objects);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View v = convertView;
        if (v == null) {
            LayoutInflater li = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            v = li.inflate(LAYOUT_ID, null);
        }
        TextView line1 = (TextView) v.findViewById(R.id.text1);
        TextView line2 = (TextView) v.findViewById(R.id.text2);
        TextView timeTV = (TextView) v.findViewById(R.id.text_time);
        Song s = getItem(position);
        line1.setText(s.title);
        line2.setText(s.album);
        timeTV.setText(MinSec.toString(s.getTrackLengthMillis()));
        return v;
    }
}
