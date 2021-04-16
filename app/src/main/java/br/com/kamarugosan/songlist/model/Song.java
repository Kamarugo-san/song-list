package br.com.kamarugosan.songlist.model;

public class Song {
    private final String title;
    private final String artist;
    private final String lyrics;
    private boolean imported = false;
    private String filePath = null;

    public Song(String title, String artist, String lyrics) {
        this.title = title;
        this.artist = artist;
        this.lyrics = lyrics;
    }

    public String getTitle() {
        return title;
    }

    public String getArtist() {
        return artist;
    }

    public String getLyrics() {
        return lyrics;
    }

    public boolean isImported() {
        return imported;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setImported(boolean imported) {
        this.imported = imported;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }
}
