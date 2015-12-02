package com.benq.musicplayer.Activity;

import android.app.FragmentManager;
import android.content.ComponentName;
import android.content.ServiceConnection;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.provider.MediaStore;
import android.support.design.widget.NavigationView;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.FrameLayout;
import android.widget.ListView;
import android.widget.Spinner;

import com.benq.musicplayer.Adapter.CategorySpinnerAdapter;
import com.benq.musicplayer.Adapter.MenuItemAdapter;
import com.benq.musicplayer.Fragment.ArtistAlbumFragment;
import com.benq.musicplayer.Fragment.BaseFragment;
import com.benq.musicplayer.Fragment.TrackFragment;
import com.benq.musicplayer.R;
import com.benq.musicplayer.Service.MusicService;
import com.benq.musicplayer.Utils.Constants;
import com.benq.musicplayer.Utils.LogUtils;
import com.benq.musicplayer.Utils.MusicContentObserver;

import java.lang.ref.WeakReference;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity
        implements DrawerLayout.DrawerListener, AdapterView.OnItemClickListener {

    private static final String TAG = MainActivity.class.getSimpleName();
    private BaseFragment mFragment = null;
    private ListView mMenuListView;
    private MenuItemAdapter mAdapter;
    private MusicContentObserver mMusicContentObserver;
    private int mCurrentCategory = -1;
    private Spinner mSpinner;
    private CategorySpinnerAdapter mSpinnerAdapter;
    private MusicService mService;
    private ServiceConnection mServiceConnection;
    private long mCurrentPlayId;
    private FrameLayout mSpinnerBg;
    private boolean isPress;
    private NavigationView mNavigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();
        drawer.setDrawerListener(this);
        mAdapter = new MenuItemAdapter(this);
        mNavigationView= (NavigationView) findViewById(R.id.nav_view);
        mMenuListView = (ListView) mNavigationView.findViewById(R.id.list_menu);
        mMenuListView.setAdapter(mAdapter);
        mMenuListView.setOnItemClickListener(this);
        mMusicContentObserver = new MusicContentObserver(this, new Handler() {
            @Override
            public void handleMessage(Message msg) {
                if (msg.what == MusicContentObserver.MSG_RELOAD) {
                    if (mFragment != null) {
                        mFragment.loadContent(true);
                    }
                }
                super.handleMessage(msg);
            }
        });
        mSpinnerBg = (FrameLayout) findViewById(R.id.spinner_layout);
        mSpinnerAdapter = new CategorySpinnerAdapter(this);
        mSpinner = (Spinner)findViewById(R.id.spinner);
        mSpinner.setAdapter(mSpinnerAdapter);
        isPress = false;
        mSpinner.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN && !isPress) {
                    isPress = true;
                    mSpinnerBg.setBackground(getDrawable(R.drawable.spinner_bg_pressed_shape));
                } else if (event.getAction() == MotionEvent.ACTION_UP && isPress) {
                    isPress = false;
                    mSpinnerBg.setBackground(getDrawable(R.drawable.spinner_bg_normal));
                }
                v.onTouchEvent(event);
                return true;
            }
        });
        mSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                categorySelected(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        mServiceConnection = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                mService = ((MusicService.ServiceBinder) service).getService();
                mService.registerMessenger(mMessenger);
                mCurrentPlayId = mService.getPlayMusicId();
                updateCurrentPlay(mCurrentPlayId);
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {
                mService = null;
            }
        };
        bindService(MusicService.getPlayIntent(this), mServiceConnection, 0);
    }

    final Messenger mMessenger = new Messenger(new IncomingHandler(this));

    static class IncomingHandler extends Handler {

        private final WeakReference<MainActivity> mActivity;
        IncomingHandler(MainActivity activity) {
            mActivity = new WeakReference<>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            MainActivity activity = mActivity.get();
            if (activity == null) {
                return;
            }
            switch (msg.what) {
                case MusicService.MSG_START:
                case MusicService.MSG_STOP:
                    activity.mCurrentPlayId = ((long)msg.obj);
                    activity.updateCurrentPlay((long)msg.obj);
                    break;
                default:
                    super.handleMessage(msg);
            }
        }
    }

    private void updateCurrentPlay(long musicId) {
        if (mFragment != null) {
            mFragment.updateCurrentPlay(musicId);
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {

    }

    @Override
    protected void onStart() {
        getContentResolver().registerContentObserver(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, false, mMusicContentObserver);
        super.onStart();
    }

    @Override
    protected void onStop() {
        getContentResolver().unregisterContentObserver(mMusicContentObserver);
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        if (mService != null) {
            mService.unregisterMessenger(mMessenger);
        }
        unbindService(mServiceConnection);
        super.onDestroy();
    }


    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onDrawerSlide(View drawerView, float slideOffset) {

    }

    @Override
    public void onDrawerOpened(View drawerView) {
    }

    @Override
    public void onDrawerClosed(View drawerView) {
        if (mFragment != null) {
            mFragment.loadContent(false);
        }
    }

    @Override
    public void onDrawerStateChanged(int newState) {

    }

    private void categorySelected(int position) {
        if (mCurrentCategory == position) {
            return;
        }
        FragmentManager fragmentManager = getFragmentManager();
        switch (position) {
            case 0: //album
                    mFragment = new ArtistAlbumFragment(Constants.CATEGORY_ALBUM);
                    mCurrentCategory = Constants.CATEGORY_ALBUM;
                break;
            case 1: //artist
                    mFragment = new ArtistAlbumFragment(Constants.CATEGORY_ARTIST);
                    mCurrentCategory = Constants.CATEGORY_ARTIST;
                break;
            case 2: //track
                    mFragment = new TrackFragment();
                    mCurrentCategory = Constants.CATEGORY_TRACK;
                break;
        }
        if (mFragment != null) {
            fragmentManager.beginTransaction().replace(R.id.main_frame, mFragment).commit();
            mFragment.updateCurrentPlay(mCurrentPlayId);
        }
        mAdapter.setSelectedPos(position);
        mSpinner.setSelection(position);
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(mNavigationView)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            if (mFragment != null) {
                // should call loadContent when fragment already attach activity
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        mFragment.loadContent(false);
                    }
                }, 1000);
            }
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        categorySelected(position);
    }
}
