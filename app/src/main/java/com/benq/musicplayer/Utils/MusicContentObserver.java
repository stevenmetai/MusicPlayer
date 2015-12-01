package com.benq.musicplayer.Utils;

import android.content.Context;
import android.database.ContentObserver;
import android.os.Handler;

/**
 * Created by Steven.SL.Tai on 2015/11/13.
 */
public class MusicContentObserver extends ContentObserver {

    private static final String TAG = MusicContentObserver.class.getSimpleName();
    private Handler mHandler;
    private Context mContext;
    public static int MSG_RELOAD = 1;

    /**
     * Creates a content observer.
     *
     * @param handler The handler to run {@link #onChange} on, or null if none.
     */
    public MusicContentObserver(Context context, Handler handler) {
        super(handler);
        mContext = context;
        mHandler = handler;
    }

    @Override
    public void onChange(boolean selfChange) {
        mHandler.obtainMessage(MSG_RELOAD).sendToTarget();
        super.onChange(selfChange);
    }
}
