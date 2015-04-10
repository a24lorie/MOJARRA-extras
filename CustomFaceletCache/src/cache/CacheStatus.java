package cache;

import java.io.Serializable;
import java.net.URL;

/**
 * @author alorie
 *
 */
public class CacheStatus implements Serializable 
{
    private static final long serialVersionUID = -480294459354879481L;
    
    private URL key;
    private boolean isModified;

    /**
     * @param paramKey
     * @param paramModified
     */
    public CacheStatus(URL paramKey, boolean paramModified) {
	key = paramKey;
	isModified = paramModified;
    }

    /**
     * @return the key
     */
    public URL getKey() {
	return key;
    }

    /**
     * @param key
     *            the key to set
     */
    public void setKey(URL key) {
	this.key = key;
    }

    /**
     * @return the isModified
     */
    public boolean isModified() {
	return isModified;
    }

    /**
     * @param isModified
     *            the isModified to set
     */
    public void setModified(boolean isModified) {
	this.isModified = isModified;
    }
}
