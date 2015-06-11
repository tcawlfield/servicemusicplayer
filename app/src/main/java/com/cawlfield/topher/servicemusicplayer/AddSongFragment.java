package com.cawlfield.topher.servicemusicplayer;

import android.app.Activity;
import android.app.DialogFragment;
import android.app.Fragment;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Spinner;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by topher on 6/10/15.
 */
public class AddSongFragment extends DialogFragment implements View.OnClickListener,
        AdapterView.OnItemSelectedListener {
    private static final String TAG = AddSongFragment.class.getSimpleName();

    List<String> albumList;
    List<Song> songList;
    Button cancel, okay;
    Spinner albumSpinner;
    ListView songSelLV;
    PreludeChoice preludeChoice;

    ArrayAdapter<String> albumListArrayAdapter;
    SongListArrayAdapter songListArrayAdapter;
    Handler handler;

    public static AddSongFragment newInstance(PreludeChoice preludeChoice) {
        AddSongFragment f = new AddSongFragment();
        f.preludeChoice = preludeChoice;

        return f;
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
        View rootView = inflater.inflate(R.layout.fragment_add_song, container, false);

        cancel = (Button) rootView.findViewById(R.id.cancel_btn);
        okay = (Button) rootView.findViewById(R.id.okay_btn);
        albumSpinner = (Spinner) rootView.findViewById(R.id.album_spn);
        songSelLV = (ListView) rootView.findViewById(R.id.song_choice);

        songList = new ArrayList<Song>();

        MusicCatalog mc = MusicCatalog.getInstance();
        albumList = mc.getAlbums();
        albumListArrayAdapter = new ArrayAdapter<String>(getActivity(),
                android.R.layout.simple_spinner_item, albumList);
        albumListArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        albumSpinner.setAdapter(albumListArrayAdapter);
        albumSpinner.setOnItemSelectedListener(this);

        cancel.setOnClickListener(this);
        okay.setOnClickListener(this);

        songListArrayAdapter = new SongListArrayAdapter(getActivity(), songList);
        songSelLV.setAdapter(songListArrayAdapter);

        return rootView;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
    }

    @Override
    public void onResume() {
        super.onResume();

        updateSongList();
    }

    @Override
    public void onClick(View view) {
        if (view == cancel) {
            doCancel();
        } else if (view == okay) {
            doOkay();
        }
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        updateSongList(position);
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {
        updateSongList(-1);
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    void updateSongList(int position) {
        songListArrayAdapter.clear();
        if (position >= 0) {
            String album = albumListArrayAdapter.getItem(position);
            if (album != null) {
                List<Song> songs = MusicCatalog.getInstance().getSongsByAlbum(album);
                if (songs != null) {
                    songListArrayAdapter.addAll(songs);
                }
            }
        }
    }

    void updateSongList() {
        updateSongList(albumSpinner.getSelectedItemPosition());
    }

    void doOkay() {
        SparseBooleanArray selectedPositions = songSelLV.getCheckedItemPositions();
        for (int i=0; i<selectedPositions.size(); i++) {
            if (selectedPositions.get(i)) {
                preludeChoice.addThisSong(songListArrayAdapter.getItem(i));
            }
        }
    }

    void doCancel() {
        getFragmentManager().popBackStack();
    }
}
