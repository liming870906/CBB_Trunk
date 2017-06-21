package com.tingtingfm.cbb.common.net;

import android.text.TextUtils;

import com.tingtingfm.cbb.common.configuration.RequestParamsConfiguration;
import com.tingtingfm.cbb.common.utils.encrypt.DesECBUtil;

import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/**
 * 封装请求tag,url,请求参数
 * Created by liqiang on 16/4/2.
 */
public class RequestEntity {
    private static String ENCODING = "UTF-8";

    String tag;
    String url;

    List<RequestParameter> parameters = new ArrayList<RequestParameter>();

    public RequestEntity(String url) {
        this(null, url);
    }

    public RequestEntity(String tag, String url) {
        if (TextUtils.isEmpty(url)) {
            throw new IllegalArgumentException("url can not null");
        }

        this.url = url;
        this.tag = tag;

        //TODO 添加一些默认参数
        parameters.add(new RequestParameter("session_key", RequestParamsConfiguration.getSession_key()));
        parameters.add(new RequestParameter("client", RequestParamsConfiguration.getClient()));
        parameters.add(new RequestParameter("version", RequestParamsConfiguration.getVersion()));
    }

    public void addParams(String k, String v) {
        parameters.add(new RequestParameter(k, v));
    }

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public String getUrl() {
        return url;
    }

    public List<RequestParameter> getParameters() {
        parameters.add(new RequestParameter("api_sign", generateSign()));
        return parameters;
    }

    String generateSign() {
        String param2 = getParam2() + "_" + "bw(*ez$@]a.bokLi";

//        System.out.println(sign);

        return DesECBUtil.encryption(param2).toLowerCase();
    }

    /**
     * 拼接请求参数
     *
     * @return 返回升序后拼接的参数
     */
    String getParam2() {
        String result = "";
        try {
            Collections.sort(parameters, new ParamComparable());

            StringBuilder sb = new StringBuilder();
            for (Iterator<RequestParameter> iter = parameters.iterator(); iter.hasNext(); ) {
                RequestParameter p = (RequestParameter) iter.next();
                sb.append(p.getName());
                sb.append("=");
                if (!TextUtils.isEmpty(p.getValue())) {
                    sb.append(URLEncoder.encode(p.getValue(), ENCODING));
                }
                sb.append("&");
            }

            result = sb.toString().substring(0, sb.toString().length() - 1);
            result = DesECBUtil.dispatchStr(result);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return result;
    }

    /**
     * 对字符做处理，URLEncoder.encode对*不做转义，对空格做+号转义，则需要处理如下：*需要替换成%2A, +号需要替换成%20
     * Uri.encode *需要替换成%2A
     *
     * @param value
     * @return
     */
    String dispatchStr(String value) {
        return value.replace("+", "%20").replace("*", "%2A").replace("%7E", "~");
    }

    public byte[] getParamsBytes() {
        StringBuilder sb = new StringBuilder();
        try {
            List<RequestParameter> params = getParameters();
            for (Iterator<RequestParameter> iter = params.iterator(); iter.hasNext(); ) {
                RequestParameter p = iter.next();
                sb.append(p.getName());
                sb.append("=");
                String value = null;
                if (p.getName().equals("content")) {
                    value = URLEncoder.encode(p.getValue(), ENCODING);
                } else {
                    value = p.getValue();
                }
                sb.append(value);
                sb.append("&");
            }
            sb.deleteCharAt(sb.length() - 1);

        } catch (Exception e) {
            e.printStackTrace();
        }
        return DesECBUtil.dispatchStr(sb.toString()).getBytes();
    }
}
