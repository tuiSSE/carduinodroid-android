package tuisse.carduinodroid_android.data;

import android.graphics.drawable.LayerDrawable;
import android.util.Log;

import tuisse.carduinodroid_android.CarduinodroidApplication;
import tuisse.carduinodroid_android.R;

/**
 * Created by mate on 02.02.2016.
 */
public class CarduinoDroidData extends CarduinoData implements CarduinoDroidIF{
    private final String TAG = "CarduinoDroidData";

    private ConnectionState ipState;
    private IpType ipType;


    public CarduinoDroidData(){
        super();
        try{
            init();
        }catch (Exception e){
            Log.e(TAG, e.toString());
        }
    }

    public CarduinoDroidData (CarduinoData cd){
        super();
        try{
            init();
        }catch (Exception e){
            Log.e(TAG, e.toString());
        }
    }

    private void init(){
        setIpState(new ConnectionState(ConnectionEnum.IDLE));
        setIpType(IpType.WLAN);
    }


    public synchronized LayerDrawable getIpConnLogoId(){
        int status;
        int type;
        switch (ipState.getState()){
            case  TRYFIND:
            case  FOUND:
            case  TRYCONNECT:
                status = R.drawable.status_try_connect;
                break;
            case  CONNECTED:
            case  RUNNING:
                status = R.drawable.status_connected;
                break;
            case ERROR:
                status = R.drawable.status_error;
                break;
            case STREAMERROR:
                status = R.drawable.status_connected_error;
                break;
            case TRYCONNECTERROR:
                status = R.drawable.status_try_connect_error;
                break;
            case UNKNOWN:
                status = R.drawable.status_unknown;
                break;
            default:
                status = R.drawable.status_idle;
                break;
        }
        if(ipState.isUnknown()){
            type = R.drawable.ip_type_none;
        }
        else{
            switch (ipType){
                case WLAN:
                    type = R.drawable.ip_type_wlan;
                    break;
                default:
                    type = R.drawable.ip_type_none;
                    break;
            }
        }
        return Utils.assembleDrawables(status, type);
    }

    public synchronized String getRemoteIp(){
        //TODO: implement
        return CarduinodroidApplication.getAppContext().getString(R.string.ipDummyRemote);
    }

    public synchronized String getTransceiverIp(){
        //TODO: implement
        return CarduinodroidApplication.getAppContext().getString(R.string.ipDummyTransceiver);
    }
    public synchronized ConnectionState getIpState(){
        return ipState;
    }
    public synchronized void setIpState(ConnectionState is){
        ipState = is;
    }
    public synchronized IpType getIpType(){
        return ipType;
    }
    public synchronized void setIpType(IpType it){
        ipType = it;
    }
}
