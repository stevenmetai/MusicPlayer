package com.benq.musicplayer.Utils;

import android.content.Context;
import android.content.CursorLoader;
import android.database.Cursor;

/**
 * Created by Steven.SL.Tai on 2015/11/3.
 */
public class TrackCursorLoader extends CursorLoader {

    private Context mContext;
    public TrackCursorLoader(Context context) {
        super(context);
        mContext = context;
    }

    @Override
    protected Cursor onLoadInBackground() {
        return MusicUtil.getMusics(mContext);
    }
}
