package com.benq.musicplayer.Adapter;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.drawable.AnimationDrawable;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.TextView;

import com.benq.musicplayer.Activity.PlayActivity;
import com.benq.musicplayer.Data.Music;
import com.benq.musicplayer.Data.MusicBuilder;
import com.benq.musicplayer.R;
import com.benq.musicplayer.Utils.Constants;
import com.benq.musicplayer.Utils.LogUtils;
import com.benq.musicplayer.Utils.MusicUtil;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by Steven.SL.Tai on 2015/11/2.
 */
public class ArtistAdapter extends BaseExpandListSwapAdapter {

    private static final String TAG = ArtistAdapter.class.getSimpleName();
    private Context mContext;
    private Cursor mCursor;
    private ArrayList<String> mGroupData;
    private HashMap<String, ArrayList<Music>> mChildData;
    private long mMusicId;

    public ArtistAdapter(Context context) {
        mContext = context;
        mGroupData = new ArrayList<>();
        mChildData = new HashMap<>();
    }

    @Override
    public boolean swapCursor(Cursor newCursor) {
        if (newCursor == mCursor) {
            return false;
        }
        final Cursor oldCursor = mCursor;
        mCursor = newCursor;
        if (oldCursor != null) {
            oldCursor.close();
        }
        updateList();
        return true;
    }

    private void updateList() {
        while (mCursor != null && mCursor.moveToNext()) {
            Long albumId = mCursor.getLong(mCursor.getColumnIndex(MediaStore.Audio.Media.ALBUM_ID));
            String artist = mCursor.getString(mCursor.getColumnIndex(MediaStore.Audio.Media.ARTIST));
            if (!mGroupData.contains(artist)) {
                mGroupData.add(artist);
            }
            Music music = new MusicBuilder()
                    .setAlbum(mCursor.getString(mCursor.getColumnIndex(MediaStore.Audio.Media.ALBUM)))
                    .setArtist(mCursor.getString(mCursor.getColumnIndex(MediaStore.Audio.Media.ARTIST)))
                    .setData(mCursor.getString(mCursor.getColumnIndex(MediaStore.Audio.Media.DATA)))
                    .setTitle(mCursor.getString(mCursor.getColumnIndex(MediaStore.Audio.Media.TITLE)))
                    .setAlbumId(albumId)
                    .setArtPath(MusicUtil.getAlbumArtPath(mContext, albumId))
                    .setId(mCursor.getLong(mCursor.getColumnIndex(MediaStore.Audio.Media._ID)))
                    .setDuration(mCursor.getInt(mCursor.getColumnIndex(MediaStore.Audio.Media.DURATION)))
                    .build();
            if (mChildData.containsKey(artist)) {
                mChildData.get(artist).add(music);
            } else {
                ArrayList<Music> list = new ArrayList<>();
                list.add(music);
                mChildData.put(artist, list);
            }
        }
        notifyDataSetChanged();
    }

    @Override
    public int getGroupCount() {
        return mGroupData.size();
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        return mChildData.get(mGroupData.get(groupPosition)).size();
    }

    @Override
    public Object getGroup(int groupPosition) {
        return mGroupData.get(groupPosition);
    }

    @Override
    public Object getChild(int groupPosition, int childPosition) {
        return mChildData.get(mGroupData.get(groupPosition)).get(childPosition);
    }

    @Override
    public long getGroupId(int groupPosition) {
        return 0;
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return 0;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    @Override
    public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
        View view;
        GroupViewHolder viewHolder = null;
        if (convertView == null) {
            view = LayoutInflater.from(mContext).inflate(R.layout.item_group, parent, false);
            viewHolder = new GroupViewHolder();
            viewHolder.title = (TextView) view.findViewById(R.id.title);
            view.setTag(viewHolder);
        } else {
            view = convertView;
            viewHolder = (GroupViewHolder) view.getTag();
        }

        viewHolder.title.setText(mGroupData.get(groupPosition));
        return view;
    }

    @Override
    public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
        View view;
        ChildViewHolder viewHolder = null;
        if (convertView == null) {
            view = LayoutInflater.from(mContext).inflate(R.layout.item_track, parent, false);
            viewHolder = new ChildViewHolder();
            viewHolder.title = (TextView) view.findViewById(R.id.textView);
            viewHolder.art = (ImageView) view.findViewById(R.id.imageView);
            viewHolder.playImageAnimation = (ImageView) view.findViewById(R.id.play_animation);
            view.setTag(viewHolder);
        } else {
            view = convertView;
            viewHolder = (ChildViewHolder) view.getTag();
        }
        viewHolder.title.setText(mChildData.get(mGroupData.get(groupPosition)).get(childPosition).getTitle());
        String artPath = mChildData.get(mGroupData.get(groupPosition)).get(childPosition).getArtPath();
        if (artPath != null) {
            Picasso.with(viewHolder.art.getContext()).cancelRequest(viewHolder.art);
            Picasso.with(viewHolder.art.getContext())
                    .load(new File(artPath))
//                    .placeholder(R.drawable.ic_remove_press_24dp)
                    .error(R.drawable.ic_remove_press_24dp)
                    .into(viewHolder.art);
        } else {
            viewHolder.art.setImageResource(R.drawable.ic_no_album_40dp);
        }

        if (mChildData.get(mGroupData.get(groupPosition)).get(childPosition).getId() == mMusicId) {
            //current playing
            viewHolder.playImageAnimation.setVisibility(View.VISIBLE);
            ((AnimationDrawable) viewHolder.playImageAnimation.getDrawable()).start();
        } else {
            viewHolder.playImageAnimation.setVisibility(View.INVISIBLE);
            ((AnimationDrawable) viewHolder.playImageAnimation.getDrawable()).stop();
        }
        return view;
    }

    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return true;
    }

    @Override
    public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {
        Intent intent = new Intent();
        intent.setClass(v.getContext(), PlayActivity.class);
        intent.putExtra(Constants.CURRENT_POSITION, childPosition);
        intent.putParcelableArrayListExtra(Constants.MUSIC_LIST, mChildData.get(mGroupData.get(groupPosition)));
        v.getContext().startActivity(intent);
        return true;
    }

    private class GroupViewHolder {
        private TextView title;
    }

    private class ChildViewHolder {
        private TextView title;
        private ImageView art;
        private ImageView playImageAnimation;
    }

    public void updateCurrentPlay(long id) {
        mMusicId = id;
        notifyDataSetChanged();
    }
}