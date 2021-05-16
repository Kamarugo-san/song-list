package br.com.kamarugosan.songlist.ui.activity.main.song;

import android.app.AlertDialog;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.BackgroundColorSpan;
import android.util.TypedValue;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;

import com.google.android.material.slider.Slider;

import br.com.kamarugosan.songlist.R;
import br.com.kamarugosan.songlist.model.LyricsMarking;
import br.com.kamarugosan.songlist.model.LyricsMarkingColor;
import br.com.kamarugosan.songlist.model.LyricsSelectionPosition;
import br.com.kamarugosan.songlist.model.Song;
import br.com.kamarugosan.songlist.model.SongViewModel;
import br.com.kamarugosan.songlist.storage.SongBackup;
import br.com.kamarugosan.songlist.ui.activity.main.MainBroadcastReceiver;

public class SongFragment extends Fragment {
    public static final Float DEFAULT_LYRICS_TEXT_SIZE = 14f;

    private TextView lyricsTv;
    private TextView titleTv;
    private TextView artistTv;
    private Song currentSong;
    private SongViewModel viewModel;

    private final ActionMode.Callback lyricsActionModeCallback = new ActionMode.Callback() {
        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            menu.removeItem(android.R.id.cut);

            mode.getMenuInflater().inflate(R.menu.menu_song_lyrics_selection, menu);

            return true;
        }

        @Override
        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            return false;
        }

        @Override
        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
            if (item.getItemId() == R.id.lyrics_mark) {
                return markSelectedLyrics();
            }

            if (item.getItemId() == R.id.lyrics_clear_markings) {
                return clearMarkingsOnSelection();
            }

            return false;
        }

        @Override
        public void onDestroyActionMode(ActionMode mode) {
        }
    };

    public SongFragment() {
        super(R.layout.fragment_song);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.song_edit) {
            NavHostFragment.findNavController(this).navigate(SongFragmentDirections.actionSongFragmentToEditSongFragment());
        }

        if (item.getItemId() == R.id.song_text_size) {
            Float fontSize = viewModel.getLyricsTextSize().getValue();
            float previousLyricsTextSize = fontSize != null ? fontSize : DEFAULT_LYRICS_TEXT_SIZE;

            View dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_text_size_selection, null);
            Slider textSizeSlider = dialogView.findViewById(R.id.text_size_slider);
            textSizeSlider.setValue(previousLyricsTextSize);
            textSizeSlider.addOnChangeListener((slider, value, fromUser) -> viewModel.setLyricsTextSize(value));

            new AlertDialog.Builder(requireContext())
                    .setTitle(R.string.song_text_size)
                    .setPositiveButton(R.string.all_ok, null)
                    .setNegativeButton(R.string.all_cancel, (dialog, which) -> viewModel.setLyricsTextSize(previousLyricsTextSize))
                    .setView(dialogView)
                    .create()
                    .show();
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

        titleTv = view.findViewById(R.id.song_title);
        artistTv = view.findViewById(R.id.song_artist);
        lyricsTv = view.findViewById(R.id.song_lyrics);

        viewModel = new ViewModelProvider(requireActivity()).get(SongViewModel.class);
        viewModel.getSelectedSong().observe(getViewLifecycleOwner(), song -> {
            currentSong = song;

            setHasOptionsMenu(true);

            titleTv.setText(currentSong.getTitle());
            artistTv.setText(currentSong.getArtist());

            SpannableString lyrics = new SpannableString(currentSong.getLyrics());

            if (currentSong.getMarkings() != null && !currentSong.getMarkings().isEmpty()) {
                for (LyricsMarking marking : currentSong.getMarkings()) {
                    int markingColor = ContextCompat.getColor(requireContext(), marking.getColor().getColor());
                    LyricsSelectionPosition position = marking.getPosition();

                    lyrics.setSpan(new BackgroundColorSpan(markingColor), position.getStart(), position.getEnd(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                }
            }

            lyricsTv.setText(lyrics);

            lyricsTv.setCustomSelectionActionModeCallback(lyricsActionModeCallback);

            ActionBar actionBar = ((AppCompatActivity) requireActivity()).getSupportActionBar();
            if (actionBar != null) {
                actionBar.setTitle(currentSong.getTitle());
            }
        });

        viewModel.getLyricsTextSize().observe(getViewLifecycleOwner(), lyricsTextSize -> lyricsTv.setTextSize(TypedValue.COMPLEX_UNIT_SP, lyricsTextSize));
    }

    private boolean markSelectedLyrics() {
        LyricsSelectionPosition selection = getSelectedLyricsPositions();

        View dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_lyrics_marking_color, null);
        AlertDialog dialog = new AlertDialog.Builder(requireContext())
                .setTitle(R.string.lyrics_selection_dialog_title)
                .setView(dialogView)
                .setNegativeButton(R.string.all_cancel, null)
                .create();

        View colorYellowView = dialogView.findViewById(R.id.lyrics_mark_color_yellow);
        View colorGreenView = dialogView.findViewById(R.id.lyrics_mark_color_green);
        View colorPinkView = dialogView.findViewById(R.id.lyrics_mark_color_pink);

        colorYellowView.setOnClickListener(v -> {
            markSong(selection.getStart(), selection.getEnd(), LyricsMarkingColor.YELLOW);
            dialog.dismiss();
        });

        colorGreenView.setOnClickListener(v -> {
            markSong(selection.getStart(), selection.getEnd(), LyricsMarkingColor.GREEN);
            dialog.dismiss();
        });

        colorPinkView.setOnClickListener(v -> {
            markSong(selection.getStart(), selection.getEnd(), LyricsMarkingColor.PINK);
            dialog.dismiss();
        });

        dialog.show();

        return true;
    }

    private LyricsSelectionPosition getSelectedLyricsPositions() {
        int min = 0;
        int max = lyricsTv.getText().length();
        if (lyricsTv.isFocused()) {
            final int selStart = lyricsTv.getSelectionStart();
            final int selEnd = lyricsTv.getSelectionEnd();

            min = Math.max(0, Math.min(selStart, selEnd));
            max = Math.max(0, Math.max(selStart, selEnd));
        }

        return new LyricsSelectionPosition(min, max);
    }

    private void markSong(int start, int end, LyricsMarkingColor color) {
        Song newSong = new Song(currentSong);
        newSong.addMarking(new LyricsMarking(start, end, color));

        updateSong(newSong);
    }

    private boolean clearMarkingsOnSelection() {
        Song newSong = new Song(currentSong);
        newSong.removeMarkings();

        updateSong(newSong);

        return true;
    }

    private void updateSong(Song newSong) {
        new Thread(() -> {
            SongBackup.update(requireActivity(), newSong);

            requireActivity().runOnUiThread(() -> {
                viewModel.selectSong(newSong);
                requireContext().sendBroadcast(MainBroadcastReceiver.getLoadListIntent());
            });
        }).start();
    }
}
