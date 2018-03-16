package com.adwitiya.cs7cs3.towerpower;

/**
 * Created by stackoverflow on 16/03/2018.
 */

import android.content.Context;
import android.media.MediaPlayer;

public class AudioPlay {

    public static MediaPlayer mediaPlayer;
    public static boolean isplayingAudio=false;



    public static void playAudio(Context c,int id){
        mediaPlayer = MediaPlayer.create(c,id);
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
