package com.benq.musicplayer.Utils;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.provider.MediaStore;

/**
 * Created by Steven.SL.Tai on 2015/11/2.
 */
public class MusicUtil {

    private static final String TAG = MusicUtil.class.getSimpleName();

    public static Cursor getMusics(Context context) {
        ContentResolver resolver = context.getContentResolver();
        String[] projection = {
                MediaStore.Audio.Media._ID,
                MediaStore.Audio.Media.ARTIST,
                MediaStore.Audio.Media.TITLE,
                MediaStore.Audio.Media.DATA,
                MediaStore.Audio.Media.ALBUM,
                MediaStore.Audio.Media.ALBUM_ID,
                MediaStore.Audio.Media.DURATION,
        };
        return resolver.query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, projection, null, null, null);
    }

    public static String getAlbumArtPath(Context context, Long albumId) {
        ContentResolver resolver = context.getContentResolver();
        String[] projection = {
                MediaStore.Audio.Albums.ALBUM_ART
        };
        Cursor cursor = null;
        String artPath = null;
        try {
            cursor = resolver.query(MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI, projection,
                    MediaStore.Audio.Albums._ID + " = ?", new String[]{String.valueOf(albumId)}, null);
            while (cursor != null && cursor.moveToNext()) {
                artPath = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Albums.ALBUM_ART));
            }
        } catch (Exception e) {

        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return artPath;
    }
}
