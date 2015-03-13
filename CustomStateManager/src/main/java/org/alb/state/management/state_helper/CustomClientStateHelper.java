package org.alb.state.management.state_helper;

import static com.sun.faces.config.WebConfiguration.BooleanWebContextInitParameter.AutoCompleteOffOnViewState;
import static com.sun.faces.config.WebConfiguration.BooleanWebContextInitParameter.EnableViewStateIdRendering;

import java.io.IOException;
import java.io.Writer;
import java.util.Random;

import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;
import javax.faces.render.ResponseStateManager;
import javax.servlet.http.HttpServletRequest;

import org.alb.state.management.writer.StateWriter;
import org.alb.state.management.writer.StateWritterFactory;

import com.sun.faces.renderkit.ClientSideStateHelper;
import com.sun.faces.renderkit.ServerSideStateHelper;
import com.sun.faces.util.Util;

/**
 *
 * @author alorie
 */
public class CustomClientStateHelper extends ClientSideStateHelper {
    
    private static final String MANAGEMENT_VIEW_STATE = "org.alb.writer.viewState";
    
    StateWriter stateWritter;
    
    public CustomClientStateHelper(String writerType) {
	super();
        stateWritter = StateWritterFactory.getStateHelper(writerType);
    }

    @Override
    public Object getState(FacesContext ctx, String viewId) throws IOException {

        String stateId = ClientSideStateHelper.getStateParamValue(ctx);
        String state;
        
        if (stateId == null) {
            return null;
        }

        if ("stateless".equals(stateId)) {
            return "stateless";
        } else {
            state = stateWritter.readState(stateId).toString();
            if (state == null) {
                return null;
            }
        }

        return doGetState(state);
    }

    @Override
    public void writeState(FacesContext ctx,Object state, StringBuilder stateCapture) throws IOException 
    {
	String stateString = (String) ((HttpServletRequest)ctx.getExternalContext().getRequest()).getAttribute(MANAGEMENT_VIEW_STATE);

	if(stateString==null)
	{
	    StringBuilder stateBuilder = new StringBuilder();
	    doWriteState(ctx, state, new StringBuilderWriter(stateBuilder));
	    
	    stateString = ServerSideStateHelper.getStateParamValue(ctx);
	    if(stateString== null)
		stateString = ((HttpServletRequest) ctx.getExternalContext().getRequest()).getSession().getId() + ":" + createRandomId();
	    
	    stateWritter.writeState(stateString,stateBuilder.toString());
	    
	    ((HttpServletRequest)ctx.getExternalContext().getRequest()).setAttribute(MANAGEMENT_VIEW_STATE, stateString);
	}

        if (stateCapture != null) {
            stateCapture.append(stateString);
        } else {
            ResponseWriter writer = ctx.getResponseWriter();

            writer.startElement("input", null);
            writer.writeAttribute("type", "hidden", null);
            writer.writeAttribute("name", ResponseStateManager.VIEW_STATE_PARAM, null);
            if (webConfig.isOptionEnabled(EnableViewStateIdRendering)) {
                String viewStateId = Util.getViewStateId(ctx);
                writer.writeAttribute("id", viewStateId, null);
            }

            if (stateString != null) {
                writer.writeAttribute("value", stateString, null);
            } 
            
            if (webConfig.isOptionEnabled(AutoCompleteOffOnViewState)) {
                writer.writeAttribute("autocomplete", "off", null);
            }
            writer.endElement("input");

            writeClientWindowField(ctx, writer);
            writeRenderKitIdField(ctx, writer);
        }
    }

    @Override
    protected Object doGetState(String stateString) {
        return super.doGetState(stateString);
    }

    @Override
    protected void doWriteState(FacesContext facesContext, Object state, Writer writer) throws IOException {
        super.doWriteState(facesContext, state, writer);
    }

    @Override
    protected boolean hasStateExpired(long stateTime) {
        return super.hasStateExpired(stateTime);
    }

    @Override
    public boolean isStateless(FacesContext ctx, String viewId) throws IllegalStateException {
	if (ctx.isPostback()) 
	{
	    return "stateless".equals(ServerSideStateHelper.getStateParamValue(ctx));
	}
	throw new IllegalStateException("Cannot determine whether or not the request is stateless");
    }

    /**
     * @return
     */
    private String createRandomId() {
	return Long.valueOf(new Random().nextLong()).toString();
    }

    /**
     * A simple
     * <code>Writer</code> implementation to encapsulate a
     * <code>StringBuilder</code> instance.
     */
    protected static final class StringBuilderWriter extends Writer {

        private StringBuilder sb;

        // -------------------------------------------------------- Constructors
        protected StringBuilderWriter(StringBuilder sb) {

            this.sb = sb;

        }

        // ------------------------------------------------- Methods from Writer
        @Override
        public void write(int c) throws IOException {

            sb.append((char) c);

        }

        @Override
        public void write(char cbuf[]) throws IOException {

            sb.append(cbuf);

        }

        @Override
        public void write(String str) throws IOException {

            sb.append(str);

        }

        @Override
        public void write(String str, int off, int len) throws IOException {

            sb.append(str.toCharArray(), off, len);

        }

        @Override
        public Writer append(CharSequence csq) throws IOException {

            sb.append(csq);
            return this;

        }

        @Override
        public Writer append(CharSequence csq, int start, int end)
                throws IOException {

            sb.append(csq, start, end);
            return this;

        }

        @Override
        public Writer append(char c) throws IOException {

            sb.append(c);
            return this;

        }

        @Override
        public void write(char cbuf[], int off, int len) throws IOException {

            sb.append(cbuf, off, len);

        }

        @Override
        public void flush() throws IOException {
            //no-op
        }

        @Override
        public void close() throws IOException {
            //no-op
        }
    } // END StringBuilderWriter
}
