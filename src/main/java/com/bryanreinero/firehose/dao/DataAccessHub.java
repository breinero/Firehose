package com.bryanreinero.firehose.dao;

import java.util.HashMap;
import java.util.Map;

import com.bryanreinero.firehose.circuitbreaker.BreakerBox;
import com.bryanreinero.firehose.metrics.Interval;
import com.bryanreinero.firehose.metrics.SampleSet;
import com.mongodb.MongoClient;

public class DataAccessHub implements DAOService {
	
	private final Map<String, MongoClient> clusters = new HashMap<String, MongoClient>();
	private final BreakerBox circuitBreakers = new BreakerBox();
	private final Map<String, DataAccessObject> daos = new HashMap<String, DataAccessObject>();
	private final SampleSet samples;
	
	public void addCluster( String key, MongoClient c) {
		clusters.put(key, c);
	}

	public DataAccessHub( SampleSet samples ) {
		this.samples = samples;
	}
	
	@Override
	public Object execute(String key, Map<String, Object> request)
			throws DAOException {
		
		if( circuitBreakers.isTripped(key) ) 
			throw new DAOException("Breaker "+key+" tripped");
		
		DataAccessObject dao = daos.get(key);
		if ( daos != null ) {
			Interval i = samples.set(key);
			Object o = dao.execute(request);
			i.mark();
			return o;
		}
		return null;
	}

	@Override
	public void setDataAccessObject(String key, DataAccessObject dao) {
		daos.put(key, dao);
	}

	MongoClient getCluster(String clusterName) {
		return clusters.get( clusterName );
	}

	public void setCircuitBreaker(String key, String type, Double value) {
		circuitBreakers.setBreaker(key, type, value);
	}
}
