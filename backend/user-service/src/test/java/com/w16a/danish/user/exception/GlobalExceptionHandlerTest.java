package com.w16a.danish.user.exception;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for GlobalExceptionHandler
 */
class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler globalExceptionHandler;

    @BeforeEach
    void setUp() {
        globalExceptionHandler = new GlobalExceptionHandler();
    }

    @Test
    void shouldHandleBusinessException() {
        BusinessException ex = new BusinessException(HttpStatus.CONFLICT, "Email already exists");

        ResponseEntity<Map<String, String>> response = globalExceptionHandler.handleBusinessException(ex);

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Email already exists", response.getBody().get("error"));
    }

    @Test
    void shouldHandleGenericException() {
        Exception ex = new Exception("Unexpected error");

        ResponseEntity<Map<String, String>> response = globalExceptionHandler.handleGenericException(ex);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().get("error").contains("Internal Server Error"));
    }
}
