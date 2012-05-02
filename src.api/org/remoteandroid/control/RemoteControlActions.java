package org.remoteandroid.control;

/**
 * Action String constants.
 * 
 * @author rom
 * 
 */
public interface RemoteControlActions {

    /** Broadcast used when the game-host want to start the control-host. */
    String START_CAPTURE = "org.remoteandroid.control.START_CAPTURE";

    /** Broadcast used when the game-host want to stop the control-host. */
    String STOP_CAPTURE = "org.remoteandroid.control.STOP_CAPTURE";

}
