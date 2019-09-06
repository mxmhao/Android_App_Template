package template.recyclerview_group;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.TypedValue;
import android.view.View;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

/**
 * 有分类title的 ItemDecoration
 */
public class SectionItemDecoration extends RecyclerView.ItemDecoration {
    public String titles[];
    public int titlePositions[];//不包含titles[0]的位置
    public boolean titleDocksAtTheTop = false;//是否停靠在顶部
    private Paint mPaint;
    private Rect mBounds;//用于存放测量文字Rect

    private int mTitleBgHeight;//title的高
    private int seperatorHeight = 1;//分割线的高度
    private static int COLOR_TITLE_BG = Color.parseColor("#FFDFDFDF");
    private static int COLOR_TITLE_FONT = Color.parseColor("#FF000000");
    private static int mTitleFontSize;//title字体大小


    public SectionItemDecoration(Context context) {
        super();
        mPaint = new Paint();
        mBounds = new Rect();
        mTitleBgHeight = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 30, context.getResources().getDisplayMetrics());
        mTitleFontSize = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 16, context.getResources().getDisplayMetrics());
        mPaint.setTextSize(mTitleFontSize);
        mPaint.setAntiAlias(true);
    }

    @Override
    public void onDraw(Canvas c, RecyclerView parent, RecyclerView.State state) {
        super.onDraw(c, parent, state);

        final int left = parent.getPaddingLeft();
        final int right = parent.getRight() - parent.getPaddingRight();
        // 获取当前屏幕可见 item 数量，而不是 RecyclerView 所有的 item 数量
        final int childCount = parent.getChildCount();

        View child;
        RecyclerView.LayoutParams params;
        int position;
        int bottom;

        for (int i = titleDocksAtTheTop? 1 : 0; i < childCount; i++) {//有悬浮标题时，第0个item不用绘制
            child = parent.getChildAt(i);
            params = (RecyclerView.LayoutParams) child.getLayoutParams();
            position = params.getViewLayoutPosition();
            bottom = child.getTop() - params.topMargin;

            if (0 == position || (position = titleIndexAt(position)) > 0) {
                drawTitleArea(c, left, bottom - mTitleBgHeight, right, bottom, titles[position]);
            } else if (i > 0) {//画分割线，第0个不用绘制
                mPaint.setColor(COLOR_TITLE_BG);
                c.drawRect(left, bottom - seperatorHeight, right, bottom, mPaint);
            }
        }
    }

    /**
     * 绘制Title区域背景和文字的方法
     */
    private void drawTitleArea(Canvas c, int bgLeft, int bgTop, int bgRight, int bgBottom, String title) {//最先调用，绘制在最下层
        //绘制背景
        mPaint.setColor(COLOR_TITLE_BG);
        c.drawRect(bgLeft, bgTop, bgRight, bgBottom, mPaint);
        //绘制文字
        mPaint.setColor(COLOR_TITLE_FONT);
        mPaint.getTextBounds(title, 0, title.length(), mBounds);
        c.drawText(title, bgLeft,
                bgBottom - (mTitleBgHeight/2 - mBounds.height()/2),//y是baseline的位置，这里用文字的底部粗略当baseline
                mPaint);
    }

    @Override
    public void onDrawOver(Canvas c, RecyclerView parent, RecyclerView.State state) {//最后调用 绘制在最上层
        super.onDrawOver(c, parent, state);
        if (!titleDocksAtTheTop) return;//不用停靠在顶部

        final int position = ((LinearLayoutManager) parent.getLayoutManager()).findFirstVisibleItemPosition();
        if (RecyclerView.NO_POSITION == position) {// 越界检查
            return;
        }

        String title = null;
        if (0 == position || null == titlePositions || position < titlePositions[0]) {
            title = titles[0];
        } else if (null != titlePositions) {
            for (int i = titlePositions.length - 1; i > -1; --i) {
                if (position >= titlePositions[i]) {//只要在此范围内，就有标题
                    title = titles[i + 1];
                    break;
                }
            }
        }
        if (null == title) {
            return;
        }

        //第一个可见条目的位置
//        View child = manager.findViewByPosition(position);
        final View child = parent.getChildAt(0);//获取可见的最上面的一个；这种方式获取会不会有问题
        //最上面的item底部的高度<=标题背景的高度，且下一个item含有标题
        if (child.getBottom() <= mTitleBgHeight && titleIndexAt(position + 1) > 0) {
            c.translate(0, child.getBottom() - mTitleBgHeight);//上移
        }

        final int top = parent.getPaddingTop();
        drawTitleArea(c, parent.getPaddingLeft(), top,
                parent.getRight() - parent.getPaddingRight(),
                top + mTitleBgHeight, title);
    }

    @Override
    public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
        super.getItemOffsets(outRect, view, parent, state);
//        int position = ((RecyclerView.LayoutParams) view.getLayoutParams()).getViewLayoutPosition();
//        int position = parent.getLayoutManager().getPosition(view);
        int position = parent.getChildAdapterPosition(view);
        if (0 == position || titleIndexAt(position) > 0) {
            outRect.top = mTitleBgHeight;
        } else {
            outRect.top = seperatorHeight;
        }
    }

    private int titleIndexAt(int position) {
        if (null == titlePositions) return 0;
        for (int i = 0; i < titlePositions.length; ++i) {
            if (position == titlePositions[i]) {
                return i+1;
            }
        }
        return 0;
    }

    public static void setGroup(SectionItemDecoration decoration, SectionRVAdapter adapter, List<String> titles, ArrayList<ArrayList<String>> group) {
        int sections = group.size();
        if (sections == 0) {
            decoration.titlePositions = null;
            decoration.titles = null;
            adapter.count = 0;
            adapter.sectionFirstItemPosition = null;
            return;
        }
        decoration.titles = titles.toArray(new String[titles.size()]);

        int count = group.get(0).size();
        if (sections > 1) {//有多个分组
            //计算从第二组开始的，每组第一项的位置
            int[] sectionFirstItemPosition = new int[sections - 1];
            for (int i = 1; i < sections; i++) {
                sectionFirstItemPosition[i-1] = count;
                count += group.get(i).size();
            }
            decoration.titlePositions = sectionFirstItemPosition;
            adapter.sectionFirstItemPosition = sectionFirstItemPosition;
        } else {
            decoration.titlePositions = null;
            adapter.sectionFirstItemPosition = null;
        }
        adapter.count = count;
    }
}
