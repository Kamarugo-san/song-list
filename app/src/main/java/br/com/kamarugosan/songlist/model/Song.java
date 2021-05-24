package br.com.kamarugosan.songlist.model;

import androidx.annotation.NonNull;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
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
    private final String hash;

    public Song(String title, String artist, String lyrics) {
        this.title = title;
        this.artist = artist;
        this.lyrics = lyrics;
        this.markings = new ArrayList<>();

        String hash = null;

        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] digested = md.digest(getFullTrimmedNormalizedContent().getBytes());

            BigInteger no = new BigInteger(1, digested);

            StringBuilder hashText = new StringBuilder(no.toString(16));
            while (hashText.length() < 32) {
                hashText.insert(0, "0");
            }

            hash = hashText.toString();
        } catch (NoSuchAlgorithmException exception) {
            exception.printStackTrace();
        }

        this.hash = hash;
    }

    public Song(@NonNull Song song) {
        this.title = song.title;
        this.artist = song.artist;
        this.lyrics = song.lyrics;
        this.filePath = song.filePath;
        this.markings = new ArrayList<>(song.markings);
        this.hash = song.hash;
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

    @NonNull
    private String getFullTrimmedNormalizedContent() {
        return lowerCaseAndNormalizeString(title).trim() + "|" +
                lowerCaseAndNormalizeString(artist).trim() + "|" +
                lowerCaseAndNormalizeString(lyrics).trim();
    }

    public boolean hasSameHash(Song song) {
        if (hash != null && !hash.isEmpty()) {
            return hash.equals(song.hash);
        }

        return false;
    }
}
