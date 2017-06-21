package com.tingtingfm.cbb.common.net;

import com.alibaba.fastjson.JSON;

import java.lang.reflect.Type;


/**
 * Created by think on 2016/12/20.
 *
 * 只支持转换成对象，不支持转换成数组对象
 */

public abstract class BaseRequestCallback<T> implements RequestCallback<T> {
    protected Type type;
    public BaseRequestCallback() {
        type = ClassTypeReflect.getModelClazz(getClass());
    }

    @Override
    public T parseNetworkResponse(String content) {
        return JSON.parseObject(content, type);
    }
}
