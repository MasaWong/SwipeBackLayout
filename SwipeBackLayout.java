package mw.ankara.base.widget;

import android.app.Activity;
import android.content.Context;
import android.support.v4.view.MotionEventCompat;
import android.support.v4.view.ViewCompat;
import android.support.v4.widget.ViewDragHelper;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.LinearLayout;

/**
 * swipe back layout : use to swipe right to finish activity
 * main code:
 * {@link #mDragHelper} help to deal with dragging event
 * {@link #mDraggableView} the child view that can be dragged
 * {@link DragCallback} drag callback used in {@link #mDragHelper}
 * {@link android.support.v4.view.ViewCompat} animate scrolling
 *
 * @author MasaWong
 * @date 15/1/7.
 */
public class SwipeBackLayout extends LinearLayout {

    private ViewDragHelper mDragHelper;

    private int mDraggableId;
    private View mDraggableView;

    public SwipeBackLayout(Context context) {
        super(context);
        createAndInit();
    }

    public SwipeBackLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SwipeBackLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        createAndInit();
    }

    /**
     * create {@link #mDragHelper} and set only tracking from left edge
     */
    private void createAndInit() {
        mDragHelper = ViewDragHelper.create(this, 2.0f, new DragCallback());
        mDragHelper.setEdgeTrackingEnabled(ViewDragHelper.EDGE_LEFT);
    }

    /**
     * after inflated, find {@link #mDraggableView} by {@link #mDraggableId}
     */
    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();

        if (mDraggableId != 0) {
            mDraggableView = findViewById(mDraggableId);
        } else if (getChildCount() > 0) {
            mDraggableView = getChildAt(0);
        } else {
            mDraggableView = null;
        }
    }

    /**
     * pass event to {@link #mDragHelper}
     * @param event
     * @return
     */
    @Override
    public boolean onInterceptTouchEvent(MotionEvent event) {
        int action = MotionEventCompat.getActionMasked(event);
        if (action == MotionEvent.ACTION_CANCEL || action == MotionEvent.ACTION_UP) {
            mDragHelper.cancel();
            return super.onInterceptTouchEvent(event);
        } else {
            return mDragHelper.shouldInterceptTouchEvent(event);
        }
    }

    /**
     * pass event to {@link #mDragHelper}
     * @param event
     * @return
     */
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        mDragHelper.processTouchEvent(event);

        int action = MotionEventCompat.getActionMasked(event);
        if (action == MotionEvent.ACTION_CANCEL || action == MotionEvent.ACTION_UP) {
            if (mDraggableView.getLeft() > getWidth() / 3) {
                // animate to position right edge and then finish activity
                slideTo(getRight());
            } else {
                // animate back to position left edge
                slideTo(getLeft());
            }
        }
        return true;
    }

    /**
     * called by {@link #slideTo(int)} indirectly
     * if need continue scrolling, use {@link android.support.v4.view.ViewCompat} to animate scroll,
     * else if need finish activity, finish it
     */
    @Override
    public void computeScroll() {
        if (mDragHelper.continueSettling(true)) {
            ViewCompat.postInvalidateOnAnimation(this);
        } else if (mDraggableView.getLeft() == getWidth()) {
            ((Activity) getContext()).onBackPressed();
        }
    }

    /**
     * animate scroll to
     * @param position final position that scrolled to
     * @return if scrolling is necessary
     */
    protected boolean slideTo(int position) {
        if (mDragHelper.smoothSlideViewTo(mDraggableView, position, 0)) {
            ViewCompat.postInvalidateOnAnimation(this);
            return true;
        } else {
            return false;
        }
    }

    public void setDraggableViewId(int id) {
        mDraggableId = id;
    }

    /**
     * drag callback for {@link android.support.v4.widget.ViewDragHelper},this class implements
     * abstract method {@link #tryCaptureView(android.view.View, int)} and override method
     * {@link #clampViewPositionHorizontal(android.view.View, int, int)} to drag horizontally
     * {@link #onEdgeDragStarted(int, int)} determine starting dragging from left edge
     *
     */
    protected class DragCallback extends ViewDragHelper.Callback {

        @Override
        public boolean tryCaptureView(View child, int pointerId) {
            return child == mDraggableView;
        }

        @Override
        public int clampViewPositionHorizontal(View child, int left, int dx) {
            return left;
        }

        @Override
        public void onEdgeDragStarted(int edgeFlags, int pointerId) {
            mDragHelper.captureChildView(mDraggableView, pointerId);
        }
    }
}
