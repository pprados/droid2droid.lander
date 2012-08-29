package org.droid2droid.control;

interface RemoteControl {

    List getEvents(boolean start);
    
    void stopCapture();

}