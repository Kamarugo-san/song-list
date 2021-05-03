package br.com.kamarugosan.songlist.ui.activity.main.list;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.view.ActionMode;
import androidx.appcompat.widget.SearchView;
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
import br.com.kamarugosan.songlist.storage.DeletionAttempt;
import br.com.kamarugosan.songlist.storage.SongBackup;
import br.com.kamarugosan.songlist.storage.SongSharing;
import br.com.kamarugosan.songlist.ui.activity.main.MainBroadcastReceiver;

public class SongListFragment extends Fragment {
    private FloatingActionButton addSongFab;
    private ActionMode selectionActionMode;
    private final ActionMode.Callback selectionActionModeCallback = new ActionMode.Callback() {
        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            mode.getMenuInflater().inflate(R.menu.menu_song_list_selection, menu);
            addSongFab.setVisibility(View.GONE);
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

            if (item.getItemId() == R.id.select_song_delete) {
                return deleteSelectedSongsConfirmation();
            }

            if (item.getItemId() == R.id.select_song_select_all) {
                return selectAllSongs();
            }

            return false;
        }

        @Override
        public void onDestroyActionMode(ActionMode mode) {
            addSongFab.setVisibility(View.VISIBLE);
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

    private final ActivityResultLauncher<Intent> shareSongsLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
    });

    public SongListFragment() {
        super(R.layout.fragment_song_list);
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.menu_song_list, menu);
        setupSearchView(menu);

        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        setHasOptionsMenu(true);

        viewModel = new ViewModelProvider(requireActivity()).get(SongViewModel.class);
        songRecyclerView = view.findViewById(R.id.song_list_recyclerview);

        addSongFab = view.findViewById(R.id.song_list_add_song_fab);
        addSongFab.setOnClickListener(v -> NavHostFragment.findNavController(SongListFragment.this)
                .navigate(SongListFragmentDirections.actionSongListFragmentToEditSongFragment()));

        setupList();

        viewModel.selectSong(null);
        viewModel.getSongFilter().observe(getViewLifecycleOwner(), s -> adapter.filter(s));
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

        viewModel.getSongList().observe(getViewLifecycleOwner(), songs -> {
            adapter.setDataSet(songs);

            String filterValue = viewModel.getSongFilter().getValue();
            if (filterValue != null && !filterValue.isEmpty()) {
                adapter.filter(filterValue);
            }
        });
    }

    private void processPosition(int position) {
        Song song = adapter.getFilteredDataSet().get(position);
        if (selectionActionMode != null) {
            if (!song.isImported()) {
                Toast.makeText(requireContext(), R.string.list_selection_action_mode_cannot_select_default_song, Toast.LENGTH_SHORT).show();

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
        int selectedItemsAmount = adapter.getSelectedItemsPositions().size();
        selectionActionMode.setTitle(getResources().getQuantityString(R.plurals.list_selection_action_mode_title, selectedItemsAmount, selectedItemsAmount));
    }

    private boolean selectAllSongs() {
        adapter.getSelectedItemsPositions().clear();

        for (int i = 0; i < adapter.getFilteredDataSet().size(); i++) {
            if (adapter.getFilteredDataSet().get(i).isImported()) {
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

    private boolean deleteSelectedSongsConfirmation() {
        int selectedItemsAmount = adapter.getSelectedItems().size();

        new AlertDialog.Builder(requireContext())
                .setMessage(getResources().getQuantityString(R.plurals.list_selection_action_mode_delete_confirmation, selectedItemsAmount, selectedItemsAmount))
                .setPositiveButton(R.string.list_selection_action_mode_delete, (dialog, which) -> deleteSelectedSongs())
                .setNegativeButton(R.string.all_cancel, null)
                .create()
                .show();

        return true;
    }

    private void deleteSelectedSongs() {
        List<Song> songsToDelete = adapter.getSelectedItems();

        AlertDialog deletingSongsDialog = new AlertDialog.Builder(requireContext())
                .setMessage(getResources().getQuantityString(R.plurals.list_selection_action_mode_delete_in_progress, songsToDelete.size(), songsToDelete.size()))
                .setCancelable(false)
                .create();

        deletingSongsDialog.show();

        new Thread(() -> {
            DeletionAttempt deletionAttempt = SongBackup.deleteBash(songsToDelete);

            requireActivity().runOnUiThread(() -> {
                deletingSongsDialog.dismiss();

                if (!deletionAttempt.isSuccessful()) {
                    String deletionFailedMessage = getResources().getQuantityString(
                            R.plurals.list_selection_action_mode_delete_failed,
                            deletionAttempt.getFailedDeletions(),
                            deletionAttempt.getFailedDeletions()
                    );

                    new AlertDialog.Builder(requireContext())
                            .setMessage(deletionFailedMessage)
                            .setPositiveButton(R.string.all_ok, null)
                            .create()
                            .show();
                }

                requireContext().sendBroadcast(MainBroadcastReceiver.getLoadListIntent());

                selectionActionMode.finish();
            });
        }).start();
    }

    private void setupSearchView(@NonNull Menu menu) {
        MenuItem searchMenuItem = menu.findItem(R.id.song_search);
        SearchView searchView = (SearchView) searchMenuItem.getActionView();
        searchView.setQueryHint(getString(R.string.list_search_query_hint));

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                viewModel.filterSong(newText);
                return true;
            }
        });

        String filterValue = viewModel.getSongFilter().getValue();
        if (filterValue != null && !filterValue.isEmpty()) {
            searchView.setQuery(filterValue, false);
            searchView.setIconified(false);
        }
    }
}
