package br.com.kamarugosan.songlist.ui.activity.open;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import br.com.kamarugosan.songlist.R;
import br.com.kamarugosan.songlist.storage.SongBackup;
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
            boolean success = SongBackup.copyFromZip(ImportActivity.this, data);

            runOnUiThread(() -> {
                Toast.makeText(this, success ? R.string.import_success_message : R.string.import_failed_message, Toast.LENGTH_LONG).show();
                sendBroadcast(new Intent(MainBroadcastReceiver.ACTION_LOAD_LIST));
                finish();
            });
        }).start();
    }
}
