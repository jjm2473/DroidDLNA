package com.zxt.dlna.dmp;

import android.app.Activity;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnErrorListener;
import android.media.MediaPlayer.OnInfoListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.media.MediaPlayer.OnSeekCompleteListener;
import android.media.MediaPlayer.OnVideoSizeChangedListener;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.MediaController;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import android.widget.Toast;

import com.zxt.dlna.R;
import com.zxt.dlna.dmr.MediaType;
import com.zxt.dlna.util.Action;
import com.zxt.dlna.util.Utils;

import java.io.IOException;

public class Player extends Activity implements OnCompletionListener, OnErrorListener,
        OnInfoListener, OnPreparedListener, OnSeekCompleteListener, OnVideoSizeChangedListener,
        SurfaceHolder.Callback, MediaController.MediaPlayerControl, OnClickListener {
    public final static String LOGTAG = "GPlayer";
    private static final int MEDIA_PLAYER_BUFFERING_UPDATE = 4001;
    private static final int MEDIA_PLAYER_COMPLETION = 4002;
    private static final int MEDIA_PLAYER_ERROR = 4003;
    private static final int MEDIA_PLAYER_INFO = 4004;
    private static final int MEDIA_PLAYER_PREPARED = 4005;
    private static final int MEDIA_PLAYER_PROGRESS_UPDATE = 4006;
    private static final int MEDIA_PLAYER_VIDEO_SIZE_CHANGED = 4007;
    private static final int MEDIA_PLAYER_VOLUME_CHANGED = 4008;
    private static final int MEDIA_PLAYER_HIDDEN_CONTROL = 4009;

    private MediaListener mMediaListener = null;
    View surfaceParent;
    ImageView noVideo;
    SurfaceView surfaceView;
    SurfaceHolder surfaceHolder;
    MediaPlayer mMediaPlayer;
    private Intent createIntent;
    //MediaController mediaController;
    boolean readyToPlay = false;
    String playURI;
    private AudioManager mAudioManager;
    private TextView mTextViewTime;

    private View mProgressLayout;

    private SeekBar mSeekBarProgress;

    private TextView mTextViewLength;

    private ImageButton mPauseButton;

    private ProgressBar mProgressBarPreparing;

    private TextView mTextProgress;

    private TextView mTextInfo;

    private RelativeLayout mBufferLayout;

    private LinearLayout mLayoutBottom;

    private RelativeLayout mLayoutTop;

    private TextView mVideoTitle;

    private Button mLeftButton;

    private Button mRightButton;

    private ImageView mSound;

    private SeekBar mSeekBarSound;

    private volatile boolean mCanSeek = true;

    private boolean isMute;

    private long lastBack = 0;

    private Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            if(msg.what != MEDIA_PLAYER_PROGRESS_UPDATE){
                Log.d(LOGTAG, "msg=" + msg.what);
            }
            switch (msg.what) {
                case MEDIA_PLAYER_BUFFERING_UPDATE: {

                    break;
                }
                case MEDIA_PLAYER_COMPLETION: {

                    break;
                }
                case MEDIA_PLAYER_ERROR: {

                    break;
                }
                case MEDIA_PLAYER_INFO: {

                    break;
                }
                case MEDIA_PLAYER_PREPARED: {
                    mBufferLayout.setVisibility(View.GONE);
                    break;
                }
                case MEDIA_PLAYER_PROGRESS_UPDATE: {
                    if (null == mMediaPlayer || !mMediaPlayer.isPlaying()) {
                        break;
                    }

                    int position = mMediaPlayer.getCurrentPosition();
                    int duration = mMediaPlayer.getDuration();
                    if (null != mMediaListener) {
                        mMediaListener.positionChanged(position);
                        mMediaListener.durationChanged(duration);
                    }

                    mTextViewLength.setText(Utils.secToTime(duration / 1000));
                    mTextViewTime.setText(Utils.secToTime(position / 1000));
                    if(duration > 0) {
                        mSeekBarProgress.setMax(duration);
                        mSeekBarProgress.setProgress(position);
                    }
                    mHandler.sendEmptyMessageDelayed(MEDIA_PLAYER_PROGRESS_UPDATE, 500);

                    break;
                }
                case MEDIA_PLAYER_VIDEO_SIZE_CHANGED: {

                    break;
                }
                case MEDIA_PLAYER_VOLUME_CHANGED: {
                    mSeekBarSound.setProgress(mAudioManager
                            .getStreamVolume(AudioManager.STREAM_MUSIC));
                    break;
                }
                case MEDIA_PLAYER_HIDDEN_CONTROL: {
                    mLayoutTop.setVisibility(View.GONE);
                    mLayoutBottom.setVisibility(View.GONE);
                    break;
                }
                default:
                    break;
            }
        }
    };

    public void setMediaListener0(MediaListener mediaListener) {
        mMediaListener = mediaListener;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        createIntent = getIntent();

        setContentView(R.layout.gplayer);
        mAudioManager = (AudioManager) getSystemService(Service.AUDIO_SERVICE);

        surfaceParent = findViewById(R.id.gplayer_parent);
        if(Build.VERSION.SDK_INT >= 16){
            surfaceParent.setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN|View.SYSTEM_UI_FLAG_HIDE_NAVIGATION|View.SYSTEM_UI_FLAG_IMMERSIVE);
            surfaceParent.setOnSystemUiVisibilityChangeListener(new View.OnSystemUiVisibilityChangeListener() {
                @Override
                public void onSystemUiVisibilityChange(int visibility) {
                    if(mMediaPlayer.isPlaying()){
                        resize();
                    }
                }
            });
        }
        noVideo = (ImageView) findViewById(R.id.novideo);
        surfaceView = (SurfaceView) findViewById(R.id.gplayer_surfaceview);
        surfaceHolder = surfaceView.getHolder();
        surfaceHolder.addCallback(this);
        surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);

        mMediaPlayer = new MediaPlayer();
        mMediaPlayer.setOnCompletionListener(this);
        mMediaPlayer.setOnErrorListener(this);
        mMediaPlayer.setOnInfoListener(this);
        mMediaPlayer.setOnPreparedListener(this);
        mMediaPlayer.setOnSeekCompleteListener(this);
        mMediaPlayer.setOnVideoSizeChangedListener(this);

        initControl();
    }

    private void setTitle(String name) {
        if (!TextUtils.isEmpty(name)) {
            mVideoTitle.setText(name);
        }
    }

    private void initControl() {
        // mediaController = new MediaController(this);

        mBufferLayout = (RelativeLayout) findViewById(R.id.buffer_info);
        mProgressBarPreparing = (ProgressBar) findViewById(R.id.player_prepairing);
        mTextProgress = (TextView) findViewById(R.id.prepare_progress);
        mTextInfo = (TextView) findViewById(R.id.info);

        mLayoutTop = (RelativeLayout) findViewById(R.id.layout_top);
        mVideoTitle = (TextView) findViewById(R.id.video_title);
        mLeftButton = (Button) findViewById(R.id.topBar_back);
        mRightButton = (Button) findViewById(R.id.topBar_list_switch);
        mLeftButton.setOnClickListener(this);
        mRightButton.setOnClickListener(this);

        mTextViewTime = (TextView) findViewById(R.id.current_time);
        mTextViewLength = (TextView) findViewById(R.id.totle_time);
        mPauseButton = (ImageButton) findViewById(R.id.play);
        mPauseButton.setOnClickListener(this);
        mLayoutBottom = (LinearLayout) findViewById(R.id.layout_control);
        mTextProgress = (TextView) findViewById(R.id.prepare_progress);
        mTextInfo = (TextView) findViewById(R.id.info);

        mProgressLayout = findViewById(R.id.progress_layout);
        mCanSeek = true;
        mSeekBarProgress = (SeekBar) findViewById(R.id.seekBar_progress);
        mSeekBarProgress.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                int id = seekBar.getId();
                switch (id) {
                    case R.id.seekBar_progress:
                        if (mCanSeek) {
                            int position = seekBar.getProgress();
                            if (mMediaPlayer != null) {
                                mMediaPlayer.seekTo(position);
                            }
                        }
                        break;
                    default:
                        break;
                }

            }

        });

        mSound = (ImageView) findViewById(R.id.sound);
        mSound.setOnClickListener(this);
        mSeekBarSound = (SeekBar) findViewById(R.id.seekBar_sound);
        mSeekBarSound.setMax(mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC));
        mSeekBarSound.setProgress(mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC));
        mSeekBarSound.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, progress, 0);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
    }

    @Override
    protected void onNewIntent(Intent intent) {
        Log.i(LOGTAG, "onNewIntent");
        if(Intent.ACTION_VIEW.equals(intent.getAction())){
            playURI = intent.getDataString();
            String type = MediaType.UNKNOWN;
            if(intent.getType() == null){
                type = intent.getStringExtra("type");
            } else {
                type = MediaType.fromMime(intent.getType());
            }
            String name = intent.getStringExtra("name");

            switch (type) {
                case MediaType.AUDIO:
                    noVideo.setVisibility(View.VISIBLE);
                    surfaceView.setVisibility(View.GONE);
                    //mMediaPlayer.setDisplay(null);
                    break;
                case MediaType.VIDEO:
                    noVideo.setVisibility(View.GONE);
                    surfaceView.setVisibility(View.VISIBLE);
                    //mMediaPlayer.setDisplay(surfaceHolder);
                    break;
                default:
                    finish();
                    return;
            }

            setTitle(name);
        }

        if (!TextUtils.isEmpty(playURI)) {

            mHandler.removeCallbacks(toExit);

            if(mMediaPlayer.isPlaying()){
                mMediaPlayer.pause();
            }
            mProgressLayout.setVisibility(View.VISIBLE);
            mHandler.postDelayed(toNewUri, 500);
        }
        //super.onNewIntent(intent);
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onStop() {
        if (mMediaPlayer != null && mMediaPlayer.isPlaying()) {
            mMediaPlayer.pause();
        }
        updatePausePlay();
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        fastExit();
        super.onDestroy();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_DOWN) {
            if (System.currentTimeMillis() - lastBack > 1000) {
                Toast.makeText(this, R.string.player_exit, Toast.LENGTH_SHORT).show();
                lastBack = System.currentTimeMillis();
            } else {
                fastExit();
            }
            return true;
        } else if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN) {
            mHandler.sendEmptyMessageDelayed(MEDIA_PLAYER_VOLUME_CHANGED, 100);
        } else if (keyCode == KeyEvent.KEYCODE_VOLUME_UP) {
            mHandler.sendEmptyMessageDelayed(MEDIA_PLAYER_VOLUME_CHANGED, 100);
        }
        return super.onKeyDown(keyCode, event);
    }

    private void delayExit(){
        mHandler.postDelayed(toExit, 1000);
    }

    private void fastExit(){
        if (null != mMediaListener) {
            mMediaListener.endOfMedia();
            mMediaListener = null;
        }
        exit();
    }

    private void exit() {
        if (mMediaPlayer != null) {
            mMediaPlayer.release();
            mMediaPlayer = null;
        }
        finish();
    }

    @Override
    public void onClick(View v) {
        // if (!mMediaPlayerLoaded)
        // return;
        int id = v.getId();
        switch (id) {
            case R.id.topBar_back:
                fastExit();
                break;
            case R.id.sound:
                isMute = !isMute;
                mAudioManager.setStreamMute(AudioManager.STREAM_MUSIC, isMute);
                if (isMute) {
                    mSound.setImageResource(R.drawable.phone_480_sound_mute);
                } else {
                    mSound.setImageResource(R.drawable.phone_480_sound_on);
                }
                break;
            case R.id.play: {
                doPauseResume();
                break;
            }
            default:
                break;
        }
    }

    private void updatePausePlay() {
        if (mMediaPlayer == null || mPauseButton == null) {
            return;
        }

        int resource = mMediaPlayer.isPlaying() ? R.drawable.button_pause
                : R.drawable.button_play;
        mPauseButton.setBackgroundResource(resource);
    }

    private void doPauseResume() {
        if (mMediaPlayer == null) {
            return;
        }
        if (mMediaPlayer.isPlaying()) {
            mMediaPlayer.pause();
            if (null != mMediaListener) {
                mMediaListener.pause();
            }
        } else {
            mMediaPlayer.start();
            mHandler.sendEmptyMessageDelayed(MEDIA_PLAYER_PROGRESS_UPDATE, 200);

            if (null != mMediaListener) {
                mMediaListener.start();
            }
        }
        updatePausePlay();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int action = event.getAction();
        if (action == MotionEvent.ACTION_DOWN) {
            int visibility = mLayoutTop.getVisibility();
            if (visibility != View.VISIBLE) {
                mLayoutTop.setVisibility(View.VISIBLE);
                mLayoutBottom.setVisibility(View.VISIBLE);
            } else {
                mLayoutTop.setVisibility(View.GONE);
                mLayoutBottom.setVisibility(View.GONE);
            }

        }

        // if (mediaController.isShowing()) {
        // mediaController.hide();
        // } else {
        // mediaController.show(10000);
        // }
        return false;
    }

    public int getAudioSessionId() {
        return 1;
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        Log.v(LOGTAG, "surfaceChanged Called");
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        Log.v(LOGTAG, "surfaceCreated Called");
        mMediaPlayer.setDisplay(holder);
        if(createIntent != null){
            onNewIntent(createIntent);
            createIntent = null;
        }
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        Log.v(LOGTAG, "surfaceDestroyed Called");
        if(mMediaPlayer != null){
            mMediaPlayer.setDisplay(null);
        }
    }

    @Override
    public void onVideoSizeChanged(MediaPlayer mp, int width, int height) {
        Log.v(LOGTAG, "onVideoSizeChanged : "+width+", "+height);
    }

    @Override
    public void onSeekComplete(MediaPlayer mp) {
        Log.v(LOGTAG, "onSeekComplete Called");
        if (null != mMediaListener) {
            mMediaListener.positionChanged(mMediaPlayer.getCurrentPosition());
        }
    }

    private void resize(){
        int maxWidth = surfaceParent.getWidth();
        int maxHeight = surfaceParent.getHeight();
        int videoWidth = mMediaPlayer.getVideoWidth();
        int videoHeight = mMediaPlayer.getVideoHeight();
        float heightRatio = (float) maxHeight / (float) videoHeight;
        float widthRatio = (float) maxWidth / (float) videoWidth;
        if(heightRatio != Float.NaN && heightRatio != Float.POSITIVE_INFINITY &&
                widthRatio != Float.NaN && widthRatio != Float.POSITIVE_INFINITY) {
            if (heightRatio < widthRatio) {
                videoHeight = (int) Math.floor((float) videoHeight * heightRatio);
                videoWidth = (int) Math.floor((float) videoWidth * heightRatio);
            } else {
                videoHeight = (int) Math.floor((float) videoHeight * widthRatio);
                videoWidth = (int) Math.floor((float) videoWidth * widthRatio);
            }
            noVideo.setVisibility(View.GONE);
            surfaceView.setLayoutParams(new FrameLayout.LayoutParams(videoWidth, videoHeight, Gravity.CENTER));
            surfaceView.setVisibility(View.VISIBLE);
        } else {
            Log.i(LOGTAG, "invalid video size "+videoWidth+", "+videoHeight);
            surfaceView.setVisibility(View.GONE);
            noVideo.setVisibility(View.VISIBLE);
            //surfaceView.setLayoutParams(new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT, Gravity.CENTER));

        }
    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        Log.v(LOGTAG, "onPrepared Called");
        resize();
        mp.start();
        updatePausePlay();
        if (null != mMediaListener) {
            mMediaListener.start();
        }

        // mediaController.setMediaPlayer(this);
        // mediaController.setAnchorView(this.findViewById(R.id.gplayer_surfaceview));
        // mediaController.setEnabled(true);
        // mediaController.show(5000);
        mHandler.sendEmptyMessage(MEDIA_PLAYER_PREPARED);

        mHandler.sendEmptyMessage(MEDIA_PLAYER_PROGRESS_UPDATE);
        mHandler.sendEmptyMessageDelayed(MEDIA_PLAYER_HIDDEN_CONTROL, 5000);
    }

    @Override
    public boolean onInfo(MediaPlayer mp, int whatInfo, int extra) {
        if (whatInfo == MediaPlayer.MEDIA_INFO_BAD_INTERLEAVING) {
            Log.v(LOGTAG, "Media Info, Media Info Bad Interleaving " + extra);
        } else if (whatInfo == MediaPlayer.MEDIA_INFO_NOT_SEEKABLE) {
            Log.v(LOGTAG, "Media Info, Media Info Not Seekable " + extra);
            mCanSeek = false;
            mProgressLayout.setVisibility(View.GONE);
        } else if (whatInfo == MediaPlayer.MEDIA_INFO_UNKNOWN) {
            Log.v(LOGTAG, "Media Info, Media Info Unknown " + extra);
        } else if (whatInfo == MediaPlayer.MEDIA_INFO_VIDEO_TRACK_LAGGING) {
            Log.v(LOGTAG, "MediaInfo, Media Info Video Track Lagging " + extra);
        } else if (whatInfo == MediaPlayer.MEDIA_INFO_METADATA_UPDATE) {
            Log.v(LOGTAG, "MediaInfo, Media Info Metadata Update " + extra);
        }
        return false;
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        Log.v(LOGTAG, "onCompletion Called");
        if (null != mMediaListener) {
            mMediaListener.endOfMedia();
        }
        updatePausePlay();
        delayExit();
    }

    private void toastError(String msg){
        Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
    }

    @Override
    public boolean onError(MediaPlayer mp, int whatError, int extra) {
        Log.d(LOGTAG, "onError Called " + whatError + "  " + extra);
        if (whatError == MediaPlayer.MEDIA_ERROR_SERVER_DIED) {
            Log.e(LOGTAG, "Media Error, Server Died " + extra);
            toastError("Media Error, Server Died " + extra);
        } else if (whatError == MediaPlayer.MEDIA_ERROR_UNKNOWN) {
            Log.e(LOGTAG, "Media Error, Error Unknown " + extra);
            toastError("Media Error, Error Unknown " + extra);
        } else if (whatError == MediaPlayer.MEDIA_ERROR_IO) {
            Log.e(LOGTAG, "Media Error, Error IO " + extra);
            toastError("Media Error, Error IO " + extra);
        } else if (whatError == MediaPlayer.MEDIA_ERROR_MALFORMED) {
            Log.e(LOGTAG, "Media Error, Error Malformed " + extra);
            toastError("Media Error, Error Malformed " + extra);
        } else if (whatError == -38) {
            return true;
        } else {
            toastError("Media Error, Error (" + whatError + "," + extra + ")");
        }

        return false;
    }

    @Override
    public boolean canPause() {
        return true;
    }

    @Override
    public boolean canSeekBackward() {
        return true;
    }

    @Override
    public boolean canSeekForward() {
        return true;
    }

    @Override
    public int getBufferPercentage() {
        return 0;
    }

    @Override
    public int getCurrentPosition() {
        return mMediaPlayer.getCurrentPosition();
    }

    @Override
    public int getDuration() {
        return mMediaPlayer.getDuration();
    }

    @Override
    public boolean isPlaying() {
        return mMediaPlayer.isPlaying();
    }

    public void setUri(String uri) {
        try {
            mMediaPlayer.reset();
            playURI = uri;
            mMediaPlayer.setDataSource(playURI);
        } catch (IllegalArgumentException e) {
            Log.v(LOGTAG, e.getMessage());
        } catch (IllegalStateException e) {
            Log.v(LOGTAG, e.getMessage());
        } catch (IOException e) {
            Log.v(LOGTAG, e.getMessage());
        }
    }

    @Override
    public void pause() {
        if (mMediaPlayer.isPlaying()) {
            mMediaPlayer.pause();
            if (null != mMediaListener) {
                mMediaListener.pause();
            }
        }
    }

    @Override
    public void seekTo(int pos) {
        if(mCanSeek) {
            mMediaPlayer.seekTo(pos);
            if (null != mMediaListener) {
                mMediaListener.positionChanged(pos);
            }
        }
    }

    @Override
    public void start() {

        try {
            mMediaPlayer.start();
            mHandler.sendEmptyMessageDelayed(MEDIA_PLAYER_PROGRESS_UPDATE, 200);

            if (null != mMediaListener) {
                mMediaListener.start();
            }
        } catch (Exception e) {
            Log.e(LOGTAG, "start()", e);
        }
    }

    public void stop() {

        try {
            if(mMediaPlayer.isPlaying()){
                mMediaPlayer.stop();
            }
            if (null != mMediaListener) {
                mMediaListener.stop();
            }
        } catch (Exception e) {
            Log.e(LOGTAG, "stop()", e);
        }

        delayExit();
    }

    public interface MediaListener {
        void pause();

        void start();

        void stop();

        void endOfMedia();

        void positionChanged(int position);

        void durationChanged(int duration);
    }

    class PlayBrocastReceiver extends BroadcastReceiver {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            String str1 = intent.getStringExtra("helpAction");

            if(mMediaPlayer == null){
                return;
            }

            if (str1.equals(Action.PLAY)) {
                start();
                updatePausePlay();
            } else if (str1.equals(Action.PAUSE)) {
                pause();
                updatePausePlay();
            } else if (str1.equals(Action.SEEK)) {
                if(mCanSeek) {
                    boolean isPaused = false;
                    if (!mMediaPlayer.isPlaying()) {
                        isPaused = true;
                    }
                    int position = intent.getIntExtra("position", 0);
                    seekTo(position);
                    if (isPaused) {
                        pause();
                    } else {
                        start();
                    }
                }

            } else if (str1.equals(Action.SET_VOLUME)) {
                int volume = (int) (intent.getDoubleExtra("volume", 0) * mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC));
                mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, volume, 0);
                mHandler.sendEmptyMessageDelayed(MEDIA_PLAYER_VOLUME_CHANGED, 100);
            } else if (str1.equals(Action.STOP)) {
                stop();
            }

        }
    }
    private Runnable toNewUri = new Runnable() {
        @Override
        public void run() {
            if(!Player.this.isFinishing() && mMediaPlayer != null) {
                try {
                    mMediaPlayer.reset();
                    mMediaPlayer.setDataSource(playURI);
                    mCanSeek = true;
                    mMediaPlayer.prepareAsync();
                } catch (IOException e) {
                    Log.e(LOGTAG, "", e);
                    mVideoTitle.setText("IOException");
                }
            }
        }
    };

    private Runnable toExit = new Runnable() {
        @Override
        public void run() {
            fastExit();
        }
    };
}
