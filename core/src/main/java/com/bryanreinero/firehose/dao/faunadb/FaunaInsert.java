package com.bryanreinero.firehose.dao.faunadb;
import com.faunadb.client.FaunaClient;

import static com.faunadb.client.query.Language.*;

import com.bryanreinero.firehose.util.Operation;
import com.faunadb.client.query.Expr;

public class FaunaInsert extends Operation {

    private final Expr document;

    public FaunaInsert(Expr document, FaunaDAO descriptor) {
        super(descriptor);
        this.document = document;
    }

    @Override
    public Object call() throws Exception {
        return null;
    }
}
