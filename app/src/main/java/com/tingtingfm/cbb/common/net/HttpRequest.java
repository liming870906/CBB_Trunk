package com.tingtingfm.cbb.common.net;

import android.os.Handler;
import android.os.Looper;

import com.tingtingfm.cbb.R;
import com.tingtingfm.cbb.TTApplication;
import com.tingtingfm.cbb.common.helper.ErrorCode;
import com.tingtingfm.cbb.common.log.TTLog;
import com.tingtingfm.cbb.common.utils.BaseUtils;
import com.tingtingfm.cbb.common.utils.HttpUtils;
import com.tingtingfm.cbb.common.utils.JsonFormatUtils;
import com.tingtingfm.cbb.common.utils.NetUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.HttpURLConnection;

import javax.net.ssl.HttpsURLConnection;

public final class HttpRequest implements Runnable {
    private static final int DEFAULT_SOCKET_BUFFER_SIZE = 8 * 1024; // 8KB
    private static final String HEADER_ACCEPT_ENCODING = "Accept-Encoding";
    private static final String ENCODING_GZIP = "gzip";

    private RequestCallback requestCallback = null;
    private byte[] paramByte = null;
    private String url = null;
    private String tag = "";
    protected Handler handler;

    public HttpRequest(final RequestEntity entity,
                       final RequestCallback callBack) {
        tag = entity.getTag();
        url = entity.getUrl();
        this.paramByte = entity.getParamsBytes();
        requestCallback = callBack;

        handler = new Handler(Looper.myLooper());

        TTLog.i(entity.getParameters().toString());
    }

    @Override
    public void run() {
        HttpsURLConnection connection = null;
        String newUrl = url;
        String message = "";
        try {
            if (!NetUtils.isNetConnected()) {
                handleNetworkError(ErrorCode.errorcode_net_error,
                        TTApplication.getAppResources().getString(R.string.login_not_net));
                return;
            }

            if (requestCallback != null) {
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        requestCallback.onStart();
                    }
                });
            }

            connection = HttpUtils.getConnection(newUrl);
            // 发送请求参数
            HttpUtils.postParams(connection.getOutputStream(), paramByte);

            // 获取状态
            if ((requestCallback != null)) {
                final int statusCode = connection.getResponseCode();
                if (statusCode == HttpURLConnection.HTTP_OK) {
                    message = BaseUtils.inputStreamToString(connection.getInputStream());

                    TTLog.d("HTTPResponse:\n"
                            + "URL: " + newUrl
                            + "\nresult: " + JsonFormatUtils.format(message)
                            + "\nStartTime: " + System.currentTimeMillis());

                    // 设置回调函数
                    final BaseResponse responseInJson = stringToBaseResponse(message);
                    if (responseInJson.hasError()) {
                        handleNetworkError(responseInJson.getErrno(), responseInJson.getError());
                    } else {
                        final Object o = requestCallback.parseNetworkResponse(message);
                        handler.post(new Runnable() {

                            @Override
                            public void run() {
                                requestCallback.onCancel();
                                requestCallback.onSuccess(o);
                            }

                        });
                    }
                } else {
                    handleNetworkError(statusCode,
                            TTApplication.getAppResources().getString(R.string.login_not_net));
                }
            }
        } catch (final Exception e) {
            handleNetworkError(ErrorCode.errorcode_internal_error,
                    TTApplication.getAppResources().getString(R.string.login_not_net));
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    void handleNetworkError(final int code, final String errorMsg) {
        handler.post(new Runnable() {
            @Override
            public void run() {
                if (requestCallback != null) {
                    requestCallback.onCancel();
                    requestCallback.onFail(code, errorMsg);
                }
            }
        });
    }

    BaseResponse stringToBaseResponse(String message) throws JSONException {
        BaseResponse response = new BaseResponse();
        JSONObject object = new JSONObject(message);
        response.setErrno(object.getInt("errno"));
        response.setError(object.getString("error"));
        response.setServer(object.getString("server"));
        response.setServer_time(object.getLong("server_time"));

        return response;
    }


}