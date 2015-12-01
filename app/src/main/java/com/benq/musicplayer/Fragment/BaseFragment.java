package com.benq.musicplayer.Fragment;

import android.app.Fragment;

/**
 * Created by Steven.SL.Tai on 2015/11/4.
 */
public abstract class BaseFragment extends Fragment {

    public abstract void loadContent(boolean dataChanged);
    public abstract void updateCurrentPlay(long id);
}
