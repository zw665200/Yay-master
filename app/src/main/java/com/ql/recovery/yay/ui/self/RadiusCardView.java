package com.ql.recovery.yay.ui.self;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Region;
import android.graphics.drawable.ColorDrawable;
import android.util.AttributeSet;

import androidx.cardview.widget.CardView;

import com.ql.recovery.yay.R;

/**
 * @author Herr_Z
 * @description:
 * @date : 2022/11/18 16:18
 */
public class RadiusCardView extends CardView {

    private final float tlRadius;
    private final float trRadius;
    private final float brRadius;
    private final float blRadius;
    private final Path mPath;

    public RadiusCardView(Context context) {
        this(context, null);
    }

    public RadiusCardView(Context context, AttributeSet attrs) {
        this(context, attrs, R.attr.materialCardViewStyle);
    }

    public RadiusCardView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setRadius(0);
        TypedArray array = context.obtainStyledAttributes(attrs, R.styleable.RadiusCardView);
        tlRadius = array.getDimension(R.styleable.RadiusCardView_topLeftRadius, 0);
        trRadius = array.getDimension(R.styleable.RadiusCardView_topRightRadius, 0);
        brRadius = array.getDimension(R.styleable.RadiusCardView_bottomRightRadius, 0);
        blRadius = array.getDimension(R.styleable.RadiusCardView_bottomLeftRadius, 0);
        setBackground(new ColorDrawable());
        mPath = new Path();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        RectF rectF = getRectF();
        float[] radius = {tlRadius, tlRadius, trRadius, trRadius, brRadius, brRadius, blRadius, blRadius};
        mPath.addRoundRect(rectF, radius, Path.Direction.CW);
        canvas.clipPath(mPath, Region.Op.INTERSECT);
        super.onDraw(canvas);
    }

    private RectF getRectF() {
        Rect rect = new Rect();
        getDrawingRect(rect);
        return new RectF(rect);
    }
}
