package com.bryanreinero.firehose.dao.faunadb;

import com.bryanreinero.firehose.util.OperationDescriptor;
import com.faunadb.client.FaunaClient;
import com.faunadb.client.query.Expr;
import com.faunadb.client.types.Result;
import com.faunadb.client.types.Value;

import java.util.HashMap;
import java.util.Map;

import static com.faunadb.client.query.Language.*;

public class FaunaService extends OperationDescriptor {
    private final FaunaClient faunaClient;


    public FaunaService(final String name, final FaunaClient faunaClient) {
        super(name);
        this.faunaClient = faunaClient;
    }

    /**
     *  This method assumes that the document Map contains a field for id that is an Integer.  This will be used
     *  to lookup the document and perform and update if it exists and a create if it does not
     * @param collectionName
     * @param document
     * @return
     * @throws Exception
     */
    public Value addOrUpdateDocument(final String collectionName, final Map<String, Value> document) throws Exception {
        System.out.println("addOrUpdateDocument");

        final String indexName = collectionName + "_by_id";

        Value addDocumentResults = faunaClient.query(
                If(
                        // todo: accept index name?
                        Exists(Match(Index(indexName), document.get("id"))),
                        Replace(Select(Value("ref"), Get(Match(Index(indexName), document.get("id")))),
                                Obj("data",
                                        Obj(document)
                                        )
                                ),
                        Create(
                                Collection(Value(collectionName)),
                                Obj("data",
                                        Obj(document)
                                )
                        )
                )
        ).get();
        System.out.println(String.format("Added document to collection %s \n %s \n", collectionName, addDocumentResults));
        return addDocumentResults;

    }
}
