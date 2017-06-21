package com.tingtingfm.cbb.bean;

import android.os.Parcel;
import android.os.Parcelable;

public class UpdateInfo implements Parcelable {
    private String url;
    private String v;
    private int force;
    private String intro;

    public String getUrl() {
        return url;
    }

    public String getVersion() {
        return v;
    }

    public int getForce() {
        return force;
    }

    public String getIntro() {
        return intro;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public void setV(String v) {
        this.v = v;
    }

    public void setForce(int force) {
        this.force = force;
    }

    public void setIntro(String intro) {
        this.intro = intro;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.url);
        dest.writeString(this.v);
        dest.writeInt(this.force);
        dest.writeString(this.intro);
    }

    public UpdateInfo() {
    }

    protected UpdateInfo(Parcel in) {
        this.url = in.readString();
        this.v = in.readString();
        this.force = in.readInt();
        this.intro = in.readString();
    }

    public static final Parcelable.Creator<UpdateInfo> CREATOR = new Parcelable.Creator<UpdateInfo>() {
        @Override
        public UpdateInfo createFromParcel(Parcel source) {
            return new UpdateInfo(source);
        }

        @Override
        public UpdateInfo[] newArray(int size) {
            return new UpdateInfo[size];
        }
    };
}