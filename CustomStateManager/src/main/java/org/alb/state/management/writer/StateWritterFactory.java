package org.alb.state.management.writer;

import java.net.UnknownHostException;

import org.alb.state.management.writer.coherence.StateWriterCoherenceImp;
import org.alb.state.management.writer.mongodb.MogoDbStateWritter;


public class StateWritterFactory {
    /**
     * @param typeHelper
     * @return
     */
    public static StateWriter getStateHelper(String typeHelper)
    {
	switch (typeHelper) {
	case "mongodb":
	    try {
		return (StateWriter) new MogoDbStateWritter();
	    } catch (UnknownHostException e) {
		return null;
	    }
	case "coherence":
	    return StateWriterCoherenceImp.getInstance();
	case "coherencePof":
	    return StateWriterCoherenceImp.getInstance();
	default:
	    return null;
	}
    }
}
