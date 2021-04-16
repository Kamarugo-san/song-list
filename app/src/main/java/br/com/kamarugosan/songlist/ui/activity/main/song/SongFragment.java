package br.com.kamarugosan.songlist.ui.activity.main.song;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import br.com.kamarugosan.songlist.R;
import br.com.kamarugosan.songlist.model.SongViewModel;

public class SongFragment extends Fragment {
    public SongFragment() {
        super(R.layout.fragment_song);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        TextView titleTv = view.findViewById(R.id.song_title);
        TextView artistTv = view.findViewById(R.id.song_artist);
        TextView lyricsTv = view.findViewById(R.id.song_lyrics);

        SongViewModel viewModel = new ViewModelProvider(requireActivity()).get(SongViewModel.class);
        viewModel.getSelectedSong().observe(getViewLifecycleOwner(), song -> {
            titleTv.setText(song.getTitle());
            artistTv.setText(song.getArtist());
            lyricsTv.setText(song.getLyrics());

            ActionBar actionBar = ((AppCompatActivity) requireActivity()).getSupportActionBar();
            if (actionBar != null) {
                actionBar.setTitle(song.getTitle());
            }
        });
    }
}
