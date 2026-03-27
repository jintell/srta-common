package org.meldtech.platform.srta.common.security;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Custom annotation for fine-grained authorization.
 * To be used alongside {@code @PreAuthorize} or custom security logic.
 *
 * <p><strong>WARNING:</strong> This annotation is currently <em>not enforced</em> by any
 * AOP aspect or Spring Security interceptor. It serves as documentation only.
 * Consumers must pair it with {@code @PreAuthorize("hasAuthority('...')")} or
 * implement a custom {@code PermissionEnforcingAspect} to enforce the declared permissions.
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface RequiresPermission {
    String[] value();
}
