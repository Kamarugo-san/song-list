package br.com.kamarugosan.songlist.ui.activity.main.list;

import android.content.Context;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import br.com.kamarugosan.songlist.R;
import br.com.kamarugosan.songlist.model.Song;

public class SongViewHolder extends RecyclerView.ViewHolder {
    private final ListItemClickListener clickListener;
    private final ListItemLongClickListener longClickListener;
    private final View root;
    private final TextView titleTv;
    private final TextView artistTv;
    private final View goToIv;
    private final View selectedIv;

    public SongViewHolder(@NonNull View itemView, ListItemClickListener clickListener, ListItemLongClickListener longClickListener) {
        super(itemView);

        this.root = itemView.findViewById(R.id.item_song_root);
        this.titleTv = itemView.findViewById(R.id.item_song_title);
        this.artistTv = itemView.findViewById(R.id.item_song_artist);
        this.goToIv = itemView.findViewById(R.id.item_song_go_to);
        this.selectedIv = itemView.findViewById(R.id.item_song_selected);
        this.clickListener = clickListener;
        this.longClickListener = longClickListener;
    }

    public void bind(@NonNull Song song, Context context, boolean isSelected) {
        if (clickListener != null) {
            root.setOnClickListener(v -> clickListener.onItemClick(getAdapterPosition()));
        } else {
            root.setOnClickListener(null);
        }

        if (longClickListener != null) {
            root.setOnLongClickListener(v -> {
                longClickListener.onItemLongClick(getAdapterPosition());
                return true;
            });
        } else {
            root.setOnLongClickListener(null);
        }

        titleTv.setText(song.getTitle());
        artistTv.setText(song.getArtist());

        if (isSelected) {
            itemView.setBackgroundColor(ContextCompat.getColor(context, R.color.list_item_selected_background));
            selectedIv.setVisibility(View.VISIBLE);
            goToIv.setVisibility(View.GONE);
        } else {
            itemView.setBackgroundColor(ContextCompat.getColor(context, R.color.list_item_background));
            selectedIv.setVisibility(View.GONE);
            goToIv.setVisibility(View.VISIBLE);
        }
    }
}
