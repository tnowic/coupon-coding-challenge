package pl.nowito.coupon.advice;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import pl.nowito.coupon.dto.ErrorResponse;
import pl.nowito.coupon.error.CouponBusinessRuleViolationException;
import pl.nowito.coupon.error.CouponNotFoundException;
import pl.nowito.coupon.error.DuplicateCouponCodeException;

import java.util.List;

import static java.lang.String.format;
import static pl.nowito.coupon.error.support.ErrorMessageSupportEnum.*;

@ControllerAdvice
public class CouponExceptionHandler {


    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponse> handleDataIntegrityViolationException(DataIntegrityViolationException e) {
        return new ResponseEntity<>(new ErrorResponse(
                ERROR_DATA_INTEGRITY_VIOLATION.getErrorCode(),
                List.of(),
                format(ERROR_DATA_INTEGRITY_VIOLATION.getMsgTemplate(), e.getMessage()),
                ERROR_DATA_INTEGRITY_VIOLATION.getSuggestion()),
                HttpStatus.CONFLICT);
    }

    @ExceptionHandler(CouponNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleCouponNotFoundException(CouponNotFoundException e) {
        return new ResponseEntity<>(new ErrorResponse(
                ERROR_COUPON_CODE_NOT_FOUND.getErrorCode(),
                List.of(e.getCouponCode()),
                format(ERROR_COUPON_CODE_NOT_FOUND.getMsgTemplate(), e.getCouponCode()),
                ERROR_COUPON_CODE_NOT_FOUND.getSuggestion())
                , HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(DuplicateCouponCodeException.class)
    public ResponseEntity<ErrorResponse> handleDuplicateRequestException(DuplicateCouponCodeException e) {
        return new ResponseEntity<>(new ErrorResponse(e.getErrorCode(), e.getParams(), e.getMessage(), e.getSuggestion()), HttpStatus.CONFLICT);
    }


    @ExceptionHandler(CouponBusinessRuleViolationException.class)
    public ResponseEntity<ErrorResponse> handleBusinessRuleViolationException(CouponBusinessRuleViolationException e) {
        return new ResponseEntity<>(new ErrorResponse(e.getErrorCode(), e.getParams(), e.getMessage(), e.getSuggestion()), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ErrorResponse> handleBRuntimeException(RuntimeException e) {
        return new ResponseEntity<>(new ErrorResponse(INTERNAL_SERVER_ERROR.getErrorCode(), List.of(), INTERNAL_SERVER_ERROR.getMsgTemplate(), INTERNAL_SERVER_ERROR.getSuggestion()),
                HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
