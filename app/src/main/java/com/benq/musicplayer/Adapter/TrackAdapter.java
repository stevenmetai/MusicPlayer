package com.benq.musicplayer.Adapter;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.drawable.AnimationDrawable;
import android.provider.MediaStore;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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

/**
 * Created by Steven.SL.Tai on 2015/11/3.
 */
public class TrackAdapter extends RecyclerView.Adapter {

    private static final String TAG = TrackAdapter.class.getSimpleName();
    private Context mContext;
    private ArrayList<Music> mMusicList;
    private Cursor mCursor;
    private long mMusicId;

    public TrackAdapter(Context context) {
        mContext = context;
        mMusicList = new ArrayList<>();
    }

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

    public void updateList() {
        while (mCursor != null && mCursor.moveToNext()) {
            Long albumId = mCursor.getLong(mCursor.getColumnIndex(MediaStore.Audio.Media.ALBUM_ID));
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
            boolean isSameTitle = false;
            for (Music temp : mMusicList) {
                if (temp.getTitle().equals(music.getTitle())) {
                    isSameTitle = true;
                    break;
                }
            }
            if (!isSameTitle) {
                mMusicList.add(music);
            }
        }
        notifyDataSetChanged();
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        // create a new view
        View itemLayoutView = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_track, null);
        // create ViewHolder
        ViewHolder viewHolder = new ViewHolder(itemLayoutView);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, int position) {

        ((ViewHolder) viewHolder).txtViewTitle.setText(mMusicList.get(position).getTitle());

        String artPath = mMusicList.get(position).getArtPath();
        if (artPath != null) {
            Picasso.with(((ViewHolder) viewHolder).imgViewIcon.getContext()).cancelRequest((((ViewHolder) viewHolder).imgViewIcon));
            Picasso.with(((ViewHolder) viewHolder).imgViewIcon.getContext())
                    .load(new File(artPath))
//                    .placeholder(R.drawable.ic_remove_press_24dp)
                    .error(R.drawable.ic_remove_press_24dp)
                    .into(((ViewHolder) viewHolder).imgViewIcon);
        } else {
            ((ViewHolder) viewHolder).imgViewIcon.setImageResource(R.drawable.ic_no_album_40dp);
        }

        if (mMusicList.get(position).getId() == mMusicId) {
            //current playing
            ((ViewHolder) viewHolder).playImageAnimation.setVisibility(View.VISIBLE);
            ((AnimationDrawable)((ViewHolder) viewHolder).playImageAnimation.getDrawable()).start();
        } else {
            ((ViewHolder) viewHolder).playImageAnimation.setVisibility(View.INVISIBLE);
            ((AnimationDrawable)((ViewHolder) viewHolder).playImageAnimation.getDrawable()).stop();
        }
    }

    @Override
    public int getItemCount() {
        return mMusicList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        public TextView txtViewTitle;
        public ImageView imgViewIcon;
        public ImageView playImageAnimation;

        public ViewHolder(View itemLayoutView) {
            super(itemLayoutView);
            itemLayoutView.setOnClickListener(this);
            txtViewTitle = (TextView) itemLayoutView.findViewById(R.id.textView);
            imgViewIcon = (ImageView) itemLayoutView.findViewById(R.id.imageView);
            playImageAnimation = (ImageView) itemLayoutView.findViewById(R.id.play_animation);
        }

        @Override
        public void onClick(View view) {
            Intent intent = new Intent();
            intent.setClass(view.getContext(), PlayActivity.class);
            intent.putExtra(Constants.CURRENT_POSITION, getAdapterPosition());
            intent.putParcelableArrayListExtra(Constants.MUSIC_LIST, mMusicList);
            view.getContext().startActivity(intent);
        }
    }

    public void updateCurrentPlay(long id) {
        mMusicId = id;
        notifyDataSetChanged();
    }
}
