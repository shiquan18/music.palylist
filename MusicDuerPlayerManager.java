package com.judian.jdmusic.manager.player;

import com.judian.jdmusic.App;
import com.judian.jdmusic.core.DuerSongInfo;
import com.judian.jdmusic.voice.DoAction;

import java.util.List;

public class MusicDuerPlayerManager implements BaseDuerPlayer.IMusicPlayerListener {

    private static MusicDuerPlayerManager mManager;

    public static MusicDuerPlayerManager getInstance() {
        if (mManager == null) {
            synchronized (MusicDuerPlayerManager.class) {
                if (mManager == null) {
                    mManager = new MusicDuerPlayerManager();
                }
            }
        }
        return mManager;
    }

    private DuerPlayer mDuerPlayer;

    private MusicDuerPlayerManager() {
        mDuerPlayer = new DuerPlayer(App.getAppContext());
        mDuerPlayer.registerListener(this);
    }

    public DuerPlayer getDuerPlayer() {
        return mDuerPlayer;
    }

    @Override
    public void onPrepared(BaseDuerPlayer mp) {
        mDuerPlayer.start();
    }

    @Override
    public void onBufferingUpdate(BaseDuerPlayer mp, int percent) {

    }

    @Override
    public void onSeekComplete(BaseDuerPlayer mp) {

    }

    @Override
    public void onCompletion(BaseDuerPlayer mp) {
    }

    @Override
    public boolean onError(BaseDuerPlayer mp, int what, String erroMsg) {
        return false;
    }

    @Override
    public void onPlaySongChange(DuerSongInfo eglSong) {

    }

    @Override
    public void onPlayListChange(List<DuerSongInfo> eglSongs) {

    }

    @Override
    public void onPlayStateChange(int state) {
        DoAction.getInstance().playState(state);
    }

    @Override
    public void onAudioPlayerChange(BaseDuerPlayer mp) {

    }
}
