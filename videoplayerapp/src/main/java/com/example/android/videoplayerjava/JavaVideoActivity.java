package com.example.android.videoplayerjava;

import android.app.PictureInPictureParams;
import android.content.res.Configuration;
import android.media.AudioManager;
import android.os.Build;
import android.support.v4.media.MediaDescriptionCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Rational;

import com.example.android.videoplayersample.R;
import com.google.android.exoplayer2.ext.mediasession.MediaSessionConnector;
import com.google.android.exoplayer2.ext.mediasession.TimelineQueueNavigator;
import com.google.android.exoplayer2.ui.PlayerView;

import org.jetbrains.anko.AnkoLogger;
import org.jetbrains.annotations.NotNull;

public class JavaVideoActivity extends AppCompatActivity implements AnkoLogger {
    private MediaSessionCompat mediaSession;
    private MediaSessionConnector mediaSessionConnector;
    private JavaPlayerHolder playerHolder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_java_video);
        setVolumeControlStream(AudioManager.STREAM_MUSIC);
        createMediaSession();
        createMediaSessionConnector();
        createPlayer();
    }

    @Override
    protected void onStart() {
        super.onStart();
        startPlayer();
        activateMediaSession();
    }

    @Override
    protected void onStop() {
        super.onStop();
        stopPlayer();
        deactivateMediaSession();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        releasePlayer();
        releaseMediaSession();
    }

    private void createMediaSession() {
        mediaSession = new MediaSessionCompat(this, getPackageName());
    }

    private void createMediaSessionConnector() {
        MediaSessionConnector var1 = new MediaSessionConnector(this.getMediaSession());
        var1.setQueueNavigator(new TimelineQueueNavigator(mediaSession) {
            @Override
            public MediaDescriptionCompat getMediaDescription(int windowIndex) {
                return JavaMediaCatalog.getInstance().getItemAt(windowIndex);
            }
        });
        mediaSessionConnector = var1;
    }

    private void activateMediaSession() {
        mediaSessionConnector.setPlayer(playerHolder.getAudioFocusPlayer(), null);
        mediaSession.setActive(true);
    }

    private void deactivateMediaSession() {
        mediaSessionConnector.setPlayer(null, null);
        mediaSession.setActive(false);
    }

    private void releaseMediaSession() {
        mediaSession.release();
    }

    private void createPlayer() {
        playerHolder = new JavaPlayerHolder(this, new JavaPlayerState(), (PlayerView) findViewById(R.id.exoplayerview_activity_video));
    }

    private void startPlayer() {
        playerHolder.start();
    }

    private void stopPlayer() {
        playerHolder.stop();
    }

    private void releasePlayer() {
        playerHolder.release();
    }

    @Override
    protected void onUserLeaveHint() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            enterPictureInPictureMode(new PictureInPictureParams.Builder()
                    .setAspectRatio(new Rational(16, 9))
                    .build()
            );
        }
    }

    @Override
    public void onPictureInPictureModeChanged(boolean isInPictureInPictureMode, Configuration newConfig) {
        PlayerView view = findViewById(R.id.exoplayerview_activity_video);
        view.setUseController(!isInPictureInPictureMode);
    }

    @NotNull
    @Override
    public String getLoggerTag() {
        return "logger tag";
    }

    private MediaSessionCompat getMediaSession() {
        return mediaSession;
    }
}
