package com.dkanada.gramophone.views;

import android.content.Context;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;

import androidx.annotation.NonNull;
import androidx.core.widget.NestedScrollView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class InceptionScrollView extends NestedScrollView {
    private RecyclerView recyclerView;

    public InceptionScrollView(@NonNull Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public void onNestedPreScroll(@NonNull View target, int dx, int dy, @NonNull int[] consumed, int type) {
        // if scrolling down and scroll view not at bottom and recycler view fills screen but scroll view doesn't fill the screen
        if (dy > 0 && !isSvScrolledToBottom(this) && !isRvFillParent(this, recyclerView) && isSvFillParent(this)) {
            scrollBy(0, dy);
            consumed[1] = dy;
            return;
        }

        // send to normal scroll view method
        super.onNestedPreScroll(target, dx, dy, consumed, type);
    }

    public void setRecyclerView(RecyclerView recyclerView) {
        this.recyclerView = recyclerView;

        InceptionScrollView scrollView = this;
        this.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                Rect scrollBounds = new Rect();
                getDrawingRect(scrollBounds);

                ViewGroup.LayoutParams params = recyclerView.getLayoutParams();
                params.height = scrollBounds.bottom;

                recyclerView.setLayoutParams(params);
                scrollView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
            }
        });
    }

    private static boolean isSvScrolledToBottom(NestedScrollView sv) {
        return !sv.canScrollVertically(1);
    }

    // might be useful in the future
    private static boolean isRvScrolledToTop(RecyclerView rv) {
        final LinearLayoutManager lm = (LinearLayoutManager) rv.getLayoutManager();
        return lm != null && lm.findFirstCompletelyVisibleItemPosition() == 0;
    }

    private static boolean isRvFillParent(NestedScrollView sv, RecyclerView rv) {
        Rect scrollLocationInParent = new Rect();
        sv.getHitRect(scrollLocationInParent);

        Rect viewLocationVisible = new Rect();
        rv.getDrawingRect(viewLocationVisible);

        return viewLocationVisible == scrollLocationInParent;
    }

    // this would probably need changes to do exactly what it says in the name
    private static boolean isSvFillParent(NestedScrollView sv) {
        Rect scrollBounds = new Rect();
        sv.getHitRect(scrollBounds);

        Rect viewBounds = new Rect();
        sv.getDrawingRect(viewBounds);

        return viewBounds.top >= scrollBounds.top;
    }
}
