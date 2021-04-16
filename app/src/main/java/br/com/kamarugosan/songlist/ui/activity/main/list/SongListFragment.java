package br.com.kamarugosan.songlist.ui.activity.main.list;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.view.ActionMode;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import br.com.kamarugosan.songlist.R;
import br.com.kamarugosan.songlist.model.Song;
import br.com.kamarugosan.songlist.model.SongViewModel;
import br.com.kamarugosan.songlist.storage.SongSharing;

public class SongListFragment extends Fragment {
    private ActionMode selectionActionMode;
    private final ActionMode.Callback selectionActionModeCallback = new ActionMode.Callback() {
        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            mode.getMenuInflater().inflate(R.menu.menu_song_list_selection, menu);
            return true;
        }

        @Override
        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            return false;
        }

        @Override
        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
            if (item.getItemId() == R.id.select_song_share) {
                return shareSelectedSongs();
            }

            if (item.getItemId() == R.id.select_song_select_all) {
                return selectAllSongs();
            }

            return false;
        }

        @Override
        public void onDestroyActionMode(ActionMode mode) {
            selectionActionMode = null;

            if (!adapter.getSelectedItemsPositions().isEmpty()) {
                ArrayList<Integer> selectedItems = new ArrayList<>(adapter.getSelectedItemsPositions());

                adapter.getSelectedItemsPositions().clear();

                for (Integer selectedItem : selectedItems) {
                    adapter.notifyItemChanged(selectedItem);
                }
            }
        }
    };

    private SongViewModel viewModel;
    private SongAdapter adapter;
    private RecyclerView songRecyclerView;

    private ActivityResultLauncher<Intent> shareSongsLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
    });

    public SongListFragment() {
        super(R.layout.fragment_song_list);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        setHasOptionsMenu(true);

        viewModel = new ViewModelProvider(requireActivity()).get(SongViewModel.class);
        songRecyclerView = view.findViewById(R.id.song_list_recyclerview);

        FloatingActionButton addSongFab = view.findViewById(R.id.song_list_add_song_fab);
        addSongFab.setOnClickListener(v -> NavHostFragment.findNavController(SongListFragment.this)
                .navigate(SongListFragmentDirections.actionSongListFragmentToAddSongFragment()));

        setupList();
    }

    private void setupList() {
        adapter = new SongAdapter(
                requireContext(),
                new ArrayList<>(),
                this::processPosition,
                position -> {
                    if (selectionActionMode == null) {
                        selectionActionMode = ((AppCompatActivity) requireActivity()).startSupportActionMode(selectionActionModeCallback);
                    }

                    processPosition(position);
                }
        );

        songRecyclerView.setAdapter(adapter);

        viewModel.getSongList().observe(getViewLifecycleOwner(), songs -> adapter.setList(songs));
    }

    private void processPosition(int position) {
        Song song = adapter.getList().get(position);
        if (selectionActionMode != null) {
            if (!song.isImported()) {
                new AlertDialog.Builder(requireContext())
                        .setMessage(R.string.list_selection_action_mode_cannot_select_default_song)
                        .setPositiveButton(R.string.all_ok, null)
                        .create()
                        .show();

                updateActionModeVisibility();
                return;
            }

            if (adapter.getSelectedItemsPositions().contains(position)) {
                adapter.getSelectedItemsPositions().remove((Integer) position);
            } else {
                adapter.getSelectedItemsPositions().add(position);
            }

            adapter.notifyItemChanged(position);

            updateActionModeVisibility();
        } else {
            viewModel.selectSong(song);

            NavHostFragment.findNavController(this)
                    .navigate(SongListFragmentDirections.actionSongListFragmentToSongFragment());
        }
    }

    private void updateActionModeVisibility() {
        if (adapter.getSelectedItemsPositions().isEmpty()) {
            selectionActionMode.finish();
        } else {
            updateActionModeTitle();
        }
    }

    private void updateActionModeTitle() {
        int size = adapter.getSelectedItemsPositions().size();
        selectionActionMode.setTitle(getResources().getQuantityString(R.plurals.list_selection_action_mode_title, size, size));
    }

    private boolean selectAllSongs() {
        adapter.getSelectedItemsPositions().clear();

        for (int i = 0; i < adapter.getList().size(); i++) {
            if (adapter.getList().get(i).isImported()) {
                adapter.getSelectedItemsPositions().add(i);
                adapter.notifyItemChanged(i);
            }
        }

        updateActionModeTitle();

        return true;
    }

    private boolean shareSelectedSongs() {
        AlertDialog preparingToShareDialog = new AlertDialog.Builder(requireContext())
                .setMessage(R.string.list_selection_action_mode_share_preparing)
                .setCancelable(false)
                .create();

        preparingToShareDialog.show();

        new Thread(() -> {
            List<Song> selectedSongs = adapter.getSelectedItems();
            File exportedSongsFile = SongSharing.shareSongs(selectedSongs, requireContext());

            requireActivity().runOnUiThread(() -> {
                preparingToShareDialog.dismiss();

                if (exportedSongsFile == null) {
                    new AlertDialog.Builder(requireContext())
                            .setMessage(R.string.list_selection_action_mode_share_failure)
                            .setPositiveButton(R.string.all_ok, null)
                            .create()
                            .show();

                    return;
                }

                Uri exportedSongsUri = FileProvider.getUriForFile(requireContext(), requireContext().getPackageName(), exportedSongsFile);

                Intent shareIntent = new Intent();
                shareIntent.setAction(Intent.ACTION_SEND);
                shareIntent.putExtra(Intent.EXTRA_STREAM, exportedSongsUri);
                shareIntent.putExtra(Intent.EXTRA_TEXT, getString(R.string.all_song_sharing_text));
                shareIntent.setType("application/zip");

                shareSongsLauncher.launch(Intent.createChooser(shareIntent, getString(R.string.all_song_sharing_text)));
            });
        }).start();

        return true;
    }
}
