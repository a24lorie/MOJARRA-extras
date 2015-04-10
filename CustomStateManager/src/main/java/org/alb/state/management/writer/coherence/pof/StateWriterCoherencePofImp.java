package org.alb.state.management.writer.coherence.pof;

import org.alb.state.management.writer.StateWriter;
import org.alb.state.management.writer.coherence.pof.processor.UpdateProcessor;

import com.tangosol.net.CacheFactory;
import com.tangosol.net.NamedCache;

public class StateWriterCoherencePofImp implements StateWriter
{
    private static StateWriterCoherencePofImp instance; 
    private NamedCache cache;
    
    /**
     * 
     */
    public StateWriterCoherencePofImp()
    {
	super();
	CacheFactory.ensureCluster();
	cache = CacheFactory.getCache("jsf_state_cache");
    }

    public static StateWriterCoherencePofImp getInstance(){
	if(instance == null){
	    instance = new StateWriterCoherencePofImp();
	}

	return instance;
    }
    
    @Override
    public void writeState(Object id, Object[] state)
    {
	Object dbObj = readState(id);
	if (dbObj != null) {
	    cache.invoke(id, new UpdateProcessor(state));
	}else{
	    cache.put(id, state);
	}
    }

    @Override
    public Object[] readState(Object id) {
	return (Object[]) cache.get(id);
    }
}
