package com.tingtingfm.cbb.common.helper;

import com.tingtingfm.cbb.common.net.DefaultThreadPool;
import com.tingtingfm.cbb.common.net.HttpRequest;
import com.tingtingfm.cbb.common.net.RequestCallback;
import com.tingtingfm.cbb.common.net.RequestEntity;


/**
 * Created by think on 2016/12/20.
 */

public class HttpRequestHelper {

    public static void post(RequestEntity entity, RequestCallback callback) {
        HttpRequest request = new HttpRequest(entity, callback);
        DefaultThreadPool.getInstance().execute(request);
    }

    public static void post(String url, RequestCallback callback) {
        post(new RequestEntity(url), callback);
    }
}
