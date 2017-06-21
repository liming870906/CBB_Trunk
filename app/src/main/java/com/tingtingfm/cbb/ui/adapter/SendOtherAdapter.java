package com.tingtingfm.cbb.ui.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.tingtingfm.cbb.R;
import com.tingtingfm.cbb.bean.ContactsInfo;
import com.tingtingfm.cbb.bean.DepartmentInfo;
import com.tingtingfm.cbb.bean.GroupInfo;
import com.tingtingfm.cbb.bean.SendInfo;
import com.tingtingfm.cbb.common.configuration.Constants;

import java.util.ArrayList;

/**
 * Created by liming on 17/1/6.
 */

public class SendOtherAdapter extends BaseAdapter {

    private SendInfo mSendInfo;
    private int mChoose;
    private LayoutInflater inflater;
    private Context context;

    public SendOtherAdapter(SendInfo mSendInfo, int mChoose, Context context) {
        this.mSendInfo = mSendInfo;
        this.mChoose = mChoose;
        this.inflater = LayoutInflater.from(context);
        this.context = context;
    }

    public void setData(SendInfo mSendInfo) {
        if (mSendInfo == null) {
            this.mSendInfo = new SendInfo(new ArrayList<ContactsInfo>(), new ArrayList<GroupInfo>(), new ArrayList<DepartmentInfo>());
        } else {
            this.mSendInfo = mSendInfo;
        }
        this.notifyDataSetChanged();
    }



    @Override
    public int getCount() {
        if (mSendInfo != null) {
            switch (mChoose) {
                case Constants.SEND_GROUP_NAVIGATION_TAG:
                    return mSendInfo.getGroup() != null ? mSendInfo.getGroup().size() : 0;
                case Constants.SEND_DEPARTMENT_NAVIGATION_TAG:
                    return mSendInfo.getDepartment() != null ? mSendInfo.getDepartment().size() : 0;
                default:
                    return 0;
            }
        } else {
            return 0;
        }
    }

    @Override
    public Object getItem(int i) {
        if (mSendInfo != null) {
            switch (mChoose) {
                case Constants.SEND_GROUP_NAVIGATION_TAG:
                    return mSendInfo.getGroup() != null ? mSendInfo.getGroup().get(i) : null;
                case Constants.SEND_DEPARTMENT_NAVIGATION_TAG:
                    return mSendInfo.getDepartment() != null ? mSendInfo.getDepartment().get(i) : i;
                default:
                    return 0;
            }
        } else {
            return null;
        }
    }

    @Override
    public long getItemId(int i) {
        return i;
    }


    @Override
    public View getView(int position, View view, ViewGroup viewGroup) {
        ViewHolderOther _otherHolder = null;
        if (view == null) {
            switch (mChoose) {
                case Constants.SEND_GROUP_NAVIGATION_TAG:
                case Constants.SEND_DEPARTMENT_NAVIGATION_TAG:
                    view = this.inflater.inflate(R.layout.item_send_other_lv, viewGroup, false);
                    _otherHolder = new ViewHolderOther();
                    _otherHolder.tvContent = (TextView) view.findViewById(R.id.tv_item_send_group_and_department);
                    _otherHolder.tvNumber = (TextView) view.findViewById(R.id.tv_item_send_group_and_department_number);
                    view.setTag(_otherHolder);
                    break;
            }
        } else {
            switch (mChoose) {
                case Constants.SEND_GROUP_NAVIGATION_TAG:
                case Constants.SEND_DEPARTMENT_NAVIGATION_TAG:
                    _otherHolder = (ViewHolderOther) view.getTag();
                    break;
            }
        }


        if (mChoose == Constants.SEND_GROUP_NAVIGATION_TAG) {
            GroupInfo _groupInfo = (GroupInfo) getItem(position);
            if (_groupInfo != null) {
                _otherHolder.tvContent.setText(context.getString(R.string.send_group_or_department_text, _groupInfo.getGroup_name()));
                _otherHolder.tvNumber.setText(context.getString(R.string.send_group_or_department_text_number,_groupInfo.getContact_count()));
            }
        } else if (mChoose == Constants.SEND_DEPARTMENT_NAVIGATION_TAG) {
            DepartmentInfo _departmentInfo = (DepartmentInfo) getItem(position);
            if (_departmentInfo != null) {
                _otherHolder.tvContent.setText(context.getString(R.string.send_group_or_department_text, _departmentInfo.getDepartment_name()));
                _otherHolder.tvNumber.setText(context.getString(R.string.send_group_or_department_text_number, _departmentInfo.getContact_count()));
            }
        }

        return view;
    }

    class ViewHolderOther {
        TextView tvContent,tvNumber;
    }

}
