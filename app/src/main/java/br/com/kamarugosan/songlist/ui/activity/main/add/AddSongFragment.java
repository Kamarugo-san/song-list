package br.com.kamarugosan.songlist.ui.activity.main.add;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;

import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import br.com.kamarugosan.songlist.R;
import br.com.kamarugosan.songlist.model.Song;
import br.com.kamarugosan.songlist.storage.SongBackup;
import br.com.kamarugosan.songlist.model.SongViewModel;
import br.com.kamarugosan.songlist.ui.activity.main.MainBroadcastReceiver;

public class AddSongFragment extends Fragment {
    private TextInputLayout titleLayout;
    private TextInputLayout artistLayout;
    private TextInputLayout lyricsLayout;
    private TextInputEditText titleInput;
    private TextInputEditText artistInput;
    private TextInputEditText lyricsInput;

    private SongViewModel viewModel;

    public AddSongFragment() {
        super(R.layout.fragment_add_song);
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.menu_add_song, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.add_song_save) {
            validateForm();
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        titleLayout = view.findViewById(R.id.add_song_title_layout);
        artistLayout = view.findViewById(R.id.add_song_artist_layout);
        lyricsLayout = view.findViewById(R.id.add_song_lyrics_layout);
        titleInput = view.findViewById(R.id.add_song_title_input);
        artistInput = view.findViewById(R.id.add_song_artist_input);
        lyricsInput = view.findViewById(R.id.add_song_lyrics_input);

        viewModel = new ViewModelProvider(requireActivity()).get(SongViewModel.class);

        setHasOptionsMenu(true);
    }

    private void validateForm() {
        Song song = createSong();

        titleLayout.setError(null);
        artistLayout.setError(null);
        lyricsLayout.setError(null);

        boolean isValid = true;

        if (song.getTitle() == null || song.getTitle().isEmpty()) {
            isValid = false;
            titleLayout.setError(getString(R.string.song_missing_title));
        }

        if (song.getArtist() == null || song.getArtist().isEmpty()) {
            isValid = false;
            artistLayout.setError(getString(R.string.song_missing_artist));
        }

        if (song.getLyrics() == null || song.getLyrics().isEmpty()) {
            isValid = false;
            lyricsLayout.setError(getString(R.string.song_missing_lyrics));
        }

        if (isValid) {
            new AlertDialog.Builder(requireContext())
                    .setMessage(R.string.add_song_save_confirmation)
                    .setPositiveButton(R.string.all_save, (dialog, which) -> save(song))
                    .setNegativeButton(R.string.all_cancel, null)
                    .create()
                    .show();
        }
    }

    private void save(Song song) {
        AlertDialog savingDialog = new AlertDialog.Builder(requireContext())
                .setMessage(R.string.add_song_saving)
                .setCancelable(false)
                .create();
        savingDialog.show();

        new Thread(() -> {
            boolean saved = SongBackup.save(requireActivity(), song);

            requireActivity().runOnUiThread(() -> {
                savingDialog.hide();

                if (saved) {
                    if (getView() != null) {
                        InputMethodManager imm = (InputMethodManager) requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                        imm.hideSoftInputFromWindow(requireView().getWindowToken(), 0);
                    }

                    requireContext().sendBroadcast(new Intent(MainBroadcastReceiver.ACTION_LOAD_LIST));
                    viewModel.selectSong(song);
                    NavHostFragment.findNavController(AddSongFragment.this)
                            .navigate(AddSongFragmentDirections.actionAddSongFragmentToSongFragment());
                } else {
                    Snackbar.make(titleInput, R.string.add_song_failed_to_save, Snackbar.LENGTH_LONG).show();
                }
            });
        }).start();
    }

    @NonNull
    private Song createSong() {
        return new Song(
                getValueFromInput(titleInput),
                getValueFromInput(artistInput),
                getValueFromInput(lyricsInput)
        );
    }

    @Nullable
    private String getValueFromInput(@NonNull TextInputEditText input) {
        if (input.getText() != null) {
            return input.getText().toString();
        }

        return null;
    }
}
