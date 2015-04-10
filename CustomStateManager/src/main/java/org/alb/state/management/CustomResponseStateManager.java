package org.alb.state.management;

import static com.sun.faces.config.WebConfiguration.WebContextInitParameter.StateSavingMethod;

import java.io.IOException;

import javax.faces.FacesException;
import javax.faces.context.FacesContext;
import javax.faces.render.ResponseStateManager;

import org.alb.state.management.state_helper.StateHelperFactory;

import com.sun.faces.config.WebConfiguration;
import com.sun.faces.renderkit.StateHelper;
import com.sun.faces.util.RequestStateManager;

/**
 *
 * @author Leonard
 */
public class CustomResponseStateManager extends ResponseStateManager {

    private StateHelper helper;

    public CustomResponseStateManager() 
    {
	WebConfiguration webConfig = WebConfiguration.getInstance();
	String stateMode = webConfig.getOptionValue(StateSavingMethod);
	helper = StateHelperFactory.getStateHelper(stateMode);
    }

    /* (non-Javadoc)
     * @see javax.faces.render.ResponseStateManager#isPostback(javax.faces.context.FacesContext)
     */
    @Override
    public boolean isPostback(FacesContext context) 
    {
	return context.getExternalContext().getRequestParameterMap().
		containsKey(ResponseStateManager.VIEW_STATE_PARAM);
    }

    /* (non-Javadoc)
     * @see javax.faces.render.ResponseStateManager#getCryptographicallyStrongTokenFromSession(javax.faces.context.FacesContext)
     */
    @Override
    public String getCryptographicallyStrongTokenFromSession(FacesContext context)
    {
	return helper.getCryptographicallyStrongTokenFromSession(context);
    }

    /* (non-Javadoc)
     * @see javax.faces.render.ResponseStateManager#getViewState(javax.faces.context.FacesContext, java.lang.Object)
     */
    @Override
    public String getViewState(FacesContext context, Object state) 
    {
	StringBuilder sb = new StringBuilder(32);
	try {
	    helper.writeState(context, state, sb);
	} catch (IOException e) {
	    throw new FacesException(e);
	}
	return sb.toString();
    }

    /* (non-Javadoc)
     * @see javax.faces.render.ResponseStateManager#getState(javax.faces.context.FacesContext, java.lang.String)
     */
    @Override
    public Object getState(FacesContext context, String viewId) 
    {
	Object state = RequestStateManager.get(context, RequestStateManager.FACES_VIEW_STATE);
	if (state == null) {
	    try {
		state = helper.getState(context, viewId);
		if (state != null) {
		    RequestStateManager.set(context, RequestStateManager.FACES_VIEW_STATE, state);
		}
	    } catch (IOException e) {
		throw new FacesException(e);
	    }
	}
	return state;
    }

    /* (non-Javadoc)
     * @see javax.faces.render.ResponseStateManager#writeState(javax.faces.context.FacesContext, java.lang.Object)
     */
    @Override
    public void writeState(FacesContext context, Object state) throws IOException 
    {
	helper.writeState(context, state, null);
    }

    /* (non-Javadoc)
     * @see javax.faces.render.ResponseStateManager#getTreeStructureToRestore(javax.faces.context.FacesContext, java.lang.String)
     */
    @Override
    public Object getTreeStructureToRestore(FacesContext context, String viewId)
    {
	Object[] state = (Object[]) getState(context, viewId);
	if (state != null) {
	    return state[0];
	}
	return null;

    }

    /* (non-Javadoc)
     * @see javax.faces.render.ResponseStateManager#isStateless(javax.faces.context.FacesContext, java.lang.String)
     */
    @Override
    public boolean isStateless(FacesContext facesContext, String viewId) 
    {
	return helper.isStateless(facesContext, viewId);
    }
}
