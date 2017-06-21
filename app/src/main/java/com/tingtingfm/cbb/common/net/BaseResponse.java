package com.tingtingfm.cbb.common.net;

public class BaseResponse {
    private int errno;
    private String error;
    private String server;
    private long server_time;

    public BaseResponse() {
    }

    public boolean hasError() {
        return errno != 0 ? true : false;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    public int getErrno() {
        return errno;
    }

    public void setErrno(int errno) {
        this.errno = errno;
    }

    public String getServer() {
        return server;
    }

    public void setServer(String server) {
        this.server = server;
    }

    public long getServer_time() {
        return server_time;
    }

    public void setServer_time(long server_time) {
        this.server_time = server_time;
    }
}
