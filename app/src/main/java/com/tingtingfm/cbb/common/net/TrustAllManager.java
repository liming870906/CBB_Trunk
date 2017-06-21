package com.tingtingfm.cbb.common.net;

import java.security.cert.X509Certificate;
import java.security.cert.CertificateException;
import javax.net.ssl.X509TrustManager;

public class TrustAllManager implements X509TrustManager {

    @Override
    public void checkClientTrusted(X509Certificate[] certificates, String s) throws CertificateException {

    }

    @Override
    public void checkServerTrusted(X509Certificate[] certificates, String s) throws CertificateException {

    }

    @Override
    public java.security.cert.X509Certificate[] getAcceptedIssuers() {
        return new java.security.cert.X509Certificate[0];
    }
}