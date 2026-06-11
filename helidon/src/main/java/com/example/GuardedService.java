package com.example;

import jakarta.enterprise.context.ApplicationScoped;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Bean whose @Retry comes ONLY from a class-level stereotype ({@link RetryStereotype},
 * maxRetries=2). If the implementation discovers the stereotype-declared interceptor
 * binding, the method is invoked 3 times; otherwise it is invoked once.
 */
@RetryStereotype
@ApplicationScoped
public class GuardedService implements FailingService {

    private final AtomicInteger invocations = new AtomicInteger();

    @Override
    public void reset() {
        invocations.set(0);
    }

    @Override
    public int getInvocations() {
        return invocations.get();
    }

    @Override
    public void alwaysFails() {
        invocations.incrementAndGet();
        throw new IllegalStateException("simulated failure");
    }
}
