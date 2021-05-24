package br.com.kamarugosan.songlist.storage;

import android.app.Activity;
import android.content.res.AssetManager;

import androidx.annotation.NonNull;

import com.google.gson.Gson;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import br.com.kamarugosan.songlist.model.Song;

public class DefaultSongsLoader {
    /**
     * The folder the songs are stored on the assets.
     */
    public static final String SONGS_FOLDER = "songs";

    /**
     * Loads all songs from the default asset directory.
     *
     * @param activity the {@link Activity} to get the assets from
     * @return all the loaded songs. Ignores the ones that could not be parsed
     */
    @NonNull
    public static List<Song> loadDefaultSongs(@NonNull Activity activity) {
        final List<Song> list = new ArrayList<>();
        final Gson gson = new Gson();

        try {
            AssetManager assets = activity.getAssets();
            String[] assetsList = assets.list(SONGS_FOLDER);

            for (String file : assetsList) {
                InputStream fileInputStream = assets.open(SONGS_FOLDER + "/" + file);

                Song song = SongBackup.readSongFile(fileInputStream, gson);
                if (song != null) {
                    list.add(new Song(song.getTitle(), song.getArtist(), song.getLyrics()));
                }
            }
        } catch (IOException exception) {
            exception.printStackTrace();
        }

        return list;
    }
}
