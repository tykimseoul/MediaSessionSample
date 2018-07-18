package com.example.android.videoplayerjava;

import android.annotation.TargetApi;
import android.media.AudioAttributes;
import android.media.AudioFocusRequest;
import android.media.AudioManager;
import android.os.Build;
import android.os.Looper;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v4.media.AudioAttributesCompat;
import android.util.Log;

import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.PlaybackParameters;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.PlayerMessage;
import com.google.android.exoplayer2.SeekParameters;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.Timeline;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.TrackGroupArray;
import com.google.android.exoplayer2.trackselection.TrackSelectionArray;

import org.jetbrains.anko.AnkoLogger;
import org.jetbrains.annotations.NotNull;

public class JavaAudioFocusWrapper implements ExoPlayer,AnkoLogger {
    private AudioAttributesCompat audioAttributes;
    private AudioManager audioManager;
    private SimpleExoPlayer player;
    private boolean shouldPlayWhenReady = false;
    @RequiresApi(Build.VERSION_CODES.O)
    private AudioFocusRequest audioFocusRequest;
    private final float MEDIA_VOLUME_DEFAULT = 1.0F;
    private final float MEDIA_VOLUME_DUCK = 0.2f;
    private AudioManager.OnAudioFocusChangeListener audioFocusListener = new AudioManager.OnAudioFocusChangeListener() {
        @Override
        public void onAudioFocusChange(int i) {
            switch (i) {
                case AudioManager.AUDIOFOCUS_GAIN:
                    if (shouldPlayWhenReady || player.getPlayWhenReady()) {
                        player.setPlayWhenReady(true);
                        player.setVolume(MEDIA_VOLUME_DEFAULT);
                    }
                    shouldPlayWhenReady = false;
                    break;
                case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
                    if (player.getPlayWhenReady()) {
                        player.setVolume(MEDIA_VOLUME_DUCK);
                    }
                    break;
                case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
                    shouldPlayWhenReady = player.getPlayWhenReady();
                    player.setPlayWhenReady(false);
                    break;
                case AudioManager.AUDIOFOCUS_LOSS:
                    abandonAudioFocus();
                    break;
            }
        }
    };

    public JavaAudioFocusWrapper(AudioAttributesCompat audioAttributes, AudioManager audioManager, SimpleExoPlayer player) {
        this.audioAttributes = audioAttributes;
        this.audioManager = audioManager;
        this.player = player;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            buildFocusRequest();
        }
    }

    public void setPlayWhenReady(boolean playWhenReady){
        if(playWhenReady){
            requestAudioFocus();
        }else{
            abandonAudioFocus();
        }
    }

    private void requestAudioFocus(){
        int result;
        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.O){
            result=requestAudioFocusOreo();
        }else{
            result=audioManager.requestAudioFocus(audioFocusListener, audioAttributes.getLegacyStreamType(),AudioManager.AUDIOFOCUS_GAIN);
        }
        if(result==AudioManager.AUDIOFOCUS_REQUEST_GRANTED){
            shouldPlayWhenReady=true;
            audioFocusListener.onAudioFocusChange(AudioManager.AUDIOFOCUS_GAIN);
        }else{
            Log.w("TAG","Playback not started: Audio focus request denied");
        }
    }

    private void abandonAudioFocus() {
        player.setPlayWhenReady(false);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            abandonAudioFocusOreo();
        }else{
            audioManager.abandonAudioFocus(audioFocusListener);
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private int requestAudioFocusOreo() {
        return audioManager.requestAudioFocus(audioFocusRequest);
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private void abandonAudioFocusOreo() {
        audioManager.abandonAudioFocusRequest(audioFocusRequest);
    }

    @TargetApi(Build.VERSION_CODES.O)
    private void buildFocusRequest() {
        audioFocusRequest = new AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN)
                .setAudioAttributes((AudioAttributes) audioAttributes.unwrap())
                .setOnAudioFocusChangeListener(audioFocusListener)
                .build();

    }

    public SimpleExoPlayer getPlayer() {
        return player;
    }

    @NotNull
    @Override
    public String getLoggerTag() {
        return "wrapper";
    }

    @Override
    public Looper getPlaybackLooper() {
        return null;
    }

    @Override
    public void prepare(MediaSource mediaSource) {
        player.prepare(mediaSource);
    }

    @Override
    public void prepare(MediaSource mediaSource, boolean resetPosition, boolean resetState) {
        player.prepare(mediaSource, resetPosition, resetState);
    }

    @Override
    public PlayerMessage createMessage(PlayerMessage.Target target) {
        return player.createMessage(target);
    }

    @Override
    public void sendMessages(ExoPlayerMessage... messages) {
        player.sendMessages(messages);
    }

    @Override
    public void blockingSendMessages(ExoPlayerMessage... messages) {
        player.blockingSendMessages(messages);
    }

    @Override
    public void setSeekParameters(@Nullable SeekParameters seekParameters) {
        player.setSeekParameters(seekParameters);
    }

    @Nullable
    @Override
    public VideoComponent getVideoComponent() {
        return player.getVideoComponent();
    }

    @Nullable
    @Override
    public TextComponent getTextComponent() {
        return player.getTextComponent();
    }

    @Override
    public void addListener(Player.EventListener listener) {
        player.addListener(listener);
    }

    @Override
    public void removeListener(Player.EventListener listener) {
        player.removeListener(listener);
    }

    @Override
    public int getPlaybackState() {
        return player.getPlaybackState();
    }

    @Override
    public boolean getPlayWhenReady() {
        return player.getPlayWhenReady();
    }

    @Override
    public void setRepeatMode(int repeatMode) {
        player.setRepeatMode(repeatMode);
    }

    @Override
    public int getRepeatMode() {
        return player.getRepeatMode();
    }

    @Override
    public void setShuffleModeEnabled(boolean shuffleModeEnabled) {
        player.setShuffleModeEnabled(shuffleModeEnabled);
    }

    @Override
    public boolean getShuffleModeEnabled() {
        return player.getShuffleModeEnabled();
    }

    @Override
    public boolean isLoading() {
        return player.isLoading();
    }

    @Override
    public void seekToDefaultPosition() {
        player.seekToDefaultPosition();
    }

    @Override
    public void seekToDefaultPosition(int windowIndex) {
        player.seekToDefaultPosition(windowIndex);
    }

    @Override
    public void seekTo(long positionMs) {
        player.seekTo(positionMs);
    }

    @Override
    public void seekTo(int windowIndex, long positionMs) {
        player.seekTo(windowIndex, positionMs);
    }

    @Override
    public void setPlaybackParameters(@Nullable PlaybackParameters playbackParameters) {
        player.setPlaybackParameters(playbackParameters);
    }

    @Override
    public PlaybackParameters getPlaybackParameters() {
        return player.getPlaybackParameters();
    }

    @Override
    public void stop() {
        player.stop();
    }

    @Override
    public void stop(boolean reset) {
        player.stop(reset);
    }

    @Override
    public void release() {
        player.release();
    }

    @Override
    public int getRendererCount() {
        return player.getRendererCount();
    }

    @Override
    public int getRendererType(int index) {
        return player.getRendererType(index);
    }

    @Override
    public TrackGroupArray getCurrentTrackGroups() {
        return player.getCurrentTrackGroups();
    }

    @Override
    public TrackSelectionArray getCurrentTrackSelections() {
        return player.getCurrentTrackSelections();
    }

    @Nullable
    @Override
    public Object getCurrentManifest() {
        return player.getCurrentManifest();
    }

    @Override
    public Timeline getCurrentTimeline() {
        return player.getCurrentTimeline();
    }

    @Override
    public int getCurrentPeriodIndex() {
        return player.getCurrentPeriodIndex();
    }

    @Override
    public int getCurrentWindowIndex() {
        return player.getCurrentWindowIndex();
    }

    @Override
    public int getNextWindowIndex() {
        return player.getNextWindowIndex();
    }

    @Override
    public int getPreviousWindowIndex() {
        return player.getPreviousWindowIndex();
    }

    @Override
    public long getDuration() {
        return player.getDuration();
    }

    @Override
    public long getCurrentPosition() {
        return player.getCurrentPosition();
    }

    @Override
    public long getBufferedPosition() {
        return player.getBufferedPosition();
    }

    @Override
    public int getBufferedPercentage() {
        return player.getBufferedPercentage();
    }

    @Override
    public boolean isCurrentWindowDynamic() {
        return player.isCurrentWindowDynamic();
    }

    @Override
    public boolean isCurrentWindowSeekable() {
        return player.isCurrentWindowSeekable();
    }

    @Override
    public boolean isPlayingAd() {
        return player.isPlayingAd();
    }

    @Override
    public int getCurrentAdGroupIndex() {
        return player.getCurrentAdGroupIndex();
    }

    @Override
    public int getCurrentAdIndexInAdGroup() {
        return player.getCurrentAdIndexInAdGroup();
    }

    @Override
    public long getContentPosition() {
        return player.getContentPosition();
    }
}
