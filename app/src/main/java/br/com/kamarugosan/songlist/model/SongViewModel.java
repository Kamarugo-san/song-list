package br.com.kamarugosan.songlist.model;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.List;

public class SongViewModel extends ViewModel {
    private final MutableLiveData<List<Song>> songList = new MutableLiveData<>();
    private final MutableLiveData<Song> selectedSong = new MutableLiveData<>();

    public void postSongList(List<Song> value){
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
}
