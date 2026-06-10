package com.yudi.asmara.expensereport.network;

public interface NetworkResult {
    void onSuccess(String response);
    void onError(String message);
}
