package com.example.android.videoplayerjava;

import android.content.Context;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.support.v4.media.AudioAttributesCompat;
import android.support.v4.media.MediaDescriptionCompat;
import android.util.Log;
import android.widget.Toast;

import com.example.android.videoplayersample.AudioFocusWrapper;
import com.example.android.videoplayersample.MediaCatalog;
import com.example.android.videoplayersample.PlayerState;
import com.google.android.exoplayer2.ExoPlaybackException;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.extractor.DefaultExtractorsFactory;
import com.google.android.exoplayer2.extractor.ExtractorsFactory;
import com.google.android.exoplayer2.extractor.ts.DefaultTsPayloadReaderFactory;
import com.google.android.exoplayer2.source.ConcatenatingMediaSource;
import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.dash.DashMediaSource;
import com.google.android.exoplayer2.source.dash.DefaultDashChunkSource;
import com.google.android.exoplayer2.source.hls.HlsMediaSource;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.ui.PlayerView;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.util.Util;
import com.google.android.exoplayer2.source.hls.HlsDataSourceFactory;

import org.jetbrains.anko.AnkoLogger;
import org.jetbrains.annotations.NotNull;

public class JavaPlayerHolder implements AnkoLogger {
    private Context context;
    private JavaPlayerState playerState;
    private PlayerView playerView;
    private ExoPlayer audioFocusPlayer;

    public JavaPlayerHolder(Context context, JavaPlayerState playerState, PlayerView playerView) {
        this.context = context;
        this.playerState = playerState;
        this.playerView = playerView;
        AudioManager audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        AudioAttributesCompat audioAttributes;
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.KITKAT) {
            audioAttributes = new AudioAttributesCompat.Builder()
                    .setContentType(AudioAttributesCompat.CONTENT_TYPE_MUSIC)
                    .setUsage(AudioAttributesCompat.USAGE_MEDIA)
                    .build();
        } else {
            audioAttributes = new AudioAttributesCompat.Builder()
                    .setContentType(AudioManager.STREAM_MUSIC)
                    .build();
        }
        SimpleExoPlayer player = ExoPlayerFactory.newSimpleInstance(context, new DefaultTrackSelector());
        audioFocusPlayer = new JavaAudioFocusWrapper(audioAttributes, audioManager, player);
        playerView.setPlayer(player);
        Log.i("LOG", "SimpleExoPlayer created");
    }

    public MediaSource buildMediaSource() {
        MediaSource[] result = new MediaSource[JavaMediaCatalog.getInstance().getList().size()];
        int index = 0;
        for (MediaDescriptionCompat mediaDescriptionCompat : JavaMediaCatalog.getInstance().getList()) {
            result[index] = createExtractorMediaSource(mediaDescriptionCompat.getMediaUri());
            index++;
        }
        return new ConcatenatingMediaSource(result);
    }

    public MediaSource createExtractorMediaSource(Uri uri) {
        DefaultBandwidthMeter bandwidthMeter = new DefaultBandwidthMeter();
        DataSource.Factory dataSourceFactory = new DefaultDataSourceFactory(context, Util.getUserAgent(context, "webtoon"), bandwidthMeter);
        ExtractorsFactory extractorsFactory = new DefaultExtractorsFactory().setTsExtractorFlags(DefaultTsPayloadReaderFactory.FLAG_DETECT_ACCESS_UNITS).setTsExtractorFlags(DefaultTsPayloadReaderFactory.FLAG_ALLOW_NON_IDR_KEYFRAMES);
        MediaSource videoSource;
        if (uri.getLastPathSegment().contains(".m3u8")) {
            videoSource = new HlsMediaSource.Factory(dataSourceFactory).createMediaSource(uri);
//        }else if(uri.getLastPathSegment().contains(".mp4")){
//            videoSource=new DashMediaSource.Factory(new DefaultDashChunkSource.Factory(dataSourceFactory),dataSourceFactory).createMediaSource(uri);
        } else {
            videoSource = new ExtractorMediaSource.Factory(dataSourceFactory).setExtractorsFactory(extractorsFactory).createMediaSource(uri);
        }
        return videoSource;
    }

    public void start() {
        audioFocusPlayer.prepare(buildMediaSource());
        audioFocusPlayer.setPlayWhenReady(playerState.isWhenReady());
        audioFocusPlayer.seekTo(playerState.getWindow(), playerState.getPosition());
        attachLogging(audioFocusPlayer);
        Log.i("TAG", "SimpleExoPlayer started");
    }

    public void stop() {
        playerState.setPosition(audioFocusPlayer.getCurrentPosition());
        playerState.setWindow(audioFocusPlayer.getCurrentWindowIndex());
        playerState.setWhenReady(audioFocusPlayer.getPlayWhenReady());
        audioFocusPlayer.stop(true);
        Log.i("TAG", "SimpleExoPlayer stopped");
    }

    public void release() {
        audioFocusPlayer.release();
        Log.i("TAG", "SimpleExoPlayer released");

    }

    public void attachLogging(ExoPlayer exoPlayer) {
        exoPlayer.addListener(new Player.DefaultEventListener() {
            @Override
            public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {
                Log.i("TAG", "player state changed: " + getState(playbackState));
                switch (playbackState) {
                    case Player.STATE_ENDED:
                        Toast.makeText(context, "playback ended", Toast.LENGTH_SHORT).show();
                        break;
                    case Player.STATE_READY:
                        if (playWhenReady) {
                            Toast.makeText(context, "playback started", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(context, "playback paused", Toast.LENGTH_SHORT).show();
                        }
                        break;
                }
            }

            @Override
            public void onPlayerError(ExoPlaybackException error) {
                Log.i("TAG", "player error: " + error.getMessage());
            }

            public String getState(int state) {
                switch (state) {
                    case Player.STATE_BUFFERING:
                        return "STATE BUFFERING";
                    case Player.STATE_ENDED:
                        return "STATE ENDED";
                    case Player.STATE_IDLE:
                        return "STATE IDLE";
                    case Player.STATE_READY:
                        return "STATE READY";
                    default:
                        return "?";
                }
            }
        });
    }

    public ExoPlayer getAudioFocusPlayer() {
        return audioFocusPlayer;
    }

    @NotNull
    @Override
    public String getLoggerTag() {
        return "player holder tag";
    }


}
