package com.tingtingfm.cbb.ui.eventbus;

/**
 * Created by liming on 17/4/17.
 */

public class MessageEvent {
    public int arg1, arg2, what;
    public Object obj;
    private MessageEvent messageEvent;

    public MessageEvent() {
        this.messageEvent = this;
    }

    public MessageEvent Obtion(int what) {
        this.what = what;
        return this.messageEvent;
    }

    public MessageEvent Obtion(int what, Object obj) {
        Obtion(what);
        this.obj = obj;
        return this.messageEvent;
    }

    public MessageEvent Obtion(int what, int arg1, int arg2) {
        Obtion(what);
        this.arg1 = arg1;
        this.arg2 = arg2;
        return this.messageEvent;
    }

    public MessageEvent Obtion(int what, int arg1, int arg2, Object obj) {
        Obtion(what, arg1, arg2);
        this.obj = obj;
        return this.messageEvent;
    }
}


