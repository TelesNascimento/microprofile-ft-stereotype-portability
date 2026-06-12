# Proposed specification clarification

## Scope statement

This is proposed as a clarification of behaviour that already follows from existing normative
text, rather than a new feature. The argument is a direct composition of current requirements:

1. The MicroProfile Fault Tolerance specification states that its annotations are interceptor
   bindings, and that it depends on Jakarta Interceptors and CDI.
2. Jakarta CDI 4.1 section 8.1.1 states that an interceptor binding declared by a stereotype is
   inherited by any bean that declares that stereotype; section 8.3 states that a binding
   declared at class level replaces the one a stereotype contributes.

Applying a class-level, stereotype-declared Fault Tolerance annotation therefore follows from
these requirements together. No TCK test exercises it today, which is why implementations were
free to diverge; at least one (Payara) already applies it, so it is implementable. The open
question for the working group is whether to verify this in the TCK or to leave it explicitly
optional.

For runtimes that ignore stereotype-declared annotations today this is a behaviour change, so
it is best treated as a clarification carried in a new specification version (see Backward
compatibility). The per-implementation table below is the concrete basis for that decision.

## Draft clarification text

Proposed addition to the "Relationship to Jakarta Interceptors" section:

> Because the Fault Tolerance annotations are interceptor bindings, they may be declared on a
> CDI stereotype. Such a stereotype must be defined as `@Target(TYPE)`, as required by Jakarta
> Contexts and Dependency Injection, so the binding is contributed at the class level. A Fault
> Tolerance annotation declared by a stereotype is inherited by any managed (class-based) bean
> that declares that stereotype and, like any class-level interceptor binding, applies to all
> business methods of the bean. A Fault Tolerance annotation of the same type declared directly
> on the bean class or method replaces the one declared by the stereotype; Fault Tolerance
> annotations of different types accumulate (for example, a `@Retry` declared by the stereotype
> and a `@Fallback` declared on the bean both apply).
>
> This applies to managed beans only. Stereotype-declared interceptor bindings on producer
> fields, producer methods and synthetic beans are out of scope, as those are not intercepted in
> a portable manner.
>
> When multiple stereotypes applied to the same bean declare the same Fault Tolerance annotation
> with conflicting attributes, the resulting behaviour is implementation-defined and is not
> covered by this specification.

This is a proposal for the working group to review and adjust.

(Scope follows from CDI semantics: only managed beans are intercepted, so stereotype-declared
interceptor bindings apply at the class level only, never at the method level; the CDI Full
producer-method-result case is excluded because it is not portably interceptable.)

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
with different attributes, the resolution is not defined by CDI and is left implementation-defined.
The draft clarification text above states this explicitly, and the TCK matrix does not assert
behaviour for that case.
