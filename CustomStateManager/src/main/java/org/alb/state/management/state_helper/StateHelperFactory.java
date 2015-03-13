package org.alb.state.management.state_helper;

import javax.faces.application.StateManager;

import com.sun.faces.renderkit.ClientSideStateHelper;
import com.sun.faces.renderkit.ServerSideStateHelper;
import com.sun.faces.renderkit.StateHelper;

/**
 * @author alorie
 *
 */
public class StateHelperFactory {
    
    /**
     * @param typeHelper
     * @return
     */
    public static StateHelper getStateHelper(String typeHelper)
    {

	switch(typeHelper){
	case StateManager.STATE_SAVING_METHOD_CLIENT:
	    return new ClientSideStateHelper();
	case StateManager.STATE_SAVING_METHOD_SERVER:
	    return new ServerSideStateHelper();
	default:
	    return new CustomServerStateHelper(typeHelper);
	}
    }
}
