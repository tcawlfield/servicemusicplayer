package com.cawlfield.topher.servicemusicplayer;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

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
