package com.tingtingfm.cbb.common.net;

import java.util.Comparator;

/**
 * Created by lqsir on 2016/4/14.
 */
public class ParamComparable implements Comparator<RequestParameter> {

    @Override
    public int compare(RequestParameter lhs, RequestParameter rhs) {
        return lhs.getName().compareTo(rhs.getName());
    }
}
