package com.benq.musicplayer.Data;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by Steven.SL.Tai on 2015/11/3.
 */
public class Music implements Parcelable {

    private Long mId;
    private String mArtist;
    private String mAlbum;
    private String mTitle;
    private String mData;
    private String mArtPath;
    private Long mAlbumId;
    private int mDuration;

    Music(Long id, String artist, String album, String title, String data, Long albumId, String artPath, int duration) {
        mId = id;
        mArtist = artist;
        mAlbum = album;
        mTitle = title;
        mData = data;
        mAlbumId = albumId;
        mArtPath = artPath;
        mDuration = duration;
    }

    public Long getId() {
        return mId;
    }

    public String getArtPath() {
        return mArtPath;
    }

    public String getArtist() {
        return mArtist;
    }

    public String getAlbum() {
        return mAlbum;
    }

    public String getTitle() {
        return mTitle;
    }

    public String getData() {
        return mData;
    }

    public Long getAlbumId() {
        return mAlbumId;
    }

    public int getDuration() {
        return mDuration;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(mId);
        dest.writeString(mArtist);
        dest.writeString(mAlbum);
        dest.writeString(mTitle);
        dest.writeString(mData);
        dest.writeString(mArtPath);
        dest.writeLong(mAlbumId);
        dest.writeInt(mDuration);
    }

    public static final Parcelable.Creator<Music> CREATOR = new Creator<Music>() {
        @Override
        public Music createFromParcel(Parcel source) {
            Music music = new MusicBuilder()
                    .setId(source.readLong())
                    .setArtist(source.readString())
                    .setAlbum(source.readString())
                    .setTitle(source.readString())
                    .setData(source.readString())
                    .setArtPath(source.readString())
                    .setAlbumId(source.readLong())
                    .setDuration(source.readInt())
                    .build();
            return music;
        }

        @Override
        public Music[] newArray(int size) {
            return new Music[size];
        }
    };

    @Override
    public boolean equals(Object o) {
        if (o instanceof Music) {
            Music music = (Music) o;
            return getId().equals(music.getId());
        }
            return super.equals(o);
    }
}
