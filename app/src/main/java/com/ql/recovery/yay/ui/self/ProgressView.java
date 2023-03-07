package com.ql.recovery.yay.ui.self;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DrawFilter;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.PaintFlagsDrawFilter;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;

import androidx.annotation.IntDef;
import androidx.annotation.Nullable;

import com.ql.recovery.yay.R;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Created by BlueFire on 2019/9/26  10:56
 * Describe:
 */
public class ProgressView extends View {
    //默认外环颜色
    private static final int OUTER_RING_COLOR = 0xfffbd930;
    //默认内环色
    private static final int OUTER_RING2_COLOR = 0x1afbd930;
    //默认进度条填充颜色
    private static final int OUTER_RING3_COLOR = 0xff643EF9;

    protected Context mContext;
    private String topText, centerText, bottomText;
    private float bottomTextSize;
    private float strokeWidth;
    private int topTextColor, centerTextColor, bottomTextColor, strokeColor, submergedTextColor, progressColor;
    private int max, progress;
    private @BindingText
    int bindingText;

    private Paint bottomPaint, outerRingPaint, outerRingPaint2, outerRingPaint3;
    private DrawFilter mDrawFilter;
    private Rect textRect;
    private Path cirPath;

    private int width, height;

    public static final int[] SWEEP_GRADIENT_COLORS = new int[]{0xff643EF9, 0xff643EF9, 0xff643EF9};

    @IntDef({NONE, TOP_TEXT, CENTER_TEXT, BOTTOM_TEXT})
    @Retention(RetentionPolicy.SOURCE)
    private @interface BindingText {
    }

    public static final int NONE = 0;
    public static final int TOP_TEXT = 1;
    public static final int CENTER_TEXT = 2;
    public static final int BOTTOM_TEXT = 3;

    public ProgressView(Context context) {
        super(context);
        init(context, null);
    }

    public ProgressView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public ProgressView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    private void init(Context context, AttributeSet attrs) {
        this.mContext = context;
        TypedArray array = context.obtainStyledAttributes(attrs, R.styleable.ProgressView);
        bindingText = array.getInt(R.styleable.ProgressView_bindingText, NONE);
        submergedTextColor = array.getColor(R.styleable.ProgressView_submerged_textColor, 0xffffff0);
        max = array.getInt(R.styleable.ProgressView_max, 100);
        strokeWidth = array.getDimension(R.styleable.ProgressView_stroke_width, getResources().getDimension(R.dimen.stroke_width));
        strokeColor = array.getColor(R.styleable.ProgressView_stroke_color, OUTER_RING_COLOR);
        bottomText = array.getString(R.styleable.ProgressView_bottom_text);
        bottomTextColor = array.getColor(R.styleable.ProgressView_bottom_textColor, Color.BLACK);
        progressColor = array.getColor(R.styleable.ProgressView_progress_color, OUTER_RING3_COLOR);
        bottomTextSize = array.getDimension(R.styleable.ProgressView_bottom_textSize, getResources().getDimension(R.dimen.bottom_text_size));

        SWEEP_GRADIENT_COLORS[0] = progressColor;
        SWEEP_GRADIENT_COLORS[1] = progressColor;
        SWEEP_GRADIENT_COLORS[2] = progressColor;

        if (topText == null) topText = "";
        if (centerText == null) centerText = "";
        if (bottomText == null) bottomText = "";

        //外环
        outerRingPaint = new Paint();
        outerRingPaint.setAntiAlias(true);
        outerRingPaint.setColor(OUTER_RING_COLOR);
        outerRingPaint.setStyle(Paint.Style.FILL);
        outerRingPaint.setStrokeCap(Paint.Cap.ROUND);

        //内环
        outerRingPaint2 = new Paint();
        outerRingPaint2.setAntiAlias(true);
        outerRingPaint2.setColor(OUTER_RING2_COLOR);
        outerRingPaint2.setStyle(Paint.Style.FILL);

        //进度条
        outerRingPaint3 = new Paint();
        outerRingPaint3.setAntiAlias(true);
        outerRingPaint3.setStrokeWidth(strokeWidth);
        outerRingPaint3.setColor(OUTER_RING3_COLOR);
        outerRingPaint3.setStyle(Paint.Style.STROKE);
        outerRingPaint3.setStrokeCap(Paint.Cap.ROUND);

        bottomPaint = new Paint();
        bottomPaint.setAntiAlias(true);
        bottomPaint.setTextSize(bottomTextSize);
        mDrawFilter = new PaintFlagsDrawFilter(0, Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG);

        int progress = array.getInt(R.styleable.ProgressView_progress, 0);
        setProgress(progress);
        textRect = new Rect();
        cirPath = new Path();
        array.recycle();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        if (widthMode == MeasureSpec.AT_MOST && heightMode == MeasureSpec.AT_MOST) {
            int size = dp2px(mContext, 150);
            setMeasuredDimension(size, size);
        } else if (widthMode == MeasureSpec.AT_MOST) {
            setMeasuredDimension(dp2px(mContext, 150), MeasureSpec.getSize(heightMeasureSpec));
        } else if (heightMode == MeasureSpec.AT_MOST) {
            int size = MeasureSpec.getSize(widthMeasureSpec);
            setMeasuredDimension(size, size);
        } else {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        }
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        if (w < h)
            width = height = w;
        else
            width = height = h;

        // 根据view总宽度得出所有对应的y值
        cirPath.addCircle(width / 2.0f, height / 2.0f, width / 2.0f - strokeWidth + 0.3f, Path.Direction.CW);
    }

    @SuppressLint("DrawAllocation")
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        float proHeight = height - (float) progress / max * (height - 4 * strokeWidth) - 2 * strokeWidth;

        if (strokeWidth > 0) {
            //绘制外环
//            canvas.drawCircle(width / 2.0f, height / 2.0f, width / 2.0f, outerRingPaint);
            canvas.drawCircle(width / 2.0f, height / 2.0f, width / 2.0f - strokeWidth, outerRingPaint2);

            //绘制内环
            RectF rf1 = new RectF(strokeWidth, strokeWidth, width - strokeWidth, width - strokeWidth);
            canvas.drawArc(rf1, 0, 360, false, outerRingPaint2);

            if (progress > 0) {
                RectF rf = new RectF(strokeWidth, strokeWidth, width - strokeWidth, width - strokeWidth);
                LinearGradient shader = new LinearGradient(0, height / 2.0f, width, height / 2.0f, SWEEP_GRADIENT_COLORS, null, Shader.TileMode.MIRROR);
                outerRingPaint3.setShader(shader);
                canvas.drawArc(rf, 0, (float) (progress * 3.6), false, outerRingPaint3);
            }

            //画文字
            float bottomTextY = height / 2.0f + textRect.height() / 2.0f;

            bottomPaint.setColor(bottomTextY > proHeight + textRect.height() ? (submergedTextColor == 0xffffff0 ? bottomTextColor : submergedTextColor) : bottomTextColor);
            bottomPaint.getTextBounds(bottomText, 0, bottomText.length(), textRect);
            bottomPaint.setTypeface(Typeface.createFromAsset(mContext.getAssets(), "fonts/gilland-r.otf"));
            canvas.drawText(bottomText, width / 2.0f - textRect.width() / 2.0f, height / 2.0f + textRect.height() / 2.0f, bottomPaint);

        }
    }

    private int dp2px(Context context, float dpVal) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                dpVal, context.getResources().getDisplayMetrics());
    }

    public int getMax() {
        return max;
    }

    public void setMax(int max) {
        this.max = max;
        invalidate();
    }

    public int getProgress() {
        return progress;
    }

    /**
     * 可在子线程中刷新
     *
     * @param progress 进度
     */
    @SuppressLint("DefaultLocale")
    public void setProgress(int progress) {
        this.progress = progress;
        bottomText = String.valueOf(progress);
        postInvalidate();
    }

    public int getBindingText() {
        return bindingText;
    }

    public void setBindingText(@BindingText int bindingText) {
        this.bindingText = bindingText;
    }

    public String getTopText() {
        return topText;
    }

    public void setTopText(String topText) {
        this.topText = topText == null ? "" : topText;
        invalidate();
    }

    public String getCenterText() {
        return centerText;
    }

    public void setCenterText(String centerText) {
        this.centerText = centerText == null ? "" : centerText;
        invalidate();
    }

    public String getBottomText() {
        return bottomText;
    }

    public void setBottomText(String bottomText) {
        this.bottomText = bottomText == null ? "" : bottomText;
        invalidate();
    }

    public float getBottomTextSize() {
        return bottomTextSize;
    }

    public void setBottomTextSize(float bottomTextSize) {
        bottomPaint.setTextSize(bottomTextSize);
        invalidate();
    }

    public int getTopTextColor() {
        return topTextColor;
    }

    public void setTopTextColor(int topTextColor) {
        this.topTextColor = topTextColor;
        invalidate();
    }

    public int getCenterTextColor() {
        return centerTextColor;
    }

    public void setCenterTextColor(int centerTextColor) {
        this.centerTextColor = centerTextColor;
        invalidate();
    }

    public int getBottomTextColor() {
        return bottomTextColor;
    }

    public void setBottomTextColor(int bottomTextColor) {
        this.bottomTextColor = bottomTextColor;
        invalidate();
    }

    public float getStrokeWidth() {
        return strokeWidth;
    }

    public void setStrokeWidth(float strokeWidth) {
        this.strokeWidth = strokeWidth;
        invalidate();
    }

    public int getStrokeColor() {
        return strokeColor;
    }

    public void setStrokeColor(int strokeColor) {
        outerRingPaint.setColor(strokeColor);
        invalidate();
    }

}

