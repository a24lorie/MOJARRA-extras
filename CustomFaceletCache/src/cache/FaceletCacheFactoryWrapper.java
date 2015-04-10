package cache;

import java.util.logging.Level;
import java.util.logging.Logger;

import javax.faces.view.facelets.Facelet;
import javax.faces.view.facelets.FaceletCache;
import javax.faces.view.facelets.FaceletCacheFactory;

import com.sun.faces.config.WebConfiguration;
import com.sun.faces.facelets.impl.FaceletCacheFactoryImpl;
import com.sun.faces.util.FacesLogger;

/**
 * com.renta4.r4j2ee.jsf.arq.cache
 * @author Renta 4
 *
 */
public class FaceletCacheFactoryWrapper extends FaceletCacheFactoryImpl {
    protected final static Logger log = FacesLogger.FACELETS_FACTORY.getLogger();

    protected final static int R4_CUSTOM_CACHE = -2;

    /*
     * (non-Javadoc)
     * 
     * @see com.sun.faces.facelets.impl.FaceletCacheFactoryImpl#getFaceletCache()
     */
    @Override
    public FaceletCache<?> getFaceletCache() {
	log.log(Level.FINE, "getFaceletCache");
	WebConfiguration webConfig = WebConfiguration.getInstance();
	String refreshPeriod = webConfig.getOptionValue(WebConfiguration.WebContextInitParameter.FaceletsDefaultRefreshPeriod);

	if (R4_CUSTOM_CACHE == Integer.parseInt(refreshPeriod)) {
	    FaceletCache<Facelet> result = CustomFaceletCache.getInstance();
	    return result;
	} else {
	    return super.getFaceletCache();
	}
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.faces.view.facelets.FaceletCacheFactory#getWrapped()
     */
    @Override
    public FaceletCacheFactory getWrapped() {
	log.log(Level.FINE, "getWrapped");
	return super.getWrapped();
    }

}
