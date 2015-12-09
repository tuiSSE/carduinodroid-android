package com.swp.tuilmenau.carduinodroid_android;

/**
 * Created by keX on 09.12.2015.
 */
abstract public class SerialConnection {
    protected enum State {
        IDLE(0), CONNECTED(1), TRYCONNECT(2), RUNNING(3), ERROR(-1),STREAMERROR(-2);
        private int state;
        private State(int s) {
            state = s;
        }
        public int getState() {
            return state;
        }
    }
    protected State state;

    abstract public boolean  find();
    abstract public boolean  connect();
    abstract public boolean  close();
    abstract public void     send(byte[] buffer);
    abstract public byte[]   receive();


    public SerialConnection(){
        state = State.IDLE;
    }

    public boolean reset() {
        state = State.IDLE;
        return true;
    }

    public void run() {
        state = State.RUNNING;
    }

    public boolean isIdle(){
        return state == State.IDLE;
    }
    public boolean isConnected(){
        return state == State.CONNECTED;
    }
    public boolean isError(){
        return (state == State.ERROR) || (state == State.STREAMERROR);
    }
    public boolean isTryConnect(){
        return state == State.TRYCONNECT;
    }
    public boolean isRunning(){
        return state == State.RUNNING;
    }

    public State getState(){
        return state;
    }
}
