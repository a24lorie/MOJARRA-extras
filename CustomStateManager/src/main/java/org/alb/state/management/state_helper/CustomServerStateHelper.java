package org.alb.state.management.state_helper;

import static com.sun.faces.config.WebConfiguration.BooleanWebContextInitParameter.AutoCompleteOffOnViewState;
import static com.sun.faces.config.WebConfiguration.BooleanWebContextInitParameter.EnableViewStateIdRendering;

import java.io.IOException;
import java.io.Writer;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import javax.faces.component.NamingContainer;
import javax.faces.component.UIViewRoot;
import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;
import javax.faces.render.ResponseStateManager;
import javax.servlet.http.HttpServletRequest;

import org.alb.state.management.writer.StateWriter;
import org.alb.state.management.writer.StateWritterFactory;

import com.sun.faces.renderkit.ServerSideStateHelper;
import com.sun.faces.util.RequestStateManager;
import com.sun.faces.util.Util;

/**
 *
 * @author alorie
 */
public class CustomServerStateHelper extends ServerSideStateHelper {

    private static final String MANAGEMENT_VIEW_STATE = "org.alb.writer.viewState";

    StateWriter writer;

    /**
     * 
     */
    public CustomServerStateHelper(String writerType) 
    {
	super();
	writer = StateWritterFactory.getStateHelper(writerType);
    }

    /* (non-Javadoc)
     * @see com.sun.faces.renderkit.ServerSideStateHelper#getState(javax.faces.context.FacesContext, java.lang.String)
     */
    @Override
    public Object getState(FacesContext ctx, String viewId) {

	String stateId = ServerSideStateHelper.getStateParamValue(ctx);

	if (stateId == null) {
	    return null;
	}

	if ("stateless".equals(stateId)) {
	    return "stateless";
	} else {
	    Object[] state = (Object[]) writer.readState(stateId);
	    Object[] restoredState = new Object[2];

	    restoredState[0] = state[0];
	    restoredState[1] = state[1];

	    if(state != null)
	    {
		if (state.length == 2 && state[1] != null) {
		    restoredState[1] = handleRestoreState(state[1]);
		}
	    }

	    return restoredState;
	}
    }

    /* (non-Javadoc)
     * @see com.sun.faces.renderkit.ServerSideStateHelper#writeState(javax.faces.context.FacesContext, java.lang.Object, java.lang.StringBuilder)
     */
    @Override
    public void writeState(FacesContext ctx,Object state, StringBuilder stateCapture) throws IOException 
    {
	UIViewRoot viewRoot = ctx.getViewRoot();
	Object stateId;

	if (!viewRoot.isTransient()) 
	{
	    String stateString = (String) ((HttpServletRequest)ctx.getExternalContext().getRequest()).getAttribute(MANAGEMENT_VIEW_STATE);
	    stateId = ServerSideStateHelper.getStateParamValue(ctx);

	    if (stateString == null) {
		Util.notNull("state", state);
		Object[] stateToWrite = (Object[]) state;

		Object structure = stateToWrite[0];
		Object savedState = handleSaveState(stateToWrite[1]);

		if(stateId == null)
		{
		    String idInLogicalMap = (String) RequestStateManager.get(ctx, RequestStateManager.LOGICAL_VIEW_MAP);
		    if (idInLogicalMap == null) {
			idInLogicalMap = ((generateUniqueStateIds) ? createRandomId() : createIncrementalRequestId(ctx));
		    }
		    
		    String idInActualMap = null;
		    if (ctx.getPartialViewContext().isPartialRequest()) {
			// If partial request, do not change actual view Id, because page not actually changed.
			// Otherwise partial requests will soon overflow cache with values that would be never used.
			idInActualMap = (String) RequestStateManager.get(ctx, RequestStateManager.ACTUAL_VIEW_MAP);
		    }
		    if (null == idInActualMap) {
			idInActualMap = ((generateUniqueStateIds) ? createRandomId() : createIncrementalRequestId(ctx));
		    }

		    stateId = idInLogicalMap + ':' + idInActualMap; 
		}

		writer.writeState(stateId,new Object[]{ structure, savedState });

		((HttpServletRequest)ctx.getExternalContext().getRequest()).setAttribute(MANAGEMENT_VIEW_STATE, stateId);
	    }
	}
	else {
	    stateId = "stateless";
	}

	if (stateCapture != null) {
	    stateCapture.append(stateId);
	} else {
	    ResponseWriter writer = ctx.getResponseWriter();

	    writer.startElement("input", null);
	    writer.writeAttribute("type", "hidden", null);

	    String viewStateParam = ResponseStateManager.VIEW_STATE_PARAM;

	    if ((namespaceParameters) && (viewRoot instanceof NamingContainer)) {
		String namingContainerId = viewRoot.getContainerClientId(ctx);
		if (namingContainerId != null) {
		    viewStateParam = namingContainerId + viewStateParam;
		}
	    }
	    writer.writeAttribute("name", viewStateParam, null);
	    if (webConfig.isOptionEnabled(EnableViewStateIdRendering)) {
		String viewStateId = Util.getViewStateId(ctx);
		writer.writeAttribute("id", viewStateId, null);
	    }
	    writer.writeAttribute("value", stateId, null);
	    if (webConfig.isOptionEnabled(AutoCompleteOffOnViewState)) {
		writer.writeAttribute("autocomplete", "off", null);
	    }
	    writer.endElement("input");

	    writeClientWindowField(ctx, writer); 
	    writeRenderKitIdField(ctx, writer);
	}
    }

    @Override
    public boolean isStateless(FacesContext ctx, String viewId) throws IllegalStateException 
    {
	if (ctx.isPostback()) 
	{
	    return "stateless".equals(ServerSideStateHelper.getStateParamValue(ctx));
	}
	throw new IllegalStateException("Cannot determine whether or not the request is stateless");
    }

    /**
     * @param ctx the <code>FacesContext</code> for the current request
     * @return a unique ID for building the keys used to store
     *  views within a session
     */
    private String createIncrementalRequestId(FacesContext ctx) {

	Map<String, Object> sm = ctx.getExternalContext().getSessionMap();
	AtomicInteger idgen = (AtomicInteger) sm.get(STATEMANAGED_SERIAL_ID_KEY);
	if (idgen == null) {
	    idgen = new AtomicInteger(1);
	}

	// always call put/setAttribute as we may be in a clustered environment.
	sm.put(STATEMANAGED_SERIAL_ID_KEY, idgen);
	return (UIViewRoot.UNIQUE_ID_PREFIX + idgen.getAndIncrement());

    }

    private String createRandomId() {
	return Long.valueOf(random.nextLong()).toString();
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
	public Writer append(CharSequence csq, int start, int end) throws IOException {
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
	public void flush() throws IOException {}

	@Override
	public void close() throws IOException {}
    } // END StringBuilderWriter

    // -------------------------------------------------------------------------
    // -------------------------------------------------------------------------
    // -------------------------------------------------------------------------
}
