package tmnt.example.refreshlayout.RefreshLayout;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.support.v7.widget.RecyclerView;
import android.test.suitebuilder.TestMethod;
import android.text.LoginFilter;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Scroller;
import android.widget.TextView;

import tmnt.example.refreshlayout.DensityUtils;
import tmnt.example.refreshlayout.R;

/**
 * Created by tmnt on 2017/4/18.
 */

public class RefreshLayout extends ViewGroup {

    private View header;
    private View footer;
    private LinearLayout header_contain;
    private LinearLayout footer_contain;
    private LinearLayout pbHeader;
    private LinearLayout pbFooter;
    private TextView tvHeader;
    private TextView tvFooter;
    private int mContentHeight;
    private int count;
    private boolean isCanRefresh;
    private boolean isCanLoad;
    private static final int REFRESHOVER_HANDLER = 0100;
    private static final int LOADOVER_HANDLER = 0200;

    private boolean isRefreshOver;
    private boolean isLoadOver;

    private OnRefreshListener mOnRefreshListener;
    private Context mContext;

    private static final String TAG = "RefreshLayout";
    private Scroller mScroller;
    private int y;

    private RefreshHandler handler = new RefreshHandler();
    private int mLastMotionY;
    private int mLastMotionX;

    public RefreshLayout(Context context) {
        super(context);
        init(context);
        this.mContext = context;
    }

    public RefreshLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
        this.mContext = context;
    }

    public RefreshLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
        this.mContext = context;
    }

    private void init(Context context) {
        header = LayoutInflater.from(context).inflate(R.layout.header_lay, null);
        header_contain = (LinearLayout) header.findViewById(R.id.header_contain);
        pbHeader = (LinearLayout) header.findViewById(R.id.pb_header);
        tvHeader = (TextView) header.findViewById(R.id.tv_header);

        footer = LayoutInflater.from(context).inflate(R.layout.footer_lay, null);
        footer_contain = (LinearLayout) footer.findViewById(R.id.footer_contain);
        pbFooter = (LinearLayout) footer.findViewById(R.id.pb_footer);
        tvFooter = (TextView) footer.findViewById(R.id.tv_footer);

        mScroller = new Scroller(context);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();

        addView(header);
        addView(footer);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        count = getChildCount();
        for (int i = 0; i < count; i++) {
            View child = getChildAt(i);
            measureChild(child, widthMeasureSpec, heightMeasureSpec);
        }
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        mContentHeight = 0;
        for (int i = 0; i < count; i++) {
            View child = getChildAt(i);
            if (child == header) {
                child.layout(0, 0 - child.getMeasuredHeight(), child.getMeasuredWidth(), 0);
            } else if (child != footer) {
                child.layout(0, mContentHeight, child.getMeasuredWidth(), mContentHeight + child.getMeasuredHeight());
                mContentHeight += child.getMeasuredHeight();

            } else {

                child.layout(0, mContentHeight, child.getMeasuredWidth(), mContentHeight + child.getMeasuredHeight());
            }
        }
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {

        boolean resume = false;
        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:
                // 发生down事件时,记录y坐标
                mLastMotionY = (int) ev.getRawY();
                mLastMotionX = (int) ev.getRawX();
                resume = false;
                break;
            case MotionEvent.ACTION_MOVE:
                View v = getChildAt(0);
                if (v instanceof AbsListView) {
                    Log.i(TAG, "onInterceptTouchEvent: " + (ev.getRawY() - mLastMotionY));
                    AbsListView absListView = (AbsListView) v;
                    if (ev.getRawY() - mLastMotionY > 0 && absListView.getFirstVisiblePosition() == 0) {
                        resume = true;
                    } else if (ev.getRawY() - mLastMotionY < 0
                            && absListView.getLastVisiblePosition() == absListView.getAdapter().getCount() - 1) {
                        Log.i(TAG, "onInterceptTouchEvent: " + absListView.getLastVisiblePosition() + " real:" + absListView.getAdapter().getCount());
                        resume = true;
                    } else {
                        resume = false;
                    }
                } else if (v instanceof RecyclerView) {
                    RecyclerView recyclerView = (RecyclerView) v;
                    if (ev.getRawY() - mLastMotionY > 0 && !recyclerView.canScrollVertically(1)) {
                        resume = true;
                    } else if (ev.getRawY() - mLastMotionY < 0 && !recyclerView.canScrollVertically(-1)) {
                        resume = true;
                    } else {
                        resume = false;
                    }
                }

                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                break;
        }
        return resume;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                y = (int) event.getRawY();

                break;
            case MotionEvent.ACTION_UP:
                int upY = (int) event.getRawY();

                if (upY - y > 0) {
                    if (isCanRefresh) {
                        mScroller.startScroll(0, this.getScrollY(), 0
                                , Math.abs(getScrollY()) - header_contain.getMeasuredHeight() + 1, 2000);
                        if (mScroller.computeScrollOffset()) {
                            header_contain.setVisibility(GONE);
                            pbHeader.setVisibility(VISIBLE);
                        }
                    }

                } else if (upY - y < 0) {
                    if (isCanLoad) {
                        mScroller.startScroll(0, this.getScrollY(), 0
                                , -(this.getScrollY() - footer_contain.getMeasuredHeight()) + 1, 2000);
                        if (mScroller.computeScrollOffset()) {
                            footer_contain.setVisibility(GONE);
                            pbFooter.setVisibility(VISIBLE);
                        }
                    }

                }

                break;
            case MotionEvent.ACTION_MOVE:
                int currY = (int) event.getRawY();

                if (currY - y > 0) {
                    if (isCanRefresh) {
                        if (Math.abs(currY - y) <= header.getMeasuredHeight() / 2) {
                            this.scrollTo(0, -(currY - y));
                            if (Math.abs(currY - y) >= header_contain.getMeasuredHeight()) {
                                tvHeader.setText("松开刷新");
                            }
                        }
                    }

                } else if (currY - y < 0) {
                    if (isCanLoad) {
                        if (Math.abs(currY - y) <= header.getMeasuredHeight() / 2) {
                            this.scrollTo(0, -(currY - y));
                            if (Math.abs(currY - y) >= footer_contain.getMeasuredHeight()) {
                                tvFooter.setText("松开刷新");
                            }
                        }

                    }

                }
                break;
        }
        return true;
    }

    @Override
    public void computeScroll() {
        super.computeScroll();
        if (mScroller.computeScrollOffset()) {
            this.scrollTo(mScroller.getCurrX(), mScroller.getCurrY());
            postInvalidate();
            if (mScroller.isFinished()) {
                if (mOnRefreshListener != null) {
                    if (pbHeader.getVisibility() == VISIBLE) {
                        mOnRefreshListener.onRefresh();
                    } else if (pbFooter.getVisibility() == VISIBLE) {
                        mOnRefreshListener.onLoad();
                    }
                }
            }
        }
    }

    public void setOnRefreshListener(OnRefreshListener onRefreshListener) {
        mOnRefreshListener = onRefreshListener;
    }

    public void setLoadOver(boolean loadOver) {
        isLoadOver = loadOver;
        handler.sendEmptyMessage(LOADOVER_HANDLER);

    }

    public void setRefreshOver(boolean refreshOver) {
        isRefreshOver = refreshOver;
        handler.sendEmptyMessage(REFRESHOVER_HANDLER);

    }

    public void setCanRefresh(boolean canRefresh) {
        isCanRefresh = canRefresh;

    }

    public void setCanLoad(boolean canLoad) {
        isCanLoad = canLoad;
    }


    private void headerRecover() {
        Log.i(TAG, "setLoadOver: " + mScroller.getCurrY());
        mScroller.startScroll(0, mScroller.getCurrY()
                , 0, header_contain.getMeasuredHeight(), 1000);

        pbHeader.setVisibility(GONE);
        header_contain.setVisibility(VISIBLE);
        tvHeader.setText("继续向下");

    }

    private void footerRecover() {
        Log.i(TAG, "setLoadOver: " + mScroller.getCurrY());
        mScroller.startScroll(0, mScroller.getCurrY()
                , 0, -header_contain.getMeasuredHeight(), 1000);

        pbFooter.setVisibility(GONE);
        footer_contain.setVisibility(VISIBLE);
        tvFooter.setText("继续向上");
    }

    class RefreshHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            Log.i(TAG, "handleMessage: " + msg.what);
            if (msg.what == REFRESHOVER_HANDLER) {
                headerRecover();
            } else if (msg.what == LOADOVER_HANDLER) {
                footerRecover();
            }
        }
    }

}
