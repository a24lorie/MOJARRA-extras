package org.alb.state.management.writer;

/**
 * @author alorie
 *
 */
public interface StateWriter 
{
    /**
     * @param id
     * @param state
     */
    public void writeState(Object id, Object[] state);

    /**
     * @param id
     * @return
     */
    public Object[] readState(Object id);
}
