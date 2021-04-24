package br.com.kamarugosan.songlist.ui.activity.main.list;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import br.com.kamarugosan.songlist.R;
import br.com.kamarugosan.songlist.model.Song;

public class SongAdapter extends RecyclerView.Adapter<SongViewHolder> {
    private List<Song> fullDataSet;
    private List<Song> filteredDataSet;
    private final Context context;
    private final List<Integer> selectedItemsPositions = new ArrayList<>();
    private final ListItemClickListener clickListener;
    private final ListItemLongClickListener longClickListener;

    public SongAdapter(Context context, List<Song> fullDataSet, ListItemClickListener clickListener, ListItemLongClickListener longClickListener) {
        this.context = context;
        this.fullDataSet = fullDataSet;
        this.filteredDataSet = new ArrayList<>(fullDataSet);
        this.clickListener = clickListener;
        this.longClickListener = longClickListener;
    }

    @NonNull
    @Override
    public SongViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_song, parent, false);

        return new SongViewHolder(view, clickListener, longClickListener);
    }

    @Override
    public void onBindViewHolder(@NonNull SongViewHolder holder, int position) {
        holder.bind(filteredDataSet.get(position), context, selectedItemsPositions.contains(position));
    }

    @Override
    public int getItemCount() {
        return filteredDataSet.size();
    }

    public List<Song> getFilteredDataSet() {
        return this.filteredDataSet;
    }

    public void filter(String filter) {
        if (filter == null || filter.isEmpty()) {
            this.filteredDataSet = new ArrayList<>(fullDataSet);
        } else {
            this.filteredDataSet = new ArrayList<>();

            for (Song song : fullDataSet) {
                if (song.contains(filter)) {
                    this.filteredDataSet.add(song);
                }
            }
        }

        notifyDataSetChanged();
    }

    public void setDataSet(List<Song> dataSet) {
        this.fullDataSet = dataSet;
        this.filteredDataSet = new ArrayList<>(dataSet);
        notifyDataSetChanged();
    }

    public List<Integer> getSelectedItemsPositions() {
        return selectedItemsPositions;
    }

    public List<Song> getSelectedItems() {
        List<Song> selectedItems = new ArrayList<>();

        for (Integer selectedItemPosition : selectedItemsPositions) {
            selectedItems.add(filteredDataSet.get(selectedItemPosition));
        }

        return selectedItems;
    }
}
