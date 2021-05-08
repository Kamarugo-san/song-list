package br.com.kamarugosan.songlist.model;

public class LyricsSelectionPosition {
    private final int start;
    private final int end;

    public LyricsSelectionPosition(int start, int end) {
        this.start = start;
        this.end = end;
    }

    public int getStart() {
        return start;
    }

    public int getEnd() {
        return end;
    }
}
