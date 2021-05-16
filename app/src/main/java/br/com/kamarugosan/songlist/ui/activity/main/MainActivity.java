package br.com.kamarugosan.songlist.ui.activity.main;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.NavigationUI;

import java.util.List;
import java.util.Objects;

import br.com.kamarugosan.songlist.R;
import br.com.kamarugosan.songlist.model.Song;
import br.com.kamarugosan.songlist.model.SongViewModel;
import br.com.kamarugosan.songlist.storage.DefaultSongsLoader;
import br.com.kamarugosan.songlist.storage.SongBackup;
import br.com.kamarugosan.songlist.ui.activity.main.song.SongFragment;

import static br.com.kamarugosan.songlist.ui.activity.main.song.SharedPreferencesConstants.PREF_DEFAULT_SONGS_IMPORTED;
import static br.com.kamarugosan.songlist.ui.activity.main.song.SharedPreferencesConstants.PREF_LYRICS_TEXT_SIZE;
import static br.com.kamarugosan.songlist.ui.activity.main.song.SharedPreferencesConstants.SHARED_PREFERENCES_MAIN_NAME;

public class MainActivity extends AppCompatActivity {
    private static final String THREAD_NAME_LOAD_SONG_FILES = "loadSongFiles";

    private NavController navController;
    private SongViewModel viewModel;
    private MainBroadcastReceiver broadcastReceiver;

    private SharedPreferences prefs;

    public MainActivity() {
        super(R.layout.activity_main);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        setTheme(R.style.Theme_SongList);

        super.onCreate(savedInstanceState);

        setup();
        loadList();
    }

    @Override
    public boolean onSupportNavigateUp() {
        return navController.navigateUp() || super.onSupportNavigateUp();
    }

    @Override
    protected void onDestroy() {
        if (broadcastReceiver != null) {
            unregisterReceiver(broadcastReceiver);
        }

        super.onDestroy();
    }

    public void loadList() {
        new Thread(() -> {
            if (!prefs.getBoolean(PREF_DEFAULT_SONGS_IMPORTED, false)) {
                List<Song> songs = DefaultSongsLoader.loadDefaultSongs(MainActivity.this);

                for (Song song : songs) {
                    SongBackup.save(MainActivity.this, song);
                }

                prefs.edit().putBoolean(PREF_DEFAULT_SONGS_IMPORTED, true).apply();
            }

            List<Song> list = SongBackup.loadAll(MainActivity.this);
            viewModel.postSongList(list);
        }, THREAD_NAME_LOAD_SONG_FILES).start();
    }

    private void setup() {
        viewModel = new ViewModelProvider(this).get(SongViewModel.class);

        prefs = getSharedPreferences(SHARED_PREFERENCES_MAIN_NAME, Context.MODE_PRIVATE);
        float prefsLyricsTextSize = prefs.getFloat(PREF_LYRICS_TEXT_SIZE, SongFragment.DEFAULT_LYRICS_TEXT_SIZE);
        viewModel.setLyricsTextSize(prefsLyricsTextSize);

        viewModel.getLyricsTextSize().observe(this, lyricsTextSize -> prefs.edit().putFloat(PREF_LYRICS_TEXT_SIZE, lyricsTextSize).apply());

        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager().findFragmentById(R.id.nav_host_container);
        navController = Objects.requireNonNull(navHostFragment).getNavController();

        Toolbar toolbar = findViewById(R.id.toolbar);

        setSupportActionBar(toolbar);
        NavigationUI.setupActionBarWithNavController(this, navController);

        broadcastReceiver = new MainBroadcastReceiver(this);
        registerReceiver(broadcastReceiver, MainBroadcastReceiver.getIntentFilter());
    }
}
