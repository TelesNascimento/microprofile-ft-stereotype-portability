package com.example;

import jakarta.enterprise.context.ApplicationScoped;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Edge case: a bean declaring TWO stereotypes that each declare @Retry with a different
 * maxRetries ({@link RetryStereotype} = 2, {@link RetryTwoStereotype} = 4). Resolution of
 * conflicting interceptor bindings contributed by multiple stereotypes is not defined by
 * the specifications, so the outcome is implementation specific:
 *   invocations == 3 means maxRetries=2 won, invocations == 5 means maxRetries=4 won,
 *   invocations == 1 means neither was applied.
 */
@RetryStereotype
@RetryTwoStereotype
@ApplicationScoped
public class ConflictService implements FailingService {

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
