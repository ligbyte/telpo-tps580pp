package com.stkj.cashier.utils.rxjava;

import retrofit2.Response;

public final class HttpException extends retrofit2.HttpException {
    public HttpException(Response<?> response) {
        super(response);
    }
}
