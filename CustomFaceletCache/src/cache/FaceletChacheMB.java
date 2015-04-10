package cache;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.RequestScoped;
import javax.faces.event.ActionEvent;
import javax.faces.view.facelets.Facelet;
import javax.faces.view.facelets.FaceletCache;

import cache.CustomFaceletCache.Record;

/**
 * @author renta4
 *
 */
@ManagedBean
@RequestScoped
public class FaceletChacheMB 
{

    // -------------------------------------------------------------------------
    // ATTRIBUTOS --------------------------------------------------------------
    // -------------------------------------------------------------------------

    // ViewParams
    private String opp;
    private String cacheId;

    // LocalStorageAttributes
    private List<CacheStatus> cacheListStatus;

    // -------------------------------------------------------------------------
    // CONTRUCTOR(S) -----------------------------------------------------------
    // -------------------------------------------------------------------------
    public FaceletChacheMB() {
	cacheListStatus = new ArrayList<CacheStatus>();
    }

    // -------------------------------------------------------------------------
    // EVENTS ------------------------------------------------------------------
    // -------------------------------------------------------------------------

    /**
     * @param event
     */
    public void loadFaceletCacheAction(ActionEvent event) {
	System.out.println("Entra >>> loadFaceletCacheAction");

	System.out.println( "\t viewParam -> opp = " + opp);
	System.out.println( "\t viewParam -> cacheId = " + cacheId);

	if (!"".equals(opp)) {
	    if ("CNSLT".equals(opp)) {
		try {
		    FaceletCache<Facelet> faceletCache = CustomFaceletCache.getInstance();
		    if (faceletCache instanceof CustomFaceletCache) {
			((CustomFaceletCache) faceletCache).removeFaceletFromCache(new URL(cacheId));
		    }
		} catch (Exception e) {
		    // TODO mostrar mensaje de error
		}
	    }

	    loadCacheList(CustomFaceletCache.getInstance());
	}

	System.out.println( "Sale <<< loadFaceletCacheAction");
    }

    // -------------------------------------------------------------------------
    // FUNCIONALIDAD PROPIA ----------------------------------------------------
    // -------------------------------------------------------------------------

    private void loadCacheList(FaceletCache<Facelet> faceletCache) {
	System.out.println( "Entra >>> loadCacheList");

	if (faceletCache instanceof CustomFaceletCache) {
	    Set<Entry<URL, Future<Record>>> faceletCacheKeys = ((CustomFaceletCache) faceletCache).getFaceletCacheKeySet();
	    for (Map.Entry<URL, Future<Record>> entry : faceletCacheKeys) {
		try {
		    URL key = entry.getKey();
		    cacheListStatus.add(new CacheStatus(key, entry.getValue().get().isModified(key)));
		    System.out.println( "\t -> Cache Item: " + key.toString());
		} catch (ClassCastException clssCastExc) {
		    // IGNORE
		} catch (InterruptedException interrExc) {
		    // IGNORE
		} catch (ExecutionException execExc) {
		    // IGNORE
		}
	    }
	}
	System.out.println( "Sale <<< loadCacheList");
    }

    // -------------------------------------------------------------------------
    // METODOS ACCESO ----------------------------------------------------------
    // -------------------------------------------------------------------------

    /**
     * @return the cacheId
     */
    public String getCacheId() {
	return cacheId;
    }

    /**
     * @param cacheId
     *            the cacheId to set
     */
    public void setCacheId(String cacheId) {
	this.cacheId = cacheId;
    }

    /**
     * @return the opp
     */
    public String getOpp() {
	return opp;
    }

    /**
     * @param opp
     *            the opp to set
     */
    public void setOpp(String opp) {
	this.opp = opp;
    }

    /**
     * @return the cacheListStatus
     */
    public List<CacheStatus> getCacheListStatus() {
	return cacheListStatus;
    }

    // -----------------------------------------------------------------------------
    // -----------------------------------------------------------------------------
    // -----------------------------------------------------------------------------
}
