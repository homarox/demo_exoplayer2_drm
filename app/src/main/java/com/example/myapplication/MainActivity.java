package com.example.myapplication;

import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
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

public class MainActivity extends AppCompatActivity {

    private PlayerView videoView;
    private TextView txtVideoTitle;
    private SimpleExoPlayer player;

    private long playbackPosition;
    private int currentWindow;
    private boolean playWhenReady = true;

//    private DataSource.Factory mediaDataSourceFactory;
    private DefaultHttpDataSourceFactory mediaDataSourceFactory;
    private BandwidthMeter bandwidthMeter;
    private DefaultTrackSelector trackSelector;
    boolean haveStartPosition = currentWindow != C.INDEX_UNSET;
    private LoadControl defaultLoadControl;

//    private String contentUri = "https://storage.googleapis.com/wvmedia/cenc/h264/tears/tears.mpd"; // DASH Video with DRM
    private String contentUri = "https://storage.googleapis.com/wvmedia/clear/h264/tears/tears.mpd"; // DASH Video with DRM Clear
//    private String contentUri = "https://www.youtube.com/api/manifest/dash/id/bf5bb2419360daf1/source/youtube?as=fmp4_audio_clear,fmp4_sd_hd_clear&sparams=ip,ipbits,expire,source,id,as&ip=0.0.0.0&ipbits=0&expire=19000000000&signature=51AF5F39AB0CEC3E5497CD9C900EBFEAECCCB5C7.8506521BFC350652163895D4C26DEE124209AA9E&key=ik0"; // DASH Video with no DRM
//    private String contentUri = "http://clips.vorwaerts-gmbh.de/big_buck_bunny.mp4"; // Normal Video
    private ExoPlayer.EventListener playerEventListener;
    private DefaultDrmSessionManager<FrameworkMediaCrypto> drmSessionManager;

    private final String LOG = "ExoPlayerLog";
    private static final DefaultBandwidthMeter BANDWIDTH_METER = new DefaultBandwidthMeter();

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
            mediaDataSourceFactory = new DefaultHttpDataSourceFactory("mediaPlayerSample");

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

        MediaSource mediaSource = buildeMediaSource(Uri.parse(contentUri));

        player.prepare(mediaSource, !haveStartPosition, false);
        // END
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

    private MediaSource buildeMediaSource (Uri uri){
        /* DASH Video with no DRM and with DRM Clear*/
        DataSource.Factory manifestDataSourceFactory = new DefaultHttpDataSourceFactory("ua");
        DashChunkSource.Factory dashChunkSourceFactory = new DefaultDashChunkSource.Factory(new DefaultHttpDataSourceFactory("ua", BANDWIDTH_METER));
        return new DashMediaSource.Factory(dashChunkSourceFactory, manifestDataSourceFactory).createMediaSource(uri);
        /**/

        /* use for No DRM content
        return new ExtractorMediaSource.Factory(mediaDataSourceFactory).createMediaSource(Uri.parse(contentUri)); // 1
*/
    }
}
