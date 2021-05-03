package br.com.kamarugosan.songlist.ui.activity.main.song;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;

import br.com.kamarugosan.songlist.R;
import br.com.kamarugosan.songlist.model.SongViewModel;

public class SongFragment extends Fragment {
    public SongFragment() {
        super(R.layout.fragment_song);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.song_edit) {
            NavHostFragment.findNavController(this).navigate(SongFragmentDirections.actionSongFragmentToEditSongFragment());
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.menu_song, menu);

        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        TextView titleTv = view.findViewById(R.id.song_title);
        TextView artistTv = view.findViewById(R.id.song_artist);
        TextView lyricsTv = view.findViewById(R.id.song_lyrics);

        SongViewModel viewModel = new ViewModelProvider(requireActivity()).get(SongViewModel.class);
        viewModel.getSelectedSong().observe(getViewLifecycleOwner(), song -> {
            if (song.isImported()) {
                setHasOptionsMenu(true);
            }

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
