package br.com.kamarugosan.songlist.ui.activity.main.edit;

import android.content.Context;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;

import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.util.List;

import br.com.kamarugosan.songlist.R;
import br.com.kamarugosan.songlist.model.Song;
import br.com.kamarugosan.songlist.model.SongViewModel;
import br.com.kamarugosan.songlist.storage.SongBackup;
import br.com.kamarugosan.songlist.ui.activity.main.MainBroadcastReceiver;

public class EditSongFragment extends Fragment {
    private TextInputLayout titleLayout;
    private TextInputLayout artistLayout;
    private TextInputLayout lyricsLayout;
    private TextInputEditText titleInput;
    private TextInputEditText artistInput;
    private TextInputEditText lyricsInput;

    private SongViewModel viewModel;

    private Song songToEdit = null;

    public EditSongFragment() {
        super(R.layout.fragment_add_song);
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.menu_edit_song, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.edit_song_save) {
            validateFormAndSave();
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

        songToEdit = viewModel.getSelectedSong().getValue();
        if (songToEdit != null) {
            ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle(R.string.edit_song_title);

            titleInput.setText(songToEdit.getTitle());
            artistInput.setText(songToEdit.getArtist());
            lyricsInput.setText(songToEdit.getLyrics());
        }

        setHasOptionsMenu(true);
    }

    private void validateFormAndSave() {
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
                    .setPositiveButton(R.string.all_save, (dialog, which) -> save(song, false))
                    .setNegativeButton(R.string.all_cancel, null)
                    .create()
                    .show();
        }
    }

    private void save(Song song, boolean ignoreDuplicate) {
        AlertDialog savingDialog = new AlertDialog.Builder(requireContext())
                .setMessage(R.string.add_song_saving)
                .setCancelable(false)
                .create();
        savingDialog.show();

        new Thread(() -> {
            if (songToEdit != null) {
                song.setFilePath(songToEdit.getFilePath());

                if (!ignoreDuplicate && isDuplicated(song)) {
                    requireActivity().runOnUiThread(() -> {
                        savingDialog.hide();
                        showDuplicateDialog(song);
                    });

                    return;
                }

                boolean saved = SongBackup.update(requireActivity(), song);

                requireActivity().runOnUiThread(() -> finishFragment(song, savingDialog, saved));
            } else {
                if (!ignoreDuplicate && isDuplicated(song)) {
                    requireActivity().runOnUiThread(() -> {
                        savingDialog.hide();
                        showDuplicateDialog(song);
                    });

                    return;
                }

                boolean saved = SongBackup.save(requireActivity(), song);

                requireActivity().runOnUiThread(() -> finishFragment(song, savingDialog, saved));
            }
        }).start();
    }

    private void showDuplicateDialog(Song song) {
        new AlertDialog.Builder(requireContext())
                .setMessage(R.string.song_is_duplicated_message)
                .setPositiveButton(R.string.all_save, (dialog, which) -> save(song, true))
                .setNegativeButton(R.string.all_cancel, null)
                .setCancelable(false)
                .create()
                .show();
    }

    private boolean isDuplicated(Song song) {
        List<Song> songList = viewModel.getSongList().getValue();
        if (songList == null) {
            return false;
        }

        for (Song savedSong : songList) {
            if (savedSong.hasSameHash(song)) {
                if (song.getFilePath() != null && savedSong.getFilePath().equals(song.getFilePath())) {
                    // It's the same song being saved without changes
                    continue;
                }

                return true;
            }
        }

        return false;
    }

    private void finishFragment(Song song, @NonNull AlertDialog savingDialog, boolean saved) {
        savingDialog.hide();

        if (saved) {
            if (getView() != null) {
                InputMethodManager imm = (InputMethodManager) requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(requireView().getWindowToken(), 0);
            }

            requireContext().sendBroadcast(MainBroadcastReceiver.getLoadListIntent());
            viewModel.selectSong(song);
            NavHostFragment.findNavController(EditSongFragment.this)
                    .navigate(EditSongFragmentDirections.actionEditSongFragmentToSongFragment());
        } else {
            Snackbar.make(titleInput, R.string.add_song_failed_to_save, Snackbar.LENGTH_LONG).show();
        }
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
