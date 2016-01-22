package tuisse.carduinodroid_android;

import android.content.Context;
import android.media.AudioManager;
import android.media.SoundPool;

import tuisse.carduinodroid_android.CarduinodroidApplication;
import tuisse.carduinodroid_android.R;

/**
 * Provides methods to load Soundfiles into RAM and play then.
 *
 * @author Paul Thorwirth & Felix Lewandowski
 * @version 1.0
 * @see SoundPool
 * @see AudioManager
 */
public class Sound
{

    SoundPool soundpool;
    AudioManager audioManager;
    int soundID;
    int volume;

    /**
     * Initializes the SoundPool and loads the audio-file for the horn.
     * Sets the Media-Volume to Maximum.
     */
    public Sound()
    {
        soundpool = new SoundPool (5, AudioManager.STREAM_MUSIC, 0);
        audioManager = (AudioManager) CarduinodroidApplication.getAppContext().getSystemService(Context.AUDIO_SERVICE);
        soundID = soundpool.load(CarduinodroidApplication.getAppContext(), R.raw.horn, 1);
        volume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
        audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC), 0);

    }

    /**
     * Plays the SoundFile associated with the horn.
     */
    public void horn()
    {
        soundpool.play(soundID, 1, 1, 1, 0, 1);
    }

    /**
     * Sets the Media-Volume to the previously saved value.
     */
    public void resetVolume()
    {
        audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, volume, 0);
    }
}
