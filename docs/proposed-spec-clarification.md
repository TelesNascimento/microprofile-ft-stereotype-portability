# Proposed specification clarification

## Scope statement

This is proposed as a specification clarification, not a new feature. The behaviour appears to
follow from text that is already in the specifications: MicroProfile Fault Tolerance states
that its annotations are interceptor bindings, and Jakarta CDI 4.1 defines how interceptor
bindings declared on a stereotype are inherited (section 8.1.1) and how a class-level binding
replaces a stereotype-contributed one (section 8.3). The behaviour is implementable today, as
at least one implementation already exhibits it. The open question for the working group is
whether MicroProfile Fault Tolerance intends to rely on that behaviour and to verify it in the
TCK, or to leave it explicitly optional.

## Draft clarification text

Proposed addition to the "Relationship to Jakarta Interceptors" section:

> Because the Fault Tolerance annotations are interceptor bindings, they may be declared on a
> CDI stereotype. A stereotype that declares interceptor bindings must be defined as
> `@Target(TYPE)`, as required by Jakarta Contexts and Dependency Injection. A Fault Tolerance
> annotation declared by a stereotype is inherited by any bean that declares that stereotype
> and, like any class-level interceptor binding, applies to all business methods of the bean. A
> Fault Tolerance annotation of the same type declared directly on the bean class or method
> replaces the one declared by the stereotype.

This is a proposal for the working group to review and adjust.

## Backward compatibility

Applications that do not declare Fault Tolerance annotations through a stereotype are
unaffected. For applications that do, the behaviour changes from "silently ignored" (on most
runtimes today) to "applied as declared" (the intended semantics, already exhibited by at
least one implementation). This is a behaviour change for those applications, not an API
break; it is appropriate for a new specification version, consistent with MicroProfile's
versioning approach and the "no backward compatibility guarantee" pillar of the working group
charter.

Per-implementation impact (from the cross-implementation results in the README; versions there):

| Implementation | Current behaviour | Impact of this clarification |
|---|---|---|
| Payara | already applies stereotype-declared FT annotations | none |
| SmallRye / Quarkus / WildFly | did not apply; fixed in SmallRye #1276 (7.0.0) | aligned once 7.0.0 ships |
| Open Liberty | does not apply today | behaviour change; vendor should note in release notes |
| Helidon | does not apply today | behaviour change; vendor should note in release notes |
| Apache TomEE | does not apply today | behaviour change; vendor should note in release notes |

No application using these annotations on a stereotype today can be relying on a portable
behaviour, because the behaviour is not portable today; that is the gap this clarification
closes. The working group should confirm the affected vendors are content to make this change
in the target specification version.

## Out of scope: conflicting stereotypes

When two stereotypes applied to the same bean each declare the same Fault Tolerance annotation
with different attributes, the resolution is not defined by CDI and is left implementation-defined
by this clarification. The TCK matrix therefore does not assert behaviour for that case. This
should be stated explicitly in the spec text so the contract is unambiguous.
