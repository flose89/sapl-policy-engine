package io.sapl.springdatar2dbc.sapl.handlers;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import io.sapl.springdatar2dbc.database.MethodInvocationForTesting;
import io.sapl.api.pdp.AuthorizationSubscription;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.ArrayList;
import java.util.List;

@SpringBootTest
class EnforceAnnotationHandlerTest {

    @Autowired
    EnforceAnnotationHandler enforceAnnotationHandler;

    @Test
    void when_methodHasAnEnforceAnnotationWithStaticValues_then_enforceAnnotation() {
        // GIVEN
        var expectedResult   = AuthorizationSubscription.of("subject", "general_protection_reactive_r2dbc_repository",
                "resource", "environment");
        var methodInvocation = new MethodInvocationForTesting("findAllByFirstname",
                new ArrayList<>(List.of(String.class)), null, null);

        // WHEN
        var result = enforceAnnotationHandler.enforceAnnotation(methodInvocation);

        // THEN
        Assertions.assertEquals(result, expectedResult);
    }

    @Test
    void when_methodHasNoEnforceAnnotationWithStaticValues_then_returnNull() {
        // GIVEN
        var methodInvocation = new MethodInvocationForTesting("findAllByAge", new ArrayList<>(List.of(int.class)), null,
                null);

        // WHEN
        var result = enforceAnnotationHandler.enforceAnnotation(methodInvocation);

        // THEN
        Assertions.assertNull(result);
    }

    @Test
    void when_methodHasAnEnforceAnnotationWithStaticClassInEvaluationContext_then_enforceAnnotation() {
        // GIVEN
        var expectedResult   = AuthorizationSubscription.of("test value",
                "general_protection_reactive_r2dbc_repository", "Static class set: field, test value", 56);
        var methodInvocation = new MethodInvocationForTesting("findAllByAgeAfterAndFirstname",
                new ArrayList<>(List.of(int.class, String.class)), new ArrayList<>(List.of(18, "test value")), null);

        // WHEN
        var result = enforceAnnotationHandler.enforceAnnotation(methodInvocation);

        // THEN
        Assertions.assertEquals(result, expectedResult);
    }

    @Test
    void when_methodHasAnEnforceAnnotationWithStaticClassInEvaluationContextPart2_then_enforceAnnotation() {
        // GIVEN
        var environment = JsonNodeFactory.instance.objectNode().put("testNode", "testValue");

        var expectedResult   = AuthorizationSubscription.of("firstname", "general_protection_reactive_r2dbc_repository",
                "Static class set: firstname, test value", environment);
        var methodInvocation = new MethodInvocationForTesting("findAllByFirstnameAndAgeBefore",
                new ArrayList<>(List.of(String.class, int.class)), new ArrayList<>(List.of("firstname", 4)), null);

        // WHEN
        var result = enforceAnnotationHandler.enforceAnnotation(methodInvocation);

        // THEN
        Assertions.assertEquals(result, expectedResult);
    }

    @Test
    void when_methodHasAnEnforceAnnotationAndJsonStringIsNotValid_then_throwParseException() {
        // GIVEN
        var methodInvocation = new MethodInvocationForTesting("findById", new ArrayList<>(List.of(String.class)),
                new ArrayList<>(List.of()), null);

        // WHEN

        // THEN
        Assertions.assertThrows(JsonParseException.class,
                () -> enforceAnnotationHandler.enforceAnnotation(methodInvocation));
    }

    @Test
    void when_methodHasAnEnforceAnnotationAndMethodOfStaticClassIsAboutToUseButNoStaticClassInAnnotationAttached_then_throwNoSuchMethodException() {
        // GIVEN
        var methodInvocation = new MethodInvocationForTesting("findByIdBefore", new ArrayList<>(List.of(String.class)),
                new ArrayList<>(List.of()), null);

        // WHEN

        // THEN
        Assertions.assertThrows(NoSuchMethodException.class,
                () -> enforceAnnotationHandler.enforceAnnotation(methodInvocation));
    }

    @Test
    void when_methodHasAnEnforceAnnotationAndMethodOfStaticClassIsNotTheRightClass_then_throwNoSuchMethodException() {
        // GIVEN
        var methodInvocation = new MethodInvocationForTesting("findByIdAfter", new ArrayList<>(List.of(String.class)),
                new ArrayList<>(List.of()), null);

        // WHEN

        // THEN
        Assertions.assertThrows(NoSuchMethodException.class,
                () -> enforceAnnotationHandler.enforceAnnotation(methodInvocation));
    }

    @Test
    void when_methodHasAnEnforceAnnotationAndMethodOfStaticClassIsAboutToUseButMethodNotExist_then_throwNoSuchMethodException() {
        // GIVEN
        var methodInvocation = new MethodInvocationForTesting("findByIdAndAge",
                new ArrayList<>(List.of(String.class, int.class)), new ArrayList<>(List.of()), null);

        // WHEN

        // THEN
        Assertions.assertThrows(NoSuchMethodException.class,
                () -> enforceAnnotationHandler.enforceAnnotation(methodInvocation));
    }
}