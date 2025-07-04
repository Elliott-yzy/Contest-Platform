package com.w16a.danish.competition.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

/**
 * @author Eddy ZHANG
 * @date 2025/03/18
 * @description BusinessException
 */
@Getter
public class BusinessException extends RuntimeException {
    private final HttpStatus status;

    public BusinessException(HttpStatus status, String message) {
        super(message);
        this.status = status;
    }
}
