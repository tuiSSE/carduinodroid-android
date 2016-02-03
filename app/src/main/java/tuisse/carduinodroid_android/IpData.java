package tuisse.carduinodroid_android;

import android.graphics.drawable.LayerDrawable;

/**
 * Created by keX on 04.01.2016.
 */
public class IpData {
    public IpProtocolRx IpRx;
    public IpProtocolTx IpTx;

    private ConnectionState ipState;
    private IpType ipType;

    public IpData(){
        IpRx = new IpProtocolRx();
        IpTx = new IpProtocolTx();

        ipState = new ConnectionState(ConnectionEnum.IDLE);
        ipType = IpType.WLAN;
    }

    public synchronized LayerDrawable getIPConnLogoId(){
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
        return Utils.assembleDrawables(status,type);
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
