package com.cawlfield.topher.servicemusicplayer;

import android.app.Activity;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.Fragment;
import android.graphics.Point;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.SparseBooleanArray;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by topher on 6/10/15.
 */
public class AddSongFragment extends DialogFragment implements View.OnClickListener,
        AdapterView.OnItemSelectedListener, AdapterView.OnItemClickListener {
    private static final String TAG = AddSongFragment.class.getSimpleName();

    private static String lastAlbumSelected;

    List<String> albumList;
    List<Song> songList;
    Button cancel, okay;
    Spinner albumSpinner;
    ListView songSelLV;
    PreludeChoice preludeChoice;
    TextView previewSongTitle;

    ArrayAdapter<String> albumListArrayAdapter;
    SongListArrayAdapter songListArrayAdapter;
    Handler handler;
    SongPlayer songPlayer;

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

    //@Override
    //public Dialog onCreateDialog (Bundle savedInstanceState) {
    //
    //}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_add_song, container, false);

        cancel = (Button) rootView.findViewById(R.id.cancel_btn);
        okay = (Button) rootView.findViewById(R.id.okay_btn);
        albumSpinner = (Spinner) rootView.findViewById(R.id.album_spn);
        previewSongTitle = (TextView) rootView.findViewById(R.id.preview_song_title);
        songSelLV = (ListView) rootView.findViewById(R.id.song_choice);
        songSelLV.setOnItemClickListener(this);
        //songSelLV.setMultiChoiceModeListener(this);

        songList = new ArrayList<Song>();

        MusicCatalog mc = MusicCatalog.getInstance();
        albumList = mc.getAlbums();
        albumListArrayAdapter = new ArrayAdapter<String>(getActivity(),
                android.R.layout.simple_spinner_item, albumList);
        albumListArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        albumSpinner.setAdapter(albumListArrayAdapter);
        albumSpinner.setOnItemSelectedListener(this);
        if (lastAlbumSelected != null && albumList.contains(lastAlbumSelected)) {
            albumSpinner.setSelection(albumList.indexOf(lastAlbumSelected));
        }

        cancel.setOnClickListener(this);
        okay.setOnClickListener(this);

        songListArrayAdapter = new SongListArrayAdapter(getActivity(), songList);
        songSelLV.setAdapter(songListArrayAdapter);

        // Fix up the dialog a bit:
        Dialog thisDialog = getDialog();
        if (thisDialog != null) {
            thisDialog.setTitle("Add Songs");
        }

        ImageButton playPause = (ImageButton) rootView.findViewById(R.id.play_pause);
        SeekBar sb = (SeekBar) rootView.findViewById(R.id.seek_bar);
        songPlayer = new SongPlayer(getActivity(), playPause, sb);

        return rootView;
    }

    @Override
    public void onStart() {
        super.onStart();
        Dialog thisDialog = getDialog();
        if (thisDialog != null) {
            Display display = getActivity().getWindowManager().getDefaultDisplay();
            Point size = new Point();
            display.getSize(size);
            int screenWidth = size.x;
            int screenHeight = size.y;
            int dialogWidth = (int) (screenWidth * 0.95 + 0.5);
            int dialogHeight = (int) (screenHeight * 0.95 + 0.5);

            thisDialog.getWindow().setLayout(dialogWidth, dialogHeight);
        }
        songPlayer.fragmentStarting();
        previewSongTitle.setText("");
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
        // An album was selected from the spinner
        updateSongList(position);
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {
        updateSongList(-1);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        songPlayer.fragmentFinishing();
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
        songListArrayAdapter.notifyDataSetChanged();
    }

    void updateSongList() {
        updateSongList(albumSpinner.getSelectedItemPosition());
    }

    void doOkay() {
        SparseBooleanArray selectedPositions = songSelLV.getCheckedItemPositions();
        for (int i=0; i<selectedPositions.size(); i++) {
            if (selectedPositions.valueAt(i)) {
                preludeChoice.addThisSong(songListArrayAdapter.getItem(selectedPositions.keyAt(i)));
            }
        }
        lastAlbumSelected = (String) albumSpinner.getSelectedItem();
        dismiss();
    }

    void doCancel() {
        lastAlbumSelected = (String) albumSpinner.getSelectedItem();
        dismiss();
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        // An item in the song selection list was clicked.
        if (songSelLV.getCheckedItemPositions().get(position)) {
            // This item is "checked".
            Song selectedSong = (Song) songSelLV.getItemAtPosition(position);
            if (selectedSong != null) { // Can't think of how this would not be true
                songPlayer.setSong(selectedSong);
                previewSongTitle.setText(selectedSong.title);
            }
        } else {
            previewSongTitle.setText("");
            songPlayer.setSong(null);
        }
    }
}
