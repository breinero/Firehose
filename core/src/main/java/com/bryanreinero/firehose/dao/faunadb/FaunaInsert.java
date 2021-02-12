package com.bryanreinero.firehose.dao.faunadb;

import com.bryanreinero.firehose.metrics.Interval;
import com.bryanreinero.firehose.util.Operation;
import com.bryanreinero.firehose.util.Result;
import com.faunadb.client.query.Expr;
import com.faunadb.client.types.Value;

public class FaunaInsert extends Operation {

    private final Expr document;
    private final String collectionName;
    private final FaunaService descriptor;

    public FaunaInsert(String collectionName, Expr document, FaunaService descriptor) {
        super(descriptor);
        this.document = document;
        this.collectionName = collectionName;
        this.descriptor = descriptor;
    }

    @Override
    public Result<Value> call() throws Exception {
        Result<Value> res = new Result<Value>(false);
        incAttempts();

        try ( Interval i = descriptor.getSamples().set( getName() )) {
            final Value addDocResult = descriptor.addDocument(this.collectionName, this.document);
            res.setResults(addDocResult);
        } catch (Exception e) {
            res.setFailed("Error adding document, " + e.getMessage());
        }

        return res;
    }
}
