package com.swp.tuilmenau.carduinodroid_android;

import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;
import com.swp.tuilmenau.carduinodroid_android.R;
import static android.content.pm.ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;

public class SettingsActivity extends PreferenceActivity
{
    @Override
    protected void onCreate(final Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        // prevent the application from switching to landscape-mode
        setRequestedOrientation(SCREEN_ORIENTATION_PORTRAIT);
        getFragmentManager().beginTransaction().replace(android.R.id.content, new MyPreferenceFragment()).commit();
    }

    public class MyPreferenceFragment extends PreferenceFragment
    {
        @Override
        public void onCreate(final Bundle savedInstanceState)
        {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.prefs);
        }
    }
}