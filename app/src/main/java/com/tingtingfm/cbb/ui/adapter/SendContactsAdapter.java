package com.tingtingfm.cbb.ui.adapter;

import android.content.Context;
import android.graphics.drawable.shapes.PathShape;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import com.tingtingfm.cbb.R;
import com.tingtingfm.cbb.bean.ContactsInfo;
import com.tingtingfm.cbb.bean.DepartmentInfo;
import com.tingtingfm.cbb.bean.GroupInfo;
import com.tingtingfm.cbb.bean.SendInfo;
import com.tingtingfm.cbb.common.configuration.AccoutConfiguration;
import com.tingtingfm.cbb.common.configuration.Constants;
import com.tingtingfm.cbb.common.utils.DisplayImageOptionsUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

/**
 * Created by liming on 17/1/6.
 */

public class SendContactsAdapter extends BaseAdapter {

    private SendInfo mSendInfo;
    private int mChoose;
    private LayoutInflater inflater;
    private Context context;
    private ContactsInfo mContactsInfo;

    public SendContactsAdapter(SendInfo mSendInfo, int mChoose, Context context) {
        this.mSendInfo = mSendInfo;
        this.mChoose = mChoose;
        this.inflater = LayoutInflater.from(context);
        this.context = context;
    }


    public void setData(SendInfo mSendInfo,ContactsInfo info) {
        this.mContactsInfo = info;
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
                case Constants.SEND_CONTACT_NAVIGATION_TAG:
                    return mSendInfo.getContacts() != null ? mSendInfo.getContacts().size() : 0;
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
                case Constants.SEND_CONTACT_NAVIGATION_TAG:
                    return mSendInfo.getContacts() != null ? mSendInfo.getContacts().get(i) : null;
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
        ViewHolderConstact _contactsHolder = null;
        if (view == null) {
            switch (mChoose) {
                case Constants.SEND_CONTACT_NAVIGATION_TAG:
                    view = this.inflater.inflate(R.layout.item_send_contacts_lv, viewGroup, false);
                    _contactsHolder = new ViewHolderConstact();
                    _contactsHolder.ibChoose = (CheckBox) view.findViewById(R.id.cb_item_send_choose);
                    _contactsHolder.ivIcon = (ImageView) view.findViewById(R.id.iv_item_send_contact_icon);
                    _contactsHolder.tvName = (TextView) view.findViewById(R.id.tv_item_send_contact_name);
                    _contactsHolder.tvRote = (TextView) view.findViewById(R.id.tv_item_send_rote);
                    view.setTag(_contactsHolder);
                    break;
            }
        } else {
            switch (mChoose) {
                case Constants.SEND_CONTACT_NAVIGATION_TAG:
                    _contactsHolder = (ViewHolderConstact) view.getTag();
                    break;
            }
        }

        if (mChoose == Constants.SEND_CONTACT_NAVIGATION_TAG) {
            ContactsInfo _contactsInfo = (ContactsInfo) getItem(position);
            if (_contactsInfo != null) {
                _contactsHolder.tvName.setText(_contactsInfo.getAdmin_name());
                _contactsHolder.tvRote.setText(_contactsInfo.getDept()+"-"+_contactsInfo.getRole());
                _contactsHolder.ibChoose.setChecked(mContactsInfo == null ? false : mContactsInfo.getAdmin_id() == _contactsInfo.getAdmin_id() ? true : false);
                //头像设置
                DisplayImageOptionsUtils.getInstance().displayImage(_contactsInfo.getFace_url(), _contactsHolder.ivIcon,true);
                if(_contactsInfo.getAdmin_id() == AccoutConfiguration.getLoginInfo().getUserid()){
                    _contactsHolder.ibChoose.setVisibility(View.INVISIBLE);
                }else{
                    _contactsHolder.ibChoose.setVisibility(View.VISIBLE);
                }
            }
        }
        return view;
    }

    class ViewHolderConstact {
        ImageView ivIcon;
        TextView tvName, tvRote;
        CheckBox ibChoose;
    }

}
