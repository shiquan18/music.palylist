package com.judian.jdmusic.core;

import android.util.Log;

import com.judian.jdmusic.util.LogUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class DuerPlayListManager {
    /* 播放模式 列表循环 单曲播放 随机播放*/
    public enum PlayMode {
        LOOP_LIST("REPEAT_ALL", 0), LOOP_SINGLE("REPEAT_ONE", 1), SHUFFLE("SHUFFLE", 2);
        private int value;
        private String key;

        PlayMode(String key, int value) {
            this.value = value;
            this.key = key;
        }

        public static PlayMode valueOf(int value) {
            switch (value) {
                case 0:
                    return LOOP_LIST;
                case 1:
                    return LOOP_SINGLE;
                case 2:
                    return SHUFFLE;
                default:
                    return LOOP_LIST;
            }
        }

        public static PlayMode keyOf(String key) {
            if ("REPEAT_ALL".equals(key)) {
                return LOOP_LIST;
            } else if ("REPEAT_ONE".equals(key)) {
                return LOOP_SINGLE;
            } else if ("SHUFFLE".equals(key)) {
                return SHUFFLE;
            } else {
                return LOOP_LIST;
            }
        }

        public int getValue() {
            return value;
        }

        public String getKey() {
            return key;
        }
    }

    private static DuerPlayListManager playListManager;

    private PlayModeCallBack mPlayModeCallBack;

    private PlayMode mCurrentMode = PlayMode.LOOP_LIST;

    private int mCurrentPos = 0;

    private DuerPlayListInfor mPlayListInfor;

    //歌曲列表
    private List<DuerSongInfo> eglSongs;

    public interface PlayModeCallBack {
        void onModeChange(PlayMode playMode);//播放模式改编
    }

    public List<DuerSongInfo> getEglSongs() {
        return eglSongs;
    }

    public void setPlayListInfor(DuerPlayListInfor duerPlayListInfor) {
        this.mPlayListInfor = duerPlayListInfor;
    }

    public DuerPlayListInfor getPlayListInfor() {
        return mPlayListInfor;
    }

    public void registerPlayModeCallBack(PlayModeCallBack playModeCallBack) {
        this.mPlayModeCallBack = playModeCallBack;
    }

    public DuerSongInfo getEglSongByPos(int pos) {
        DuerSongInfo eglSong = null;
        if (eglSongs.size() == 0 || pos > eglSongs.size()) {
            LogUtils.e("pos larger than songlist size " + eglSongs.size());
        } else {
            mCurrentPos = pos;
            eglSong = eglSongs.get(pos);
        }
        return eglSong;
    }

    public DuerSongInfo getEglSongBySongId(String songId) {
        DuerSongInfo eglSong = null;
        if (eglSongs.size() == 0 || eglSongs == null) {
            LogUtils.e("pos larger than songlist size " + eglSongs.size());
        } else {
            for (int i = 0; i < eglSongs.size(); i++) {
                if (songId.equals(eglSongs.get(i).songId)) {
                    mCurrentPos = i;
                    eglSong = eglSongs.get(i);
                }
            }
        }
        return eglSong;
    }

    private DuerPlayListManager() {
        /* 保证其线程安全 */
        eglSongs = Collections.synchronizedList(new ArrayList<DuerSongInfo>());
    }

    public static DuerPlayListManager getInstance() {
        if (playListManager == null) {
            playListManager = new DuerPlayListManager();
        }
        return playListManager;
    }

    /* 改变播放模式 */
    public PlayMode switchPlayMode() {
        int curr = mCurrentMode.getValue();
        mCurrentMode = PlayMode.valueOf((++curr) % 3);
        return modeChange();
    }

    public PlayMode switchPlayMode(int modeKey) {
        mCurrentMode = PlayMode.valueOf(modeKey);
        return modeChange();
    }

    public PlayMode switchPlayMode(String value) {
        if (value.equals(mCurrentMode.getKey())) {
            return mCurrentMode;
        }
        mCurrentMode = PlayMode.keyOf(value);
        return modeChange();
    }

    private PlayMode modeChange() {
        if (mPlayModeCallBack != null) {
            mPlayModeCallBack.onModeChange(mCurrentMode);
        }
        return mCurrentMode;
    }

    public PlayMode getPlayMode() {
        return mCurrentMode;
    }

    public void clearPlayList() {
        mCurrentPos = 0;
        eglSongs.clear();
        mPlayListInfor = null;
    }

    public synchronized void removeSong(DuerSongInfo eglSong) {
        if (eglSongs.indexOf(eglSong) > 0) {
            boolean result = eglSongs.remove(eglSong);
            if (result) {
            }
        }
    }

    public synchronized void removeSong(int pos) {
        if (pos < eglSongs.size()) {
            DuerSongInfo eglSong = eglSongs.remove(pos);
            if (eglSong != null) {
            }
        }
    }

    public synchronized void addSongsToPlayList(List<DuerSongInfo> songs, String playListName, String playListId, String playListType, String playImgPath, String extStr) {
        mPlayListInfor = new DuerPlayListInfor(songs, playListName, playListId, playListType, playImgPath, extStr);
        mPlayListInfor.setmCurrPos(0);
        addSongsToPlayList(songs, true);
    }

    public synchronized void addSongsToPlayList(List<DuerSongInfo> songs, boolean clear) {
        if (clear) {
            eglSongs.clear();
            mCurrentPos = 0;
        }
        if (songs == null || songs.size() == 0) {
            LogUtils.e("addSongsToPlayList songs is null");
            return;
        }
        eglSongs.addAll(songs);
    }

    public synchronized void addSongsToPlayList(DuerSongInfo song, boolean clear) {
        if (song == null) {
            LogUtils.e("addSongsToPlayList song is null");
            return;
        }
        if (clear) {
            eglSongs.clear();
            mCurrentPos = 0;
        }
        eglSongs.add(song);
    }

    public synchronized void addSongToPlayList(DuerSongInfo eglSong, int pos) {
        boolean flag = true;
        if (pos <= eglSongs.size()) {
            for (int i = 0; i < eglSongs.size(); i++) {
                if (eglSongs.get(i).getToken().equals(eglSong.getToken())) {
                    flag = false;
                }
            }
            if (flag) {
                eglSongs.add(pos, eglSong);
            }
        }
        if (eglSongs.size() > 10) {
            movePlayListKeppCurrent();
        }
    }

    /* 删除但是保留当前播放的歌曲 */
    public synchronized void movePlayListKeppCurrent() {
        DuerSongInfo eglSong = null;
        if (mCurrentPos < eglSongs.size()) {
            eglSong = eglSongs.get(mCurrentPos);
            eglSongs.clear();
            mCurrentPos = 0;
            eglSongs.add(eglSong);
        } else {
            LogUtils.e("mCurrentPos " + mCurrentPos + " is larger list size" + eglSongs.size());
        }
    }

    /* 获取歌曲 */
    public DuerSongInfo getCurrentSong() {
        DuerSongInfo eglSong = null;
        if (mCurrentPos < eglSongs.size()) {
            eglSong = eglSongs.get(mCurrentPos);
        } else {
            LogUtils.e("mCurrentPos " + mCurrentPos + " is larger list size" + eglSongs.size());
            return null;
        }
        return eglSong;
    }

    public int getCurrentPos() {
        return mCurrentPos;
    }

    public synchronized DuerSongInfo getNextSong() {
        if (eglSongs == null || eglSongs.size() == 0) {
            LogUtils.e("eglsong list is empty");
            return null;
        }
        DuerSongInfo tmpEglSong = null;
        int currentModeValue = mCurrentMode.getValue();
        if (currentModeValue == PlayMode.LOOP_LIST.getValue()) {
            if (mCurrentPos == eglSongs.size() - 1) {
                mCurrentPos = 0;
            } else {
                mCurrentPos++;
            }
            tmpEglSong = eglSongs.get(mCurrentPos);
        } else if (currentModeValue == PlayMode.LOOP_SINGLE.getValue()) {
            tmpEglSong = eglSongs.get(mCurrentPos);
        } else if (currentModeValue == PlayMode.SHUFFLE.getValue()) {
            tmpEglSong = eglSongs.get(new Random().nextInt(eglSongs.size()));
        }
        playSongChange(tmpEglSong);
        return tmpEglSong;
    }

    public synchronized DuerSongInfo getPreSong() {
        if (eglSongs == null || eglSongs.size() == 0) {
            LogUtils.e("eglsong list is empty");
            return null;
        }
        DuerSongInfo tmpEglSong = null;
        int currentModeValue = mCurrentMode.getValue();
        if (currentModeValue == PlayMode.LOOP_LIST.getValue()) {
            if (mCurrentPos == 0) {
                mCurrentPos = eglSongs.size() - 1;
            } else {
                mCurrentPos--;
            }
            tmpEglSong = eglSongs.get(mCurrentPos);
        } else if (currentModeValue == PlayMode.LOOP_SINGLE.getValue()) {
            tmpEglSong = eglSongs.get(mCurrentPos);
        } else if (currentModeValue == PlayMode.SHUFFLE.getValue()) {
            tmpEglSong = eglSongs.get(new Random().nextInt(eglSongs.size()));
        }
        playSongChange(tmpEglSong);
        return tmpEglSong;
    }

    public void setCurrentEglSong(DuerSongInfo eglSong) {
        int index = eglSongs.indexOf(eglSong);
        if (index > 0) {
            mCurrentPos = index;
        } else {
            eglSongs.add(0, eglSong);
            mCurrentPos = 0;
        }
        playSongChange(getCurrentSong());
    }

    public synchronized void addSongToNext(DuerSongInfo eglSong) {
        int index = eglSongs.indexOf(eglSong);
        if (index > 0) {
            eglSongs.remove(eglSong);
            if (index < mCurrentPos) {
                mCurrentPos--;
            }
        }
        eglSongs.add(mCurrentPos + 1, eglSong);
    }

    public void setCurrentEglSong(int pos) {
        if (pos < eglSongs.size()) {
            mCurrentPos = pos;
        } else {
            LogUtils.e("pos " + pos + "outside of list size" + eglSongs.size());
        }
    }

    private void playSongChange(DuerSongInfo eglSong) {
        if (eglSong == null) {
            LogUtils.e("playSongChange eglSong is null");
            return;
        }
    }

    public boolean isLastSong(DuerSongInfo eglSong) {
        synchronized (eglSongs) {
            return eglSongs.indexOf(eglSong) == eglSongs.size() - 1;
        }
    }
}
