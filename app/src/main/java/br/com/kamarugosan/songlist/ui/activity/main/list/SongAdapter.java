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
    private List<Song> list;
    private final Context context;
    private final List<Integer> selectedItemsPositions = new ArrayList<>();
    private final ListItemClickListener clickListener;
    private final ListItemLongClickListener longClickListener;

    public SongAdapter(Context context, List<Song> list, ListItemClickListener clickListener, ListItemLongClickListener longClickListener) {
        this.context = context;
        this.list = list;
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
        holder.bind(list.get(position), context, selectedItemsPositions.contains(position));
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public List<Song> getList() {
        return this.list;
    }

    public void setList(List<Song> list) {
        this.list = list;
        notifyDataSetChanged();
    }

    public List<Integer> getSelectedItemsPositions() {
        return selectedItemsPositions;
    }

    public List<Song> getSelectedItems() {
        List<Song> selectedItems = new ArrayList<>();

        for (Integer selectedItemPosition : selectedItemsPositions) {
            selectedItems.add(list.get(selectedItemPosition));
        }

        return selectedItems;
    }
}
