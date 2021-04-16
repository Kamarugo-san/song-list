package br.com.kamarugosan.songlist.storage;

import android.content.Context;

import androidx.annotation.NonNull;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import br.com.kamarugosan.songlist.R;
import br.com.kamarugosan.songlist.model.Song;

public class SongSharing {
    /**
     * The name of the folder that contains the exported songs files.
     */
    private static final String EXPORTED_SONGS_FOLDER = "exported";

    /**
     * Pattern to create the name of the zip file containing the exported songs.
     */
    private static final String EXPORTED_SONGS_FILE_NAME = "%1$s.zip";

    public static File shareSongs(List<Song> songsToShare, @NonNull Context context) {
        if (songsToShare == null || songsToShare.isEmpty()) {
            return null;
        }

        File externalFilesDir = context.getExternalFilesDir(null);

        File exportedSongsFolder = new File(externalFilesDir, EXPORTED_SONGS_FOLDER);

        if (!exportedSongsFolder.exists()) {
            if (!exportedSongsFolder.mkdirs()) {
                throw new RuntimeException("Unable to create exported songs folder");
            }
        }

        List<String> filePaths = new ArrayList<>();
        for (Song song : songsToShare) {
            if (song.getFilePath() != null && !song.getFilePath().isEmpty()) {
                filePaths.add(song.getFilePath());
            }
        }

        File exportedSongsFile = new File(externalFilesDir, EXPORTED_SONGS_FOLDER + "/" + getExportedSongsFileName(context));

        try (FileOutputStream destination = new FileOutputStream(exportedSongsFile);
             ZipOutputStream zipOutputStream = new ZipOutputStream(new BufferedOutputStream(destination))) {
            int bufferSize = 2048;
            byte[] data = new byte[bufferSize];

            for (String songFile : filePaths) {
                try (FileInputStream fileInputStream = new FileInputStream(songFile);
                     BufferedInputStream origin = new BufferedInputStream(fileInputStream, bufferSize)) {

                    ZipEntry zipEntry = new ZipEntry(getFileNameFromPath(songFile));
                    zipOutputStream.putNextEntry(zipEntry);

                    int count;
                    while ((count = origin.read(data, 0, bufferSize)) != -1) {
                        zipOutputStream.write(data, 0, count);
                    }
                }
            }

            zipOutputStream.finish();
            zipOutputStream.close();

            return exportedSongsFile;
        } catch (IOException ioException) {
            ioException.printStackTrace();
        }

        return null;
    }

    @NonNull
    private static String getFileNameFromPath(@NonNull String filePath) {
        return filePath.substring(filePath.lastIndexOf("/") + 1);
    }

    @NonNull
    private static String getExportedSongsFileName(@NonNull Context context) {
        String appName = context.getString(R.string.app_name);

        return String.format(EXPORTED_SONGS_FILE_NAME, appName);
    }
}
