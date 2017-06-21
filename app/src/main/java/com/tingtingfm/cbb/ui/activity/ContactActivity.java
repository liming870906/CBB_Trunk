package com.tingtingfm.cbb.ui.activity;

import android.content.Intent;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ListView;

import com.tingtingfm.cbb.R;
import com.tingtingfm.cbb.bean.ContactsInfo;
import com.tingtingfm.cbb.bean.DepartmentInfo;
import com.tingtingfm.cbb.bean.GroupInfo;
import com.tingtingfm.cbb.common.configuration.AccoutConfiguration;
import com.tingtingfm.cbb.common.configuration.Constants;
import com.tingtingfm.cbb.common.configuration.UrlManager;
import com.tingtingfm.cbb.common.helper.HttpRequestHelper;
import com.tingtingfm.cbb.common.net.BaseRequestCallback;
import com.tingtingfm.cbb.common.net.RequestEntity;
import com.tingtingfm.cbb.response.GetContactsInfoResponse;
import com.tingtingfm.cbb.response.SendMaterialResponse;
import com.tingtingfm.cbb.ui.adapter.ContactsAdapter;

import java.util.List;

import butterknife.BindView;

public class ContactActivity extends BaseActivity implements AdapterView.OnItemClickListener {

    @BindView(R.id.lv_contact_data_view)
    ListView lvContactData;
    @BindView(R.id.ll_contact_no_data_layout)
    LinearLayout llContactNodataLayout;
    private ContactsInfo mContactsInfo;
    private GroupInfo mGroupInfo;
    private DepartmentInfo mDepartmentInfo;
    private String mTitle;
    private int mCount;
    private ContactsAdapter mAdapter;
    private String mType;
    private List<ContactsInfo> mInfos;
    private String mIDList;

    private static final String TAG = "ContactActivity====>";

    @Override
    protected View initContentView() {
        return LayoutInflater.from(this).inflate(R.layout.activity_contact, null);
    }

    @Override
    protected void handleCreate() {
        //引用获得数据方法
        getIntentData();
        //设置标题
        setTitle();
        //设置右上角按钮文本
        setRightView3Visibility(View.INVISIBLE);
        //设置文本
        setRightView3Content(R.string.send_right_submit_text);
        //声明适配器对象
        mAdapter = new ContactsAdapter(null, this);
        //设置
        lvContactData.setAdapter(mAdapter);
        //加载数据
        loadData();
        //添加监听器方法
        addListener();
    }

    private void setTitle() {
        mContentView.setEllipsize(TextUtils.TruncateAt.END);
        if (mCount > 0) {
            //设置标题
            mContentView.setText(getString(R.string.send_group_or_department_text, mTitle) + getString(R.string.send_group_or_department_text_number, mCount));
        } else {
            mContentView.setText(getString(R.string.send_group_or_department_text, mTitle));
        }
    }

    @Override
    protected void onRightView3Click() {
        super.onRightView3Click();
        if (mContactsInfo != null) {
            if (mContactsInfo.getAdmin_id() == AccoutConfiguration.getLoginInfo().getUserid()) {
                showToast(R.string.send_title_click_not_send_tips);
            } else {
                sendMaterial();
            }
        } else {
            showToast(R.string.send_title_click_tips);
        }
    }

    /**
     * 添加监听器方法
     */
    private void addListener() {
        lvContactData.setOnItemClickListener(this);
    }

    /**
     * 网络请求数据
     */
    private void loadData() {
        //请求数据
        RequestEntity _entity = new RequestEntity(UrlManager.GET_CONTACTS_INFO);
        if (Constants.SEND_GOTO_TYPE_VALUE_GROUP.equals(mType)) {
            _entity.addParams("type", "group");
            _entity.addParams("id", String.valueOf(mGroupInfo.getGroup_id()));
        } else if (Constants.SEND_GOTO_TYPE_VALUE_DEPARTMENT.equals(mType)) {
            _entity.addParams("type", "dept");
            _entity.addParams("id", String.valueOf(mDepartmentInfo.getDepartment_id()));
        }
        HttpRequestHelper.post(_entity, new BaseRequestCallback<GetContactsInfoResponse>() {
            @Override
            public void onStart() {

            }

            @Override
            public void onSuccess(GetContactsInfoResponse response) {
                Log.i(TAG, response.getData().toString());
                //获得数据
                mInfos = response.getData();
                //发送更新数据
                basicHandler.sendEmptyMessage(Constants.UPDATE_CONTACTS_DATA_TAG);
            }

            @Override
            public void onFail(int code, String errorMessage) {
                showToast(errorMessage);
            }

            @Override
            public void onCancel() {

            }
        });
    }

    /**
     * 获得数据方法
     */
    private void getIntentData() {
        Intent _intent = getIntent();
        mType = _intent.getStringExtra(Constants.SEND_GOTO_TYPE_KEY);
        if (Constants.SEND_GOTO_TYPE_VALUE_GROUP.equals(mType)) {
            mGroupInfo = (GroupInfo) _intent.getSerializableExtra(Constants.SEND_GOTO_KEY_OTHER_INFO);
            if (mGroupInfo != null) {
                mTitle = mGroupInfo.getGroup_name();
                mCount = mGroupInfo.getContact_count();
            }
        } else if (Constants.SEND_GOTO_TYPE_VALUE_DEPARTMENT.equals(mType)) {
            mDepartmentInfo = (DepartmentInfo) _intent.getSerializableExtra(Constants.SEND_GOTO_KEY_OTHER_INFO);
            if (mDepartmentInfo != null) {
                mTitle = mDepartmentInfo.getDepartment_name();
                mCount = mDepartmentInfo.getContact_count();
            }
        }
//        mContactsInfo = (ContactsInfo) _intent.getSerializableExtra(Constants.SEND_GOTO_KEY_CONTACTS_INFO);
        mIDList = _intent.getStringExtra(Constants.SEND_ID_LIST);
    }

    @Override
    protected void processMessage(Message msg) {
        switch (msg.what) {
            case Constants.UPDATE_CONTACTS_DATA_TAG:
                if (mInfos != null && mInfos.size() > 0) {
                    mAdapter.setData(mInfos, mContactsInfo);
                    changeShowNoView(false);
                } else {
                    changeShowNoView(true);
                }
                break;
        }
    }

    /**
     * 是否显示没有布局
     *
     * @param isVisibility
     */
    private void changeShowNoView(boolean isVisibility) {
        lvContactData.setVisibility(isVisibility ? View.GONE : View.VISIBLE);
        llContactNodataLayout.setVisibility(isVisibility ? View.VISIBLE : View.GONE);
        setRightView3Visibility(isVisibility ? View.GONE : View.VISIBLE);
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        ContactsInfo _contactsInfo = (ContactsInfo) mAdapter.getItem(i);
        if (_contactsInfo.getAdmin_id() != AccoutConfiguration.getLoginInfo().getUserid()) {
            //判断是否为同一个对象
            if(mContactsInfo == null){
                mContactsInfo = _contactsInfo;
            }else{
                if(mContactsInfo.getAdmin_id() == _contactsInfo.getAdmin_id()){
                    mContactsInfo = null;
                }else{
                    mContactsInfo = _contactsInfo;
                }
            }
            //发送更新消息
            Message.obtain(basicHandler,Constants.UPDATE_CONTACTS_DATA_TAG).sendToTarget();
        }
    }

    /**
     * 发送素材
     */
    private void sendMaterial(){
        RequestEntity _entity = new RequestEntity(UrlManager.MATERIAL_SEND);
        _entity.addParams("id_list",mIDList);
        _entity.addParams("contact",String.valueOf(mContactsInfo.getAdmin_id()));
        HttpRequestHelper.post(_entity, new BaseRequestCallback<SendMaterialResponse>() {
            @Override
            public void onStart() {
                showLoadDialog();
            }

            @Override
            public void onSuccess(SendMaterialResponse response) {
                switch (response.getData().getSucc()){
                    case 1:
                        showToast(R.string.send_success);
//                        Intent _intent = new Intent();
//                        _intent.putExtra(Constants.SEND_GOTO_KEY_CONTACTS_INFO, mContactsInfo);
//                        setResult(RESULT_OK, _intent);
//                        finish();
                        break;
                    case 0:
                        showToast(R.string.send_failure);
                        break;
                }
                mContactsInfo = null;
                //发送更新消息
                Message.obtain(basicHandler,Constants.UPDATE_CONTACTS_DATA_TAG).sendToTarget();
            }

            @Override
            public void onFail(int code, String errorMessage) {
                showToast(errorMessage);
            }

            @Override
            public void onCancel() {
                dismissDlg();
            }
        });
    }
}
