package org.remoteandroid.control;

interface RemoteControl {

    List getEvents(boolean start);
    
    void stopCapture();

}