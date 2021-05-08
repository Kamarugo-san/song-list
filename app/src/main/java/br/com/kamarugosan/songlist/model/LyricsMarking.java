package br.com.kamarugosan.songlist.model;

public class LyricsMarking {
    private final LyricsSelectionPosition position;
    private final LyricsMarkingColor color;

    public LyricsMarking(int start, int end, LyricsMarkingColor color) {
        this.position = new LyricsSelectionPosition(start, end);
        this.color = color;
    }

    public LyricsSelectionPosition getPosition() {
        return position;
    }

    public LyricsMarkingColor getColor() {
        return color;
    }
}
