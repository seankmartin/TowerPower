package com.adwitiya.cs7cs3.towerpower.helpers;


import android.content.Context;
import android.media.MediaPlayer;

public class AudioPlay {

    private static MediaPlayer mediaPlayer;
    private static boolean isplayingAudio=false;



    public static void playAudio(Context c){
        mediaPlayer = MediaPlayer.create(c, com.adwitiya.cs7cs3.towerpower.R.raw.theme);
        if(!mediaPlayer.isPlaying())
        {
            isplayingAudio=true;
            mediaPlayer.setLooping(true);
            mediaPlayer.start();
        }
    }
    public static void stopAudio(){
        isplayingAudio=false;
        if (mediaPlayer!=null)
            mediaPlayer.stop();

    }
}
