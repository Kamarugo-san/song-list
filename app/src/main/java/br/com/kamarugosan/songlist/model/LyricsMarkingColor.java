package br.com.kamarugosan.songlist.model;

import androidx.annotation.ColorInt;

import br.com.kamarugosan.songlist.R;

public enum LyricsMarkingColor {
    GREEN(R.color.lyrics_selection_bg_green),
    PINK(R.color.lyrics_selection_bg_pink),
    YELLOW(R.color.lyrics_selection_bg_yellow);

    @ColorInt
    private final int color;

    LyricsMarkingColor(int color) {
        this.color = color;
    }

    public int getColor() {
        return color;
    }
}
