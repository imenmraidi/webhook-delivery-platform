package com.project.webhookplatform.service;

import java.time.Duration;
import java.util.concurrent.ThreadLocalRandom;

public class RetryPolicy {

    private final int maxAttempts;
    private final Duration base;
    private final Duration cap;

    public RetryPolicy(int maxAttempts, Duration base, Duration cap) {
        this.maxAttempts = maxAttempts;
        this.base = base;
        this.cap = cap;
    }

    public Outcome classify(int statusCode) {
        if (statusCode >= 200 && statusCode < 300) return Outcome.SUCCESS;
        if (statusCode == 0 || statusCode == 408 || statusCode == 429 || statusCode >= 500) return Outcome.RETRYABLE;
        return Outcome.PERMANENT;
    }

    public boolean exhausted(int attemptsMade) { return attemptsMade >= maxAttempts; }

    public Duration delayAfter(int attemptsMade) {
        long exp = base.toMillis() * (1L << Math.min(attemptsMade - 1, 20));
        long capped = Math.min(exp, cap.toMillis());
        long jittered = capped / 2 + ThreadLocalRandom.current().nextLong(capped / 2 + 1);
        return Duration.ofMillis(jittered);
    }
}
