package com.bryanreinero.firehose.dao.faunadb;

import com.bryanreinero.firehose.util.OperationDescriptor;
import com.faunadb.client.FaunaClient;
import com.faunadb.client.query.Expr;
import com.faunadb.client.types.Value;

import static com.faunadb.client.query.Language.*;

public class FaunaService extends OperationDescriptor {
    private final FaunaClient faunaClient;


    public FaunaService(final String name, final FaunaClient faunaClient) {
        super(name);
        this.faunaClient = faunaClient;
    }

    public Value addDocument(final String collectionName, final Expr document) throws Exception {
        Value addDocumentResults = faunaClient.query(
                Create(
                        Collection(Value(collectionName)),
                        Obj("data",
                                document
                        )
                )
        ).get();
        System.out.println(String.format("Added document to collection %s \n %s \n", collectionName, addDocumentResults));
        return addDocumentResults;

    }
}
