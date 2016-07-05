package com.ghx.guideviewdemo.weiget;

import com.ghx.guideviewdemo.utils.LogUtils;

import android.graphics.PorterDuffXfermode;
import android.view.ViewTreeObserver;
import android.widget.RelativeLayout;
import android.graphics.PorterDuff;
import android.widget.FrameLayout;
import android.util.AttributeSet;
import com.ghx.guideviewdemo.R;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.app.Activity;
import android.view.Gravity;
import android.view.View;


/**
 * Created by ghx on 2016/7/4.
 * <p/>
 * 导航浮层引导
 */
public class GuideView extends RelativeLayout implements ViewTreeObserver.OnGlobalLayoutListener {

    private final String TAG = getClass().getSimpleName();
    private Context mContext;
    private boolean mFirst = true;//第一次进入
    boolean mNeedDraw = true;//是否需要绘制
    private boolean mIsMeasured;//是否需要测量
    private boolean mOnClickExit;
    private OnClickCallback mOnclickListener;

    /**
     * targetView前缀。SHOW_GUIDE_PREFIX + targetView.getId()作为保存在SP文件的key。
     */
    private static final String SHOW_GUIDE_PREFIX = "show_guide_";

    /**
     * 使用者自定义的View,由提供的方法设置进来，注意和TargetView区分开来
     */

    private View mCustomGuideView;
    /**
     * 浮层覆盖的那个View，由提供的方法设置进来，注意和CustomGuideView区分开来
     */
    private View mTargetView;

    /**
     * mCustomGuideView 相对于 mTargetView 的位置。 自定义枚举类
     */
    private Direction mDirection;

    /**
     * mTargetView 的圆心
     */
    private int[] mCenter;

    /**
     * mTargetView 的外切圆半径
     */
    private int mRadius;

    /**
     * mCustomGuideView 相对于 mTargetView X轴的偏移量
     */
    private int mOffsetX;

    /**
     * mCustomGuideView 相对于 mTargetView Y轴的偏移量
     */
    private int mOffsetY;

    /**
     * 前景 Bitmap
     */
    private Bitmap mBitmap;

    /**
     * Canvas 画布
     */
    private Canvas mTemp;

    /**
     * 背景颜色， 由提供的方法设置进来
     */
    private int mBgColor;

    /**
     * 圆形画笔， 用来画 mTargetView 的形状
     */
    private Paint mCirclePaint;

    /**
     * mTargetView 的形状， 由提供的方法设置进来
     */
    private TargetViewShape mShape;

    /**
     * 画椭圆的参数，由提供的方法设置进来。 4 个数，以此为 L, T, R, B
     * 默认为圆形
     */
    private int[] mOvalParameter;

    /**
     * 画矩形圆角的参数，由提供的方法设置进来。 2 个数，X, Y 轴圆角的半径
     * 默认为无倒角
     */
    private int[] mRoundRecTParameter;

    /**
     * 绘图层叠模式
     */
    private PorterDuffXfermode mPorterDuffXfermode;

    /**
     * mTargetView 左上角坐标
     */
    private int[] mLocation;


    private GuideView(Context context) {

        this(context, null);
    }

    private GuideView(Context context, AttributeSet attrs) {

        this(context, attrs, 0);
    }

    private GuideView(Context context, AttributeSet attrs, int defStyleAttr) {

        super(context, attrs, defStyleAttr);
        mContext = context;
        init();
    }

    private void init() {
        setLayerType(View.LAYER_TYPE_SOFTWARE, null);
    }


    public void restoreState() {

        LogUtils.debug("restoreState");
        mOffsetX = 0;
        mOffsetY = 0;
        mRadius = 0;
        mCirclePaint = null;
        mIsMeasured = false;
        mCenter = null;
        mPorterDuffXfermode = null;
        mBitmap = null;
        mNeedDraw = true;
        mTemp = null;
    }

    @Override
    protected void onDraw(Canvas canvas) {

        super.onDraw(canvas);
        canvas.isHardwareAccelerated();
        LogUtils.debug("onDraw");

        if (!mIsMeasured) {
            return;
        }

        if (mTargetView == null) {
            return;
        }

        drawBackground(canvas);
    }

    private void drawBackground(Canvas canvas) {

        LogUtils.debug("drawBackground");
        mNeedDraw = false;

        // 先绘制bitmap，再将bitmap绘制到屏幕
//        mBitmap = Bitmap.createBitmap(canvas.getWidth(), canvas.getHeight(), Bitmap.Config.ARGB_8888);
//        mBitmap = Bitmap.createBitmap(canvas.getWidth(), canvas.getHeight(), Bitmap.Config.RGB_565);
//        mBitmap = Bitmap.createBitmap(canvas.getWidth(), canvas.getHeight(), Bitmap.Config.ARGB_4444);
//        mTemp = new Canvas(mBitmap);
        mTemp = canvas;

//        int byteCount = mBitmap.getByteCount();
//        Log.d("kankan", byteCount / 1024 / 1024 + "");

        //背景画笔
        Paint bgPaint = new Paint();
        //设置背景画笔的颜色
        bgPaint.setColor(mBgColor == 0 ? getResources().getColor(R.color.guide_shadow) : mBgColor);
        //画屏幕背景 Rect
        mTemp.drawRect(0, 0, mTemp.getWidth(), mTemp.getHeight(), bgPaint);

        // mTargetView 的透明圆形画笔
        if (mCirclePaint == null) {
            mCirclePaint = new Paint();
            mCirclePaint.setAntiAlias(true);
        }

        //SRC_OUT也可以
        mPorterDuffXfermode = new PorterDuffXfermode(PorterDuff.Mode.CLEAR);//SRC_OVER
        mCirclePaint.setXfermode(mPorterDuffXfermode);

        if (mShape == null) {//如果使用者没setShape，则默认为圆形
            mTemp.drawCircle(mCenter[0], mCenter[1], mRadius, mCirclePaint);//绘制圆形
        } else {

            switch (mShape) {
                case CIRCULAR://圆

                    mTemp.drawCircle(mCenter[0], mCenter[1], mRadius, mCirclePaint);//绘制圆

                    break;
                case ELLIPSE://椭圆

                    RectF rectF = new RectF();
                    if (mOvalParameter == null) {
                        rectF.left = mCenter[0] - mTargetView.getWidth() / 2;
                        rectF.top = mCenter[1] - mTargetView.getHeight() / 2;
                        rectF.right = mCenter[0] + mTargetView.getWidth() / 2;
                        rectF.bottom = mCenter[1] + mTargetView.getHeight() / 2;
                    } else if (mOvalParameter != null && mOvalParameter.length != 4) {
                        throw new IllegalArgumentException("此参数的大小必须为4(椭圆参数); " +
                                "the length of this array must be 4");
                    } else {
                        rectF.left = mCenter[0] - mTargetView.getWidth() / 2 - mOvalParameter[0];
                        rectF.top = mCenter[1] - mTargetView.getHeight() / 2 - mOvalParameter[1];
                        rectF.right = mCenter[0] + mTargetView.getWidth() / 2 + mOvalParameter[2];
                        rectF.bottom = mCenter[1] + mTargetView.getHeight() / 2 + mOvalParameter[3];
                    }
                    mTemp.drawOval(rectF, mCirclePaint);//绘制椭圆

                    break;
                case RECTANGULAR://矩形，支持倒角

                    RectF rectF2 = new RectF();
                    rectF2.left = mCenter[0] - mTargetView.getWidth() / 2;
                    rectF2.top = mCenter[1] - mTargetView.getHeight() / 2;
                    rectF2.right = mCenter[0] + mTargetView.getWidth() / 2;
                    rectF2.bottom = mCenter[1] + mTargetView.getHeight() / 2;
                    int radios[] = new int[2];
                    if (mRoundRecTParameter == null) {
                        radios[0] = 0;
                        radios[1] = 0;
                    } else if (mRoundRecTParameter != null && mRoundRecTParameter.length != 2) {
                        throw new IllegalArgumentException("此参数的大小必须为2;（矩形圆角参数） " +
                                "the length of this array must be 2");
                    } else {
                        radios[0] = mRoundRecTParameter[0];
                        radios[1] = mRoundRecTParameter[1];
                    }
                    mTemp.drawRoundRect(rectF2, radios[0], radios[1], mCirclePaint);//绘制圆角矩形

                    break;
            }
        }
        // 绘制到屏幕
//        canvas.drawBitmap(mBitmap, 0, 0, bgPaint);
//        mBitmap.recycle();
    }

    /**
     * 实现ViewTreeObserver.OnGlobalLayoutListener接口重写的方法。
     */
    @Override
    public void onGlobalLayout() {

        if (mIsMeasured) {
            return;
        }

        if (mTargetView.getHeight() > 0 && mTargetView.getWidth() > 0) {
            mIsMeasured = true;
        }

        // 获取targetView的中心坐标
        if (mCenter == null) {
            // 获取右上角坐标
            mLocation = new int[2];
            mTargetView.getLocationInWindow(mLocation);
            mCenter = new int[2];
            // 获取中心坐标
            mCenter[0] = mLocation[0] + mTargetView.getWidth() / 2;
            mCenter[1] = mLocation[1] + mTargetView.getHeight() / 2;
        }
        // 获取targetView外切圆半径
        if (mRadius == 0) {
            mRadius = getTargetViewRadius();
        }

        // 添加GuideView
        createGuideView();
    }

    private void createGuideView() {

        LogUtils.debug("createGuideView");
        LayoutParams guideViewParams = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
//        guideViewParams.setMargins();//设置了以后没看出来区别，先不管

        if (mCustomGuideView != null) {
            if (mDirection != null) {

                int width = this.getWidth();
                int height = this.getHeight();

                int left = mCenter[0] - mRadius;
                int right = mCenter[0] + mRadius;
                int top = mCenter[1] - mRadius;
                int bottom = mCenter[1] + mRadius;

                switch (mDirection) {
                    case LEFT:

                        this.setGravity(Gravity.RIGHT);
                        guideViewParams.setMargins(mOffsetX - width + left,
                                top + mOffsetY,
                                width - left - mOffsetX,
                                -top - mOffsetY);

                        break;
                    case TOP:

                        this.setGravity(Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL);
                        guideViewParams.setMargins(mOffsetX,
                                mOffsetY - height + top,
                                -mOffsetX,
                                height - top - mOffsetY);

                        break;
                    case RIGHT:

                        guideViewParams.setMargins(right + mOffsetX,
                                top + mOffsetY,
                                -right - mOffsetX,
                                -top - mOffsetY);

                        break;
                    case BOTTOM:

                        this.setGravity(Gravity.CENTER_HORIZONTAL);
                        guideViewParams.setMargins(mOffsetX,
                                bottom + mOffsetY,
                                -mOffsetX,
                                -bottom - mOffsetY);

                        break;
                    case LEFT_TOP:

                        this.setGravity(Gravity.RIGHT | Gravity.BOTTOM);
                        guideViewParams.setMargins(mOffsetX - width + left,
                                mOffsetY - height + top,
                                width - left - mOffsetX,
                                height - top - mOffsetY);

                        break;
                    case LEFT_BOTTOM:

                        this.setGravity(Gravity.RIGHT);
                        guideViewParams.setMargins(mOffsetX - width + left,
                                bottom + mOffsetY,
                                width - left - mOffsetX,
                                -bottom - mOffsetY);

                        break;
                    case RIGHT_TOP:

                        this.setGravity(Gravity.BOTTOM);
                        guideViewParams.setMargins(right + mOffsetX,
                                mOffsetY - height + top,
                                -right - mOffsetX,
                                height - top - mOffsetY);

                        break;
                    case RIGHT_BOTTOM:

                        guideViewParams.setMargins(right + mOffsetX,
                                bottom + mOffsetY,
                                -right - mOffsetX,
                                -top - mOffsetY);

                        break;
                }
            } else {//如果没设置方向
                guideViewParams.setMargins(mOffsetX, mOffsetY, -mOffsetX, -mOffsetY);
            }

            this.addView(mCustomGuideView, guideViewParams);
        }
    }

    public void showOnce() {

        if (mTargetView != null) {
            mContext.getSharedPreferences(TAG, Context.MODE_PRIVATE)
                    .edit()
                    .putBoolean(generateUniqId(mTargetView), true)
                    .commit();
        }
    }

    private String generateUniqId(View v) {

        return SHOW_GUIDE_PREFIX + v.getId();
    }

    private boolean hasShown() {

        if (mTargetView == null)
            return true;
        return mContext.getSharedPreferences(TAG, Context.MODE_PRIVATE)
                .getBoolean(generateUniqId(mTargetView), false);
    }

    public void hide() {

        LogUtils.debug("hide");
        if (mCustomGuideView != null) {
            mTargetView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
            this.removeAllViews();
            ((FrameLayout) ((Activity) mContext).getWindow().getDecorView()).removeView(this);
            restoreState();
        }
    }

    public void show() {

        LogUtils.debug("show");
        if (hasShown())
            return;

        if (mTargetView != null) {
            mTargetView.getViewTreeObserver().addOnGlobalLayoutListener(this);
        }

        this.setBackgroundResource(R.color.transparent);

        ((FrameLayout) ((Activity) mContext).getWindow().getDecorView()).addView(this);
        mFirst = false;
    }

    /**
     * 获得targetView 的宽高，如果未测量，返回｛-1， -1｝
     *
     * @return
     */
    private int[] getTargetViewSize() {

        int[] location = {-1, -1};
        if (mIsMeasured) {
            location[0] = mTargetView.getWidth();
            location[1] = mTargetView.getHeight();
        }
        return location;
    }

    /**
     * 获得targetView 的半径
     *
     * @return
     */
    private int getTargetViewRadius() {

        if (mIsMeasured) {
            int[] size = getTargetViewSize();
            int x = size[0];
            int y = size[1];

            return (int) (Math.sqrt(x * x + y * y) / 2);
        }
        return -1;
    }

    public void setOnClickExit(boolean onClickExit) {

        mOnClickExit = onClickExit;
        this.mOnClickExit = onClickExit;
    }

    public void setOnclickListener(OnClickCallback onclickListener) {

        mOnclickListener = onclickListener;
        this.mOnclickListener = onclickListener;
    }

    private void setClickInfo() {

        final boolean exit = mOnClickExit;
        setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mOnclickListener != null) {
                    mOnclickListener.onClickedGuideView();
                }
                if (exit) {
                    hide();
                }
            }

        });
    }

    /**
     * GuideView点击Callback
     */
    public interface OnClickCallback {

        void onClickedGuideView();
    }

    //--------------------------向外部 提供的方法--------------------------------------------------------------

    public void setCustomGuideView(View customGuideView) {

        mCustomGuideView = customGuideView;
        if (!mFirst) {
            restoreState();
        }
    }

    public void setTargetView(View targetView) {

        mTargetView = targetView;
    }

    public void setBgColor(int bgColor) {

        mBgColor = bgColor;
    }

    public void setShape(TargetViewShape shape) {

        mShape = shape;
    }

    public void setCenter(int[] center) {

        this.mCenter = center;
    }

    public void setRadius(int radius) {

        this.mRadius = radius;
    }

    public void setDirection(Direction direction) {

        mDirection = direction;
    }

    public void setOvalParameter(int[] ovalParameter) {

        mOvalParameter = ovalParameter;
    }

    public void setRoundRecTParameter(int[] roundRecTParameter) {

        mRoundRecTParameter = roundRecTParameter;
    }

    public void setLocation(int[] location) {

        this.mLocation = location;
    }

    public void setOffsetX(int offsetX) {

        mOffsetX = offsetX;
    }

    public void setOffsetY(int offsetY) {

        mOffsetY = offsetY;
    }

    //---------------------------------------------------------------------------------------------------------

    /**
     * mCustomGuideView 相对于 mTargetView 的方位
     */
    public enum Direction {

        LEFT, TOP, RIGHT, BOTTOM,
        LEFT_TOP, LEFT_BOTTOM,
        RIGHT_TOP, RIGHT_BOTTOM
    }

    /**
     * mTargetView 的形状，共3种。圆形，椭圆，带圆角的矩形。 默认为圆形
     */
    public enum TargetViewShape {

        CIRCULAR, ELLIPSE, RECTANGULAR
    }


    public static class Builder {

        static GuideView guiderView;
        static Builder instance = new Builder();
        Context mContext;

        private Builder() {
        }

        public Builder(Context context) {

            mContext = context;
        }

        public static Builder newInstance(Context context) {

            guiderView = new GuideView(context);
            return instance;
        }

        public Builder setTargetView(View target) {

            guiderView.setTargetView(target);
            return instance;
        }

        public Builder setBgColor(int color) {

            guiderView.setBgColor(color);
            return instance;
        }

        public Builder setDirction(Direction dir) {

            guiderView.setDirection(dir);
            return instance;
        }

        public Builder setShape(TargetViewShape shape) {

            guiderView.setShape(shape);
            return instance;
        }

        public Builder setOffset(int x, int y) {

            guiderView.setOffsetX(x);
            guiderView.setOffsetY(y);
            return instance;
        }

        public Builder setRadius(int radius) {

            guiderView.setRadius(radius);
            return instance;
        }

        public Builder setCustomGuideView(View view) {

            guiderView.setCustomGuideView(view);
            return instance;
        }

        public Builder setCenter(int X, int Y) {

            guiderView.setCenter(new int[]{X, Y});
            return instance;
        }

        public Builder showOnce() {

            guiderView.showOnce();
            return instance;
        }

        public GuideView build() {

            guiderView.setClickInfo();
            return guiderView;
        }

        public Builder setOnclickExit(boolean onclickExit) {

            guiderView.setOnClickExit(onclickExit);
            return instance;
        }

        public Builder setOnclickListener(final OnClickCallback callback) {

            guiderView.setOnclickListener(callback);
            return instance;
        }

        public Builder setOvalParameter(int[] ovalParameter) {

            guiderView.setOvalParameter(ovalParameter);
            return instance;
        }

        public Builder setRoundRecTParameter(int[] roundRecTParameter) {

            guiderView.setRoundRecTParameter(roundRecTParameter);
            return instance;
        }

    }

}
