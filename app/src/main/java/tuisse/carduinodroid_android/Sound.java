package tuisse.carduinodroid_android;

import android.content.Context;
import android.media.AudioManager;
import android.media.SoundPool;

/**
 * Provides methods to load Soundfiles into RAM and play then.
 *
 * @author Paul Thorwirth & Felix Lewandowski
 * @version 1.1
 * @see SoundPool
 * @see AudioManager
 */
public class Sound
{
    private final String TAG = "CarduinoSound";

    SoundPool soundpool;
    AudioManager audioManager;
    int soundID0;
    int soundID1;
    int soundID2;
    int volume;
    boolean playing;
    int streamId0;
    int streamId1;
    int streamId2;

    /**
     * Initializes the SoundPool and loads the audio-file for the horn.
     * Sets the Media-Volume to Maximum.
     */
    public Sound()
    {
        playing = false;
        soundpool = new SoundPool (1, AudioManager.STREAM_MUSIC, 0);
        audioManager = (AudioManager) CarduinodroidApplication.getAppContext().getSystemService(Context.AUDIO_SERVICE);
        soundID0 = soundpool.load(CarduinodroidApplication.getAppContext(), R.raw.horn0, 1);
        soundID1 = soundpool.load(CarduinodroidApplication.getAppContext(), R.raw.horn1, 1);
        soundID2 = soundpool.load(CarduinodroidApplication.getAppContext(), R.raw.horn2, 1);
        volume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
    }

    /**
     * Plays the SoundFile associated with the horn.
     */
    public synchronized void horn()
    {
        if(!playing) {
            playing = true;
            audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC), 0);
            streamId0 = soundpool.play(soundID0,1,1,1, 0,1);
            streamId1 = soundpool.play(soundID1,1,1,1,-1,1);//play forever
        }
    }

    /**
     * Sets the Media-Volume to the previously saved value.
     */
    public synchronized void stop()
    {
        soundpool.stop(streamId0);
        soundpool.stop(streamId1);
        streamId0 = soundpool.play(soundID2,1,1,1, 0,1);
        audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, volume, 0);
        playing = false;
    }
}
