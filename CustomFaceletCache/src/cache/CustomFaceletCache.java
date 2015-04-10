package cache;

import java.io.IOException;
import java.net.URL;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.faces.FacesException;
import javax.faces.view.facelets.Facelet;
import javax.faces.view.facelets.FaceletCache;

import cache.CustomExpiringConcurrentCache.ExpiryChecker;

import com.sun.faces.util.ConcurrentCache;
import com.sun.faces.util.FacesLogger;
import com.sun.faces.util.Util;

/**
 * com.renta4.r4j2ee.jsf.arq.cache
 * @author Renta 4
 *
 */
public class CustomFaceletCache extends FaceletCache<Facelet> {

    private final static Logger log = FacesLogger.FACELETS_FACTORY.getLogger();

    private final CustomExpiringConcurrentCache<URL, Record> _faceletCache;
    private final CustomExpiringConcurrentCache<URL, Record> _metadataFaceletCache;

    private static CustomFaceletCache instance; 
   
    // -------------------------------------------------------------------------
    // CONTRUCTOR(S) -----------------------------------------------------------
    // -------------------------------------------------------------------------
    
    private CustomFaceletCache() {

	// We will be delegating object storage to the ExpiringCocurrentCache
	// Create Factory objects here for the cache. The objects will be delegating to our
	// own instance factories

	ConcurrentCache.Factory<URL, Record> faceletFactory = new ConcurrentCache.Factory<URL, Record>() {
	    public Record newInstance(final URL key) throws IOException {
		return new Record(getMemberFactory().newInstance(key), System.currentTimeMillis());
	    }
	};

	ConcurrentCache.Factory<URL, Record> metadataFaceletFactory = new ConcurrentCache.Factory<URL, Record>() {
	    public Record newInstance(final URL key) throws IOException {
		return new Record(getMetadataMemberFactory().newInstance(key), System.currentTimeMillis());
	    }
	};

	CustomExpiringConcurrentCache.ExpiryChecker<URL, Record> checker = new R4ModifyExpireChecker();
	_faceletCache = new CustomExpiringConcurrentCache<URL, Record>(faceletFactory, (ExpiryChecker<URL, Record>) checker);
	_metadataFaceletCache = new CustomExpiringConcurrentCache<URL, Record>(metadataFaceletFactory, (ExpiryChecker<URL, Record>) checker);
    }

    // -------------------------------------------------------------------------
    // METODOS ACCESO ----------------------------------------------------------
    // -------------------------------------------------------------------------

    /**
     * @return
     */
    public static CustomFaceletCache getInstance()
    {
	if(instance == null){
	    instance = new CustomFaceletCache();
	}
	
	return instance;
    }
    
    /**
     * @return
     */
    public Set<Entry<URL, Future<Record>>> getFaceletCacheKeySet() {
	return this.getExpiringCacheKeySet(_faceletCache);
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.faces.view.facelets.FaceletCache#getFacelet(java.net.URL)
     */
    @Override
    public Facelet getFacelet(URL url) throws IOException {
	com.sun.faces.util.Util.notNull("url", url);
	Facelet f = null;

	try {
	    f = _faceletCache.get(url).getFacelet();
	} catch (ExecutionException e) {
	    _unwrapIOException(e);
	}
	return f;
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.faces.view.facelets.FaceletCache#isFaceletCached(java.net.URL)
     */
    @Override
    public boolean isFaceletCached(URL url) {
	com.sun.faces.util.Util.notNull("url", url);

	return _faceletCache.containsKey(url);
    }

    /**
     * @return
     */
    public Set<?> getViewMetadataFaceletCacheKeySet() {
	return this.getExpiringCacheKeySet(_metadataFaceletCache);
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.faces.view.facelets.FaceletCache#getViewMetadataFacelet(java.net.URL)
     */
    @Override
    public Facelet getViewMetadataFacelet(URL url) throws IOException {
	com.sun.faces.util.Util.notNull("url", url);

	Facelet f = null;

	try {
	    f = _metadataFaceletCache.get(url).getFacelet();
	} catch (ExecutionException e) {
	    _unwrapIOException(e);
	}
	return f;
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.faces.view.facelets.FaceletCache#isViewMetadataFaceletCached(java.net.URL)
     */
    @Override
    public boolean isViewMetadataFaceletCached(URL url) {
	com.sun.faces.util.Util.notNull("url", url);

	return _metadataFaceletCache.containsKey(url);
    }

    /**
     * @param objectCache
     * @return
     */
    private Set<Entry<URL, Future<Record>>> getExpiringCacheKeySet(CustomExpiringConcurrentCache<URL, Record> objectCache)
    {
	ConcurrentMap<URL, Future<Record>> cache = objectCache._getCache();

	if (cache != null) {
	    return cache.entrySet();
	}

	return null;
    }

    // -------------------------------------------------------------------------
    // FUNCIONALIDAD PROPIA PRIVADA --------------------------------------------
    // -------------------------------------------------------------------------

    /**
     * @param e
     * @throws IOException
     */
    private void _unwrapIOException(ExecutionException e) throws IOException {
	Throwable t = e.getCause();
	if (t instanceof IOException) {
	    throw (IOException) t;
	}
	if (t.getCause() instanceof IOException) {
	    throw (IOException) t.getCause();
	}
	if (t instanceof RuntimeException) {
	    throw (RuntimeException) t;
	}
	throw new FacesException(t);
    }

    /**
     * @param url
     * @param objectCache
     * @throws Exception
     */
    private void removefaceletCacheKey(URL url, CustomExpiringConcurrentCache<URL, Record> objectCache) throws Exception {

	ConcurrentMap<URL, Future<Record>> cache = objectCache._getCache();

	if (cache != null) {
	    while (true) {
		Future<Record> f = cache.get(url);
		if (f != null) {
		    try {
			Record obj = f.get();
			obj.set_markReload(true);
			cache.remove(url, f);
		    } catch (CancellationException ce) {
			log.log(Level.SEVERE, ce.toString(), ce);
			cache.remove(url, f);
		    } catch (ExecutionException ee) {
			cache.remove(url, f);
			throw ee;
		    } catch (InterruptedException ie) {
			throw new FacesException(ie);
		    }
		} else {
		    break;
		}
	    }
	}
    }

    // -------------------------------------------------------------------------
    // FUNCIONALIDAD PROPIA PUBLICA --------------------------------------------
    // -------------------------------------------------------------------------

    /**
     * @param url
     * @throws Exception
     */
    public void removeFaceletFromCache(URL url) throws Exception {
	this.removefaceletCacheKey(url, _faceletCache);
	this.removefaceletCacheKey(url, _metadataFaceletCache);
    }

    /**
     * This class holds the Facelet instance and its original URL's last modified time. It also produces the time when the next expiry check should be performed
     */
    public static class Record {
	Record(Facelet facelet, long creationTime) {
	    _facelet = facelet;
	    _creationTime = creationTime;
	    _lastAccess = new AtomicLong(_creationTime); // record creation time
	    _markReload = new AtomicBoolean(false); // marked to reload
	}

	Facelet getFacelet() {
	    return _facelet;
	}

	public long getLastAccess() {
	    return _lastAccess.get();
	}

	void updateLastAccess(long lastAccess) {
	    _lastAccess.set(lastAccess);
	}

	/**
	 * @return the _markReload
	 */
	public boolean is_markReload() {
	    return _markReload.get();
	}

	/**
	 * @param _markReload
	 *            the _markReload to set
	 */
	public void set_markReload(boolean _markReload) {
	    this._markReload.set(_markReload);
	}

	/**
	 * @param url
	 * @return
	 */
	public boolean isModified(URL url) {
	    long filelastModified = Util.getLastModified(url);
	    return filelastModified > getLastAccess();
	}

	private final long _creationTime;
	private final AtomicLong _lastAccess;
	private final Facelet _facelet;
	private final AtomicBoolean _markReload;
    }

    private static class R4ModifyExpireChecker implements CustomExpiringConcurrentCache.ExpiryChecker<URL, Record> {

	public boolean isExpired(URL url, Record record) {
	    if (record.is_markReload()) {
		record.updateLastAccess(Util.getLastModified(url));
	    }
	    return record.is_markReload();
	}
    }

    // -----------------------------------------------------------------------------
    // -----------------------------------------------------------------------------
    // -----------------------------------------------------------------------------
}
