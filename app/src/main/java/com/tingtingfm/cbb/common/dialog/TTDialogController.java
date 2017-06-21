package com.tingtingfm.cbb.common.dialog;

import android.content.Context;
import android.content.DialogInterface;
import android.content.res.TypedArray;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckedTextView;
import android.widget.CursorAdapter;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

import com.tingtingfm.cbb.R;
import com.tingtingfm.cbb.common.utils.DensityUtils;

import java.lang.ref.WeakReference;

public class TTDialogController {
    private final Context mContext;
    private final DialogInterface mDialogInterface;
    private final Window mWindow;
    public int mPositiveButtonStyleId = -1; //background style
    public int mNegativeButtonStyleId = -1; //background style
    public int mNeutralButtonStyleId = -1; //background style
    public int mPositiveButtonTextColorId = -1; //textColor
    public int mNegativeButtonTextColorId = -1; //textColor
    public int mNeutralButtonTextColorId = -1; //textColor
    public boolean mViewSpacingSpecified = false;
    public boolean mIsProgress = false;//true 表示显示进度条加载框
    public int mViewSpacingLeft; // 距离左边的空间
    public int mViewSpacingTop; // 距离顶部的空间
    public int mViewSpacingRight; // 距离右边的空间
    public int mViewSpacingBottom; // 距离底部的空间
    public boolean[] mCheckedItems;
    public int mCheckedItem = -1;
    public ListAdapter mAdapter;
    private CharSequence mButtonNegativeText; // 取消按钮文本
    private CharSequence mButtonPositiveText; // 确定按钮文本
    private Message mButtonPositiveMessage;
    private CharSequence mButtonNeutralText; // 默认按钮文本
    private Button mButtonPositive;
    private Button mButtonNegative;
    private Button mButtonNeutral;
    //中间按钮+右边按钮的左边线
    private View mNegativeLine;
    private View mNeutralLine;
    private View mView; // 自定义的view
    private CharSequence mMessage;
    private boolean isCenter;
    private boolean isSignleLine;
    private Handler mHandler;
    private int mIconId = -1;
    private Drawable mIcon;
    private ImageView mIconView; // 图标
    private TextView mTitleView;
    private boolean mForceInverseBackground; // 逆向背景
    private TextView mMessageView;
    private Message mButtonNeutralMessage;
    private Message mButtonNegativeMessage;
    View.OnClickListener mButtonHandler = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Message m = null;
            if (v == mButtonPositive && mButtonPositiveMessage != null) {
                m = Message.obtain(mButtonPositiveMessage);
            } else if (v == mButtonNegative && mButtonNegativeMessage != null) {
                m = Message.obtain(mButtonNegativeMessage);
            } else if (v == mButtonNeutral && mButtonNeutralMessage != null) {
                m = Message.obtain(mButtonNeutralMessage);
            }
            if (m != null) {
                m.sendToTarget();
            }
            // post a message so we dismiss after the above handlers are
            // executed
            mHandler.obtainMessage(ButtonHandler.MSG_DISMISS_DIALOG,
                    mDialogInterface).sendToTarget();
        }
    };
    private ScrollView mScrollView;
    private ListView mListView;
    private CharSequence mTitle;
    public TTDialogController(Context context, DialogInterface di,
                              Window window) {
        this.mContext = context;
        this.mDialogInterface = di;
        this.mWindow = window;
        this.mHandler = new ButtonHandler(di);

    }

    /**
     *
     */
    static boolean canTextInput(View v) {
        if (v.onCheckIsTextEditor()) {
            return true;
        }
        if (!(v instanceof ViewGroup)) {
            return false;
        }
        ViewGroup vg = (ViewGroup) v;
        int i = vg.getChildCount();
        while (i > 0) {
            i--;
            v = vg.getChildAt(i);
            if (canTextInput(v)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 初始化
     */
    public void installContent() {
        mWindow.requestFeature(Window.FEATURE_NO_TITLE);

        WindowManager.LayoutParams lp = mWindow.getAttributes();
        lp.dimAmount = 0.1f;
        mWindow.setAttributes(lp);
        mWindow.addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);

        if (mView == null || !canTextInput(mView)) {
            mWindow.setFlags(WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM, // 可以在编辑的时候可以与输入法交互
                    WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM);
        }


        mWindow.setContentView(R.layout.tt_alert_dialog);
        mWindow.setGravity(Gravity.CENTER_VERTICAL);

        setupView();

    }

    private void setupView() {
        LinearLayout contentPanel = (LinearLayout) mWindow
                .findViewById(R.id.contentPanel);
        setupContent(contentPanel);

        boolean hasButtons = setupButtons();

        LinearLayout topPanel = (LinearLayout) mWindow
                .findViewById(R.id.topPanel);

        TypedArray a = mContext.obtainStyledAttributes(null,
                R.styleable.TTAlertDialog, 0, 0);

        boolean hasTitle = setupTitle(topPanel);

        LinearLayout buttonPanel = (LinearLayout) mWindow.findViewById(R.id.buttonPanel);
//        View viewLine = mWindow.findViewById(R.id.view_line1);
//        if (TextUtils.isEmpty(mMessage) || TextUtils.isEmpty(mTitle)) {
//            viewLine.setVisibility(View.GONE);
//        }
        if (!hasButtons) {
            buttonPanel.setVisibility(View.GONE);
        }

        FrameLayout customPanel = null;
        if (mView != null) {
            customPanel = (FrameLayout) mWindow.findViewById(R.id.customPanel);
            FrameLayout custom = (FrameLayout) mWindow.findViewById(R.id.custom);

            if (!mIsProgress) {
//                int width = (int) ScreenUtils.getScreenWidth() - DensityUtils.dp2px(mContext, 20.6f) * 2;
                custom.addView(mView, new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT));
            } else {
                custom.addView(mView, new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT));
            }

            if (mViewSpacingSpecified) {
                custom.setPadding(mViewSpacingLeft, mViewSpacingTop,
                        mViewSpacingRight, mViewSpacingBottom);
            }
            if (mListView != null) {
                ((LinearLayout.LayoutParams) customPanel.getLayoutParams()).weight = 0;
            }

        } else {
            mWindow.findViewById(R.id.customPanel).setVisibility(View.GONE);
        }

        setBackground(topPanel, contentPanel, customPanel, hasButtons, a,
                true, buttonPanel);
        a.recycle();

    }

    public void setTitle(CharSequence title) {
        mTitle = title;
        if (mTitleView != null) {
            mTitleView.setText(title);
        }
    }

    private boolean setupTitle(LinearLayout topPanel) {
        boolean hasTitle = true;
        final boolean hasTextTitle = !TextUtils.isEmpty(mTitle);
//        mIconView = (ImageView) mWindow.findViewById(R.id.icon);
        if (hasTextTitle) {
            mTitleView = (TextView) topPanel.findViewById(R.id.alertTitle);
            mTitleView.setVisibility(View.VISIBLE);
            mTitleView.setText(mTitle);
//            if (mIconId > 0) {
//                mIconView.setImageResource(mIconId);
//            } else if (mIcon != null) {
//                mIconView.setImageDrawable(mIcon);
//            } else if (mIconId == 0) {
//                mIconView.setVisibility(View.GONE);
//            }
        } else {
            // 隐藏标题的模版
            mTitleView = (TextView) topPanel.findViewById(R.id.alertTitle);
            mTitleView.setVisibility(View.GONE);
//            mIconView.setVisibility(View.GONE);
//            topPanel.setVisibility(View.GONE);
            hasTitle = false;
        }
        return hasTitle;
    }

    public void setMessage(CharSequence message) {
        mMessage = message;
        if (mMessageView != null) {
            mMessageView.setText(message);
        }
    }

    public void setMessageGravity(boolean center) {
        isCenter = center;
        if (mMessageView != null) {
            mMessageView.setGravity(center ? Gravity.CENTER : Gravity.LEFT);
        }
    }

    public void setMessageLine(boolean signleLine) {
        isSignleLine = signleLine;
    }

    /**
     * set the view to display in the dialog
     */
    public void setView(View view) {
        mView = view;
        mViewSpacingSpecified = false;
    }

    public void setView(View view, boolean isProgress) {
        mView = view;
        mIsProgress = isProgress;
    }

    /**
     * set view to display in the dialog along with the spacing that view
     */
    public void setView(View view, int viewSpacingLeft, int viewSpacingTop, int viewSpacingRight,
                        int viewSpacingBottom) {
        mView = view;
        mViewSpacingSpecified = true;
        mViewSpacingBottom = viewSpacingBottom;
        mViewSpacingLeft = viewSpacingLeft;
        mViewSpacingRight = viewSpacingRight;
        mViewSpacingTop = viewSpacingTop;
    }

    /**
     * set the icon to display in the dialog
     */
    public void setIcon(Drawable icon) {
        mIcon = icon;
        if ((mIconView != null) && (mIcon != null)) {
            mIconView.setImageDrawable(icon);
        }
    }

    public void setIcon(int resId) {
        mIconId = resId;
        if (mIconView != null) {
            if (resId > 0) {
                mIconView.setImageResource(mIconId);
            } else if (resId == 0) {
                mIconView.setVisibility(View.GONE);
            }
        }
    }

    public void setInverseBackgroundForced(boolean forceInverseBackground) {
        mForceInverseBackground = forceInverseBackground;
    }

    public ListView getListView() {
        return mListView;
    }

    /**
     * 获得button
     */
    public Button getButton(int whichButton) {
        switch (whichButton) {
            case DialogInterface.BUTTON_POSITIVE:
                return mButtonPositive;
            case DialogInterface.BUTTON_NEGATIVE:
                return mButtonNegative;
            case DialogInterface.BUTTON_NEUTRAL:
                return mButtonNeutral;
            default:
                return null;
        }
    }

    public void setButton(int whichButton, CharSequence text,
                          DialogInterface.OnClickListener listener, Message msg, int styleId, int textColorId) {
        if (msg == null && listener != null) {
            msg = mHandler.obtainMessage(whichButton, listener);
        }
        switch (whichButton) {

            case DialogInterface.BUTTON_POSITIVE:
                mButtonPositiveText = text;
                mButtonPositiveMessage = msg;
                mPositiveButtonStyleId = styleId;
                mPositiveButtonTextColorId = textColorId;
                break;

            case DialogInterface.BUTTON_NEGATIVE:
                mButtonNegativeText = text;
                mButtonNegativeMessage = msg;
                mNegativeButtonStyleId = styleId;
                mNegativeButtonTextColorId = textColorId;
                break;

            case DialogInterface.BUTTON_NEUTRAL:
                mButtonNeutralText = text;
                mButtonNeutralMessage = msg;
                mNeutralButtonStyleId = styleId;
                mNeutralButtonTextColorId = textColorId;
                break;

            default:
                throw new IllegalArgumentException("Button does not exist");
        }
    }

    public boolean onKeyDown(int keyCode, KeyEvent event) {
        return mScrollView != null && mScrollView.executeKeyEvent(event);
    }

    public boolean onKeyUp(int keyCode, KeyEvent event) {
        return mScrollView != null && mScrollView.executeKeyEvent(event);
    }

    /**
     * set contentPanel to display in the dialog 在对话框中显示内容
     */
    private void setupContent(LinearLayout contentPanel) {
        mScrollView = (ScrollView) mWindow.findViewById(R.id.scrollView);
        mScrollView.setFocusable(false);

        mMessageView = (TextView) mWindow.findViewById(R.id.message);
        if (mMessageView == null) {
            return;
        }

        if (mMessage != null) {
            mMessageView.setText(mMessage);

            if (isCenter) {
                mMessageView.setGravity(isCenter ? Gravity.CENTER : Gravity.LEFT);
                int paddingTop = 0;
                int paddingBottom = 0;

                if (isSignleLine) {
                    paddingTop = DensityUtils.dp2px(mContext, 4.0f);
                    paddingBottom = DensityUtils.dp2px(mContext, 13.7f);
                }

                mMessageView.setPadding(mMessageView.getPaddingLeft(),
                        0,
                        mMessageView.getPaddingRight(),
                        paddingBottom);
            }
        } else {
            mMessageView.setVisibility(View.GONE);
            mScrollView.removeView(mMessageView);

            if (mListView != null) {
                contentPanel.removeView(mWindow.findViewById(R.id.scrollView));
                contentPanel.addView(mListView, new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT));
                contentPanel.setLayoutParams(new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT, 0, 1.0f));
            } else {
                contentPanel.setVisibility(View.GONE);
            }
        }

    }

    private boolean setupButtons() {
        int BIT_BUTTON_POSITIVE = 1;
        int BIT_BUTTON_NEGATIVE = 2;
        int BIT_BUTTON_NEUTRAL = 4;
        int whichButtons = 0;
        mButtonPositive = (Button) mWindow.findViewById(R.id.button1);
        mButtonPositive.setOnClickListener(mButtonHandler);
        if (mPositiveButtonStyleId != -1) {
            mButtonPositive.setTextColor(mPositiveButtonTextColorId);
            mButtonPositive.setBackgroundResource(mPositiveButtonStyleId);
        }
        if (TextUtils.isEmpty(mButtonPositiveText)) {
            mButtonPositive.setVisibility(View.GONE);
        } else {
            mButtonPositive.setText(mButtonPositiveText);
            mButtonPositive.setVisibility(View.VISIBLE);
            whichButtons = whichButtons | BIT_BUTTON_POSITIVE;
        }

        mNegativeLine = (View) mWindow.findViewById(R.id.button2_line);
        mButtonNegative = (Button) mWindow.findViewById(R.id.button2);
        mButtonNegative.setOnClickListener(mButtonHandler);
        if (mNegativeButtonStyleId != -1) {
            mButtonNegative.setTextColor(mNegativeButtonTextColorId);
            mButtonNegative.setBackgroundResource(mNegativeButtonStyleId);
        }
        if (TextUtils.isEmpty(mButtonNegativeText)) {
            mButtonNegative.setVisibility(View.GONE);
			mNegativeLine.setVisibility(View.GONE);
        } else {
            mButtonNegative.setText(mButtonNegativeText);
            mButtonNegative.setVisibility(View.VISIBLE);
			mNegativeLine.setVisibility(View.VISIBLE);

            whichButtons = whichButtons | BIT_BUTTON_NEGATIVE;
        }

        mNeutralLine = (View) mWindow.findViewById(R.id.button3_line);
        mButtonNeutral = (Button) mWindow.findViewById(R.id.button3);
        mButtonNeutral.setOnClickListener(mButtonHandler);
        if (mNeutralButtonStyleId != -1) {
            mButtonNeutral.setTextColor(mNeutralButtonTextColorId);
            mButtonNeutral.setBackgroundResource(mNeutralButtonStyleId);
        }
        if (TextUtils.isEmpty(mButtonNeutralText)) {
            mButtonNeutral.setVisibility(View.GONE);
			mNeutralLine.setVisibility(View.GONE);
        } else {
            mButtonNeutral.setText(mButtonNeutralText);
            mButtonNeutral.setVisibility(View.VISIBLE);
			mNeutralLine.setVisibility(View.VISIBLE);

            whichButtons = whichButtons | BIT_BUTTON_NEUTRAL;
        }

		/*
         * If we only have 1 button it should be centered on the layout and
		 * expand to fill 50% of the available space.
		 */
        if (whichButtons == BIT_BUTTON_POSITIVE) {
            centerButton(mButtonPositive);
        } else if (whichButtons == BIT_BUTTON_NEGATIVE) {
            centerButton(mButtonNeutral);
        } else if (whichButtons == BIT_BUTTON_NEUTRAL) {
            centerButton(mButtonNeutral);
        }

        return whichButtons != 0;
    }

    /**
     * 设置button的属性
     */
    private void centerButton(Button button) {
//		LinearLayout.LayoutParams params = (LayoutParams) button
//				.getLayoutParams();
//		params.gravity = Gravity.CENTER_HORIZONTAL;
//		params.weight = 0.5f;
//		button.setLayoutParams(params);

//		View leftSpacer = mWindow.findViewById(R.id.leftSpacer); // 左边的空间
//		leftSpacer.setVisibility(View.VISIBLE);
//		View rightSpacer = mWindow.findViewById(R.id.rightSpacer); // 右边的空间
//		rightSpacer.setVisibility(View.VISIBLE);
//        button.setBackgroundDrawable(mContext.getResources().getDrawable(R.drawable.button_blue_bg_blue_normal));
    }

    private void setBackground(LinearLayout topPanel,
                               LinearLayout contentPanel, View customPanel, boolean hasButtons,
                               TypedArray a, boolean hasTitle, LinearLayout buttonPanel) {

        /**
         * Get all the different background required
         */
        int fullDark = a.getResourceId(R.styleable.TTAlertDialog_bottomBright,
                R.drawable.dialog_rectangle);
        int topDark = a.getResourceId(R.styleable.TTAlertDialog_topDark,
                R.drawable.dialog_rectangle_top);
        int centerDark = a.getResourceId(R.styleable.TTAlertDialog_centerDark,
                R.drawable.dialog_rectangle_center);
        int bottomDark = a.getResourceId(R.styleable.TTAlertDialog_bottomDark,
                R.drawable.dialog_rectangle_bottom);
        int fullBright = a.getResourceId(R.styleable.TTAlertDialog_fullBright,
                R.drawable.dialog_rectangle);
        int topBright = a.getResourceId(R.styleable.TTAlertDialog_topBright,
                R.drawable.dialog_rectangle_top);
        int centerBright = a.getResourceId(
                R.styleable.TTAlertDialog_centerBright,
                R.drawable.dialog_rectangle_center);
        int bottomBright = a.getResourceId(
                R.styleable.TTAlertDialog_bottomBright,
                R.drawable.dialog_rectangle_bottom);
        int bottomMedium = a.getResourceId(
                R.styleable.TTAlertDialog_bottomMedium,
                R.drawable.dialog_rectangle_bottom);

        /**
         * 设置个view的背景 首先判断背景颜色是否为 light or dark 再去运行时去设置背景颜色 top，middle， bottom
         * ，full views
         */
        View[] views = new View[4];
        boolean[] light = new boolean[4];
        View lastView = null;
        boolean lastLight = false;

        int pos = 0;
        if (hasTitle) {
            views[pos] = topPanel;
            light[pos] = false;
            pos++;
        }
        /**
         * 内容view 不管显示一个text 或者 listview 如果是listview 我们就是使用的 ..背景 如果都不是则去隐藏
         * 设置为null
         */
        views[pos] = (contentPanel.getVisibility() == View.GONE) ? null : contentPanel;
        light[pos] = mListView != null;
        pos++;

        if (customPanel != null) {
            views[pos] = customPanel;
            light[pos] = mForceInverseBackground;
            pos++;
        }
        if (hasButtons) {
            views[pos] = buttonPanel;
            light[pos] = true;
        }

        boolean setView = false;
        for (pos = 0; pos < views.length; pos++) {
            View v = views[pos];
            if (v == null) {
                continue;
            }
            if (lastView != null) {
                if (!setView) {
//                    lastView.setBackgroundResource(topDark);
                } else {
                    lastView.setBackgroundResource(centerDark);
                }
                setView = true;

            }
            lastView = v;
            lastLight = light[pos];
        }

        if (lastView != null) {
            // ListViews set Bright background but button use Medium background
            if (setView) {
                lastView.setBackgroundResource(bottomDark);
            } else {
                lastView.setBackgroundResource(fullDark);
            }
        }
        /**
         * 如果显示上下文文菜单 and 只要按一个取消按钮 然后去显示
         */
        if ((mListView != null) && (mAdapter != null)) {
            mListView.setAdapter(mAdapter);
            if (mCheckedItem > -1) {
                mListView.setItemChecked(mCheckedItem, true);
                mListView.setSelection(mCheckedItem);
            }
        }
    }

    /**
     * Button clicks have Message.what as the BUTTON{1,2,3} constant
     */
    private static final class ButtonHandler extends Handler {
        private static final int MSG_DISMISS_DIALOG = 1;

        private WeakReference<DialogInterface> mDialog;

        public ButtonHandler(DialogInterface dialog) {
            mDialog = new WeakReference<DialogInterface>(dialog);
        }

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {

                case DialogInterface.BUTTON_POSITIVE:
                case DialogInterface.BUTTON_NEGATIVE:
                case DialogInterface.BUTTON_NEUTRAL:
                    ((DialogInterface.OnClickListener) msg.obj).onClick(
                            mDialog.get(), msg.what);
                    break;

                case MSG_DISMISS_DIALOG:
                    ((DialogInterface) msg.obj).dismiss();
            }
        }
    }

    public static class RecycleListView extends ListView {
        boolean mRecycleOnMeasure = true;

        public RecycleListView(Context context) {
            super(context);
        }

        public RecycleListView(Context context, AttributeSet attrs) {
            super(context, attrs);
        }

        public RecycleListView(Context context, AttributeSet attrs, int defStyle) {
            super(context, attrs, defStyle);
        }

        protected boolean recycleOnMesaure() {
            return mRecycleOnMeasure;
        }

    }

    public static class LFDialogParams {
        public final Context mContext;

        public final LayoutInflater mInflater;

        public int mIconId = 0;
        public Drawable mIcon;
        public CharSequence mTitle;
        public View mCustomTitleView;
        public CharSequence mMessage;
        public boolean isCenter;
        public boolean isSingleLine;
        public CharSequence mPositiveButtonText;
        public DialogInterface.OnClickListener mPositiveButtonListener;
        public CharSequence mNegativeButtonText;
        public DialogInterface.OnClickListener mNegativeButtonListener;
        public CharSequence mNeutralButtonText;
        public DialogInterface.OnClickListener mNeutralButtonListener;
        public boolean mCancelable;
        public DialogInterface.OnCancelListener mOnCancelListener;
        public DialogInterface.OnKeyListener mOnKeyListener;
        public CharSequence[] mItems;
        public ListAdapter mAdapter;
        public DialogInterface.OnClickListener mOnClickListener;
        public View mView;
        public int mViewSpacingLeft;
        public int mViewSpacingTop;
        public int mViewSpacingRight;
        public int mViewSpacingBottom;
        public boolean mViewSpacingSpecified = false;
        public boolean[] mCheckedItems;
        public boolean mIsMultiChoice;
        public boolean mIsSingleChoice;
        public int mCheckedItem = -1;
        public DialogInterface.OnMultiChoiceClickListener mOnCheckboxClickListener;
        public Cursor mCursor;
        public String mLabelColumn;
        public String mIsCheckedColumn;
        public boolean mForceInverseBackground;
        public AdapterView.OnItemSelectedListener mOnItemSelectedListener;
        public OnPrepareListViewListener mOnPrepareListViewListener;
        public boolean mRecycleOnMeasure = true;
        public int mPositiveButtonStyleId = -1; //background style
        public int mNegativeButtonStyleId = -1; //background style
        public int mNeutralButtonStyleId = -1; //background style
        public int mPositiveButtonTextColorId = -1; //textColor
        public int mNegativeButtonTextColorId = -1; //textColor
        public int mNeutralButtonTextColorId = -1; //textColor

        public LFDialogParams(Context context) {
            mContext = context;
            mCancelable = true;
            mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        }

        public void apply(TTDialogController dialog) {
            if (mTitle != null) {
                dialog.setTitle(mTitle);
            }
            if (mIcon != null) {
                dialog.setIcon(mIcon);
            }
            if (mIconId >= 0) {
                dialog.setIcon(mIconId);
            }
            if (mMessage != null) {
                dialog.setMessage(mMessage);
                dialog.setMessageGravity(isCenter);
                dialog.setMessageLine(isSingleLine);
            }

            if (mPositiveButtonText != null) {
                dialog.setButton(DialogInterface.BUTTON_POSITIVE, mPositiveButtonText,
                        mPositiveButtonListener, null, mPositiveButtonStyleId, mPositiveButtonTextColorId);
            }
            if (mNegativeButtonText != null) {
                dialog.setButton(DialogInterface.BUTTON_NEGATIVE, mNegativeButtonText,
                        mNegativeButtonListener, null, mNegativeButtonStyleId, mNegativeButtonTextColorId);
            }
            if (mNeutralButtonText != null) {
                dialog.setButton(DialogInterface.BUTTON_NEUTRAL, mNeutralButtonText,
                        mNeutralButtonListener, null, mNeutralButtonStyleId, mNeutralButtonTextColorId);
            }
            if (mForceInverseBackground) {
                dialog.setInverseBackgroundForced(true);
            }
            if ((mItems != null) || (mCursor != null) || (mAdapter != null)) {
                createListView(dialog);
            }

            // for a list , the client can either supply an array of items or an
            // adapter or a cursor
            if (mView != null) {
                if (mViewSpacingSpecified) {
                    dialog.setView(mView, mViewSpacingLeft, mViewSpacingTop, mViewSpacingRight,
                            mViewSpacingBottom);
                } else {
                    dialog.setView(mView);
                }
            }
        }

        private void createListView(final TTDialogController dialog) {
            final RecycleListView listView = (RecycleListView) mInflater.inflate(
                    R.layout.tt_select_dialog, null);
            ListAdapter adapter;
            if (mIsMultiChoice) {
                if (mCursor == null) {
                    adapter = new ArrayAdapter<CharSequence>(mContext,
                            R.layout.tt_select_dialog_multichoice, R.id.text1, mItems) {
                        @Override
                        public View getView(int position, View convertView, ViewGroup parent) {
                            View view = super.getView(position, convertView, parent);
                            if (mCheckedItems != null) {
                                boolean isItemChecked = mCheckedItems[position];
                                if (isItemChecked) {
                                    listView.setItemChecked(position, true);
                                }
                            }
                            return view;
                        }
                    };
                } else {
                    adapter = new CursorAdapter(mContext, mCursor, false) {
                        private final int mLabelIndex;

                        private final int mIsCheckedIndex;

                        {
                            final Cursor cursor = getCursor();
                            mLabelIndex = cursor.getColumnIndexOrThrow(mLabelColumn);
                            mIsCheckedIndex = cursor.getColumnIndexOrThrow(mIsCheckedColumn);
                        }

                        @Override
                        public View newView(Context context, Cursor cursor, ViewGroup parent) {

                            return mInflater.inflate(R.layout.tt_select_dialog_multichoice, parent,
                                    false);
                        }

                        @Override
                        public void bindView(View view, Context context,
                                             Cursor cursor) {
                            CheckedTextView text = (CheckedTextView) view
                                    .findViewById(android.R.id.text1);
                            text.setText(cursor.getString(mLabelIndex));
                            listView.setItemChecked(cursor.getPosition(),
                                    cursor.getInt(mIsCheckedIndex) == 1);
                        }

                    };
                }
            } else {
                int layout = mIsSingleChoice ? R.layout.tt_select_dialog_singlechoice
                        : R.layout.tt_select_dialog_item;
                if (mCursor == null) {
                    adapter = (mAdapter != null) ? mAdapter : new ArrayAdapter<CharSequence>(
                            mContext, layout, R.id.text1, mItems);
                } else {
                    adapter = new SimpleCursorAdapter(mContext, layout, mCursor, new String[]{
                            mLabelColumn
                    }, new int[]{
                            R.id.text1
                    });
                }
            }

            if (mOnPrepareListViewListener != null) {
                mOnPrepareListViewListener.onPrepareListView(listView);
            }

            dialog.mAdapter = adapter;
            dialog.mCheckedItem = mCheckedItem;

            if (mOnClickListener != null) {
                listView.setOnItemClickListener(new OnItemClickListener() {

                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        mOnClickListener.onClick(dialog.mDialogInterface, position);
                        if (!mIsSingleChoice) {
                            dialog.mDialogInterface.dismiss();
                        }
                    }
                });
            } else if (mOnCheckboxClickListener != null) {
                listView.setOnItemClickListener(new OnItemClickListener() {

                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        if (mCheckedItems != null) {
                            mCheckedItems[position] = listView.isItemChecked(position);
                        }
                        mOnCheckboxClickListener.onClick(dialog.mDialogInterface, position,
                                listView.isItemChecked(position));

                    }
                });
            }
            if (mOnItemSelectedListener != null) {
                listView.setOnItemSelectedListener(mOnItemSelectedListener);
            }
            if (mIsSingleChoice) {
                listView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
            } else if (mIsMultiChoice) {
                listView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
            }
            listView.mRecycleOnMeasure = mRecycleOnMeasure;
            dialog.mListView = listView;

        }

        /**
         * Interface definition for a callback to be invoked before the ListView
         * will be bound to an adapter.
         */
        public interface OnPrepareListViewListener {
            /**
             * Called before the ListView is bound to an adapter.
             * 在listview绑定数据前所调用
             *
             * @param listView The ListView that will be shown in the dialog.
             */
            void onPrepareListView(ListView listView);
        }

    }

}
