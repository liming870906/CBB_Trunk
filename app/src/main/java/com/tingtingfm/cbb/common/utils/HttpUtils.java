package com.tingtingfm.cbb.common.utils;

import com.tingtingfm.cbb.common.net.TrustAllManager;

import java.io.IOException;
import java.io.OutputStream;
import java.net.URL;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;

/**
 * Created by lqsir on 2017/3/29.
 */

public class HttpUtils {
    private static final String HEADER_ACCEPT_ENCODING = "Accept-Encoding";
    private static final String ENCODING_GZIP = "gzip";

    public static HttpsURLConnection getConnection(String newUrl) throws IOException,
            NoSuchAlgorithmException, KeyManagementException {
        URL url = new URL(newUrl);

        SSLContext context = SSLContext.getInstance("TLS");
        context.init(null, new TrustManager[] { new TrustAllManager() }, null);

        HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();
        connection.setSSLSocketFactory(context.getSocketFactory());
        connection.setHostnameVerifier(new HostnameVerifier() {
            @Override
            public boolean verify(String hostname, SSLSession session) {
                return true;
            }
        });
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Charset", "utf-8");
        connection.setRequestProperty(HEADER_ACCEPT_ENCODING, ENCODING_GZIP);
        connection.setConnectTimeout(30 * 1000);
        connection.setReadTimeout(30 * 1000);
        connection.setDoInput(true);
        connection.setDoOutput(true);

        return connection;
    }

    public static void postParams(OutputStream streams, byte[] params) throws IOException {
        if (params != null) {
            //写入参数值
            streams.write(params);
            //刷新、关闭
            streams.flush();
            streams.close();
        }
    }
}
