package com.applidium.headerlistview;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.widget.AbsListView;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.ListView;
import android.widget.RelativeLayout;

public class HeaderListView extends RelativeLayout {

    // TODO: Handle the case where a section has no header
    // TODO: Handle the case where the ListView has a header view
    // TODO: Handle listViews with fast scroll
    // TODO: Pass ListView XML attributes to the mListView
    // TODO: See if there are methods to dispatch to mListView

    private static final int FADE_DELAY    = 1000;
    private static final int FADE_DURATION = 2000;

    private InternalListView mListView;
    private SectionAdapter   mAdapter;
    private RelativeLayout   mHeader;
    private FrameLayout      mScrollView;

    public HeaderListView(Context context) {
        super(context);
        init(context);
    }

    public HeaderListView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    private void init(Context context) {
        mListView = new InternalListView(getContext());
        LayoutParams listParams = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
        listParams.addRule(ALIGN_PARENT_TOP);
        mListView.setLayoutParams(listParams);
        mListView.setOnScrollListener(new HeaderListViewOnScrollListener());
        mListView.setVerticalScrollBarEnabled(false);
        addView(mListView);

        mHeader = new RelativeLayout(getContext());
        LayoutParams headerParams = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
        headerParams.addRule(ALIGN_PARENT_TOP);
        mHeader.setLayoutParams(headerParams);
        mHeader.setGravity(Gravity.BOTTOM);
        addView(mHeader);

        // The list view's scroll bar can be hidden by the header, so we display our own scroll bar instead
        Drawable scrollBarDrawable = getResources().getDrawable(R.drawable.scrollbar_handle_holo_light);
        mScrollView = new FrameLayout(getContext());
        LayoutParams scrollParams = new LayoutParams(scrollBarDrawable.getIntrinsicWidth(), LayoutParams.MATCH_PARENT);
        scrollParams.addRule(ALIGN_PARENT_RIGHT);
        scrollParams.rightMargin = (int) dpToPx(2);
        mScrollView.setLayoutParams(scrollParams);

        ImageView scrollIndicator = new ImageView(context);
        scrollIndicator.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
        scrollIndicator.setImageDrawable(scrollBarDrawable);
        scrollIndicator.setScaleType(ScaleType.FIT_XY);
        mScrollView.addView(scrollIndicator);

        addView(mScrollView);
    }

    public void setAdapter(SectionAdapter adapter) {
        mAdapter = adapter;
        mListView.setAdapter(adapter);
    }

    private class HeaderListViewOnScrollListener implements AbsListView.OnScrollListener {

        private int            previousFirstVisibleItem = -1;
        private int            direction                = 0;
        private int            actualSection            = 0;
        private boolean        scrollingStart           = false;
        private boolean        doneMeasuring            = false;
        private int            lastResetSection         = -1;
        private int            nextH;
        private int            prevH;
        private View           previous;
        private View           next;
        private AlphaAnimation fadeOut                  = new AlphaAnimation(1f, 0f);

        @Override
        public void onScrollStateChanged(AbsListView view, int scrollState) {
        }

        @Override
        public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {

            updateScrollBar();

            if (totalItemCount > 0 && firstVisibleItem == 0) {
                addSectionHeader(0);
            }

            int realFirstVisibleItem = getRealFirstVisibleItem(firstVisibleItem, visibleItemCount);
            if (totalItemCount > 0 && previousFirstVisibleItem != realFirstVisibleItem) {
                direction = realFirstVisibleItem - previousFirstVisibleItem;

                actualSection = mAdapter.getSection(realFirstVisibleItem);
                
                boolean prevHasHeader = mAdapter.hasSectionHeaderView(actualSection - 1);
                boolean currIsHeader = mAdapter.isSectionHeader(realFirstVisibleItem);
                boolean currHasHeader = mAdapter.hasSectionHeaderView(actualSection);
                boolean currIsFirst = mAdapter.getRowInSection(realFirstVisibleItem) == 0;

                boolean needScrolling = currIsFirst && !currHasHeader && prevHasHeader && realFirstVisibleItem != firstVisibleItem;
                                
                if (currIsHeader && !prevHasHeader) {
                    resetHeader(direction < 0 ? actualSection - 1 : actualSection);
                    mHeader.requestLayout();
                } else if (currIsHeader || needScrolling) {
                    startScrolling();
                } else if (lastResetSection != actualSection) {
                    resetHeader(actualSection);
                }

                previousFirstVisibleItem = realFirstVisibleItem;
            }

            if (scrollingStart) {
                int scrolled = realFirstVisibleItem >= firstVisibleItem ? mListView.getChildAt(realFirstVisibleItem - firstVisibleItem).getTop() : 0;

                if (!doneMeasuring) {
                    setMeasurements(realFirstVisibleItem, firstVisibleItem);
                }

                int headerH = doneMeasuring ? (prevH - nextH) * direction * Math.abs(scrolled) / (direction < 0 ? nextH : prevH) + (direction > 0 ? nextH : prevH) : 0;

                mHeader.scrollTo(0, -Math.min(0, scrolled - headerH));
                if (doneMeasuring && headerH != mHeader.getLayoutParams().height) {
                    LayoutParams p = (LayoutParams) (direction < 0 ? next.getLayoutParams() : previous.getLayoutParams());
                    p.topMargin = headerH - p.height;
                    mHeader.getLayoutParams().height = headerH;
                    mHeader.requestLayout();
                }
            }
        }

        private void startScrolling() {
            scrollingStart = true;
            doneMeasuring = false;
            lastResetSection = -1;
        }

        private void resetHeader(int section) {
            scrollingStart = false;
            addSectionHeader(section);
            mHeader.requestLayout();
            lastResetSection = section;
        }

        private void setMeasurements(int realFirstVisibleItem, int firstVisibleItem) {

            if (direction > 0) {
                nextH = realFirstVisibleItem >= firstVisibleItem ? mListView.getChildAt(realFirstVisibleItem - firstVisibleItem).getMeasuredHeight() : 0;
            }

            previous = mHeader.getChildAt(0);
            prevH = previous != null ? previous.getMeasuredHeight() : mHeader.getHeight();

            if (direction < 0) {
                if (lastResetSection != actualSection - 1) {
                    addSectionHeader(Math.max(0, actualSection - 1));
                    next = mHeader.getChildAt(0);
                }
                nextH = mHeader.getChildCount() > 0 ? mHeader.getChildAt(0).getMeasuredHeight() : 0;
                mHeader.scrollTo(0, prevH);
            }
            doneMeasuring = previous != null && prevH > 0 && nextH > 0;
        }

        private void updateScrollBar() {
            if (mHeader != null && mListView != null && mScrollView != null) {
                int offset = mListView.computeVerticalScrollOffset();
                int range = mListView.computeVerticalScrollRange();
                int extent = mListView.computeVerticalScrollExtent();
                int top = mListView.getHeight() * offset / range;
                int bottom = mListView.getHeight() - mListView.getHeight() * (offset + extent) / range;
                mScrollView.setPadding(0, top, 0, bottom);
                fadeOut.reset();
                fadeOut.setFillBefore(true);
                fadeOut.setFillAfter(true);
                fadeOut.setStartOffset(FADE_DELAY);
                fadeOut.setDuration(FADE_DURATION);
                mScrollView.clearAnimation();
                mScrollView.startAnimation(fadeOut);
            }
        }

        private void addSectionHeader(int actualSection) {
            View previousHeader = mHeader.getChildAt(0);
            if (previousHeader != null) {
                mHeader.removeViewAt(0);
            }

            if (mAdapter.hasSectionHeaderView(actualSection)) {
                View header = mAdapter.getSectionHeaderView(actualSection, null, null);
                header.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));

                header.measure(MeasureSpec.makeMeasureSpec(mHeader.getWidth(), MeasureSpec.EXACTLY), MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED));

                mHeader.getLayoutParams().height = header.getMeasuredHeight();
                header.scrollTo(0, 0);
                mHeader.scrollTo(0, 0);
                mHeader.addView(header, 0);
            } else {
                mHeader.getLayoutParams().height = 0;
                mHeader.scrollTo(0, 0);
            }

            mScrollView.bringToFront();
        }

        private int getRealFirstVisibleItem(int firstVisibleItem, int visibleItemCount) {
            if (visibleItemCount == 0) {
                return -1;
            }
            int relativeIndex = 0, totalHeight = mListView.getChildAt(0).getTop();
            for (relativeIndex = 0; relativeIndex < visibleItemCount && totalHeight < mHeader.getHeight(); relativeIndex++) {
                totalHeight += mListView.getChildAt(relativeIndex).getHeight();
            }
            int realFVI = Math.max(firstVisibleItem, firstVisibleItem + relativeIndex - 1);
            return realFVI;
        }
    }

    public ListView getListView() {
        return (ListView) mListView;
    }

    private float dpToPx(float dp) {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, getContext().getResources().getDisplayMetrics());
    }

    protected class InternalListView extends ListView {

        public InternalListView(Context context) {
            super(context);
        }

        @Override
        protected int computeVerticalScrollExtent() {
            return super.computeVerticalScrollExtent();
        }

        @Override
        protected int computeVerticalScrollOffset() {
            return super.computeVerticalScrollOffset();
        }

        @Override
        protected int computeVerticalScrollRange() {
            return super.computeVerticalScrollRange();
        }
    }
}
