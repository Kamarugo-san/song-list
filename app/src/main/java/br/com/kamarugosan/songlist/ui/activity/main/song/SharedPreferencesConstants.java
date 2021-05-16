package br.com.kamarugosan.songlist.ui.activity.main.song;

public interface SharedPreferencesConstants {
    String SHARED_PREFERENCES_MAIN_NAME = "br.com.kamarugosan.songlist";

    /**
     * Float value holding the font size for the lyrics.
     */
    String PREF_LYRICS_TEXT_SIZE = "lyricsTextSize";

    /**
     * Boolean value that tells whether the default songs have already been imported.
     */
    String PREF_DEFAULT_SONGS_IMPORTED = "defaultSongsImported";
}
