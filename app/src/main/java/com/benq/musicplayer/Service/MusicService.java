package com.benq.musicplayer.Service;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.PowerManager;
import android.os.RemoteException;
import android.widget.RemoteViews;

import com.benq.musicplayer.Activity.PlayActivity;
import com.benq.musicplayer.Data.Music;
import com.benq.musicplayer.R;
import com.benq.musicplayer.Utils.Constants;
import com.benq.musicplayer.Utils.LogUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

public class MusicService extends Service implements MediaPlayer.OnPreparedListener, MediaPlayer.OnErrorListener
        , MediaPlayer.OnCompletionListener {

    private static final String TAG = MusicService.class.getSimpleName();
    public static final int MSG_UPDATE_TIME = 0;
    public static final int MSG_START = 1;
    public static final int MSG_STOP = 2;
    public static final int MSG_UPDATE_POS = 3;
    public static final int MSG_STOP_SERVICE = 4;

    private int mCurPos;
    private ArrayList<Music> mMusicList;
    private ArrayList<Music> mShuffleList;
    private ArrayList<Music> mCurrentList;
    private MediaPlayer mMediaPlayer = null;
    private NotificationManager mNotificationManager;
    private Notification mNotification = null;
    private final int NOTIFICATION_ID = 1;
    private Timer mTimer;
    private Music mMusic;
    private ServiceBinder mBinder = new ServiceBinder();
    private int mRepeat;
    private boolean mShuffle;
    private ArrayList<Messenger> mClients;
    private BroadcastReceiver mReceiver;
    private boolean mRegister = false;

    enum State {
        Retrieving,
        Stopped,
        Preparing,
        Playing,
        Paused
    }

    State mState = State.Retrieving;

    public void registerMessenger(Messenger messenger) {
        if (!mClients.contains(messenger)) {
            mClients.add(messenger);
        }
    }

    public void unregisterMessenger(Messenger messenger) {
        if (mClients.contains(messenger)) {
            mClients.remove(messenger);
        }
    }

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        LogUtils.e(TAG, "onError", what, extra);
        mp.reset();
        return false;
    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        mMediaPlayer.start();
        if (mTimer == null) {
            mTimer = new Timer();
            mTimer.schedule(new TimerTask() {

                @Override
                public void run() {
                    sendMessage(MSG_UPDATE_TIME, getCurrentPosition());
                }

            }, 0, 1000);
        }
        mState = State.Playing;
        sendMessage(MSG_START, mMusic.getId());
        setUpAsForeground();
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        LogUtils.d(TAG, "onCompletion");
        if (mRepeat == Constants.ONE_REPEAT) {
            // do nothing just replay
        } else {
            mCurPos++;
            if (mCurPos >= mCurrentList.size()) {
                if (mRepeat == Constants.ALL_REPEAT) {
                    mCurPos = 0;
                } else {
                    stopMusic();
                    return;
                }
            }

        }
        startMusic();
    }

    public MusicService() {
        mClients = new ArrayList<>();
    }

    public class ServiceBinder extends Binder {
        public MusicService getService() {
            return MusicService.this;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public void unbindService(ServiceConnection conn) {
        super.unbindService(conn);
    }

    @Override
    public void onCreate() {
        mNotificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        mMediaPlayer = new MediaPlayer();
        mMediaPlayer.setOnCompletionListener(this);
        mMediaPlayer.setOnPreparedListener(this);
        mMediaPlayer.setOnErrorListener(this);
        mMediaPlayer.setWakeMode(getApplicationContext(), PowerManager.PARTIAL_WAKE_LOCK);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        LogUtils.d(TAG, "onStartCommand");
        if (!mRegister) {
            mReceiver = new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    // click close button on notification
                    LogUtils.d(TAG, "onReceive to stop");
                    sendMessage(MSG_STOP_SERVICE, null);
                    stopSelf();
                }
            };
            getApplicationContext().registerReceiver(mReceiver, new IntentFilter(Constants.ACTION_CLOSE));
            mRegister = true;
        }
        return START_STICKY;
    }

    public void onDestroy() {
        LogUtils.d(TAG, "onDestroy");
        stopMusic();
        getApplicationContext().unregisterReceiver(mReceiver);
        mMediaPlayer.release();
        mState = State.Retrieving;
    }

    public void previous() {
        if (mCurPos <= 0 || mState.equals(State.Preparing)) {
            return;
        }
        mCurPos--;
        startMusic();
    }

    public void next() {
        if (mCurPos >= mCurrentList.size() || mState.equals(State.Preparing)) {
            return;
        }
        mCurPos++;
        startMusic();
    }

    public void prepareMusic(int position, ArrayList<Music> musicList, ArrayList<Music> shuffleList, boolean shuffle, int repeat) {
        mCurPos = position;
        mMusicList = musicList;
        mShuffleList = shuffleList;
        mShuffle = shuffle;
        mRepeat = repeat;
        if (mShuffle) {
            mCurrentList = shuffleList;
        } else {
            mCurrentList = musicList;
        }
        startMusic();
    }

    public void startMusic() {
        if (mCurrentList == null || mCurPos >= mCurrentList.size()) {
            return;
        }
        mMusic = mCurrentList.get(mCurPos);
        if (mMediaPlayer.isPlaying()) {
            mMediaPlayer.stop();
        }
        try {
            mMediaPlayer.reset();
            mMediaPlayer.setDataSource(mMusic.getData());
            mState = State.Preparing;
            mMediaPlayer.prepareAsync();
        } catch (IllegalArgumentException e) {
            LogUtils.e(TAG, e.toString());
        } catch (SecurityException e) {
            LogUtils.e(TAG, e.toString());
        } catch (IllegalStateException e) {
            LogUtils.e(TAG, e.toString());
        } catch (IOException e) {
            LogUtils.e(TAG, e.toString());
        }
    }

    public void pauseMusic() {
        if (mState.equals(State.Playing)) {
            mMediaPlayer.pause();
            mState = State.Paused;
        }
    }

    public void restartMusic() {
        if (!mState.equals(State.Preparing) && !mState.equals(State.Retrieving)) {
            mMediaPlayer.start();
            mState = State.Playing;
        }
    }

    public void stopMusic() {
        mState = State.Stopped;
        try {
            if (mMediaPlayer.isPlaying()) {
                mMediaPlayer.stop();
            }
        } catch (IllegalStateException e) {
            LogUtils.e(TAG, e.getMessage());
        }
        mTimer.cancel();
        mTimer = null;
        sendMessage(MSG_STOP, (long)0);
    }

    public boolean isPlaying() {
        return mState.equals(State.Playing);
    }

    public int getCurrentPosition() {
        return mMediaPlayer.getCurrentPosition();
    }

    public int getMusicListPosition() {
        return mMusicList != null ? mMusicList.indexOf(mMusic) : -1;
    }

    public int getCurrentListPosition() {
        return mCurPos;
    }


    public long getPlayMusicId() {
        return mMusic != null ? mMusic.getId() : -1;
    }

    public void seekMusicTo(int pos) {
        mMediaPlayer.seekTo(pos * 1000);
    }

    public void setShuffle(boolean isShuffle) {
        mShuffle = isShuffle;
        if (mShuffle) {
            mCurrentList = mShuffleList;
        } else {
            mCurrentList = mMusicList;
        }
        // update position
        mCurPos = mCurrentList.indexOf(mMusic);
        sendMessage(MSG_UPDATE_POS, mCurPos);
    }

    private void sendMessage(int msg, Object obj) {
        for (Messenger client : mClients) {
            try {
                client.send(Message.obtain(null, msg, obj));
            } catch (RemoteException e) {
                LogUtils.e(TAG, e.getMessage());
            }
        }
    }

    public boolean getShuffle() {
        return mShuffle;
    }

    public ArrayList<Music> getMusicList() {
        return mMusicList;
    }

    public ArrayList<Music> getShuffleList() {
        return mShuffleList;
    }

    public void setRepeat(int repeat) {
        mRepeat = repeat;
    }

    public int getRepeat() {
        return mRepeat;
    }

    private void setUpAsForeground() {
        RemoteViews remoteViews = new RemoteViews(getPackageName(), R.layout.custom_notification);
        remoteViews.setTextViewText(R.id.title, mMusic.getTitle());
        Intent intent = new Intent();
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.setClass(getApplicationContext(), PlayActivity.class);
        intent.putParcelableArrayListExtra(Constants.MUSIC_LIST, mCurrentList);
        intent.putExtra(Constants.CURRENT_POSITION, mCurPos);
        intent.putExtra(Constants.IS_FROM_NOTIFICATION, true);
        PendingIntent pi = PendingIntent.getActivity(getApplicationContext(), 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        mNotification = new Notification.Builder(getApplicationContext())
                .setAutoCancel(false)
                .setContent(remoteViews)
                .setContentIntent(pi)
                .setSmallIcon(R.drawable.ic_play_normal_36dp)
                .build();
        // for close button use
        Intent closeIntent = new Intent(Constants.ACTION_CLOSE);
        PendingIntent pendingCloseIntent = PendingIntent.getBroadcast(this, 0, closeIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        remoteViews.setOnClickPendingIntent(R.id.close, pendingCloseIntent);
        startForeground(NOTIFICATION_ID, mNotification);
    }

    public static Intent getPlayIntent(Context context) {
        Intent intent = new Intent();
        intent.setClass(context, MusicService.class);
        return intent;
    }
}
