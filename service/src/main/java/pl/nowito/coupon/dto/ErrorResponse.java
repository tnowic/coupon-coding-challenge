package pl.nowito.coupon.dto;

import java.util.List;

public record ErrorResponse(String errorCode,
                            List<Object> params,
                            String errorMessage,
                            String suggestion) {
}
