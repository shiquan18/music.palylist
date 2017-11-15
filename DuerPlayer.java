package com.judian.jdmusic.manager.player;

import android.content.Context;
import android.media.MediaPlayer;
import android.os.PowerManager;
import android.text.TextUtils;
import android.util.Log;

import com.judian.jdmusic.App;
import com.judian.jdmusic.core.DuerSongInfo;
import com.midea.key.MideaModeSwitchManager;
import com.midea.led.LedController;

import java.io.IOException;

/**
 * Created by Randychen on 2017/2/23.
 * Des
 */

public class DuerPlayer extends BaseDuerPlayer implements MediaPlayer.OnCompletionListener {
    private MediaPlayer mMediaPlayer;

    public DuerPlayer(Context context) {
        super(context);
    }

    private void addListener() {
        mMediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer arg0) {
                Log.d("", "onPrepared");
                notifyStateChange(STATE_PREPARED);
                if (listener != null) {
                    listener.onPrepared(DuerPlayer.this);
                }
            }
        });

        mMediaPlayer.setOnErrorListener(new MediaPlayer.OnErrorListener() {
            @Override
            public boolean onError(MediaPlayer arg0, int arg1, int arg2) {
                Log.e("", "onError arg1" + arg1 + "&arg2" + arg2);
                notifyStateChange(STATE_ERROR);
                if (listener != null) {
                    return listener.onError(DuerPlayer.this, arg1, "播放器出错");
                }
                return false;
            }
        });

        mMediaPlayer.setOnSeekCompleteListener(new MediaPlayer.OnSeekCompleteListener() {
            @Override
            public void onSeekComplete(MediaPlayer player) {
                Log.d("", "onSeekComplete");
                notifyStateChange(STATE_PLAYING);
                if (listener != null) {
                    listener.onSeekComplete(DuerPlayer.this);
                }
                mPostion = mMediaPlayer.getCurrentPosition() / 1000;
            }
        });

        mMediaPlayer.setOnCompletionListener(this);

        mMediaPlayer.setOnBufferingUpdateListener(new MediaPlayer.OnBufferingUpdateListener() {
            @Override
            public void onBufferingUpdate(MediaPlayer mp, int percent) {
                if (listener != null) {
                    listener.onBufferingUpdate(DuerPlayer.this, percent);
                }
            }
        });
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        Log.d("", "onCompletion");
        notifyStateChange(STATE_FINISH);
        if (listener != null) {
            listener.onCompletion(DuerPlayer.this);
        }
    }

    @Override
    public void setDataSource(DuerSongInfo eglSong) {
        super.setDataSource(eglSong);
        if (mMediaPlayer != null) {
            release();
        }
        if (eglSong == null) {
            Log.e("", "EglSong is null");
            return;
        }

        if (TextUtils.isEmpty(eglSong.getUrl())) {
            Log.e("", "EglSong url is null");
            if (listener != null) {
                listener.onError(DuerPlayer.this, -1, "播放地址为空");
            }
            return;
        }
        try {
            mMediaPlayer = new MediaPlayer();
            mMediaPlayer.setWakeMode(App.getAppContext(), PowerManager.PARTIAL_WAKE_LOCK);
            addListener();
            mMediaPlayer.setDataSource(eglSong.getUrl());
            mMediaPlayer.prepareAsync();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void start() {
        Log.d("", "start");

        if (mCurrentStatus >= STATE_PREPARED && mCurrentStatus <= STATE_PAUSED) {
            mMediaPlayer.start();
            //呼吸 在线模式才播放
            if (MideaModeSwitchManager.getInstance().isOnlineMode()) {
                LedController.getInstance().openLedColor(LedController.CMD_OPEN_WHITE);
                LedController.getInstance().switchLedMode(LedController.SWITCH_BREAT_MODE, 0, 0, 0);
            }
            notifyStateChange(STATE_PLAYING);
        } else {
            Log.d("", "mediaPlayer status is erro for start" + mCurrentStatus);
        }
    }

    @Override
    public void pause() {
        Log.d("", "pause() mCurrentStatus:" + mCurrentStatus);

        if (mMediaPlayer != null && mCurrentStatus >= STATE_PREPARED && mCurrentStatus <= STATE_PAUSED) {
            mMediaPlayer.pause();
            if (MideaModeSwitchManager.getInstance().isOnlineMode()) {
                LedController.getInstance().openLedColor(LedController.CMD_OPEN_WHITE);
                LedController.getInstance().switchLedMode(LedController.SWITCH_NOMAL_MODE, 0, 0, 0);
            }
            notifyStateChange(STATE_PAUSED);
        } else {
            Log.d("", "mediaPlayer status is erro for start " + mCurrentStatus);
        }
    }

    @Override
    public int getDuration() {
        if (mMediaPlayer != null && mCurrentStatus >= STATE_PREPARED && mCurrentStatus <= STATE_PAUSED) {
            try {
                mDuration = mMediaPlayer.getDuration() / 1000;
                return mDuration;
            } catch (Exception e) {
                e.printStackTrace();
                return 0;
            }
        }
        return 0;
    }

    @Override
    public int getCurrentPosition() {
        if (mMediaPlayer != null && mCurrentStatus >= STATE_PREPARED && mCurrentStatus <= STATE_PAUSED) {
            try {
                mPostion = mMediaPlayer.getCurrentPosition() / 1000;
                return mPostion;
            } catch (Exception e) {
                e.printStackTrace();
                return 0;
            }
        }
        return 0;
    }

    @Override
    public void stop() {
        if (mMediaPlayer != null && mCurrentStatus >= STATE_PREPARED) {
            mMediaPlayer.stop();
            notifyStateChange(STATE_STOPPED);
            mPostion = 0;
            mDuration = 0;
        }
    }

    @Override
    public void seekTo(int msec) {
        if (mMediaPlayer != null && mCurrentStatus >= STATE_PREPARED) {
            mMediaPlayer.seekTo(msec);
        }
    }

    @Override
    public boolean isPlayering() {
        try {
            return mCurrentStatus == STATE_PLAYING;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean isPlayerPausedPlayback() {
        return mCurrentStatus == STATE_PAUSED;
    }

    @Override
    public void release() {
        if (mMediaPlayer != null) {
            mMediaPlayer.release();
            mMediaPlayer = null;
            mCurrentStatus = STATE_FINISH;
            mPostion = 0;
            mDuration = 0;
        }
    }

    @Override
    public void reset() {
        super.reset();
        release();
    }
}
