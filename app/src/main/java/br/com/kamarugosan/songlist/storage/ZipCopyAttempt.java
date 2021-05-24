package br.com.kamarugosan.songlist.storage;

import java.util.List;

import br.com.kamarugosan.songlist.model.Song;

public class ZipCopyAttempt {
    public static final int RESULT_SUCCESSFUL = 1;
    public static final int RESULT_PARTIAL_FAILURE = 0;
    public static final int RESULT_FAILURE = -1;

    final int entryCount;
    final int failures;
    final List<Song> copiedSongs;

    public ZipCopyAttempt(int entryCount, int failures, List<Song> copiedSongs) {
        this.entryCount = entryCount;
        this.failures = failures;
        this.copiedSongs = copiedSongs;
    }

    public int getEntryCount() {
        return entryCount;
    }

    public int getFailures() {
        return failures;
    }

    public int getResult() {
        if (failures == 0 && entryCount > 0) {
            return RESULT_SUCCESSFUL;
        }

        if (failures > 0 && failures < entryCount) {
            return RESULT_PARTIAL_FAILURE;
        }

        return RESULT_FAILURE;
    }

    public List<Song> getCopiedSongs() {
        return copiedSongs;
    }
}
