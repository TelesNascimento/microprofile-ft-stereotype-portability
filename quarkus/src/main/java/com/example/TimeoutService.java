package com.example;

import jakarta.enterprise.context.ApplicationScoped;

/**
 * Bean whose @Timeout comes ONLY from a class-level stereotype ({@link TimeoutStereotype},
 * 500ms). The method sleeps longer than the timeout: if the stereotype-declared @Timeout is
 * applied, the caller receives a TimeoutException; otherwise the method sleeps the full time.
 */
@TimeoutStereotype
@ApplicationScoped
public class TimeoutService {

    private static final long SLEEP_MILLIS = 1500L;

    public void slow() {
        try {
            Thread.sleep(SLEEP_MILLIS);
        } catch (InterruptedException interrupted) {
            Thread.currentThread().interrupt();
        }
    }
}
