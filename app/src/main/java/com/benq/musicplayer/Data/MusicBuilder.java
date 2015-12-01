package com.benq.musicplayer.Data;

/**
 * Created by Steven.SL.Tai on 2015/11/3.
 */
public class MusicBuilder {

    private Long _Id;
    private String _Artist;
    private String _Album;
    private String _Title;
    private String _Data;
    private Long _AlbumId;
    private String _ArtPath;
    private int _Duration;

    public Music build() {
        return new Music(_Id, _Artist, _Album, _Title, _Data, _AlbumId, _ArtPath, _Duration);
    }

    public MusicBuilder setId(Long id) {
        _Id = id;
        return this;
    }

    public MusicBuilder setArtPath(String artPath) {
        _ArtPath = artPath;
        return this;
    }

    public MusicBuilder setArtist(String artist) {
        _Artist = artist;
        return this;
    }

    public MusicBuilder setAlbum(String album) {
        _Album = album;
        return this;
    }

    public MusicBuilder setData(String data) {
        _Data = data;
        return this;
    }

    public MusicBuilder setTitle(String title) {
        _Title = title;
        return this;
    }

    public MusicBuilder setAlbumId(Long albumId) {
        _AlbumId = albumId;
        return this;
    }

    public MusicBuilder setDuration(int duration) {
        _Duration = duration;
        return this;
    }
}
