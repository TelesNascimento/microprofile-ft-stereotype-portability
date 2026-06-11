package com.example;

import jakarta.enterprise.context.ApplicationScoped;
import java.util.concurrent.atomic.AtomicInteger;
import org.eclipse.microprofile.faulttolerance.Retry;

/**
 * Positive control: @Retry is declared DIRECTLY on the class (not via a stereotype).
 * Every implementation is expected to invoke the method 3 times. If this control passes
 * but {@link GuardedService} does not, the gap is specific to stereotype discovery rather
 * than the Fault Tolerance runtime as a whole.
 */
@Retry(maxRetries = 2)
@ApplicationScoped
public class DirectRetryService implements FailingService {

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
