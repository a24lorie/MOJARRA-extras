package cache;
import java.util.concurrent.Callable;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.faces.FacesException;

import com.sun.faces.util.ConcurrentCache;
import com.sun.faces.util.FacesLogger;

public final class CustomExpiringConcurrentCache <K, V> extends ConcurrentCache<K, V> 
{
    private static final Logger _LOGGER = FacesLogger.UTIL.getLogger();
    
    /**
     * Interface for checking whether a cached object expired
     */
    public interface ExpiryChecker<K, V>{
	/**
	 * Checks whether a cached object expired
	 * @param key cache key
	 * @param value cached value
	 * @return true if the value expired and should be removed from the cache, false otherwise
	 */
	public boolean isExpired(K key, V value);
    }

    protected final ExpiryChecker<K, V> _checker;
    protected final ConcurrentMap<K, Future<V>> _cache = new ConcurrentHashMap<K, Future<V>>();
    
    /**
     * Public constructor.
     * @param f used to create new instances of objects that are not already available
     * @param checker used to check whether an object in the cache has expired
     */
    public CustomExpiringConcurrentCache(Factory<K, V> f, ExpiryChecker<K, V> checker) {
	super(f);
	_checker = checker;
    }

    @Override
    public V get(final K key) throws ExecutionException {
	// This method uses a design pattern from "Java concurrency in practice".
	// The pattern ensures that only one thread gets to create an object  missing in the cache,
	// while the all the other threads tring to get it are waiting
	while (true) {
	    boolean newlyCached = false;

	    Future<V> f = _cache.get(key);
	    if (f == null) {
		Callable<V> callable = new Callable<V>() {
		    public V call() throws Exception {
			return getFactory().newInstance(key);
		    }
		};
		FutureTask<V> ft = new FutureTask<V>(callable);
		// here is the real beauty of the concurrent utilities.
		// 1.  putIfAbsent() is atomic
		// 2.  putIfAbsent() will return the value already associated
			//     with the specified key
		// So, if multiple threads make it to this point
		// they will all be calling f.get() on the same
		// FutureTask instance, so this guarantees that the instances
		// that the invoked Callable will return will be created once
		f = _cache.putIfAbsent(key, ft);
		if (f == null) {
		    f = ft;
		    ft.run();
		    newlyCached = true;
		}
	    }
	    try {
		V obj = f.get();
		if (!newlyCached && _getExpiryChecker().isExpired(key, obj)) {

		    // Note that we are using both key and value in remove() call to ensure
		    // that we are not removing the Future added after expiry check by a different thread
		    _cache.remove(key, f);
		}
		else {
		    return obj;
		}
	    } catch (CancellationException ce) {
		if (_LOGGER.isLoggable(Level.SEVERE)) {
		    _LOGGER.log(Level.SEVERE,
			    ce.toString(),
			    ce);
		}
		_cache.remove(key, f);
	    } catch (ExecutionException ee) {
		_cache.remove(key, f);
		throw ee;
	    } catch (InterruptedException ie) {                 
		throw new FacesException(ie); 

	    }
	}  
    }

    @Override
    public boolean containsKey(final K key) {

	Future<V> f = _cache.get(key);

	if (f != null && f.isDone() && !f.isCancelled()) {

	    try {
		// Call get() with a 0 timeout to avoid any wait
		V obj = f.get(0, TimeUnit.MILLISECONDS);
		if (_getExpiryChecker().isExpired(key, obj)) {

		    // Note that we are using both key and value in remove() call to ensure
		    // that we are not removing the Future added after expiry check by a different thread
		    _cache.remove(key, f);
		} else {

		    return true;
		}
	    } catch (TimeoutException ce) {
		// do nothing. This just indicates that the object is not yet ready
	    } catch (CancellationException ce) {
		if (_LOGGER.isLoggable(Level.SEVERE)) {
		    _LOGGER.log(Level.SEVERE, ce.toString(), ce);
		}
	    } catch (InterruptedException ie) {
		throw new FacesException(ie);

	    } catch (ExecutionException ee) {
		// Do nothing - the FutureTask will be removed by the thread that called get() on this class
	    }
	}

	return false;
    }


    public ExpiryChecker<K, V> _getExpiryChecker() {
	return _checker;
    }
    
    public  ConcurrentMap<K, Future<V>> _getCache() {
   	return _cache;
    }
}
