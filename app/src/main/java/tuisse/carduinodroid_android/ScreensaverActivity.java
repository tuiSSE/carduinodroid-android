package tuisse.carduinodroid_android;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import tuisse.carduinodroid_android.data.DataHandler;

public class ScreensaverActivity extends AppCompatActivity {
    private static final String TAG = "CarduinoStatusActivity";

    private RelativeLayout screensaverView;
    private ImageView screensaverImageView;
    private IntentFilter communicationStatusChangeFilter;
    private CommunicationStatusChangeReceiver communicationStatusChangeReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_screensaver);
        setScreensaverStatusBarColor(getResources().getColor(R.color.colorScreensaverStatusbar));
        screensaverView = (RelativeLayout) findViewById(R.id.screensaverView);
        screensaverView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                Log.d(TAG, "onTouchEvent");
                finish();
                return true;
            }
        });
        screensaverImageView = (ImageView) findViewById(R.id.screensaverImageView);
        communicationStatusChangeReceiver =  new CommunicationStatusChangeReceiver();
        communicationStatusChangeFilter =    new IntentFilter(Constants.EVENT.COMMUNICATION_STATUS_CHANGED);
    }

    @Override
    protected void onResume() {
        super.onResume();
        LocalBroadcastManager.getInstance(this).registerReceiver(communicationStatusChangeReceiver, communicationStatusChangeFilter);
        screensaverImageView.setColorFilter(getResources().getColor(getDataHandler().getCommunicationStatusColor()));
        Log.d(TAG, "onScreensaverActivityResume");
    }

    @Override
    protected void onPause() {
        super.onPause();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(communicationStatusChangeReceiver);
        Log.d(TAG, "onScreensaverActivityPause");
    }

    private void setScreensaverStatusBarColor(int color){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = this.getWindow();
            // clear FLAG_TRANSLUCENT_STATUS flag:
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            // add FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS flag to the window
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            // finally change the color
            window.setStatusBarColor(color);
        }
    }

    private DataHandler getDataHandler(){
        return ((CarduinodroidApplication) getApplication()).dataHandler;
    }

    private class CommunicationStatusChangeReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(TAG, "communication status change event: " + getDataHandler().getCommunicationStatus());
            screensaverImageView.setColorFilter(getResources().getColor(getDataHandler().getCommunicationStatusColor()));
        }
    }
}
