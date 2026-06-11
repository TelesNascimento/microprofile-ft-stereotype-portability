package com.example;

import jakarta.enterprise.inject.Stereotype;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.eclipse.microprofile.faulttolerance.Retry;

// Second stereotype declaring @Retry with a DIFFERENT maxRetries (4 -> 5 invocations),
// to probe what happens when two stereotypes on the same bean both declare @Retry.
@Stereotype
@Retry(maxRetries = 4)
@Inherited
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface RetryTwoStereotype {
}
