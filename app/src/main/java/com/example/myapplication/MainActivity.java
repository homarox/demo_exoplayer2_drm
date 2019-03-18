package com.example.myapplication;

import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.example.myapplication.player.DashRendererBuilder;
import com.example.myapplication.player.DemoPlayer;
import com.example.myapplication.player.WidevineTestMediaDrmCallback;
import com.google.android.exoplayer2.C;
import com.google.android.exoplayer2.DefaultLoadControl;
import com.google.android.exoplayer2.ExoPlaybackException;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.LoadControl;
import com.google.android.exoplayer2.PlaybackParameters;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.Timeline;
import com.google.android.exoplayer2.drm.DefaultDrmSessionManager;
import com.google.android.exoplayer2.drm.FrameworkMediaCrypto;
import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.TrackGroupArray;
import com.google.android.exoplayer2.trackselection.AdaptiveTrackSelection;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.TrackSelection;
import com.google.android.exoplayer2.trackselection.TrackSelectionArray;
import com.google.android.exoplayer2.ui.PlayerView;
import com.google.android.exoplayer2.upstream.BandwidthMeter;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.upstream.DefaultHttpDataSourceFactory;
import com.google.android.exoplayer2.upstream.TransferListener;
import com.google.android.exoplayer2.util.Util;

public class MainActivity extends AppCompatActivity {

    private PlayerView videoView;
    private TextView txtVideoTitle;
    private SimpleExoPlayer player;

    private long playbackPosition;
    private int currentWindow;
    private boolean playWhenReady = true;

    private DataSource.Factory mediaDataSourceFactory;
    private BandwidthMeter bandwidthMeter;
    private DefaultTrackSelector trackSelector;
    boolean haveStartPosition = currentWindow != C.INDEX_UNSET;
    private LoadControl defaultLoadControl;

    private String contentUri = "http://clips.vorwaerts-gmbh.de/big_buck_bunny.mp4";
    private ExoPlayer.EventListener playerEventListener;
    private DefaultDrmSessionManager<FrameworkMediaCrypto> drmSessionManager;

    private final String LOG = "ExoPlayerLog";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initResource();
        if (Util.SDK_INT > 23) {
            initPlayer();
        }
    }

    private void initPlayer() {

        // Simple Player
        if (player == null) {
            defaultLoadControl = new DefaultLoadControl();
//            drmSessionManager

            bandwidthMeter = new DefaultBandwidthMeter();
            mediaDataSourceFactory = new DefaultDataSourceFactory(this, Util.getUserAgent(this, "mediaPlayerSample"), (TransferListener<? super DataSource>) bandwidthMeter);

            TrackSelection.Factory videoTrackSelectionFactory = new AdaptiveTrackSelection.Factory(bandwidthMeter);
            trackSelector = new DefaultTrackSelector(videoTrackSelectionFactory);

            player = ExoPlayerFactory.newSimpleInstance(this, trackSelector); // No DRM
//            player = ExoPlayerFactory.newSimpleInstance(this, trackSelector, defaultLoadControl, drmSessionManager); // with DRM

            videoView.setPlayer(player);

            player.setPlayWhenReady(playWhenReady);  // Set Auto Play
            player.setRepeatMode(Player.REPEAT_MODE_ALL);
            player.addListener(playerEventListener);

            if (haveStartPosition) {
                player.seekTo(currentWindow, playbackPosition);
            }
        }

        MediaSource mediaSource = new ExtractorMediaSource.Factory(mediaDataSourceFactory).createMediaSource(Uri.parse(contentUri));

        player.prepare(mediaSource, !haveStartPosition, false);
        // END
    }

    private MediaSource buildMediaSource(Uri uri) {
        return new ExtractorMediaSource.Factory(
                new DefaultHttpDataSourceFactory("exoplayer-codelab")).
                createMediaSource(uri);
    }

    private void initResource() {
        videoView = findViewById(R.id.video_view);
        txtVideoTitle = findViewById(R.id.txt_video_info);
        drmSessionManager = null;
        playerEventListener = new ExoPlayer.EventListener(){
            @Override
            public void onTimelineChanged(Timeline timeline, Object manifest, int reason) {
                Log.i(LOG,"onTimelineChanged");
            }

            @Override
            public void onTracksChanged(TrackGroupArray trackGroups, TrackSelectionArray trackSelections) {
                Log.i(LOG,"onTracksChanged");
            }

            @Override
            public void onLoadingChanged(boolean isLoading) {
                Log.i(LOG,"onLoadingChanged");
            }

            @Override
            public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {
                Log.i(LOG,"onPlayerStateChanged");
            }

            @Override
            public void onRepeatModeChanged(int repeatMode) {
                Log.i(LOG,"onRepeatModeChanged");
            }

            @Override
            public void onShuffleModeEnabledChanged(boolean shuffleModeEnabled) {
                Log.i(LOG,"onShuffleModeEnabledChanged");
            }

            @Override
            public void onPlayerError(ExoPlaybackException error) {
                Log.i(LOG,"onPlayerError");
            }

            @Override
            public void onPositionDiscontinuity(int reason) {
                Log.i(LOG,"onPositionDiscontinuity");
            }

            @Override
            public void onPlaybackParametersChanged(PlaybackParameters playbackParameters) {
                Log.i(LOG,"onPlaybackParametersChanged");
            }

            @Override
            public void onSeekProcessed() {
                Log.i(LOG,"onSeekProcessed");
            }
        };
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (Util.SDK_INT <= 23) {
            releasePlayer();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (Util.SDK_INT > 23) {
            releasePlayer();
        }
    }

    private void releasePlayer() {
        if (player != null) {
            playbackPosition = player.getCurrentPosition();
            currentWindow = player.getCurrentWindowIndex();
            playWhenReady = player.getPlayWhenReady();
            player.release();
            player = null;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        hideSystemUi();
        if ((Util.SDK_INT <= 23 || player == null)) {
            initPlayer();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        if ((Util.SDK_INT <= 23 || player == null)) {
            initPlayer();
        }
    }

    private void hideSystemUi() {
        videoView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE
                | View.SYSTEM_UI_FLAG_FULLSCREEN
                | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
    }

    private DemoPlayer.RendererBuilder getRendererBuilder() {
        String userAgent = "ExoPlayerSample";
        String contentId = "";
        String provider = "widevine_test";
        return new DashRendererBuilder(this, userAgent, contentUri,
                new WidevineTestMediaDrmCallback(contentId,provider));
    }
}
