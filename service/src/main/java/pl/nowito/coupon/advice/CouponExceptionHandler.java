package pl.nowito.coupon.advice;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import pl.nowito.coupon.error.CouponBusinessRuleViolationException;
import pl.nowito.coupon.error.CouponNotFoundException;

import java.util.Map;

import static java.lang.String.format;

@ControllerAdvice
public class CouponExceptionHandler {

    private static final String ERROR_KEY = "error";
    private static final String SUGGESTION_KEY = "suggestion";

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<Object> handleDataIntegrityViolationException(DataIntegrityViolationException e) {
        return new ResponseEntity<>(Map.of(ERROR_KEY, format("Data integrity violation. %s", e.getMessage())), HttpStatus.CONFLICT);
    }

    @ExceptionHandler(CouponBusinessRuleViolationException.class)
    public ResponseEntity<Object> handleBusinessRuleViolationException(CouponBusinessRuleViolationException e) {
        return new ResponseEntity<>(Map.of(ERROR_KEY, format("Business rule violation. %s", e.getErrorMessage()),
                SUGGESTION_KEY, e.getSuggestion()), HttpStatus.CONFLICT);
    }

    @ExceptionHandler(CouponNotFoundException.class)
    public ResponseEntity<Object> handleCouponNotFoundException(CouponNotFoundException e) {
        return new ResponseEntity<>(Map.of(ERROR_KEY, format("Coupon with code: %s not found" , e.getCode()),
                        SUGGESTION_KEY, "Please check your code and try again"), HttpStatus.NOT_FOUND);
    }

}
