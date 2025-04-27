package com.zipline.global.exception.publicitem;

import com.zipline.global.exception.BaseException;
import com.zipline.global.exception.publicitem.errorcode.PublicItemErrorCode;

public class PublicItemException extends BaseException {

    public PublicItemException(PublicItemErrorCode errorCode) {
        super(errorCode);
    }
}
