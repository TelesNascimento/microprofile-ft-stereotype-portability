package com.example;

import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import org.eclipse.microprofile.faulttolerance.exceptions.TimeoutException;

/**
 * Endpoints that exercise Fault Tolerance annotations declared via CDI stereotypes and
 * report the observed behaviour as plain text.
 */
@Path("check")
public class CheckResource {

    private static final int EXPECTED_WITH_RETRY = 3;

    @Inject
    private GuardedService guardedService;

    @Inject
    private DirectRetryService directRetryService;

    @Inject
    private TimeoutService timeoutService;

    @Inject
    private ConflictService conflictService;

    @GET
    @Produces("text/plain")
    public String stereotype() {
        int invocations = invokeAndCount(guardedService);
        return "stereotype: invocations=" + invocations + " (expected " + EXPECTED_WITH_RETRY + ") -> "
                + stereotypeVerdict(invocations);
    }

    @GET
    @Path("direct")
    @Produces("text/plain")
    public String direct() {
        int invocations = invokeAndCount(directRetryService);
        return "direct: invocations=" + invocations + " (expected " + EXPECTED_WITH_RETRY + ") -> "
                + controlVerdict(invocations);
    }

    @GET
    @Path("conflict")
    @Produces("text/plain")
    public String conflict() {
        int invocations = invokeAndCount(conflictService);
        return "conflict: invocations=" + invocations + " -> " + conflictVerdict(invocations);
    }

    @GET
    @Path("timeout")
    @Produces("text/plain")
    public String timeout() {
        long start = System.currentTimeMillis();
        try {
            timeoutService.slow();
        } catch (TimeoutException expected) {
            return "timeout: TimeoutException -> @Timeout via stereotype APPLIED";
        } catch (RuntimeException unexpected) {
            return "timeout: " + unexpected.getClass().getName() + " -> @Timeout likely applied";
        }
        long elapsedMillis = System.currentTimeMillis() - start;
        return "timeout: completed in " + elapsedMillis + "ms (no TimeoutException) -> "
                + "@Timeout via stereotype NOT APPLIED (gap present)";
    }

    private int invokeAndCount(FailingService service) {
        service.reset();
        try {
            service.alwaysFails();
        } catch (RuntimeException ignored) {
            // Only the invocation count matters here, because it reveals whether the
            // Fault Tolerance interceptor engaged via the stereotype.
        }
        return service.getInvocations();
    }

    private String stereotypeVerdict(int invocations) {
        if (invocations > 1) {
            return "STEREOTYPE @Retry APPLIED (retry engaged)";
        }
        return "STEREOTYPE @Retry NOT APPLIED (gap present)";
    }

    private String controlVerdict(int invocations) {
        if (invocations > 1) {
            return "DIRECT @Retry APPLIED (control OK, runtime works)";
        }
        return "DIRECT @Retry NOT APPLIED (control failed, setup issue)";
    }

    private String conflictVerdict(int invocations) {
        if (invocations == EXPECTED_WITH_RETRY) {
            return "maxRetries=2 won (RetryStereotype, declared first)";
        }
        if (invocations == 5) {
            return "maxRetries=4 won (RetryTwoStereotype)";
        }
        if (invocations == 1) {
            return "neither stereotype applied";
        }
        return "unexpected invocation count";
    }
}
