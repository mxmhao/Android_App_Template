package template;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;
import android.view.animation.LinearInterpolator;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.Optional;

public class CircleProgressView extends View {

    private Paint circlePaint;
    private Paint circleBgPaint;
    private float strokeWidth;
    private float radius;
    private float centerX;
    private float centerY;
    private RectF rectF;
    private float sweepAngle;
    private ValueAnimator animator;

    public CircleProgressView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public CircleProgressView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    public CircleProgressView(Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }

    public CircleProgressView(Context context) {
        super(context);
        init();
    }

    private void init() {
        sweepAngle = 0;
        strokeWidth = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 10,
                getResources().getDisplayMetrics());
        // 动画环画笔
        circlePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        circlePaint.setColor(Color.BLUE);
        circlePaint.setStyle(Paint.Style.STROKE);
        circlePaint.setStrokeCap(Paint.Cap.ROUND);
        circlePaint.setStrokeWidth(strokeWidth);

        // 背景环画笔
        circleBgPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        circleBgPaint.setColor(Color.GRAY);
        circleBgPaint.setStyle(Paint.Style.STROKE);
        circleBgPaint.setStrokeCap(Paint.Cap.ROUND);
        circleBgPaint.setStrokeWidth(strokeWidth);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        float minSize = Math.min(getHeight(), getWidth());
        radius = (minSize - strokeWidth) / 2;
        centerX = getWidth() / 2.0f;
        centerY = getHeight() / 2.0f;
        rectF = new RectF();
        rectF.left = 0 + strokeWidth/2;
        rectF.top = 0 + strokeWidth/2;
        rectF.right = minSize - strokeWidth/2;
        rectF.bottom = minSize - strokeWidth/2;
    }

    @Override
    protected void onDraw(@NonNull Canvas canvas) {
        super.onDraw(canvas);

        // 绘制圆环
        canvas.drawCircle(centerX, centerY, radius, circleBgPaint);
//        canvas.drawArc(rectF, 0, 360, false, circleBgPaint);
        canvas.drawArc(rectF, -90, sweepAngle, false, circlePaint);
    }

    /**
     * 开始动画
     * @param totalTime 总时间
     * @param remainingTime 剩余时间
     */
    public void startAnim(long totalTime, long remainingTime) {
        Optional.ofNullable(animator).ifPresent(ValueAnimator::cancel);

        animator = ValueAnimator.ofFloat(-360.0f * remainingTime / totalTime, 0);
        animator.setDuration(remainingTime);
        animator.setInterpolator(new LinearInterpolator());
        animator.addUpdateListener(animation -> {
            sweepAngle = (Float) animation.getAnimatedValue();
            invalidate();
        });
        animator.start();
    }

    public void stopAnim() {
        if (null != animator) {
            animator.cancel();
            animator = null;
        }
        sweepAngle = 0;
        invalidate();
    }
}
