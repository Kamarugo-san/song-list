package br.com.kamarugosan.songlist.model;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.List;

public class SongViewModel extends ViewModel {
    private final MutableLiveData<List<Song>> songList = new MutableLiveData<>();
    private final MutableLiveData<Song> selectedSong = new MutableLiveData<>();
    private final MutableLiveData<String> songFilter = new MutableLiveData<>(null);

    public void postSongList(List<Song> value) {
        songList.postValue(value);
    }

    public LiveData<List<Song>> getSongList() {
        return songList;
    }

    public void selectSong(Song song) {
        selectedSong.setValue(song);
    }

    public LiveData<Song> getSelectedSong() {
        return selectedSong;
    }

    public void filterSong(String filter) {
        songFilter.setValue(filter);
    }

    public LiveData<String> getSongFilter() {
        return songFilter;
    }
}
