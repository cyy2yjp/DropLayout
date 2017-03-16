package droplayout.wdwd.com.droplayout.view;

import android.content.Context;
import android.support.v4.widget.ViewDragHelper;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;

/**
 *
 * Created by tomchen on 17/3/8.
 */

public class DragLayout extends FrameLayout {

    private ViewDragHelper viewDragHelper;

    private View dragContentView;
    private View headView;

    private View captureView;
    private int contentTop;

    private boolean dispatchingChildrenDownFaked = false;
    private boolean dispatchingChildrenContentView = false;
    private float dispatchingChildrenStartedAtY = Float.MAX_VALUE;
    private boolean intercept = false;
    private float ratio;

    public DragLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        viewDragHelper = ViewDragHelper.create(this, 1.0f, callback);
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        //设置headView一直在ContentView头部
        if (captureView == headView || captureView == null) {
            headView.layout(left, contentTop, right, contentTop + headView.getMeasuredHeight());
            dragContentView.layout(left, contentTop + headView.getMeasuredHeight(), right, bottom);
        } else {
            dragContentView.layout(left, contentTop, right, bottom);
            headView.layout(left, contentTop - headView.getHeight(), right, contentTop);
        }
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        dragContentView = getChildAt(1);
        headView = getChildAt(0);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int maxWidth = MeasureSpec.getSize(widthMeasureSpec);
        int maxHeight = MeasureSpec.getSize(heightMeasureSpec);
        setMeasuredDimension(resolveSizeAndState(maxWidth, widthMeasureSpec, 0), resolveSizeAndState(maxHeight, heightMeasureSpec, 0));
    }

    ViewDragHelper.Callback callback = new ViewDragHelper.Callback() {
        @Override
        public boolean tryCaptureView(View child, int pointerId) {
            captureView = child;
            return child == headView || child == dragContentView;
        }

        //返回拖动view垂直方向拖动的范围， 返回的是与上下的边距
        @Override
        public int clampViewPositionVertical(View child, int top, int dy) {
            //top 必须限制在刚好能隐藏headerView的位置
            int newTop = top;
            if (child == headView) {
                final int minTop = -headView.getMeasuredHeight(); //刚好将topView隐藏
                final int maxTop = 0;

                if (top <= minTop) {
                    newTop = minTop;
                } else if (top >= maxTop) {
                    newTop = maxTop;
                }
            } else if (child == dragContentView) { //如果是contentView那么滑动的范围刚好在顶部到 headerView的高度
                final int minTop = 0;
                final int maxTop = headView.getMeasuredHeight();
                newTop = Math.min(Math.max(top, minTop), maxTop);
            }
            return newTop;
        }


        @Override
        public int getViewVerticalDragRange(View child) {
            return getHeight();
        }

        @Override
        public void onViewReleased(View releasedChild, float xvel, float yvel) {
            super.onViewReleased(releasedChild, xvel, yvel);
            //拖动手势释放后的处理，xvel 和yvel 是速度
        }

        @Override
        public void onViewPositionChanged(View changedView, int left, int top, int dx, int dy) {
            super.onViewPositionChanged(changedView, left, top, dx, dy);
            //在onViewPositionChanged方法中控制拖动后新位置的处理。因为拖动过程中还需对TopView进行响应的处理，所以在方法内记录拖动的top位置
            //并在onLayout中回调处理最新位置的显示
            contentTop = top;
            ratio = top * 1.0f / headView.getHeight();
            requestLayout();
        }
    };


    public void setTouchMode(boolean intercept){
        this.intercept = intercept;
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        super.onInterceptTouchEvent(ev);
        try {
            boolean intercept = this.intercept &&viewDragHelper.shouldInterceptTouchEvent(ev);
            return intercept;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }


    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        return super.dispatchTouchEvent(ev);
    }

    @Override
    public void focusableViewAvailable(View v) {
        super.focusableViewAvailable(v);
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        if (captureView == headView) {
            processEvent(ev);
            return true;
        }

        if (!dispatchingChildrenContentView) {
            processEvent(ev);
        }

        //如果在滑动 并且headerView已经隐藏 或者 显示 则滑动 RecyclerView
        if (ev.getAction() == MotionEvent.ACTION_MOVE && ratio == 0) {
            dispatchingChildrenContentView = true;//设置标识不再传递事件给viewDragHelper
            if (!dispatchingChildrenDownFaked) {//如果是首次传递事件给RecyclerView
                dispatchingChildrenStartedAtY = ev.getY(); //记录当前滑动的Y点
                ev.setAction(MotionEvent.ACTION_DOWN);
                dispatchingChildrenDownFaked = true;//翻转标识
            }
            dragContentView.dispatchTouchEvent(ev);
        }

        if (dispatchingChildrenContentView && dispatchingChildrenStartedAtY < ev.getY()) {
            resetDispatchingContentView();
        }

        if (ev.getAction() == MotionEvent.ACTION_UP || ev.getAction() == MotionEvent.ACTION_CANCEL) {
            processEvent(ev);
            dragContentView.dispatchTouchEvent(ev);
            resetDispatchingContentView();
        }
        return true;
    }

    private void processEvent(MotionEvent ev) {
        try {
            viewDragHelper.processTouchEvent(ev);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void resetDispatchingContentView() {
        dispatchingChildrenDownFaked = false;
        dispatchingChildrenContentView = false;
        dispatchingChildrenStartedAtY = Float.MAX_VALUE;
    }
}
