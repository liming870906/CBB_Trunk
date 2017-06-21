package com.tingtingfm.cbb.common.upload.config;

import java.util.UUID;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLSocketFactory;

/**
 * Created by lqsir on 2017/4/17.
 */

public final class UploadConfiguration {
    private SSLSocketFactory mSSLSocketFactory;
    private HostnameVerifier mHostnameVerifier;

    private String content_type;
    private String boundary;
    private String prefix;
    private String line_end;

    private UploadConfiguration(final Builder builder) {
        mSSLSocketFactory = builder.mSSLSocketFactory;
        mHostnameVerifier = builder.mHostnameVerifier;
        content_type = builder.content_type;
        boundary = builder.boundary;
        prefix = builder.prefix;
        line_end = builder.line_end;
    }

    public SSLSocketFactory getSSLSocketFactory() {
        return mSSLSocketFactory;
    }

    public HostnameVerifier getHostnameVerifier() {
        return mHostnameVerifier;
    }

    public String getContent_type() {
        return content_type;
    }

    public String getBoundary() {
        return boundary;
    }

    public String getPrefix() {
        return prefix;
    }

    public String getLine_end() {
        return line_end;
    }

    public static class Builder {
        private SSLSocketFactory mSSLSocketFactory;
        private HostnameVerifier mHostnameVerifier;

        private String content_type = "multipart/form-data";
        private String boundary = UUID.randomUUID().toString();
        private String prefix = "--";
        private String line_end = "\r\n";


        public UploadConfiguration build() {
            initDefaultConfiguration();
            return new UploadConfiguration(this);
        }

        private void initDefaultConfiguration() {
            if (mSSLSocketFactory == null) {
                mSSLSocketFactory = UploadConfigurationFactory.getSSLSocketFactory();
            }

            if (mHostnameVerifier == null) {
               mHostnameVerifier = UploadConfigurationFactory.getHostnameVerifier();
            }
        }
    }
}
