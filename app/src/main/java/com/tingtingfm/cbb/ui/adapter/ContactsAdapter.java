package com.tingtingfm.cbb.ui.adapter;

import android.content.Context;
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
import java.util.List;

/**
 * Created by liming on 17/1/6.
 */

public class ContactsAdapter extends BaseAdapter {

    private LayoutInflater inflater;
    private Context context;
    private List<ContactsInfo> mContactsInfos;
    private ContactsInfo mContactsInfo;

    public ContactsAdapter(ArrayList<ContactsInfo> mContactsInfos, Context context) {
        this.inflater = LayoutInflater.from(context);
        this.context = context;
        this.mContactsInfos = mContactsInfos;
    }


    public void setData(List<ContactsInfo> mContactsInfos, ContactsInfo info) {
        this.mContactsInfo = info;
        if (mContactsInfos == null) {
            this.mContactsInfos = new ArrayList<ContactsInfo>();
        } else {
            this.mContactsInfos = mContactsInfos;
        }
        this.notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return mContactsInfos != null ? mContactsInfos.size() : 0;
    }

    @Override
    public Object getItem(int i) {
        return mContactsInfos != null ? mContactsInfos.get(i) : null;
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int position, View view, ViewGroup viewGroup) {
        ViewHolderConstact _contactsHolder = null;
        if (view == null) {
            view = this.inflater.inflate(R.layout.item_send_contacts_lv, viewGroup, false);
            _contactsHolder = new ViewHolderConstact();
            _contactsHolder.ibChoose = (CheckBox) view.findViewById(R.id.cb_item_send_choose);
            _contactsHolder.ivIcon = (ImageView) view.findViewById(R.id.iv_item_send_contact_icon);
            _contactsHolder.tvName = (TextView) view.findViewById(R.id.tv_item_send_contact_name);
            _contactsHolder.tvRote = (TextView) view.findViewById(R.id.tv_item_send_rote);
            view.setTag(_contactsHolder);
        } else {
            _contactsHolder = (ViewHolderConstact) view.getTag();
        }

        ContactsInfo _contactsInfo = (ContactsInfo) getItem(position);
        if (_contactsInfo != null) {
            _contactsHolder.tvName.setText(_contactsInfo.getAdmin_name());
            _contactsHolder.tvRote.setText(_contactsInfo.getDept() + "-" + _contactsInfo.getRole());
            _contactsHolder.ibChoose.setChecked(mContactsInfo == null ? false : mContactsInfo.getAdmin_id() == _contactsInfo.getAdmin_id() ? true : false);
            //头像设置
            DisplayImageOptionsUtils.getInstance().displayImage(_contactsInfo.getFace_url(), _contactsHolder.ivIcon, true);
            if (_contactsInfo.getAdmin_id() == AccoutConfiguration.getLoginInfo().getUserid()) {
                _contactsHolder.ibChoose.setVisibility(View.INVISIBLE);
            } else {
                _contactsHolder.ibChoose.setVisibility(View.VISIBLE);
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
