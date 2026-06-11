package com.example;

/**
 * Common contract for a service whose business method always fails, so that the
 * number of invocations reveals whether a Fault Tolerance @Retry interceptor engaged.
 */
public interface FailingService {

    void reset();

    int getInvocations();

    void alwaysFails();
}
