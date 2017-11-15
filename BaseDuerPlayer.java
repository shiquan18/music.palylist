package com.judian.jdmusic.manager.player;

import android.content.Context;
import android.media.AudioManager;
import android.util.Log;

import com.judian.jdmusic.core.DuerPlayListInfor;
import com.judian.jdmusic.core.DuerSongInfo;
import com.judian.jdmusic.resource.douban.MediaHandlerInfo;

import java.util.List;

/**
 * Created by Randychen on 2017/2/23.
 * Des
 */

public abstract class BaseDuerPlayer {
    public final static int STATE_FINISH = 0;
    public final static int STATE_PREPARING = 1;
    public final static int STATE_PREPARED = 2;
    public final static int STATE_PLAYING = 3;
    public final static int STATE_PAUSED = 4;
    public final static int STATE_STOPPED = 5;
    public final static int STATE_ERROR = 6;
    public final static int COMMAND_PROGRESS_CHANGE = 7;
    private static final String TAG = "BaseDuerPlayer";

    protected int mCurrentStatus = STATE_FINISH;
    protected int mDuration = 0;
    protected int mPostion = 0;
    protected IMusicPlayerListener listener;

    protected AudioManager mAudioManager;

    protected DuerPlayListInfor mPlayListInfor;

    protected Context mContext;

    protected MediaHandlerInfo mMediaHandlerInfo;

    public BaseDuerPlayer(Context context) {
        mContext = context;
        mAudioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
    }

    public void start() {

    }

    public void pause() {

    }

    public int getDuration() {
        return 0;
    }

    public int getCurrentPosition() {
        return 0;
    }

    public void stop() {

    }

    public void seekTo(int msec) {

    }

    public boolean isPlayering() {
        return mCurrentStatus == STATE_PLAYING;
    }

    public boolean isPlayerPausedPlayback() {
        return mCurrentStatus == STATE_PAUSED;
    }

    public void release() {

    }

    public void reset() {

    }

    public int getPlayerStatus() {
        return mCurrentStatus;
    }

    public void setVolume(int volume) {
        int maxVolume = mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        Log.d("", "target volume " + volume + ";   maxVolume=" + maxVolume);
        mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, volume * maxVolume / 100,
                AudioManager.FLAG_SHOW_UI);
    }

    public int getVolume() {
        int maxVolume = mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        int volume = mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC) * 100 / maxVolume;
        return volume;
    }

    public boolean next() {
        return false;
    }

    public boolean prev() {
        return false;
    }

    //是否正在加载
    protected boolean loadingMore = false;

    //加载更多

    void setDataSource(DuerSongInfo eglSong) {
        if (listener != null) {
            listener.onPlaySongChange(eglSong);
        }
        notifyStateChange(STATE_PREPARING);
    }

    void playByPlayListInfo(DuerPlayListInfor playListInfor) {
        Log.d("", "playByPlayListInfo:" + playListInfor.toString());
        mPlayListInfor = playListInfor;
    }

    void registerListener(IMusicPlayerListener musicPlayerListener) {
        listener = musicPlayerListener;
        Log.d("", "registerListener:" + listener);
    }

    protected void notifyStateChange(int state) {
        if (state != mCurrentStatus || state == COMMAND_PROGRESS_CHANGE) {
            if (state != COMMAND_PROGRESS_CHANGE) {
                mCurrentStatus = state;
            }
            if (listener != null) {
                listener.onPlayStateChange(mCurrentStatus);
            }
        }
    }

    public interface IMusicPlayerListener {
        void onPrepared(BaseDuerPlayer mp);

        void onBufferingUpdate(BaseDuerPlayer mp, int percent);

        void onSeekComplete(BaseDuerPlayer mp);

        void onCompletion(BaseDuerPlayer mp);

        boolean onError(BaseDuerPlayer mp, int what, String erroMsg);

        void onPlaySongChange(DuerSongInfo eglSong);

        void onPlayListChange(List<DuerSongInfo> eglSongs);

        void onPlayStateChange(int state);

        void onAudioPlayerChange(BaseDuerPlayer mp);
    }
}
