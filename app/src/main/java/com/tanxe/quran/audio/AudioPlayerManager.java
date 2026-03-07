package com.tanxe.quran.audio;

import android.content.Context;
import android.net.Uri;
import android.util.Log;

import androidx.media3.common.MediaItem;
import androidx.media3.common.PlaybackParameters;
import androidx.media3.common.Player;
import androidx.media3.exoplayer.ExoPlayer;

import java.io.File;

public class AudioPlayerManager {
    private static final String TAG = "AudioPlayerManager";
    private static volatile AudioPlayerManager INSTANCE;

    private final Context context;
    private ExoPlayer player;
    private boolean isPlaying = false;
    private PlaybackCallback callback;
    private String currentReciterFolder = "Alafasy_128kbps";
    private float playbackSpeed = 1.0f;

    public interface PlaybackCallback {
        void onPlaybackEnded();
        void onError(String message);
    }

    private AudioPlayerManager(Context context) {
        this.context = context.getApplicationContext();
        initPlayer();
    }

    public static AudioPlayerManager getInstance(Context context) {
        if (INSTANCE == null) {
            synchronized (AudioPlayerManager.class) {
                if (INSTANCE == null) {
                    INSTANCE = new AudioPlayerManager(context);
                }
            }
        }
        return INSTANCE;
    }

    private void initPlayer() {
        player = new ExoPlayer.Builder(context).build();
        player.addListener(new Player.Listener() {
            @Override
            public void onPlaybackStateChanged(int playbackState) {
                if (playbackState == Player.STATE_ENDED) {
                    isPlaying = false;
                    if (callback != null) callback.onPlaybackEnded();
                }
            }
        });
    }

    public void setCallback(PlaybackCallback callback) {
        this.callback = callback;
    }

    public void setReciter(String reciterFolder) {
        this.currentReciterFolder = reciterFolder;
    }

    public String getCurrentReciterFolder() {
        return currentReciterFolder;
    }

    public void setPlaybackSpeed(float speed) {
        this.playbackSpeed = speed;
        if (player != null) {
            player.setPlaybackParameters(new PlaybackParameters(speed));
        }
    }

    public float getPlaybackSpeed() {
        return playbackSpeed;
    }

    /**
     * Play audio for a specific surah:ayah using the current reciter.
     */
    public void playAyah(int surah, int ayah, boolean repeat) {
        String filename = String.format("%03d%03d.mp3", surah, ayah);

        // Check local file with reciter subfolder first
        File localFile = new File(context.getFilesDir(), "audio/" + currentReciterFolder + "/" + filename);
        if (!localFile.exists() || localFile.length() == 0) {
            // Fallback to flat audio directory (legacy)
            localFile = new File(context.getFilesDir(), "audio/" + filename);
        }

        Uri uri;
        if (localFile.exists() && localFile.length() > 0) {
            uri = Uri.fromFile(localFile);
        } else {
            // Stream from everyayah.com
            String url = "https://everyayah.com/data/" + currentReciterFolder + "/" + filename;
            uri = Uri.parse(url);
        }

        playUri(uri, repeat);
    }

    public void playUrl(String url, boolean repeat) {
        try {
            // Check if file exists locally first
            String filename = url.substring(url.lastIndexOf('/') + 1);

            // Check reciter-specific folder
            File localFile = new File(context.getFilesDir(), "audio/" + currentReciterFolder + "/" + filename);
            if (!localFile.exists() || localFile.length() == 0) {
                // Fallback to flat audio directory
                localFile = new File(context.getFilesDir(), "audio/" + filename);
            }

            Uri uri;
            if (localFile.exists() && localFile.length() > 0) {
                uri = Uri.fromFile(localFile);
            } else {
                uri = Uri.parse(url);
            }

            playUri(uri, repeat);
        } catch (Exception e) {
            Log.e(TAG, "Error playing audio", e);
            if (callback != null) callback.onError(e.getMessage());
        }
    }

    private void playUri(Uri uri, boolean repeat) {
        try {
            player.stop();
            player.setMediaItem(MediaItem.fromUri(uri));
            player.setRepeatMode(repeat ? Player.REPEAT_MODE_ONE : Player.REPEAT_MODE_OFF);
            player.setPlaybackParameters(new PlaybackParameters(playbackSpeed));
            player.prepare();
            player.play();
            isPlaying = true;
        } catch (Exception e) {
            Log.e(TAG, "Error playing audio", e);
            if (callback != null) callback.onError(e.getMessage());
        }
    }

    public void pause() {
        if (player != null && isPlaying) {
            player.pause();
            isPlaying = false;
        }
    }

    public void resume() {
        if (player != null && !isPlaying) {
            player.play();
            isPlaying = true;
        }
    }

    public void stop() {
        if (player != null) {
            player.stop();
            isPlaying = false;
        }
    }

    public void setRepeatMode(boolean repeat) {
        if (player != null) {
            player.setRepeatMode(repeat ? Player.REPEAT_MODE_ONE : Player.REPEAT_MODE_OFF);
        }
    }

    public boolean isPlaying() {
        return isPlaying;
    }

    public void release() {
        if (player != null) {
            player.release();
            player = null;
            isPlaying = false;
        }
        INSTANCE = null;
    }
}
