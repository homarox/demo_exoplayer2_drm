package com.example.myapplication;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

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
import com.google.android.exoplayer2.source.dash.DashChunkSource;
import com.google.android.exoplayer2.source.dash.DashMediaSource;
import com.google.android.exoplayer2.source.dash.DefaultDashChunkSource;
import com.google.android.exoplayer2.trackselection.AdaptiveTrackSelection;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.TrackSelection;
import com.google.android.exoplayer2.trackselection.TrackSelectionArray;
import com.google.android.exoplayer2.ui.PlayerView;
import com.google.android.exoplayer2.upstream.BandwidthMeter;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter;
import com.google.android.exoplayer2.upstream.DefaultHttpDataSourceFactory;
import com.google.android.exoplayer2.util.Util;

import static com.example.myapplication.Utility.DASH_NODRM_MS;
import static com.example.myapplication.Utility.DRM_CLEAR_MS;
import static com.example.myapplication.Utility.DRM_SECURE_MS;
import static com.example.myapplication.Utility.EXTRACTOR_MS;
import static com.example.myapplication.Utility.INTENT_MEDIASOURCE;
import static com.example.myapplication.Utility.INTENT_MEDIATYPE;
import static com.example.myapplication.Utility.LOG_EXO;

public class PlayerActivity extends AppCompatActivity {

    private PlayerView videoView;
    private TextView txtVideoTitle;
    private SimpleExoPlayer player; // For nonDRM or DRM Clear

    private long playbackPosition;
    private int currentWindow;
    private boolean playWhenReady = true;

    //    private DataSource.Factory mediaDataSourceFactory;
    private DefaultHttpDataSourceFactory mediaDataSourceFactory;
    private BandwidthMeter bandwidthMeter;
    private DefaultTrackSelector trackSelector;
    boolean haveStartPosition = currentWindow != C.INDEX_UNSET;
    private LoadControl defaultLoadControl;

    private String contentUri;
    private String contentType;
    private Intent intent = getIntent();
    private ExoPlayer.EventListener playerEventListener;
    private DefaultDrmSessionManager<FrameworkMediaCrypto> drmSessionManager;
    private MediaSource mediaSource;

    private static final DefaultBandwidthMeter BANDWIDTH_METER = new DefaultBandwidthMeter();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player);

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
            mediaDataSourceFactory = new DefaultHttpDataSourceFactory("mediaPlayerSample");

            TrackSelection.Factory videoTrackSelectionFactory = new AdaptiveTrackSelection.Factory(bandwidthMeter);
            trackSelector = new DefaultTrackSelector(videoTrackSelectionFactory);

/*
            MediaDrmCallback drmCallback = (MediaDrmCallback) new WidevineTestMediaDrmCallback("", "widevine_test");
            try {
                drmSessionManager = DefaultDrmSessionManager.newWidevineInstance(drmCallback, null, null, null);
            } catch (UnsupportedDrmException e) {
                e.printStackTrace();
            }

            player = ExoPlayerFactory.newSimpleInstance(this, trackSelector, defaultLoadControl, drmSessionManager); // with DRM
*/
            player = ExoPlayerFactory.newSimpleInstance(this, trackSelector); // No DRM

            videoView.setPlayer(player);

            player.setPlayWhenReady(playWhenReady);  // Set Auto Play
            player.setRepeatMode(Player.REPEAT_MODE_ALL);
            player.addListener(playerEventListener);

            if (haveStartPosition) {
                player.seekTo(currentWindow, playbackPosition);
            }
        }

        buildeMediaSource(Uri.parse(contentUri));
        player.prepare(mediaSource, !haveStartPosition, false);
        // END
    }

    private void initResource() {
        videoView = findViewById(R.id.video_view);
        txtVideoTitle = findViewById(R.id.txt_video_info);
        drmSessionManager = null;

        intent = getIntent();
        if (intent != null) {
            contentUri = intent.getStringExtra(INTENT_MEDIASOURCE);
            contentType = intent.getStringExtra(INTENT_MEDIATYPE);
        } else {
            txtVideoTitle.setText("Source ERROR!");
        }

        playerEventListener = new ExoPlayer.EventListener() {
            @Override
            public void onTimelineChanged(Timeline timeline, Object manifest, int reason) {
                Log.i(LOG_EXO, "onTimelineChanged");
            }

            @Override
            public void onTracksChanged(TrackGroupArray trackGroups, TrackSelectionArray trackSelections) {
                Log.i(LOG_EXO, "onTracksChanged");
            }

            @Override
            public void onLoadingChanged(boolean isLoading) {
                Log.i(LOG_EXO, "onLoadingChanged");
            }

            @Override
            public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {
                Log.i(LOG_EXO, "onPlayerStateChanged");
            }

            @Override
            public void onRepeatModeChanged(int repeatMode) {
                Log.i(LOG_EXO, "onRepeatModeChanged");
            }

            @Override
            public void onShuffleModeEnabledChanged(boolean shuffleModeEnabled) {
                Log.i(LOG_EXO, "onShuffleModeEnabledChanged");
            }

            @Override
            public void onPlayerError(ExoPlaybackException error) {
                Log.i(LOG_EXO, "onPlayerError");
            }

            @Override
            public void onPositionDiscontinuity(int reason) {
                Log.i(LOG_EXO, "onPositionDiscontinuity");
            }

            @Override
            public void onPlaybackParametersChanged(PlaybackParameters playbackParameters) {
                Log.i(LOG_EXO, "onPlaybackParametersChanged");
            }

            @Override
            public void onSeekProcessed() {
                Log.i(LOG_EXO, "onSeekProcessed");
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

    private void buildeMediaSource(Uri uri) {
        txtVideoTitle.setText(contentType);
        switch (contentType) {
            case DASH_NODRM_MS:
            case DRM_CLEAR_MS:
                Log.i(LOG_EXO, "DRM_CLEAR_MS");
                // ** Report khi khong co tin hieu tra ve *** //
                String userAgent = Util.getUserAgent(this, "ExoPlayerSample");

                /* DASH Video with no DRM and with DRM Clear*/
                DataSource.Factory manifestDataSourceFactory = new DefaultHttpDataSourceFactory("ua");
                DashChunkSource.Factory dashChunkSourceFactory = new DefaultDashChunkSource.Factory(new DefaultHttpDataSourceFactory("ua", BANDWIDTH_METER));
                mediaSource = new DashMediaSource.Factory(dashChunkSourceFactory, manifestDataSourceFactory).createMediaSource(uri);
                break;
            case EXTRACTOR_MS:
                mediaSource = new ExtractorMediaSource.Factory(mediaDataSourceFactory).createMediaSource(Uri.parse(contentUri));
            case DRM_SECURE_MS:
                // Not Complete
                break;

            default:
                txtVideoTitle.setText("Source ERROR");
                break;
        }
        /* DASH Video with DRM and offline Key - not complete
        return (MediaSource) new DashRendererBuilder(this, userAgent, uri.toString(),
                new WidevineTestMediaDrmCallback("","widevine_test"));
        */
    }
}
