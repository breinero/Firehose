package com.bryanreinero.firehose.dao.faunadb;

import com.bryanreinero.firehose.util.Operation;
import com.bryanreinero.firehose.util.Result;
import com.faunadb.client.query.Expr;
import com.faunadb.client.types.Value;

public class FaunaInsert extends Operation {

    private final Expr document;
    private final String collectionName;

    public FaunaInsert(String collectionName, Expr document, FaunaService descriptor) {
        super(descriptor);
        this.document = document;
        this.collectionName = collectionName;
    }

    @Override
    public Result<Value> call() throws Exception {
        Result res = new Result(false);
        incAttempts();
        return null;
    }
}
