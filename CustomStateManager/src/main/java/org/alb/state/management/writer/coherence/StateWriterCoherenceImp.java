package org.alb.state.management.writer.coherence;

import java.io.Serializable;

import org.alb.state.management.writer.StateWriter;

import com.sun.faces.util.Timer;
import com.tangosol.net.CacheFactory;
import com.tangosol.net.NamedCache;

/**
 *
 * @author alorie
 */	
public class StateWriterCoherenceImp implements StateWriter, Serializable
{
    private static final long serialVersionUID = 2167492606185062311L;

    private static StateWriterCoherenceImp instance; 
    private NamedCache cache;

    /**
     * 
     */
    public StateWriterCoherenceImp()
    {
	super();
	CacheFactory.ensureCluster();
	cache = CacheFactory.getCache("jsf_state_cache");
    }

    public static StateWriterCoherenceImp getInstance(){
	if(instance == null){
	    instance = new StateWriterCoherenceImp();
	}

	return instance;
    }

    /* (non-Javadoc)
     * @see org.alb.state.management.writer.StateWriter#readStateArray(java.lang.Object)
     */
    @Override
    public Object[] readState(Object id) 
    {
	Object[] result=null;

	if(id instanceof String)
	{
	    Timer timer = Timer.getInstance();
	    if (timer != null) {
		timer.startTiming();
	    }

	    result = (Object[]) cache.get(id);

	    // stop timing
	    if (timer != null) {
		timer.stopTiming();
		timer.logResult("Execution time for cache.get");
	    }
	}
	return result;
    }

    @Override
    public void writeState(Object id, Object[] state) 
    {
	Timer timer = Timer.getInstance();

	if (timer != null) {
	    timer.startTiming();
	}

	cache.put(id, state);
	if (timer != null) {
	    timer.stopTiming();
	    timer.logResult("Execution time for cache.put");
	}
    }
}
