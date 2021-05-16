package br.com.kamarugosan.songlist.model;

import androidx.annotation.NonNull;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class Song {
    private final String title;
    private final String artist;
    private final String lyrics;
    private String filePath = null;
    private final List<LyricsMarking> markings;

    public Song(String title, String artist, String lyrics) {
        this.title = title;
        this.artist = artist;
        this.lyrics = lyrics;
        this.markings = new ArrayList<>();
    }

    public Song(@NonNull Song song) {
        this.title = song.title;
        this.artist = song.artist;
        this.lyrics = song.lyrics;
        this.filePath = song.filePath;
        this.markings = new ArrayList<>(song.markings);
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

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public List<LyricsMarking> getMarkings() {
        return markings;
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

    public void addMarking(LyricsMarking marking) {
        markings.add(marking);
    }

    public void removeMarkings() {
        markings.clear();
    }
}
