/*
 * Copyright 2013, Edmodo, Inc. 
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this work except in compliance with the License.
 * You may obtain a copy of the License in the LICENSE file, or at:
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" 
 * BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language 
 * governing permissions and limitations under the License. 
 */

package com.edmodo.cropper;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewTreeObserver;
import android.widget.ImageView;
import android.widget.Scroller;

import com.edmodo.cropper.cropwindow.edge.Edge;
import com.edmodo.cropper.cropwindow.handle.Handle;
import com.edmodo.cropper.util.AspectRatioUtil;
import com.edmodo.cropper.util.HandleUtil;
import com.edmodo.cropper.util.PaintUtil;

/**
 * Custom view that provides cropping capabilities to an image.
 */
public class CropImageView extends ImageView implements ScaleGestureDetector.OnScaleGestureListener,
        View.OnTouchListener, ViewTreeObserver.OnGlobalLayoutListener {

    // Private Constants ///////////////////////////////////////////////////////////////////////////

    @SuppressWarnings("unused")
    public static final int GUIDELINES_OFF = 0;
    public static final int GUIDELINES_ON_TOUCH = 1;
    public static final int GUIDELINES_ON = 2;
    @SuppressWarnings("unused")
    private static final String TAG = CropImageView.class.getName();

    // Member Variables ////////////////////////////////////////////////////////////////////////////
    // 绘制边框的画笔
    private Paint mBorderPaint;

    // 绘制直线的画笔
    private Paint mGuidelinePaint;

    // 绘制边框角的画笔
    private Paint mCornerPaint;

    // 绘制裁剪区域之外的部分的画笔
    private Paint mSurroundingAreaOverlayPaint;

    // The radius (in pixels) of the touchable area around the handle.
    // We are basing this value off of the recommended 48dp touch target size.
    private float mHandleRadius;

    // An edge of the crop window will snap to the corresponding edge of a
    // specified bounding box when the crop window edge is less than or equal to
    // this distance (in pixels) away from the bounding box edge.
    private float mSnapRadius;

    // 截图角标注线的宽度
    private float mCornerThickness;

    // 截图边框线的宽度
    private float mBorderThickness;

    // 截图角标注线的长度
    private float mCornerLength;

    // 裁剪区域
    @NonNull
    private RectF mBitmapRect = new RectF();
    //保存一份初始大小
    @NonNull
    private RectF mBitmapRectCopy = new RectF();

    // Holds the x and y offset between the exact touch location and the exact
    // handle location that is activated. There may be an offset because we
    // allow for some leeway (specified by 'mHandleRadius') in activating a
    // handle. However, we want to maintain these offset values while the handle
    // is being dragged so that the handle doesn't jump.
    @NonNull
    private PointF mTouchOffset = new PointF();

    // The Handle that is currently pressed; null if no Handle is pressed.
    private Handle mPressedHandle;

    // Flag indicating if the crop area should always be a certain aspect ratio (indicated by mTargetAspectRatio).
    private boolean mFixAspectRatio;

    // Current aspect ratio of the image.
    private int mAspectRatioX = 1;
    private int mAspectRatioY = 1;

    // Mode indicating how/whether to show the guidelines; must be one of GUIDELINES_OFF, GUIDELINES_ON_TOUCH, GUIDELINES_ON.
    private int mGuidelinesMode = 1;

    /**
     * 缩放手势的监测
     */
    private ScaleGestureDetector mScaleGestureDetector;
    /**
     * 监听手势
     */
    private GestureDetector mGestureDetector;
    /**
     * 对图片进行缩放平移的Matrix
     */
    private Matrix mScaleMatrix;
    /**
     * 第一次加载图片时调整图片缩放比例，使图片的宽或者高充满屏幕
     */
    private boolean mFirst;
    //第一次保存图片大小
    private boolean mFirstAdd;
    //第一次填充图像后，才能去绘制(onDraw())
    private boolean mFirstOnLayout;
    /**
     * 图片的初始化比例
     */
    private float mInitScale;
    /**
     * 图片的最大比例
     */
    private float mMaxScale;
    /**
     * 双击图片放大的比例
     */
    private float mMidScale;
    /**
     * 最小缩放比例
     */
    private float mMinScale;
    /**
     * 最大溢出值
     */
    private float mMaxOverScale;

    /**
     * 是否正在自动放大或者缩小
     */
    private boolean isAutoScale;

    //-----------------------------------------------
    /**
     * 上一次触控点的数量
     */
    private int mLastPointerCount;
    /**
     * 是否可以拖动
     */
    private boolean isCanDrag;
    /**
     * 上一次滑动的x和y坐标
     */
    private float mLastX;
    private float mLastY;
    /**
     * 可滑动的临界值
     */
    private int mTouchSlop;
    /**
     * 是否用检查左右边界
     */
    private boolean isCheckLeftAndRight;
    /**
     * 是否用检查上下边界
     */
    private boolean isCheckTopAndBottom;
    /**
     * 速度追踪器
     */
    private VelocityTracker mVelocityTracker;
    private FlingRunnable mFlingRunnable;

    // Constructors ////////////////////////////////////////////////////////////////////////////////

    public CropImageView(Context context) {
        super(context);
        init(context, null);
    }

    public CropImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public CropImageView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    private void init(@NonNull Context context, @Nullable AttributeSet attrs) {

        final TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.CropImageView, 0, 0);
        mGuidelinesMode = typedArray.getInteger(R.styleable.CropImageView_guidelines, 1);
        mFixAspectRatio = typedArray.getBoolean(R.styleable.CropImageView_fixAspectRatio, false);
        mAspectRatioX = typedArray.getInteger(R.styleable.CropImageView_aspectRatioX, 1);
        mAspectRatioY = typedArray.getInteger(R.styleable.CropImageView_aspectRatioY, 1);
        typedArray.recycle();

        final Resources resources = context.getResources();

        mBorderPaint = PaintUtil.newBorderPaint(resources);
        mGuidelinePaint = PaintUtil.newGuidelinePaint(resources);
        mSurroundingAreaOverlayPaint = PaintUtil.newSurroundingAreaOverlayPaint(resources);
        mCornerPaint = PaintUtil.newCornerPaint(resources);

        mHandleRadius = resources.getDimension(R.dimen.target_radius);
        mSnapRadius = resources.getDimension(R.dimen.snap_radius);
        mBorderThickness = resources.getDimension(R.dimen.border_thickness);
        mCornerThickness = resources.getDimension(R.dimen.corner_thickness);
        mCornerLength = resources.getDimension(R.dimen.corner_length);

        //一定要将图片的ScaleType设置成Matrix类型的
        setScaleType(ScaleType.MATRIX);
        //初始化缩放手势监听器
        mScaleGestureDetector = new ScaleGestureDetector(context, this);
        //初始化矩阵
        mScaleMatrix = new Matrix();
        setOnTouchListener(this);
        mTouchSlop = ViewConfiguration.get(context).getScaledTouchSlop();
        //初始化手势检测器，监听双击事件
        mGestureDetector = new GestureDetector(context, new GestureDetector.SimpleOnGestureListener() {
            @Override
            public boolean onDoubleTap(MotionEvent e) {
                //如果是正在自动缩放，则直接返回，不进行处理
                if (isAutoScale)
                    return true;

                //得到点击的坐标
                float x = e.getX();
                float y = e.getY();
                //如果当前图片的缩放值小于指定的双击缩放值
                if (getScale() < mMidScale) {
                    //进行自动放大
                    post(new AutoScaleRunnable(mMidScale, x, y));
                } else {
                    //当前图片的缩放值大于初试缩放值，则自动缩小
                    post(new AutoScaleRunnable(mInitScale, x, y));
                }
                return true;
            }
        });
    }

    // View Methods ////////////////////////////////////////////////////////////////////////////////

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
//        System.out.println("CropImageView.onLayout");
        mBitmapRect = getBitmapRect();
//        System.out.println("onLayout : mBitmapRect -- " + mBitmapRect.toString());
        if (mFirst) {
            mFirstOnLayout = true;
            if (!mFirstAdd) {
                mFirstAdd = true;
                mBitmapRectCopy = mBitmapRect;
            }
        }
        initCropWindow(mBitmapRect);
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
//        System.out.println("CropImageView.onDraw");

        if (mFirst && mFirstOnLayout) {
//            System.out.println("onDraw-----------------------");
            drawDarkenedSurroundingArea(canvas);
            drawGuidelines(canvas);
            drawBorder(canvas);
            drawCorners(canvas);
        }
    }

    // Public Methods //////////////////////////////////////////////////////////////////////////////

    /**
     * Sets the guidelines for the CropOverlayView to be either on, off, or to show when resizing
     * the application.
     *
     * @param guidelinesMode Integer that signals whether the guidelines should be on, off, or only
     *                       showing when resizing.
     */
    public void setGuidelines(int guidelinesMode) {
        mGuidelinesMode = guidelinesMode;
        invalidate(); // Request onDraw() to get called again.
    }

    /**
     * Sets whether the aspect ratio is fixed or not; true fixes the aspect ratio, while false
     * allows it to be changed.
     *
     * @param fixAspectRatio Boolean that signals whether the aspect ratio should be maintained.
     * @see {@link #setAspectRatio(int, int)}
     */
    public void setFixedAspectRatio(boolean fixAspectRatio) {
        mFixAspectRatio = fixAspectRatio;
        requestLayout(); // Request measure/layout to be run again.
    }

    /**
     * Sets the both the X and Y values of the aspectRatio. These only apply iff fixed aspect ratio
     * is set.
     *
     * @param aspectRatioX new X value of the aspect ratio; must be greater than 0
     * @param aspectRatioY new Y value of the aspect ratio; must be greater than 0
     * @see {@link #setFixedAspectRatio(boolean)}
     */
    public void setAspectRatio(int aspectRatioX, int aspectRatioY) {

        if (aspectRatioX <= 0 || aspectRatioY <= 0) {
            throw new IllegalArgumentException("Cannot set aspect ratio value to a number less than or equal to 0.");
        }
        mAspectRatioX = aspectRatioX;
        mAspectRatioY = aspectRatioY;

        if (mFixAspectRatio) {
            requestLayout(); // Request measure/layout to be run again.
        }
    }

    /**
     * Gets the cropped image based on the current crop window.
     *
     * @return a new Bitmap representing the cropped image
     */
    public Bitmap getCroppedImage() {

        // Implementation reference: http://stackoverflow.com/a/26930938/1068656

        final Drawable drawable = getDrawable();
        if (drawable == null || !(drawable instanceof BitmapDrawable)) {
            return null;
        }

        // Get image matrix values and place them in an array.
        final float[] matrixValues = new float[9];
        getImageMatrix().getValues(matrixValues);

        // Extract the scale and translation values. Note, we currently do not handle any other transformations (e.g. skew).
        final float scaleX = matrixValues[Matrix.MSCALE_X];
        final float scaleY = matrixValues[Matrix.MSCALE_Y];
        final float transX = matrixValues[Matrix.MTRANS_X];
        final float transY = matrixValues[Matrix.MTRANS_Y];

        // Ensure that the left and top edges are not outside of the ImageView bounds.
        final float bitmapLeft = (transX < 0) ? Math.abs(transX) : -transX;
        float bitmapTop = (transY < 0) ? Math.abs(transY) : -transY;

        // Get the original bitmap object.
        final Bitmap originalBitmap = ((BitmapDrawable) drawable).getBitmap();

        // Calculate the top-left corner of the crop window relative to the ~original~ bitmap size.
        final float cropX = (bitmapLeft + Edge.LEFT.getCoordinate()) / scaleX;
//        if (bitmapTop == 0) {
//            bitmapTop = -transY;
//        }
        final float cropY = Math.abs((bitmapTop + Edge.TOP.getCoordinate())) / scaleY;

        // Calculate the crop window size relative to the ~original~ bitmap size.
        // Make sure the right and bottom edges are not outside the ImageView bounds (this is just to address rounding discrepancies).
        final float cropWidth = Math.min(Edge.getWidth() / scaleX, originalBitmap.getWidth() - cropX);
        final float cropHeight = Math.min(Edge.getHeight() / scaleY, originalBitmap.getHeight() - cropY);

        // Crop the subset from the original Bitmap.
        return Bitmap.createBitmap(originalBitmap,
                (int) cropX,
                (int) cropY,
                (int) cropWidth,
                (int) cropHeight);
    }

    // Private Methods /////////////////////////////////////////////////////////////////////////////

    /**
     * Gets the bounding rectangle of the bitmap within the ImageView.
     */
    private RectF getBitmapRect() {

        final Drawable drawable = getDrawable();
        if (drawable == null) {
            return new RectF();
        }

        // Get image matrix values and place them in an array.
        final float[] matrixValues = new float[9];
        getImageMatrix().getValues(matrixValues);

        // Extract the scale and translation values from the matrix.
        final float scaleX = matrixValues[Matrix.MSCALE_X];
        final float scaleY = matrixValues[Matrix.MSCALE_Y];
        final float transX = matrixValues[Matrix.MTRANS_X];
        final float transY = matrixValues[Matrix.MTRANS_Y];

        // Get the width and height of the original bitmap.
        final int drawableIntrinsicWidth = drawable.getIntrinsicWidth();
        final int drawableIntrinsicHeight = drawable.getIntrinsicHeight();

        // Calculate the dimensions as seen on screen.
        final int drawableDisplayWidth = Math.round(drawableIntrinsicWidth * scaleX);
        final int drawableDisplayHeight = Math.round(drawableIntrinsicHeight * scaleY);

        // Get the Rect of the displayed image within the ImageView.
        final float left = Math.max(transX, 0);
        final float top = Math.max(transY, 0);
        final float right = Math.min(left + drawableDisplayWidth, getWidth());
        final float bottom = Math.min(top + drawableDisplayHeight, getHeight());

        return new RectF(left, top, right, bottom);
    }

    /**
     * Initialize the crop window by setting the proper {@link Edge} values.
     * <p/>
     * If fixed aspect ratio is turned off, the initial crop window will be set to the displayed
     * image with 10% margin. If fixed aspect ratio is turned on, the initial crop window will
     * conform to the aspect ratio with at least one dimension maximized.
     */
    private void initCropWindow(@NonNull RectF bitmapRect) {
//        System.out.println("CropImageView.initCropWindow");
        if (mFixAspectRatio) {
            // Initialize the crop window with the proper aspect ratio.
            initCropWindowWithFixedAspectRatio(bitmapRect);

        } else {
            final float horizontalPadding = dp2px(2.7f);
            final float verticalPadding = dp2px(2.7f);

            Edge.LEFT.setCoordinate(bitmapRect.left + horizontalPadding);
            Edge.TOP.setCoordinate(bitmapRect.top + verticalPadding);
            Edge.RIGHT.setCoordinate(bitmapRect.right - horizontalPadding);
            Edge.BOTTOM.setCoordinate(bitmapRect.bottom - verticalPadding);

//            System.out.println("initCropWindow: (" + Edge.LEFT.getCoordinate()
//                    + ", " + Edge.TOP.getCoordinate()
//                    + ", " + Edge.RIGHT.getCoordinate()
//                    + ", " + Edge.BOTTOM.getCoordinate()
//                    + ")");
        }
    }

    private void initCropWindowWithFixedAspectRatio(@NonNull RectF bitmapRect) {

        // If the image aspect ratio is wider than the crop aspect ratio,
        // then the image height is the determining initial length. Else, vice-versa.
        if (AspectRatioUtil.calculateAspectRatio(bitmapRect) > getTargetAspectRatio()) {

            final float cropWidth = AspectRatioUtil.calculateWidth(bitmapRect.height(), getTargetAspectRatio());

            Edge.LEFT.setCoordinate(bitmapRect.centerX() - cropWidth / 2f);
            Edge.TOP.setCoordinate(bitmapRect.top);
            Edge.RIGHT.setCoordinate(bitmapRect.centerX() + cropWidth / 2f);
            Edge.BOTTOM.setCoordinate(bitmapRect.bottom);

        } else {

            final float cropHeight = AspectRatioUtil.calculateHeight(bitmapRect.width(), getTargetAspectRatio());

            Edge.LEFT.setCoordinate(bitmapRect.left);
            Edge.TOP.setCoordinate(bitmapRect.centerY() - cropHeight / 2f);
            Edge.RIGHT.setCoordinate(bitmapRect.right);
            Edge.BOTTOM.setCoordinate(bitmapRect.centerY() + cropHeight / 2f);
        }
    }

    private void drawDarkenedSurroundingArea(@NonNull Canvas canvas) {

        final RectF bitmapRect = mBitmapRect;

        final float left = Edge.LEFT.getCoordinate();
        final float top = Edge.TOP.getCoordinate();
        final float right = Edge.RIGHT.getCoordinate();
        final float bottom = Edge.BOTTOM.getCoordinate();

        /*-
          -------------------------------------
          |                top                |
          -------------------------------------
          |      |                    |       |
          |      |                    |       |
          | left |                    | right |
          |      |                    |       |
          |      |                    |       |
          -------------------------------------
          |              bottom               |
          -------------------------------------
         */

        // Draw "top", "bottom", "left", then "right" quadrants according to diagram above.
        canvas.drawRect(bitmapRect.left, bitmapRect.top, bitmapRect.right, top, mSurroundingAreaOverlayPaint);
        canvas.drawRect(bitmapRect.left, bottom, bitmapRect.right, bitmapRect.bottom, mSurroundingAreaOverlayPaint);
        canvas.drawRect(bitmapRect.left, top, left, bottom, mSurroundingAreaOverlayPaint);
        canvas.drawRect(right, top, bitmapRect.right, bottom, mSurroundingAreaOverlayPaint);
    }

    private void drawGuidelines(@NonNull Canvas canvas) {

//        if (!shouldGuidelinesBeShown()) {
//            return;
//        }

        final float left = Edge.LEFT.getCoordinate();
        final float top = Edge.TOP.getCoordinate();
        final float right = Edge.RIGHT.getCoordinate();
        final float bottom = Edge.BOTTOM.getCoordinate();

        // Draw vertical guidelines.
        final float oneThirdCropWidth = Edge.getWidth() / 3;

        final float x1 = left + oneThirdCropWidth;
        canvas.drawLine(x1, top, x1, bottom, mGuidelinePaint);
        final float x2 = right - oneThirdCropWidth;
        canvas.drawLine(x2, top, x2, bottom, mGuidelinePaint);

        // Draw horizontal guidelines.
        final float oneThirdCropHeight = Edge.getHeight() / 3;

        final float y1 = top + oneThirdCropHeight;
        canvas.drawLine(left, y1, right, y1, mGuidelinePaint);
        final float y2 = bottom - oneThirdCropHeight;
        canvas.drawLine(left, y2, right, y2, mGuidelinePaint);
    }

    private void drawBorder(@NonNull Canvas canvas) {

        canvas.drawRect(Edge.LEFT.getCoordinate(),
                Edge.TOP.getCoordinate(),
                Edge.RIGHT.getCoordinate(),
                Edge.BOTTOM.getCoordinate(),
                mBorderPaint);
    }

    private void drawCorners(@NonNull Canvas canvas) {

        final float left = Edge.LEFT.getCoordinate();
        final float top = Edge.TOP.getCoordinate();
        final float right = Edge.RIGHT.getCoordinate();
        final float bottom = Edge.BOTTOM.getCoordinate();

        // Absolute value of the offset by which to draw the corner line such that its inner edge is flush with the border's inner edge.
        final float lateralOffset = (mCornerThickness - mBorderThickness) / 2f;
        // Absolute value of the offset by which to start the corner line such that the line is drawn all the way to form a corner edge with the adjacent side.
        final float startOffset = mCornerThickness - (mBorderThickness / 2f);

        // Top-left corner: left side
        canvas.drawLine(left - lateralOffset, top - startOffset, left - lateralOffset, top + mCornerLength, mCornerPaint);
        // Top-left corner: top side
        canvas.drawLine(left - startOffset, top - lateralOffset, left + mCornerLength, top - lateralOffset, mCornerPaint);

        // Top-right corner: right side
        canvas.drawLine(right + lateralOffset, top - startOffset, right + lateralOffset, top + mCornerLength, mCornerPaint);
        // Top-right corner: top side
        canvas.drawLine(right + startOffset, top - lateralOffset, right - mCornerLength, top - lateralOffset, mCornerPaint);

        // Bottom-left corner: left side
        canvas.drawLine(left - lateralOffset, bottom + startOffset, left - lateralOffset, bottom - mCornerLength, mCornerPaint);
        // Bottom-left corner: bottom side
        canvas.drawLine(left - startOffset, bottom + lateralOffset, left + mCornerLength, bottom + lateralOffset, mCornerPaint);

        // Bottom-right corner: right side
        canvas.drawLine(right + lateralOffset, bottom + startOffset, right + lateralOffset, bottom - mCornerLength, mCornerPaint);
        // Bottom-right corner: bottom side
        canvas.drawLine(right + startOffset, bottom + lateralOffset, right - mCornerLength, bottom + lateralOffset, mCornerPaint);
    }

    private boolean shouldGuidelinesBeShown() {
        return ((mGuidelinesMode == GUIDELINES_ON)
                || ((mGuidelinesMode == GUIDELINES_ON_TOUCH) && (mPressedHandle != null)));
    }

    private float getTargetAspectRatio() {
        return mAspectRatioX / (float) mAspectRatioY;
    }

    /**
     * Handles a {@link MotionEvent#ACTION_DOWN} event.
     *
     * @param x the x-coordinate of the down action
     * @param y the y-coordinate of the down action
     */
    private void onActionDown(float x, float y) {

        final float left = Edge.LEFT.getCoordinate();
        final float top = Edge.TOP.getCoordinate();
        final float right = Edge.RIGHT.getCoordinate();
        final float bottom = Edge.BOTTOM.getCoordinate();

        mPressedHandle = HandleUtil.getPressedHandle(x, y, left, top, right, bottom, mHandleRadius);

        // Calculate the offset of the touch point from the precise location of the handle.
        // Save these values in member variable 'mTouchOffset' so that we can maintain this offset as we drag the handle.
        if (mPressedHandle != null) {
            HandleUtil.getOffset(mPressedHandle, x, y, left, top, right, bottom, mTouchOffset);
            invalidate();
        }
    }

    /**
     * Handles a {@link MotionEvent#ACTION_UP} or {@link MotionEvent#ACTION_CANCEL} event.
     */
    private void onActionUp() {
        if (mPressedHandle != null) {
            mPressedHandle = null;
            invalidate();
        }
    }

    /**
     * Handles a {@link MotionEvent#ACTION_MOVE} event.
     *
     * @param x the x-coordinate of the move event
     * @param y the y-coordinate of the move event
     */
    private void onActionMove(float x, float y) {

        if (mPressedHandle == null) {
            return;
        }

        // Adjust the coordinates for the finger position's offset (i.e. the distance from the initial touch to the precise handle location).
        // We want to maintain the initial touch's distance to the pressed handle so that the crop window size does not "jump".
        x += mTouchOffset.x;
        y += mTouchOffset.y;

        // Calculate the new crop window size/position.
        if (mFixAspectRatio) {
            mPressedHandle.updateCropWindow(x, y, getTargetAspectRatio(), mBitmapRect, mSnapRadius);
        } else {
            mPressedHandle.updateCropWindow(x, y, mBitmapRect, mSnapRadius);
        }
        invalidate();
    }

    /**
     * Responds to scaling events for a gesture in progress.
     * Reported by pointer motion.
     *
     * @param detector The detector reporting the event - use this to
     *                 retrieve extended info about event state.
     * @return Whether or not the detector should consider this event
     * as handled. If an event was not handled, the detector
     * will continue to accumulate movement until an event is
     * handled. This can be useful if an application, for example,
     * only wants to update scaling factors if the change is
     * greater than 0.01.
     * <p>
     * 这个是OnScaleGestureListener中的方法，在这个方法中我们可以对图片进行放大缩小
     */
    @Override
    public boolean onScale(ScaleGestureDetector detector) {
        //当我们两个手指进行分开操作时，说明我们想要放大，这个scaleFactor是一个稍微大于1的数值
        //当我们两个手指进行闭合操作时，说明我们想要缩小，这个scaleFactor是一个稍微小于1的数值
        float scaleFactor = detector.getScaleFactor();
        //获得我们图片当前的缩放值
        float scale = getScale();
        //如果当前没有图片，则直接返回
        if (getDrawable() == null) {
            return true;
        }
        //如果scaleFactor大于1，说明想放大，当前的缩放比例乘以scaleFactor之后小于
        //最大的缩放比例时，允许放大
        //如果scaleFactor小于1，说明想缩小，当前的缩放比例乘以scaleFactor之后大于
        //最小的缩放比例时，允许缩小
        if ((scaleFactor > 1.0f && scale * scaleFactor < mMaxOverScale)
                || scaleFactor < 1.0f && scale * scaleFactor > mInitScale) {
            //边界控制，如果当前缩放比例乘以scaleFactor之后大于了最大的缩放比例
            if (scale * scaleFactor > mMaxOverScale + 0.01f) {
                //则将scaleFactor设置成mMaxScale/scale
                //当再进行matrix.postScale时
                //scale*scaleFactor=scale*(mMaxScale/scale)=mMaxScale
                //最后图片就会放大至mMaxScale缩放比例的大小
                scaleFactor = mMaxOverScale / scale;
            }
            //边界控制，如果当前缩放比例乘以scaleFactor之后小于了最小的缩放比例
            //我们不允许再缩小
            if (scale * scaleFactor < mInitScale + 0.01f) {
                //计算方法同上
                scaleFactor = mInitScale / scale;
            }
            //前两个参数是缩放的比例，是一个稍微大于1或者稍微小于1的数，形成一个随着手指放大
            //或者缩小的效果
            //detector.getFocusX()和detector.getFocusY()得到的是多点触控的中点
            //这样就能实现我们在图片的某一处局部放大的效果
            mScaleMatrix.postScale(scaleFactor, scaleFactor, detector.getFocusX(), detector.getFocusY());
            //因为图片的缩放点不是图片的中心点了，所以图片会出现偏移的现象，所以进行一次边界的检查和居中操作
            checkBorderAndCenterWhenScale();
            //将矩阵作用到图片上
            setImageMatrix(mScaleMatrix);
            requestLayout();
        }
        return true;
    }

    /**
     * Responds to the beginning of a scaling gesture. Reported by
     * new pointers going down.
     *
     * @param detector The detector reporting the event - use this to
     *                 retrieve extended info about event state.
     * @return Whether or not the detector should continue recognizing
     * this gesture. For example, if a gesture is beginning
     * with a focal point outside of a region where it makes
     * sense, onScaleBegin() may return false to ignore the
     * rest of the gesture.
     */
    @Override
    public boolean onScaleBegin(ScaleGestureDetector detector) {
        return true;
    }

    /**
     * Responds to the end of a scale gesture. Reported by existing
     * pointers going up.
     * <p>
     * Once a scale has ended, {@link ScaleGestureDetector#getFocusX()}
     * and {@link ScaleGestureDetector#getFocusY()} will return focal point
     * of the pointers remaining on the screen.
     *
     * @param detector The detector reporting the event - use this to
     *                 retrieve extended info about event state.
     */
    @Override
    public void onScaleEnd(ScaleGestureDetector detector) {
        requestLayout();
    }

    /**
     * Called when a touch event is dispatched to a view. This allows listeners to
     * get a chance to respond before the target view.
     *
     * @param v     The view the touch event has been dispatched to.
     * @param event The MotionEvent object containing full information about
     *              the event.
     * @return True if the listener has consumed the event, false otherwise.
     */
    @Override
    public boolean onTouch(View v, MotionEvent event) {
        //当双击操作时，不允许移动图片，直接返回true
        if (mGestureDetector.onTouchEvent(event)) {
            return true;
        }
        //将事件传递给ScaleGestureDetector
        mScaleGestureDetector.onTouchEvent(event);
        //用于存储多点触控产生的坐标
        float x = 0.0f;
        float y = 0.0f;
        //得到多点触控的个数
        int pointerCount = event.getPointerCount();
        if (pointerCount > 1) {
            mPressedHandle = null;
        }
        //将所有触控点的坐标累加起来
        for (int i = 0; i < pointerCount; i++) {
            x += event.getX(i);
            y += event.getY(i);
        }
        //取平均值，得到的就是多点触控后产生的那个点的坐标
        x /= pointerCount;
        y /= pointerCount;
        //如果触控点的数量变了，则置为不可滑动
        if (mLastPointerCount != pointerCount) {
            isCanDrag = false;
            mLastX = x;
            mLastY = y;
        }
        mLastPointerCount = pointerCount;
        RectF rectF = getMatrixRectF();
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                if (pointerCount == 1) {
                    final float left = Edge.LEFT.getCoordinate();
                    final float top = Edge.TOP.getCoordinate();
                    final float right = Edge.RIGHT.getCoordinate();
                    final float bottom = Edge.BOTTOM.getCoordinate();

                    mPressedHandle = HandleUtil.getPressedHandle(x, y, left, top, right, bottom, mHandleRadius);

                    // Calculate the offset of the touch point from the precise location of the handle.
                    // Save these values in member variable 'mTouchOffset' so that we can maintain this offset as we drag the handle.
                    if (mPressedHandle != null) {
                        HandleUtil.getOffset(mPressedHandle, x, y, left, top, right, bottom, mTouchOffset);
                        invalidate();
                        return true;
                    }
                }
                //初始化速度检测器
                mVelocityTracker = VelocityTracker.obtain();
                if (mVelocityTracker != null) {
                    //将当前的事件添加到检测器中
                    mVelocityTracker.addMovement(event);
                }
                //当手指再次点击到图片时，停止图片的惯性滑动
                if (mFlingRunnable != null) {
                    mFlingRunnable.cancelFling();
                    mFlingRunnable = null;
                }
                isCanDrag = false;
                //当图片处于放大状态时，禁止ViewPager拦截事件，将事件传递给图片，进行拖动
//                if (rectF.width() > getWidth() + 0.01f || rectF.height() > getHeight() + 0.01f){
//                    if (getParent() instanceof ViewPager){
//                        getParent().requestDisallowInterceptTouchEvent(true);
//                    }
//                }

                break;
            case MotionEvent.ACTION_MOVE:
                //当图片处于放大状态时，禁止ViewPager拦截事件，将事件传递给图片，进行拖动
//                if (rectF.width() > getWidth() + 0.01f || rectF.height() > getHeight() + 0.01f){
//                    if (getParent() instanceof ViewPager){
//                        getParent().requestDisallowInterceptTouchEvent(true);
//                    }
//                }
                if (pointerCount == 1 && mPressedHandle != null) {
                    // Adjust the coordinates for the finger position's offset (i.e. the distance from the initial touch to the precise handle location).
                    // We want to maintain the initial touch's distance to the pressed handle so that the crop window size does not "jump".
                    x += mTouchOffset.x;
                    y += mTouchOffset.y;

                    // Calculate the new crop window size/position.
                    if (mFixAspectRatio) {
                        mPressedHandle.updateCropWindow(x, y, getTargetAspectRatio(), mBitmapRect, mSnapRadius);
                    } else {
                        mPressedHandle.updateCropWindow(x, y, mBitmapRect, mSnapRadius);
                    }
                    invalidate();
                    mLastX = x;
                    mLastY = y;
                    return true;
                }

                //得到水平和竖直方向的偏移量
                float dx = x - mLastX;
                float dy = y - mLastY;
                //如果当前是不可滑动的状态，判断一下是否是滑动的操作
                if (!isCanDrag) {
                    isCanDrag = isMoveAction(dx, dy);
                }
                //如果可滑动
                if (isCanDrag) {
                    if (getDrawable() != null) {

                        if (mVelocityTracker != null) {
                            //将当前事件添加到检测器中
                            mVelocityTracker.addMovement(event);
                        }

                        isCheckLeftAndRight = true;
                        isCheckTopAndBottom = true;
                        //如果图片宽度小于控件宽度
                        if (rectF.width() < getWidth()) {
                            //左右不可滑动
                            dx = 0;
                            //左右不可滑动，也就不用检查左右的边界了
                            isCheckLeftAndRight = false;
                        }
                        //如果图片的高度小于控件的高度
                        if (rectF.height() < getHeight()) {
                            //上下不可滑动
                            dy = 0;
                            //上下不可滑动，也就不用检查上下边界了
                            isCheckTopAndBottom = false;
                        }
                    }
                    mScaleMatrix.postTranslate(dx, dy);
                    //当平移时，检查上下左右边界
                    checkBorderWhenTranslate();
                    setImageMatrix(mScaleMatrix);
                }
                mLastX = x;
                mLastY = y;
                break;
            case MotionEvent.ACTION_UP:
                if (mLastPointerCount == 1 && mPressedHandle != null) {
                    mPressedHandle = null;
                    invalidate();

                    return true;
                }
                //当手指抬起时，将mLastPointerCount置0，停止滑动
                mLastPointerCount = 0;
                //如果当前图片大小小于初始化大小
                if (getScale() < mInitScale) {
                    //自动放大至初始化大小
                    post(new AutoScaleRunnable(mInitScale, getWidth() / 2, getHeight() / 2));
                }
                //如果当前图片大小大于最大值
                if (getScale() > mMaxScale) {
                    //自动缩小至最大值
                    post(new AutoScaleRunnable(mMaxScale, getWidth() / 2, getHeight() / 2));
                }
                if (isCanDrag) {//如果当前可以滑动
                    if (mVelocityTracker != null) {
                        //将当前事件添加到检测器中
                        mVelocityTracker.addMovement(event);
                        //计算当前的速度
                        mVelocityTracker.computeCurrentVelocity(1000);
                        //得到当前x方向速度
                        final float vX = mVelocityTracker.getXVelocity();
                        //得到当前y方向的速度
                        final float vY = mVelocityTracker.getYVelocity();
                        mFlingRunnable = new FlingRunnable(getContext());
                        //调用fling方法，传入控件宽高和当前x和y轴方向的速度
                        //这里得到的vX和vY和scroller需要的velocityX和velocityY的负号正好相反
                        //所以传入一个负值
                        mFlingRunnable.fling(getWidth(), getHeight(), (int) -vX, (int) -vY);
                        //执行run方法
                        post(mFlingRunnable);
                    }
                }
                requestLayout();
                break;
            case MotionEvent.ACTION_CANCEL:
                System.out.println("Action_cancel");
                //释放速度检测器
                if (mVelocityTracker != null) {
                    mVelocityTracker.recycle();
                    mVelocityTracker = null;
                }
                requestLayout();
                break;
        }
        return true;
    }

    /**
     * 判断是否是移动的操作
     */
    private boolean isMoveAction(float dx, float dy) {
        //勾股定理，判断斜边是否大于可滑动的一个临界值
        return Math.sqrt(dx * dx + dy * dy) > mTouchSlop;
    }

    /**
     * Callback method to be invoked when the global layout state or the visibility of views
     * within the view tree changes
     * <p>
     * 当布局树发生变化时会调用此方法，我们可以在此方法中获得控件的宽和高
     */
    @Override
    public void onGlobalLayout() {
//        System.out.println("CropImageView.onGlobalLayout");
        //只有当第一次加载图片的时候才会进行初始化，用一个变量mFirst控制
        if (!mFirst) {
            //得到控件的宽和高
            int screemWidth = getWidth();
            int screemHeight = getHeight();
            //得到当前ImageView中加载的图片
            Drawable d = getDrawable();
            if (d == null) {//如果没有图片，则直接返回
                return;
            }
            mFirst = true;
            //得到当前图片的宽和高，图片的宽和高不一定等于控件的宽和高
            //因此我们需要将图片的宽和高与控件宽和高进行判断
            //将图片完整的显示在屏幕中
            int dw = d.getIntrinsicWidth();
            int dh = d.getIntrinsicHeight();
            //我们定义一个临时变量，根据图片与控件的宽高比例，来确定这个最终缩放值
            float scale = 1.0f;
            //如果图片宽度大于控件宽度，图片高度小于控件高度
            if (dw >= screemWidth && dh <= screemHeight) {
                //我们需要将图片宽度缩小，缩小至控件的宽度
                //至于为什么要这样计算，我们可以这样想
                //我们调用matrix.postScale（scale,scale）时，宽和高都要乘以scale的
                //当前我们的图片宽度是dw，dw*scale=dw*（width/dw）=width,这样就等于控件宽度了
                //我们的高度同时也乘以scale，这样能够保证图片的宽高比不改变，图片不变形
                scale = screemWidth * 1.0f / dw;

            }
            //如果图片的宽度小于控件宽度，图片高度大于控件高度
            if (dw <= screemWidth && dh >= screemHeight) {
                //我们就应该将图片的高度缩小，缩小至控件的高度，计算方法同上
                scale = screemHeight * 1.0f / dh;
            }
            //如果图片的宽度小于控件宽度，高度小于控件高度时，我们应该将图片放大
            //比如图片宽度是控件宽度的1/2 ，图片高度是控件高度的1/4
            //如果我们将图片放大4倍，则图片的高度是和控件高度一样了，但是图片宽度就超出控件宽度了
            //因此我们应该选择一个最小值，那就是将图片放大2倍，此时图片宽度等于控件宽度
            //同理，如果图片宽度大于控件宽度，图片高度大于控件高度，我们应该将图片缩小
            //缩小的倍数也应该为那个最小值
            if ((dw < screemWidth && dh < screemHeight) || (dw > screemWidth && dh > screemHeight)) {
                scale = Math.min(screemWidth * 1.0f / dw, screemHeight * 1.0f / dh);
            }

            //我们还应该对图片进行平移操作，将图片移动到屏幕的居中位置
            //控件宽度的一半减去图片宽度的一半即为图片需要水平移动的距离
            //高度同理，大家可以画个图看一看
            int dx = screemWidth / 2 - dw / 2;
            int dy = screemHeight / 2 - dh / 2;
            //对图片进行平移，dx和dy分别表示水平和竖直移动的距离
            mScaleMatrix.postTranslate(dx, dy);
            //对图片进行缩放，scale为缩放的比例，后两个参数为缩放的中心点
            mScaleMatrix.postScale(scale, scale, screemWidth / 2, screemHeight / 2);
            //将矩阵作用于我们的图片上，图片真正得到了平移和缩放
            setImageMatrix(mScaleMatrix);

            //初始化一下我们的几个缩放的边界值
            mInitScale = scale;
            //最大比例为初始比例的4倍
            mMaxScale = mInitScale * 4;
            //双击放大比例为初始化比例的2倍
            mMidScale = mInitScale * 2;
            //最小缩放比例为初试比例的1/4倍
            mMinScale = mInitScale / 4;
            //最大溢出值为最大值的5倍
            mMaxOverScale = mMaxScale * 5;

            requestLayout();
//            System.out.println("mInitScale: " + mInitScale
//                    + " mMaxScale: " + mMaxScale
//                    + " mMidScale: " + mMidScale
//                    + " mMinScale: " + mMinScale
//                    + " mMaxOverScale: " + mMaxOverScale);
        }
    }

    /**
     * 获得图片当前的缩放比例值
     */
    private float getScale() {
        //Matrix为一个3*3的矩阵，一共9个值
        float[] values = new float[9];
        //将Matrix的9个值映射到values数组中
        mScaleMatrix.getValues(values);
        //拿到Matrix中的MSCALE_X的值，这个值为图片宽度的缩放比例，因为图片高度
        //的缩放比例和宽度的缩放比例一致，我们取一个就可以了
        //我们还可以 return values[Matrix.MSCALE_Y];
        return values[Matrix.MSCALE_X];
    }

    /**
     * 获得缩放后图片的上下左右坐标以及宽高
     */
    private RectF getMatrixRectF() {
        //获得当前图片的矩阵
        Matrix matrix = mScaleMatrix;
        //创建一个浮点类型的矩形
        RectF rectF = new RectF();
        //得到当前的图片
        Drawable d = getDrawable();
        if (d != null) {
            //使这个矩形的宽和高同当前图片一致
            rectF.set(0, 0, d.getIntrinsicWidth(), d.getIntrinsicHeight());
            //将矩阵映射到矩形上面，之后我们可以通过获取到矩阵的上下左右坐标以及宽高
            //来得到缩放后图片的上下左右坐标和宽高
            matrix.mapRect(rectF);
        }
        return rectF;
    }

    /**
     * 当缩放时检查边界并且使图片居中
     */
    private void checkBorderAndCenterWhenScale() {
        if (getDrawable() == null) {
            return;
        }
        //初始化水平和竖直方向的偏移量
        float deltaX = 0.0f;
        float deltaY = 0.0f;
        //得到控件的宽和高
        int width = getWidth();
        int height = getHeight();
        //拿到当前图片对应的矩阵
        RectF rectF = getMatrixRectF();
        //如果当前图片的宽度大于控件宽度，当前图片处于放大状态
        if (rectF.width() >= width) {
            //如果图片左边坐标是大于0的，说明图片左边离控件左边有一定距离，
            //左边会出现一个小白边
            if (rectF.left > 0) {
                //我们将图片向左边移动
                deltaX = -rectF.left;
            }
            //如果图片右边坐标小于控件宽度，说明图片右边离控件右边有一定距离，
            //右边会出现一个小白边
            if (rectF.right < width) {
                //我们将图片向右边移动
                deltaX = width - rectF.right;
            }
        }
        //上面是调整宽度，这是调整高度
        if (rectF.height() >= height) {
            //如果上面出现小白边，则向上移动
            if (rectF.top > 0) {
                deltaY = -rectF.top;
            }
            //如果下面出现小白边，则向下移动
            if (rectF.bottom < height) {
                deltaY = height - rectF.bottom;
            }
        }
        //如果图片的宽度小于控件的宽度，我们要对图片做一个水平的居中
        if (rectF.width() < width) {
            deltaX = width / 2f - rectF.right + rectF.width() / 2f;
        }

        //如果图片的高度小于控件的高度，我们要对图片做一个竖直方向的居中
        if (rectF.height() < height) {
            deltaY = height / 2f - rectF.bottom + rectF.height() / 2f;
        }
        //将平移的偏移量作用到矩阵上
        mScaleMatrix.postTranslate(deltaX, deltaY);
    }

    /**
     * 平移时检查上下左右边界
     */
    private void checkBorderWhenTranslate() {
        //获得缩放后图片的相应矩形
        RectF rectF = getMatrixRectF();
        //初始化水平和竖直方向的偏移量
        float deltaX = 0.0f;
        float deltaY = 0.0f;
        //得到控件的宽度
        int width = getWidth();
        //得到控件的高度
        int height = getHeight();
        //如果是需要检查左和右边界
        if (isCheckLeftAndRight) {
            //如果左边出现的白边
            if (rectF.left > 0) {
                //向左偏移
                deltaX = -rectF.left;
            }
            //如果右边出现的白边
            if (rectF.right < width) {
                //向右偏移
                deltaX = width - rectF.right;
            }
        }
        //如果是需要检查上和下边界
        if (isCheckTopAndBottom) {
            //如果上面出现白边
            if (rectF.top > 0) {
                //向上偏移
                deltaY = -rectF.top;
            }
            //如果下面出现白边
            if (rectF.bottom < height) {
                //向下偏移
                deltaY = height - rectF.bottom;
            }
        }

        mScaleMatrix.postTranslate(deltaX, deltaY);
    }

    /**
     * 当view添加到window时调用，早于onGlobalLayout，因此可以在这里注册监听器
     */
    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        getViewTreeObserver().addOnGlobalLayoutListener(this);
    }

    /**
     * 当view从window上移除时调用，因此可以在这里移除监听器
     */
    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        getViewTreeObserver().removeGlobalOnLayoutListener(this);
    }

    /**
     * 自动放大缩小，自动缩放的原理是使用View.postDelay()方法，每隔16ms调用一次
     * run方法，给人视觉上形成一种动画的效果
     */
    private class AutoScaleRunnable implements Runnable {
        //比1稍微大一点，用于放大
        private final float BIGGER = 1.07f;
        //比1稍微小一点，用于缩小
        private final float SMALLER = 0.93f;
        //放大或者缩小的目标比例
        private float mTargetScale;
        //可能是BIGGER,也可能是SMALLER
        private float tempScale;
        //放大缩小的中心点
        private float x;
        private float y;

        //构造方法，将目标比例，缩放中心点传入，并且判断是要放大还是缩小
        public AutoScaleRunnable(float targetScale, float x, float y) {
            this.mTargetScale = targetScale;
            this.x = x;
            this.y = y;
            //如果当前缩放比例小于目标比例，说明要自动放大
            if (getScale() < mTargetScale) {
                //设置为Bigger
                tempScale = BIGGER;
            }
            //如果当前缩放比例大于目标比例，说明要自动缩小
            if (getScale() > mTargetScale) {
                //设置为Smaller
                tempScale = SMALLER;
            }
        }

        @Override
        public void run() {
            //这里缩放的比例非常小，只是稍微比1大一点或者比1小一点的倍数
            //但是当每16ms都放大或者缩小一点点的时候，动画效果就出来了
            mScaleMatrix.postScale(tempScale, tempScale, x, y);
            //每次将矩阵作用到图片之前，都检查一下边界
            checkBorderAndCenterWhenScale();
            //将矩阵作用到图片上
            setImageMatrix(mScaleMatrix);
            //得到当前图片的缩放值
            float currentScale = getScale();
            //如果当前想要放大，并且当前缩放值小于目标缩放值
            //或者  当前想要缩小，并且当前缩放值大于目标缩放值
            if ((tempScale > 1.0f) && currentScale < mTargetScale
                    || (tempScale < 1.0f) && currentScale > mTargetScale) {
                //每隔16ms就调用一次run方法
                postDelayed(this, 16);
            } else {
                //current*scale=current*(mTargetScale/currentScale)=mTargetScale
                //保证图片最终的缩放值和目标缩放值一致
                float scale = mTargetScale / currentScale;
                mScaleMatrix.postScale(scale, scale, x, y);
                checkBorderAndCenterWhenScale();
                setImageMatrix(mScaleMatrix);
                //自动缩放结束，置为false
                isAutoScale = false;
            }
            requestLayout();
        }
    }

    /**
     * 惯性滑动
     */
    private class FlingRunnable implements Runnable {
        private Scroller mScroller;
        private int mCurrentX, mCurrentY;

        public FlingRunnable(Context context) {
            mScroller = new Scroller(context);
        }

        public void cancelFling() {
            mScroller.forceFinished(true);
        }

        /**
         * 这个方法主要是从onTouch中或得到当前滑动的水平和竖直方向的速度
         * 调用scroller.fling方法，这个方法内部能够自动计算惯性滑动
         * 的x和y的变化率，根据这个变化率我们就可以对图片进行平移了
         */
        public void fling(int viewWidth, int viewHeight, int velocityX,
                          int velocityY) {
            RectF rectF = getMatrixRectF();
            if (rectF == null) {
                return;
            }
            //startX为当前图片左边界的x坐标
            final int startX = Math.round(-rectF.left);
            final int minX, maxX, minY, maxY;
            //如果图片宽度大于控件宽度
            if (rectF.width() > viewWidth) {
                //这是一个滑动范围[minX,maxX]，详情见下图
                minX = 0;
                maxX = Math.round(rectF.width() - viewWidth);
            } else {
                //如果图片宽度小于控件宽度，则不允许滑动
                minX = maxX = startX;
            }
            //如果图片高度大于控件高度，同理
            final int startY = Math.round(-rectF.top);
            if (rectF.height() > viewHeight) {
                minY = 0;
                maxY = Math.round(rectF.height() - viewHeight);
            } else {
                minY = maxY = startY;
            }
            mCurrentX = startX;
            mCurrentY = startY;

            if (startX != maxX || startY != maxY) {
                //调用fling方法，然后我们可以通过调用getCurX和getCurY来获得当前的x和y坐标
                //这个坐标的计算是模拟一个惯性滑动来计算出来的，我们根据这个x和y的变化可以模拟
                //出图片的惯性滑动
                mScroller.fling(startX, startY, velocityX, velocityY, minX, maxX, minY, maxY);
            }

        }

        /**
         * 每隔16ms调用这个方法，实现惯性滑动的动画效果
         */
        @Override
        public void run() {
            if (mScroller.isFinished()) {
                return;
            }
            //如果返回true，说明当前的动画还没有结束，我们可以获得当前的x和y的值
            if (mScroller.computeScrollOffset()) {
                //获得当前的x坐标
                final int newX = mScroller.getCurrX();
                //获得当前的y坐标
                final int newY = mScroller.getCurrY();
                //进行平移操作
                mScaleMatrix.postTranslate(mCurrentX - newX, mCurrentY - newY);
                checkBorderWhenTranslate();
                setImageMatrix(mScaleMatrix);

                mCurrentX = newX;
                mCurrentY = newY;
                //每16ms调用一次
                postDelayed(this, 16);
            }
        }
    }

    public int dp2px(float dpVal) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                dpVal, getResources().getDisplayMetrics());
    }

    public boolean isChange() {
        //1.大小改变
        //2.缩放
        if (getScale() != mInitScale || !cropRectChange()) {
            return true;
        }

        return false;
    }

    public boolean cropRectChange() {
        final float horizontalPadding = dp2px(2.7f);
        return mBitmapRectCopy.top + horizontalPadding == Edge.TOP.getCoordinate()
                && mBitmapRectCopy.left + horizontalPadding == Edge.LEFT.getCoordinate()
                && mBitmapRectCopy.right - horizontalPadding == Edge.RIGHT.getCoordinate()
                && mBitmapRectCopy.bottom - horizontalPadding == Edge.BOTTOM.getCoordinate();
    }
}
