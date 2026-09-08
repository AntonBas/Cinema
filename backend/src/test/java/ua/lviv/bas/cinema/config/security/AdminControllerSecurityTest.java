package ua.lviv.bas.cinema.config.security;

import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class AdminControllerSecurityTest {

    private static final String BASE_PACKAGE = "ua.lviv.bas.cinema";

    private static final List<Class<? extends Annotation>> HANDLER_ANNOTATIONS = List.of(RequestMapping.class,
            GetMapping.class, PostMapping.class, PutMapping.class, DeleteMapping.class, PatchMapping.class);

    @Test
    void everyAdminControllerMustDeclarePreAuthorize() throws ClassNotFoundException {
        var scanner = new ClassPathScanningCandidateComponentProvider(false);
        scanner.addIncludeFilter(new AnnotationTypeFilter(RestController.class));

        List<String> unprotectedControllers = new ArrayList<>();

        for (var candidate : scanner.findCandidateComponents(BASE_PACKAGE)) {
            String className = candidate.getBeanClassName();
            if (className == null || !className.contains(".controller.admin.")) {
                continue;
            }

            Class<?> controllerClass = Class.forName(className);
            if (!isProtected(controllerClass)) {
                unprotectedControllers.add(className);
            }
        }

        assertThat(unprotectedControllers)
                .as("Every <domain>/controller/admin/** class must carry @PreAuthorize (class-level, or on every "
                        + "handler method) since it is the real authorization boundary for admin endpoints, "
                        + "independent of WebSecurityConfig's coarse fallback ADMIN/CONTENT_MANAGER URL rule")
                .isEmpty();
    }

    private boolean isProtected(Class<?> controllerClass) {
        if (controllerClass.isAnnotationPresent(PreAuthorize.class)) {
            return true;
        }

        List<Method> handlerMethods = Arrays.stream(controllerClass.getDeclaredMethods())
                .filter(AdminControllerSecurityTest::isHandlerMethod).toList();

        return !handlerMethods.isEmpty()
                && handlerMethods.stream().allMatch(m -> m.isAnnotationPresent(PreAuthorize.class));
    }

    private static boolean isHandlerMethod(Method method) {
        return HANDLER_ANNOTATIONS.stream().anyMatch(method::isAnnotationPresent);
    }
}
