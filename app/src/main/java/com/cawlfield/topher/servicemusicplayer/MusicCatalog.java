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

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by topher on 8/30/14.
 */
public class MusicCatalog {
    private static String TAG = "MusicCatalog";
    static private MusicCatalog instance = null;
    private Context context = null;
    List<Hymn> allHymns;

    protected MusicCatalog() {}

    static MusicCatalog getInstance() {
        if(instance == null) {
            instance = new MusicCatalog();
        }
        return instance;
    }

    void setContext(Context ct) {
        context = ct;
    }

    class Hymn {
        int number;
        String album;
        String title;
        long id;

        Uri getUri() {
            return ContentUris.withAppendedId(
                    android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, id);
        }
    }

    void getAllHymns() {
        Log.d(TAG, "in getAllHymns()");
        allHymns = new ArrayList<Hymn>();
        Pattern pat = Pattern.compile("hymn\\s*(\\d{1,3})", Pattern.CASE_INSENSITIVE);

        ContentResolver contentResolver = context.getContentResolver();
        Uri uri = android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        String[] columns = {
                android.provider.MediaStore.Audio.Media._ID,   // 0
                android.provider.MediaStore.Audio.Media.TITLE, // 1
                android.provider.MediaStore.Audio.Media.ALBUM, // 2
                android.provider.MediaStore.Audio.Media.TRACK  // 3
        };
        // Sqlite3 is case-insensitive here;
        String selection = android.provider.MediaStore.Audio.Media.IS_MUSIC + " = 1 AND "
            + android.provider.MediaStore.Audio.Media.ALBUM + " like '%hymnal%' AND "
            + android.provider.MediaStore.Audio.Media.TITLE + " like '%hymn%'";
        Cursor cursor = contentResolver.query(uri, columns, selection, null, null);
        if (cursor == null) {
            // query failed, handle error.
        } else if (!cursor.moveToFirst()) {
            // no media on the device
        } else {
            int count=0;
            do {
                long thisId = cursor.getLong(0);
                String title = cursor.getString(1);
                String album = cursor.getString(2);
                String track = cursor.getString(3);
                //Log.i(TAG, "t="+title+" a="+album+" trk="+track);
                Matcher m = pat.matcher(title);
                if (count++ < 10) {
                    Log.d(TAG, "found hymn "+ title);
                }
                if (m.find()) {
                    Hymn h = new Hymn();
                    h.title = title;
                    h.album = album;
                    try {
                        h.number = Integer.parseInt(m.group(1));
                        h.id = thisId;
                        allHymns.add(h);
                        if (count < 11) {
                            Log.d(TAG, "  (number " + h.number + ")");
                        }
                    } catch (NumberFormatException e) {
                        Log.e(TAG, "Could not interpret '"+m.group(1)+"' as an integer.");
                    }
                }
            } while (cursor.moveToNext());
        }
    }

    List<Hymn> getHymns(int hymnNum) {
        if (null == allHymns) {
            getAllHymns();
        }

        List<Hymn> matchingHymns = new ArrayList<Hymn>();
        for (Hymn h : allHymns) {
            if (h.number == hymnNum) {
                matchingHymns.add(h);
            }
        }
        return matchingHymns;
    }
}
