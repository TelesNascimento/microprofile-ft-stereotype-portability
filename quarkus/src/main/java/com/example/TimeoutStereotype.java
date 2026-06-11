package com.example;

import jakarta.enterprise.inject.Stereotype;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.eclipse.microprofile.faulttolerance.Timeout;

@Stereotype
@Timeout(500)
@Inherited
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface TimeoutStereotype {
}
