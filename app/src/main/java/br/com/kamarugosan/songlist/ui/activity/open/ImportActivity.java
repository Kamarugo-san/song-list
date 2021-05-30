package br.com.kamarugosan.songlist.ui.activity.open;

import android.app.AlertDialog;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.List;

import br.com.kamarugosan.songlist.R;
import br.com.kamarugosan.songlist.model.Song;
import br.com.kamarugosan.songlist.storage.DeletionAttempt;
import br.com.kamarugosan.songlist.storage.SongBackup;
import br.com.kamarugosan.songlist.storage.ZipCopyAttempt;
import br.com.kamarugosan.songlist.ui.activity.main.MainBroadcastReceiver;

public class ImportActivity extends AppCompatActivity {
    public ImportActivity() {
        super(R.layout.activity_import);
    }

    @Override
    public void onBackPressed() {
        // Empty to avoid user closing the app during the import process
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        setTheme(R.style.Theme_SongList);

        super.onCreate(savedInstanceState);

        Uri data = getIntent().getData();

        if (data == null) {
            Toast.makeText(this, R.string.import_no_data_received, Toast.LENGTH_LONG).show();
            finish();
        }

        new Thread(() -> {
            List<Song> allSongs = SongBackup.loadAll(ImportActivity.this);
            ZipCopyAttempt zipCopyAttempt = SongBackup.copyFromZip(ImportActivity.this, data);
            List<Song> duplicatedSongs = new ArrayList<>();

            for (Song copiedSong : zipCopyAttempt.getCopiedSongs()) {
                if (copiedSong == null) {
                    continue;
                }

                for (Song existingSong : allSongs) {
                    if (existingSong.hasSameHash(copiedSong)) {
                        duplicatedSongs.add(copiedSong);
                        break;
                    }
                }
            }

            runOnUiThread(() -> {
                if (!duplicatedSongs.isEmpty()) {
                    new AlertDialog.Builder(this)
                            .setMessage(getResources().getQuantityString(R.plurals.import_duplicated_songs_message, duplicatedSongs.size()))
                            .setPositiveButton(R.string.all_keep, (dialog, which) -> {
                                sendBroadcast(MainBroadcastReceiver.getLoadListIntent());
                                Toast.makeText(ImportActivity.this, getResources().getQuantityText(R.plurals.import_duplicated_songs_kept, duplicatedSongs.size()), Toast.LENGTH_LONG).show();
                                finish();
                            })
                            .setNegativeButton(R.string.all_delete, (dialog, which) -> new Thread(() -> {
                                DeletionAttempt deletionAttempt = SongBackup.deleteBash(duplicatedSongs);

                                CharSequence toastText;
                                if (deletionAttempt.isSuccessful()) {
                                    toastText = getResources().getQuantityText(R.plurals.import_duplicated_songs_deleted, duplicatedSongs.size()).toString();
                                } else {
                                    toastText = getString(R.string.import_duplicated_songs_delete_failure);
                                }

                                runOnUiThread(() -> {
                                    sendBroadcast(MainBroadcastReceiver.getLoadListIntent());
                                    Toast.makeText(ImportActivity.this, toastText, Toast.LENGTH_LONG).show();
                                    finish();
                                });
                            }).start())
                            .setCancelable(false)
                            .create()
                            .show();

                    return;
                }

                String toastText = "";

                switch (zipCopyAttempt.getResult()) {
                    case ZipCopyAttempt.RESULT_SUCCESSFUL:
                        toastText = getString(R.string.import_successful_message);
                        break;

                    case ZipCopyAttempt.RESULT_PARTIAL_FAILURE:
                        toastText = getResources().getQuantityString(R.plurals.import_partial_failure_message, zipCopyAttempt.getFailures(), zipCopyAttempt.getFailures());
                        break;

                    case ZipCopyAttempt.RESULT_FAILURE:
                        toastText = getString(R.string.import_failure_message);
                        break;
                }

                sendBroadcast(MainBroadcastReceiver.getLoadListIntent());
                Toast.makeText(this, toastText, Toast.LENGTH_LONG).show();

                finish();
            });
        }).start();
    }
}
