package com.bryanreinero.firehose.dao.mongo;

import com.bryanreinero.util.Operation;
import com.bryanreinero.util.OperationDescriptor;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;

/**
 * Created by bryan on 10/24/15.
 */
public class MongoDAO<T, K extends Operation > extends OperationDescriptor {

    private MongoDatabase database;
    private MongoCollection<T> collection;
    private final String cluster;
    private final String dbName;
    private final String collectionName;

    public MongoDAO(String name, String cluster, String namespace) {
        super( name );
        this.cluster = cluster;
        String[] nameSpaceArray = namespace.split("\\.");
        dbName = nameSpaceArray[0];
        collectionName = nameSpaceArray[1];

    }

    public MongoCollection<T> getCollection(){ return collection; };

    public String getCollectionName() { return collectionName; }
    public void setCollection( MongoCollection c ) { collection = c; }

    public String getDatabaseName() { return dbName; }
    public void setDatabase( MongoDatabase db ){ this.database = db; }

    public String getClusterName() { return cluster; }
}
