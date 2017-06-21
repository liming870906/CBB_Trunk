package com.tingtingfm.cbb.common.net;

public interface RequestCallback<T> {
    void onStart();

    void onSuccess(T response);

    void onFail(int code, String errorMessage);

    T parseNetworkResponse(String content);

    void onCancel();
}
