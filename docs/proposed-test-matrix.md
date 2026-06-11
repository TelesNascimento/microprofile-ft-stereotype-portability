# Proposed TCK test matrix

A vertical matrix on a single strategy (`@Retry`), asserting invocation counts. The bean's
method always throws, so the number of invocations equals `1 + maxRetries` for the effective
configuration. This is the design discussed with the SmallRye maintainer in
smallrye/smallrye-fault-tolerance#1276.

Axes:

- Placement (where the effective `@Retry` comes from): stereotype only, class-level override,
  method-level override, class-and-method override.
- Location (how the stereotype reaches the bean): direct, inherited, nested-direct,
  nested-inherited.

`RetryStereotype` is `@Stereotype @Retry(maxRetries = 1) @Inherited @Target(TYPE)`.

| # | Test | Placement | Location | Effective @Retry | Invocations | Validates |
|---|---|---|---|---|---|---|
| 1 | retryNotAppliedWithoutStereotype | none | n/a | none | 1 | baseline, no stereotype no retry |
| 2 | retryAppliedViaDirectStereotypeWithoutOverride | stereotype only | direct | stereotype (1) | 2 | CDI 8.1.1 inheritance |
| 3 | retryAppliedViaInheritedStereotypeWithoutOverride | stereotype only | inherited | stereotype (1) | 2 | CDI 8.1.1 with @Inherited |
| 4 | retryAppliedViaDirectNestedStereotypeWithoutOverride | stereotype only | nested-direct | stereotype (1) | 2 | nested stereotype |
| 5 | retryAppliedViaInheritedNestedStereotypeWithoutOverride | stereotype only | nested-inherited | stereotype (1) | 2 | nested and inherited |
| 6 to 9 | classAnnotationOverridesRetryIn{Direct,Inherited,DirectNested,InheritedNested}Stereotype | class override | all four | class (2) | 3 | CDI 8.3 class replaces stereotype |
| 10 to 13 | methodAnnotationOverridesRetryIn{...} | method override | all four | method (3) | 4 | method-level precedence |
| 14 to 17 | methodAnnotationTakesPrecedenceOverClassAnd{...} | class and method | all four | method (3) over class (2) | 4 | method over class precedence |

Total: 17 tests.

## @Inherited rule (2 tests)

Java's `@Inherited` meta-annotation governs whether a stereotype placed on a superclass is visible on a
subclass. Two tests pin this, using a stereotype that is deliberately not `@Inherited`:

| # | Test | Stereotype location | Stereotype `@Inherited`? | Invocations | Validates |
|---|---|---|---|---|---|
| 18 | retryAppliedViaDirectNonInheritedStereotype | direct on bean class | no | 2 | direct declaration applies regardless of `@Inherited` |
| 19 | retryNotAppliedViaNonInheritedStereotypeOnSuperclass | on superclass only | no | 1 | a non-`@Inherited` stereotype is not inherited by the subclass, so it does not apply |

These match the reference-implementation behaviour from
[smallrye/smallrye-fault-tolerance#1276](https://github.com/smallrye/smallrye-fault-tolerance/pull/1276)
(direct non-inherited stereotype applies; non-inherited stereotype on a superclass does not).

## Class-level override that disables retry (1 test)

One test pins the CDI 8.3 replacement rule in the disabling direction: a class-level `@Retry(maxRetries = 0)`
replaces the stereotype-declared `@Retry(maxRetries = 1)`, so retry is turned off.

| # | Test | Effective @Retry | Invocations | Validates |
|---|---|---|---|---|
| 20 | classAnnotationWithZeroMaxRetriesOverridesStereotype | class (0) | 1 | CDI 8.3 replacement holds even when it disables retry |

Total: **20 tests** (17 matrix + 2 `@Inherited` rule + 1 disable-via-override).

Empirical result against SmallRye Fault Tolerance: all 20 tests pass when run against SmallRye main at the
[#1276](https://github.com/smallrye/smallrye-fault-tolerance/pull/1276) merge commit (`dc19fe1`, merged
Jun 12, 2026 into 7.0.0) via its TCK runner (`Tests run: 20, Failures: 0, Errors: 0, Skipped: 0`). Without
the fix, the four stereotype-only cases (2 to 5) fail with `expected [2] but found [1]`, which isolates the
gap to stereotype discovery.

## Notes for reviewers

- Why 17 and not a larger product: the matrix varies two axes only, placement (4) and location
  (4), giving 16, plus one negative baseline, for 17. It does not vary other strategies or a
  fallback, because this is deliberately a single-strategy (`@Retry`) vertical matrix. If
  `@Retry` discovery through a stereotype is correct, the same interceptor-binding mechanism
  applies to the other annotations.
- @Timeout is deferred on purpose: the reproducer shows `@Timeout` declared via a stereotype
  diverges in the same way, but the proposed TCK matrix stays on `@Retry` only (invocation
  counts are deterministic and not timing-dependent). A `@Timeout` matrix can follow in a
  separate pull request.
- Determinism and isolation: each test calls `reset()` on its bean before invoking, asserts the
  propagated `TestException`, then checks the invocation count. Counting uses an `AtomicInteger`;
  there are no sleeps and no timing-based assertions, so the tests are deterministic.
- Naming: each test name encodes the cell, for example
  `retryAppliedViaInheritedStereotypeWithoutOverride`,
  `classAnnotationOverridesRetryInDirectStereotype`,
  `methodAnnotationTakesPrecedenceOverClassAndInheritedNestedStereotype`,
  `retryNotAppliedWithoutStereotype`.
- Backward compatibility: see `docs/proposed-spec-clarification.md`. This corrects a behaviour
  that some runtimes silently skip today; applications not using stereotype-declared Fault
  Tolerance annotations are unaffected.

## Optional additional case (only if reviewers request it)

Not part of the proposed 20, would be added on request to avoid scope creep:

- Recovery path (a method that fails once then succeeds) under a stereotype-declared `@Retry`:
  expected 2 invocations with no exception. The current tests cover the exhausted path; this
  would add the success-after-retry path.
