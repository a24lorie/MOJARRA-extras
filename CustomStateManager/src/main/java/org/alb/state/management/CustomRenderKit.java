package org.alb.state.management;

import javax.faces.render.RenderKit;
import javax.faces.render.RenderKitWrapper;
import javax.faces.render.ResponseStateManager;

/**
 *
 * @author Leonard
 */
public class CustomRenderKit extends RenderKitWrapper 
{
    private RenderKit renderKit;
    private ResponseStateManager responseStateManager = new CustomResponseStateManager();

    public CustomRenderKit(RenderKit renderKit) {
        this.renderKit = renderKit;
    }

    @Override
    public synchronized ResponseStateManager getResponseStateManager() {
        
        if (responseStateManager == null) {
            responseStateManager = new CustomResponseStateManager();
        }
        return responseStateManager;
    }

    @Override
    public RenderKit getWrapped() {
        return renderKit;
    }
}
