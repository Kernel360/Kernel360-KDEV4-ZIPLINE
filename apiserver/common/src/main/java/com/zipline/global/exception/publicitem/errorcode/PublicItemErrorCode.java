package com.zipline.global.exception.publicitem.errorcode;

import com.zipline.global.exception.ErrorCode;
import org.springframework.http.HttpStatus;

public enum PublicItemErrorCode implements ErrorCode {
    PUBLIC_ITEM_PARAM_ERROR("PUBLIC-ITEM-001", "매물 검색 파라미터가 잘못되었습니다.", HttpStatus.BAD_REQUEST),
    PUBLIC_ITEM_ERROR("PUBLIC-ITEM-002", "매물 검색 중 오류가 발생했습니다.", HttpStatus.INTERNAL_SERVER_ERROR),
    INVALID_PRICE_RANGE("PUBLIC-ITEM-003", "최소 가격은 최대 가격보다 클 수 없습니다.", HttpStatus.BAD_REQUEST),
    INVALID_DEPOSIT_RANGE("PUBLIC-ITEM-004", "최소 보증금은 최대 보증금보다 클 수 없습니다.", HttpStatus.BAD_REQUEST),
    INVALID_MONTHLY_RENT_RANGE("PUBLIC-ITEM-005", "최소 월세는 최대 월세보다 클 수 없습니다.", HttpStatus.BAD_REQUEST),
    INVALID_AREA_RANGE("PUBLIC-ITEM-006", "최소 면적은 최대 면적보다 클 수 없습니다.", HttpStatus.BAD_REQUEST);


    private final String code;
    private final String message;
    private final HttpStatus status;

    PublicItemErrorCode(String code, String message, HttpStatus status) {
        this.code = code;
        this.message = message;
        this.status = status;
    }

    @Override
    public String getCode() { return code; }

    @Override
    public String getMessage() { return message; }

    @Override
    public HttpStatus getStatus() { return status; }
}
