package com.benq.musicplayer.Fragment;

import android.app.LoaderManager;
import android.content.Context;
import android.content.Loader;
import android.database.Cursor;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ExpandableListView;
import android.widget.ProgressBar;

import com.benq.musicplayer.Adapter.AlbumAdapter;
import com.benq.musicplayer.Adapter.ArtistAdapter;
import com.benq.musicplayer.Adapter.BaseExpandListSwapAdapter;
import com.benq.musicplayer.R;
import com.benq.musicplayer.Utils.Constants;
import com.benq.musicplayer.Utils.LogUtils;
import com.benq.musicplayer.Utils.TrackCursorLoader;


public class ArtistAlbumFragment extends BaseFragment implements LoaderManager.LoaderCallbacks<Cursor> {

    public static final String TAG = ArtistAlbumFragment.class.getSimpleName();
    private ExpandableListView mExpandableListView;
    private BaseExpandListSwapAdapter mAdapter;
    private ProgressBar mProgressBar;
    private int mCategory;
    private boolean mInit = false;
    private long mCurrentPlayId;

    public ArtistAlbumFragment(int category) {
        mCategory = category;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_artist_album, container, false);
        mExpandableListView = (ExpandableListView) view.findViewById(R.id.expandableListView);
        if (mCategory == Constants.CATEGORY_ALBUM) {
            mAdapter = new AlbumAdapter(getActivity());
        } else if (mCategory == Constants.CATEGORY_ARTIST) {
            mAdapter = new ArtistAdapter(getActivity());
        } else {
            Log.e(TAG, "ERROR category");
        }

        mExpandableListView.setAdapter(mAdapter);
        mProgressBar = (ProgressBar) view.findViewById(R.id.progress_bar);
        mProgressBar.setVisibility(View.VISIBLE);
        DisplayMetrics metrics = new DisplayMetrics();
        getActivity().getWindowManager().getDefaultDisplay().getMetrics(metrics);
        int width = metrics.widthPixels;
        mExpandableListView.setIndicatorBounds(width - getPixelFromDips(getResources().getDimension(R.dimen.expandlist_indicator_boundleft)),
                width - getPixelFromDips(getResources().getDimension(R.dimen.expandlist_indicator_boundleft)));
        mExpandableListView.setOnChildClickListener(mAdapter);
        mAdapter.updateCurrentPlay(mCurrentPlayId);
        return view;
    }

    private int getPixelFromDips(float pixels) {
        // Get the screen's density scale
        final float scale = getResources().getDisplayMetrics().density;
        // Convert the dps to pixels, based on density scale
        return (int) (pixels * scale + 0.5f);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new TrackCursorLoader(getActivity());
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        mAdapter.swapCursor(cursor);
        mProgressBar.setVisibility(View.GONE);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mAdapter.swapCursor(null);
    }

    @Override
    public void loadContent(boolean dataChanged) {
        if (!mInit) {
            getLoaderManager().initLoader(1, null, this);
            mInit = true;
        } else if (dataChanged) {
            getLoaderManager().restartLoader(1, null, this);
        }
    }

    @Override
    public void updateCurrentPlay(long id) {
        mCurrentPlayId = id;
        if (mAdapter != null) {
            mAdapter.updateCurrentPlay(mCurrentPlayId);
        }
    }
}
