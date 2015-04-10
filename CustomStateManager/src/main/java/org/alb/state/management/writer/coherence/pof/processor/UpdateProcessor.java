package org.alb.state.management.writer.coherence.pof.processor;

import com.tangosol.util.InvocableMap.Entry;
import com.tangosol.util.processor.AbstractProcessor;

public class UpdateProcessor extends AbstractProcessor
{
    private static final long serialVersionUID = -2794685960613544802L;

    private Object[] state;
    
    public UpdateProcessor(Object[] state) {
	super();
	this.state = state;
    }

    @Override
    public Object process(Entry entry) 
    {
	entry.setValue(state);
	return state;
    }
}
