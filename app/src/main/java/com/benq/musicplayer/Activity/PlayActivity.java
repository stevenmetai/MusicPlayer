package com.benq.musicplayer.Activity;

import android.content.ComponentName;
import android.content.Context;
import android.content.ServiceConnection;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.benq.musicplayer.Data.Music;
import com.benq.musicplayer.R;
import com.benq.musicplayer.Service.MusicService;
import com.benq.musicplayer.Utils.CircleTransform;
import com.benq.musicplayer.Utils.Constants;
import com.benq.musicplayer.Utils.LogUtils;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.lang.ref.WeakReference;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;

public class PlayActivity extends AppCompatActivity implements View.OnClickListener, SeekBar.OnSeekBarChangeListener {

    private static final String TAG = PlayActivity.class.getSimpleName();
    private ImageView mAlbumArt;
    private TextView mArtistName;
    private TextView mTrackName;
    private ImageView mRepeatButton;
    private ImageView mPreviousButton;
    private ImageView mPlayButton;
    private ImageView mNextButton;
    private ImageView mShuffleButton;
    private SeekBar mTimeBar;
    private TextView mElapsedTime;
    private TextView mFullTime;
    private ImageView mMuteButton;
    private SeekBar mVolumeBar;
    private int mVolume;
    private int mCurrentPlayPos;
    private ArrayList<Music> mMusicList;
    private ArrayList<Music> mShuffleList;
    private boolean mIsFromNotification = false;
    private int mRepeat;
    private boolean mShuffle = false;
    private boolean mMute = false;
    private AudioManager mAudioManager;
    private ServiceConnection mServiceConnection;
    private MusicService mService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_play);
        mAlbumArt = (ImageView) findViewById(R.id.album_art);
        mArtistName = (TextView) findViewById(R.id.artist);
        mTrackName = (TextView) findViewById(R.id.track_name);
        mTimeBar = (SeekBar) findViewById(R.id.time_seekBar);
        mElapsedTime = (TextView) findViewById(R.id.elapsed_time);
        mFullTime = (TextView) findViewById(R.id.full_time);
        mRepeatButton = (ImageView) findViewById(R.id.repeat);
        mPreviousButton = (ImageView) findViewById(R.id.previous);
        mPlayButton = (ImageView) findViewById(R.id.play);
        mNextButton = (ImageView) findViewById(R.id.next);
        mShuffleButton = (ImageView) findViewById(R.id.shuffle);
        mVolumeBar = (SeekBar) findViewById(R.id.vol_seekbar);
        mMuteButton = (ImageView) findViewById(R.id.mute);
        mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        mVolumeBar.setMax(mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC));
        mVolumeBar.setProgress(mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC));

        mCurrentPlayPos = getIntent().getIntExtra(Constants.CURRENT_POSITION, 0);
        mMusicList = getIntent().getParcelableArrayListExtra(Constants.MUSIC_LIST);
        mIsFromNotification = getIntent().getBooleanExtra(Constants.IS_FROM_NOTIFICATION, false);
        mShuffleList = new ArrayList<>(mMusicList);
        doShuffle(mShuffleList);
        mRepeatButton.setOnClickListener(this);
        mPreviousButton.setOnClickListener(this);
        mPlayButton.setOnClickListener(this);
        mNextButton.setOnClickListener(this);
        mShuffleButton.setOnClickListener(this);
        mMuteButton.setOnClickListener(this);
        mTimeBar.setOnSeekBarChangeListener(this);
        mVolumeBar.setOnSeekBarChangeListener(this);

        mServiceConnection = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                LogUtils.d(TAG, "onServiceConnected");
                mService = ((MusicService.ServiceBinder) service).getService();
                mService.registerMessenger(mMessenger);
                // check if click the same place of the same "Music" list
                if (mService.getMusicListPosition() == mCurrentPlayPos && mMusicList.equals(mService.getMusicList())) {
                    LogUtils.d(TAG, "same music list");
                    mShuffleList = mService.getShuffleList();
                    mShuffle = mService.getShuffle();
                    mRepeat = mService.getRepeat();
                    reloadViews();
                } else {
                    LogUtils.d(TAG, "not same music list");
                    mService.prepareMusic(mCurrentPlayPos, mMusicList, mShuffleList, mShuffle, mRepeat);
                }
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {
                LogUtils.d(TAG, "onServiceDisconnected");
                mService = null;
            }
        };

        if (!mIsFromNotification) {
            startService(MusicService.getPlayIntent(this));
            bindService(MusicService.getPlayIntent(this), mServiceConnection, 0);
        } else {
            bindService(MusicService.getPlayIntent(this), mServiceConnection, 0);
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
    }

    private void updateToStopViews() {
        mPlayButton.setImageDrawable(getDrawable(R.drawable.play_icon_selector));
        mElapsedTime.setText(getString(R.string.zero));
        mTimeBar.setProgress(0);
        finish();
    }

    final Messenger mMessenger = new Messenger(new IncomingHandler(this));

    static class IncomingHandler extends Handler {

        private final WeakReference<PlayActivity> mActivity;

        IncomingHandler(PlayActivity activity) {
            mActivity = new WeakReference<>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            PlayActivity activity = mActivity.get();
            if (activity == null) {
                return;
            }
            switch (msg.what) {
                case MusicService.MSG_UPDATE_TIME:
                    int elapsedMS = (Integer) msg.obj;
                    activity.mTimeBar.setProgress(elapsedMS / 1000 + 1);
                    activity.mElapsedTime.setText(activity.durationToString(elapsedMS));
                    break;
                case MusicService.MSG_START:
                    LogUtils.d(TAG, "handleMessage START");
                    activity.reloadViews();
                    break;
                case MusicService.MSG_STOP:
                    LogUtils.d(TAG, "handleMessage STOP");
                    activity.updateToStopViews();
                    break;
                case MusicService.MSG_UPDATE_POS:
                    LogUtils.d(TAG, "handleMessage UPDATE POS");
                    activity.mCurrentPlayPos = (Integer) msg.obj;
                    break;
                case MusicService.MSG_STOP_SERVICE:
                    activity.finish();
                    break;
                default:
                    super.handleMessage(msg);
            }
        }
    }

    private void doShuffle(ArrayList<Music> list) {
        Collections.shuffle(list);
    }

    private void reloadViews() {
        mCurrentPlayPos = mService.getCurrentListPosition();
        updateViews();
    }

    @Override
    protected void onDestroy() {
        if (mService != null) {
            mService.unregisterMessenger(mMessenger);
        }
        unbindService(mServiceConnection);
        super.onDestroy();
    }

    private void updateViews() {
        ArrayList<Music> musicList;
        if (mShuffle) {
            musicList = mShuffleList;
        } else {
            musicList = mMusicList;
        }
        Music currentMusic = musicList.get(mCurrentPlayPos);
        if (currentMusic.getArtPath() != null) {
            Picasso.with(this)
                    .load(new File(currentMusic.getArtPath()))
                    .transform(new CircleTransform())
                    .error(R.drawable.ic_remove_press_24dp)
                    .into(mAlbumArt);
        } else {
            Picasso.with(this)
                    .load(R.drawable.ic_no_album_40dp)
                    .transform(new CircleTransform())
                    .into(mAlbumArt);
        }

        mTrackName.setText(currentMusic.getTitle());
        mArtistName.setText(currentMusic.getArtist());
        mFullTime.setText(durationToString(currentMusic.getDuration()));
        mElapsedTime.setText(durationToString(mService.getCurrentPosition()));
        mTimeBar.setMax(currentMusic.getDuration() / 1000);
        mTimeBar.setProgress(mService.getCurrentPosition() / 1000 + 1);
        if (mShuffle) {
            mShuffleButton.setImageDrawable(getDrawable(R.drawable.shuffle_icon_selector));
        } else {
            mShuffleButton.setImageDrawable(getDrawable(R.drawable.no_shuffle_icon_selector));
        }
        if (mRepeat == Constants.NO_REPEAT) {
            mRepeatButton.setImageDrawable(getDrawable(R.drawable.no_repeat_icon_selector));
        } else if (mRepeat == Constants.ONE_REPEAT) {
            mRepeatButton.setImageDrawable(getDrawable(R.drawable.one_repeat_icon_selector));
        } else if (mRepeat == Constants.ALL_REPEAT) {
            mRepeatButton.setImageDrawable(getDrawable(R.drawable.repeat_all_icon_selector));
        }
    }



    private String durationToString(long duration) {
        SimpleDateFormat formatter = new SimpleDateFormat("mm:ss");
        return formatter.format(new Date(duration));
    }

    @Override
    public void onClick(View v) {
        if (mService == null) {
            return;
        }
        switch (v.getId()) {
            case R.id.repeat:
                if (mRepeat == Constants.NO_REPEAT) {
                    mRepeatButton.setImageDrawable(getDrawable(R.drawable.one_repeat_icon_selector));
                    mRepeat = Constants.ONE_REPEAT;
                } else if (mRepeat == Constants.ONE_REPEAT) {
                    mRepeatButton.setImageDrawable(getDrawable(R.drawable.repeat_all_icon_selector));
                    mRepeat = Constants.ALL_REPEAT;
                } else if (mRepeat == Constants.ALL_REPEAT) {
                    mRepeatButton.setImageDrawable(getDrawable(R.drawable.no_repeat_icon_selector));
                    mRepeat = Constants.NO_REPEAT;
                }
                mService.setRepeat(mRepeat);
                break;
            case R.id.previous:
                mPlayButton.setImageDrawable(getDrawable(R.drawable.pause_icon_selector));
                mService.previous();
                break;
            case R.id.play:
                if (mService.isPlaying()) {
                    mService.pauseMusic();
                    mPlayButton.setImageDrawable(getDrawable(R.drawable.play_icon_selector));
                } else {
                    mService.restartMusic();
                    mPlayButton.setImageDrawable(getDrawable(R.drawable.pause_icon_selector));
                }
                break;
            case R.id.next:
                mPlayButton.setImageDrawable(getDrawable(R.drawable.pause_icon_selector));
                mService.next();
                break;
            case R.id.shuffle:
                if (mShuffle) {
                    mShuffleButton.setImageDrawable(getDrawable(R.drawable.no_shuffle_icon_selector));
                    mShuffle = false;
                } else {
                    mShuffleButton.setImageDrawable(getDrawable(R.drawable.shuffle_icon_selector));
                    mShuffle = true;
                }
                mService.setShuffle(mShuffle);
                break;
            case R.id.mute:
                if (mMute) {
                    mMuteButton.setImageDrawable(getDrawable(R.drawable.no_mute_icon_selector));
                    mAudioManager.setStreamMute(AudioManager.STREAM_MUSIC, false);
                    mVolumeBar.setProgress(mVolume);
                    mMute = false;
                } else {
                    mMuteButton.setImageDrawable(getDrawable(R.drawable.mute_icon_selector));
                    mAudioManager.setStreamMute(AudioManager.STREAM_MUSIC, true);
                    mVolume = mVolumeBar.getProgress();
                    mVolumeBar.setProgress(0);
                    mMute = true;
                }
                break;
            default:
                break;
        }
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        if (mService == null) {
            return;
        }
        if (!fromUser) {
            return;
        }
        if (seekBar.getId() == R.id.time_seekBar) {
            mService.seekMusicTo(seekBar.getProgress());
        } else if (seekBar.getId() == R.id.vol_seekbar) {
            mVolume = progress;
            mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, progress, 0);
        }
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {

    }
}
