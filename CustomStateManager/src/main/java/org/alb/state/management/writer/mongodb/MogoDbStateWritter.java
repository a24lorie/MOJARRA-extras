package org.alb.state.management.writer.mongodb;

import java.net.UnknownHostException;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import org.alb.state.management.writer.StateWriter;

import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.mongodb.Mongo;

/**
 *
 * @author alorie
 */
public class MogoDbStateWritter implements StateWriter{

    private DBCollection dbCollection;

    public MogoDbStateWritter() throws UnknownHostException
    {
	Mongo mongo = new Mongo("127.0.0.1", 27017);
	DB db = mongo.getDB("jsf_db");
	dbCollection = db.getCollection(("jsf"));
    }

    /* (non-Javadoc)
     * @see org.alb.state.management.writer.StateWriter#writeStateArray(java.lang.Object, java.lang.Object[])
     */
    @Override
    public void writeState(Object id, Object[] state)
    {
	writeStateInner(id,state);
    }

    /* (non-Javadoc)
     * @see org.alb.state.management.writer.StateWriter#readStateArray(java.lang.Object)
     */
    @Override
    public Object[] readState(Object id) 
    {
	Object[] result= {};
	DBObject dbObj = readStateInner(id);
	if (dbObj != null) {
	    return ((BasicDBList)dbObj.get("state")).toArray(result);
	}
	return null;
    }

    /**
     * @param id
     * @return
     */
    private DBObject readStateInner(Object id)
    {
	BasicDBObject query = new BasicDBObject("stateId",id.toString());
	return dbCollection.findOne(query);
    }
    
    /**
     * @param id
     * @return
     */
    private void writeStateInner(Object id, Object state)
    {
	DBObject dbObj = readStateInner(id);
	if (dbObj != null) {
	    dbObj.put("state", state);
	    BasicDBObject query = new BasicDBObject("stateId",id.toString());
	    dbCollection.update(query, dbObj);
	}else{
	    //TTL Index        
	    BasicDBObject index = new BasicDBObject("date", 1);
	    BasicDBObject options = new BasicDBObject("expireAfterSeconds", TimeUnit.MINUTES.toSeconds(1));
	    dbCollection.ensureIndex(index, options);

	    BasicDBObject basicDBObject = new BasicDBObject();
	    basicDBObject.append("stateId", id);
	    basicDBObject.append("date", new Date());
	    basicDBObject.append("state", state);

	    dbCollection.insert(basicDBObject);
	}
    }
}
