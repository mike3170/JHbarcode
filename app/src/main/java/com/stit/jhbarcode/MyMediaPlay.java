package com.stit.jhbarcode;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.AssetFileDescriptor;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import com.stit.jhbarcode.model.MP3;


public class MyMediaPlay {
    // 播放音效
    public static MediaPlayer mp;
    public Context context;

    public MyMediaPlay(Context context ) {
        this.context = context;
    }

    // 播放音效
    public void play(MP3 mp3) {
        switch (mp3) {
            case sweet:
                mp = new MediaPlayer();
                this.setMp3DataSource(MP3.sweet);
                break;

            case beepLong:
                mp = new MediaPlayer();
                this.setMp3DataSource(MP3.beepLong);
                break;

            case beepError:
                mp = new MediaPlayer();
                this.setMp3DataSource(MP3.beepError);
                break;

            case beepError2:
                mp = new MediaPlayer();
                this.setMp3DataSource(MP3.beepError2);
                break;
        }

        mp.setOnCompletionListener(mp -> mp.release());
        mp.setOnPreparedListener((mediaPlayer) -> mediaPlayer.start());

        mp.prepareAsync();
    }

    private void setMp3DataSource(MP3 mp3) {
        AssetFileDescriptor file = null;

        try {
            switch (mp3) {
                case sweet:
                    file = context.getResources().openRawResourceFd(R.raw.sweet);
                    mp.setDataSource(file.getFileDescriptor(), file.getStartOffset(), file.getLength());
                    file.close();
                    break;

                case beepLong:
                    file = context.getResources().openRawResourceFd(R.raw.beeplong);
                    mp.setDataSource(file.getFileDescriptor(), file.getStartOffset(), file.getLength());
                    file.close();
                    break;

                case beepError:
                    file = context.getResources().openRawResourceFd(R.raw.beep_error);
                    mp.setDataSource(file.getFileDescriptor(), file.getStartOffset(), file.getLength());
                    file.close();
                    break;

                case beepError2:
                    file = context.getResources().openRawResourceFd(R.raw.beep_error2);
                    mp.setDataSource(file.getFileDescriptor(), file.getStartOffset(), file.getLength());
                    file.close();
                    break;
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public String getVersion() {
        // 取得版本資訊
        try {
            PackageInfo packageInfo = context.getPackageManager()
                    .getPackageInfo(context.getPackageName(), 0);
            int myVerCode = packageInfo.versionCode;
            String myVerName = packageInfo.versionName;
            return myVerName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
            return "0";
        }
    }

    // 偵測無線網路
    public boolean haveInternet() {
        boolean result = false;
        ConnectivityManager connManager = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo info = connManager.getActiveNetworkInfo();
        if (info == null || !info.isConnected()) {
            result = false;
        } else {
            if (!info.isAvailable()) {
                result = false;
            } else {
                result = true;
            }
        }

        return result;
    }

    //控制媒體音量
    public void MediaAudio(){
        //控制媒體音量
        AudioManager am= (AudioManager)context.getSystemService(Context.AUDIO_SERVICE);
        ((Activity) context).setVolumeControlStream( AudioManager.STREAM_MUSIC);
    }
}

