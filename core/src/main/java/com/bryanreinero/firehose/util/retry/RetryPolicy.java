package com.bryanreinero.firehose.util.retry;

import com.bryanreinero.firehose.util.Operation;

public abstract class RetryPolicy {

	protected final int maxRetries;
	protected final long delay, maxDuration;

	public RetryPolicy( int maxRetries, long delay, long maxDuration ){
		this.maxRetries = maxRetries;
		this.delay = delay;
		this.maxDuration = maxDuration;

	}
	abstract int getMaxRetries();
	public abstract RetryRequest getRetry(Operation o);
}
