package com.w16a.danish.user.exception;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for BusinessException
 */
class BusinessExceptionTest {

    @Test
    void shouldCreateBusinessExceptionWithCorrectStatusAndMessage() {
        String message = "Test Business Exception";
        HttpStatus status = HttpStatus.BAD_REQUEST;

        BusinessException exception = new BusinessException(status, message);

        assertEquals(status, exception.getStatus());
        assertEquals(message, exception.getMessage());
    }
}
