package com.benq.musicplayer.Fragment;

import android.app.LoaderManager;
import android.content.Loader;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import com.benq.musicplayer.Adapter.TrackAdapter;
import com.benq.musicplayer.R;
import com.benq.musicplayer.Utils.TrackCursorLoader;

public class TrackFragment extends BaseFragment implements LoaderManager.LoaderCallbacks<Cursor> {

    public static final String TAG = TrackFragment.class.getSimpleName();
    private RecyclerView mRecyclerView;
    private TrackAdapter mTrackAdapter;
    private ProgressBar mProgressBar;
    private boolean mInit = false;
    private long mCurrentPlayId;

    public TrackFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_track, container, false);
        mRecyclerView = (RecyclerView) view.findViewById(R.id.recycler_view);
        final LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        mRecyclerView.setLayoutManager(layoutManager);
        mTrackAdapter = new TrackAdapter(getActivity());
        mRecyclerView.setAdapter(mTrackAdapter);
        mProgressBar = (ProgressBar) view.findViewById(R.id.progress_bar);
        mProgressBar.setVisibility(View.VISIBLE);
        mTrackAdapter.updateCurrentPlay(mCurrentPlayId);
        return view;
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new TrackCursorLoader(getActivity());
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        mTrackAdapter.swapCursor(cursor);
        mProgressBar.setVisibility(View.GONE);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mTrackAdapter.swapCursor(null);
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
        if (mTrackAdapter != null) {
            mTrackAdapter.updateCurrentPlay(mCurrentPlayId);
        }
    }

}
