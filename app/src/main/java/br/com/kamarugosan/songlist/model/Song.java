package br.com.kamarugosan.songlist.model;

import androidx.annotation.NonNull;

import java.text.Normalizer;
import java.util.Locale;

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

    public boolean contains(@NonNull String text) {
        String lowercaseText = text.toLowerCase(Locale.getDefault());

        return lowerCaseAndNormalizeString(title).contains(lowercaseText)
                || lowerCaseAndNormalizeString(artist).contains(lowercaseText)
                || lowerCaseAndNormalizeString(lyrics).contains(lowercaseText);
    }

    @NonNull
    private String lowerCaseAndNormalizeString(String targetString) {
        targetString = targetString.toLowerCase(Locale.getDefault());
        return Normalizer.normalize(targetString, Normalizer.Form.NFD).replaceAll("[^\\p{ASCII}]", "");
    }
}
