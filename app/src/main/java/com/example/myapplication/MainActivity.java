package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

import static com.example.myapplication.Utility.DASH_NODRM_MS;
import static com.example.myapplication.Utility.DRM_CLEAR_MS;
import static com.example.myapplication.Utility.DRM_SECURE_MS;
import static com.example.myapplication.Utility.EXTRACTOR_MS;
import static com.example.myapplication.Utility.INTENT_MEDIASOURCE;
import static com.example.myapplication.Utility.INTENT_MEDIATYPE;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private Button btnExtractSc, btnDrmClearSc, btnDrmSecureSc, btnDashNoDrmSc;
    private long mLastClickTime = 0;
    private String contentUriDrmSecure = "https://storage.googleapis.com/wvmedia/cenc/h264/tears/tears.mpd"; // DASH Video with DRM
    private String contentUriDrmClear = "https://storage.googleapis.com/wvmedia/clear/h264/tears/tears.mpd"; // DASH Video with DRM Clear
    private String contentUriDash = "https://www.youtube.com/api/manifest/dash/id/bf5bb2419360daf1/source/youtube?as=fmp4_audio_clear,fmp4_sd_hd_clear&sparams=ip,ipbits,expire,source,id,as&ip=0.0.0.0&ipbits=0&expire=19000000000&signature=51AF5F39AB0CEC3E5497CD9C900EBFEAECCCB5C7.8506521BFC350652163895D4C26DEE124209AA9E&key=ik0"; // DASH Video with no DRM
    private String contentUriNormal = "http://clips.vorwaerts-gmbh.de/big_buck_bunny.mp4"; // Normal Video
    private Intent intentPlayer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initResource();
    }

    private void initResource() {
        btnExtractSc = findViewById(R.id.btn_extractor_ms);
        btnDrmClearSc = findViewById(R.id.btn_drm_clear_ms);
        btnDrmSecureSc = findViewById(R.id.btn_drm_sc_ms);
        btnDashNoDrmSc = findViewById(R.id.btn_dash_no_drm);

        btnExtractSc.setOnClickListener(this);
        btnDrmClearSc.setOnClickListener(this);
        btnDrmSecureSc.setOnClickListener(this);
        btnDashNoDrmSc.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        if (SystemClock.elapsedRealtime() - mLastClickTime < 1000) {
            return;
        }
        mLastClickTime = SystemClock.elapsedRealtime();
        intentPlayer = new Intent(this, PlayerActivity.class);

        switch (v.getId()) {
            case R.id.btn_extractor_ms:
                intentPlayer.putExtra(INTENT_MEDIASOURCE,contentUriNormal);
                intentPlayer.putExtra(INTENT_MEDIATYPE,EXTRACTOR_MS);
                startActivity(intentPlayer);
                break;
            case R.id.btn_drm_clear_ms:
                intentPlayer.putExtra(INTENT_MEDIASOURCE,contentUriDrmClear);
                intentPlayer.putExtra(INTENT_MEDIATYPE,DRM_CLEAR_MS);
                startActivity(intentPlayer);
                break;
            case R.id.btn_drm_sc_ms:
                intentPlayer.putExtra(INTENT_MEDIASOURCE,contentUriDrmSecure);
                intentPlayer.putExtra(INTENT_MEDIATYPE,DRM_SECURE_MS);
                startActivity(intentPlayer);
                break;
            case R.id.btn_dash_no_drm:
                intentPlayer.putExtra(INTENT_MEDIASOURCE,contentUriDash);
                intentPlayer.putExtra(INTENT_MEDIATYPE,DASH_NODRM_MS);
                startActivity(intentPlayer);
                break;

            default:
                break;
        }
    }
}
