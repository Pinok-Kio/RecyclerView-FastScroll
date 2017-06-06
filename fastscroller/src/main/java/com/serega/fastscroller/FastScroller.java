package com.serega.fastscroller;

import android.annotation.SuppressLint;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.*;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.ColorInt;
import android.support.annotation.NonNull;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Layout;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import java.util.Arrays;

/**
 * @author S.A.Bobrischev
 *         Developed by Magora Team (magora-systems.com). 2017.
 */
@SuppressLint("ViewConstructor")
public final class FastScroller extends View implements View.OnLayoutChangeListener {
    private static final int NO_ALPHA = 255;
    private static final float DENSITY = Resources.getSystem().getDisplayMetrics().density;
    private static final int REQUIRED_WIDTH_PX = dp(132);
    private final RecyclerView recyclerView;
    private final TextPaint letterPaint = new TextPaint(Paint.ANTI_ALIAS_FLAG);
    private final RectF rect = new RectF();
    private final RectF thumbRect = new RectF();
    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final boolean isRtl;
    private final int[] colors = new int[6];
    private final float[] radii = new float[8];

    private final int defaultRadius = dp(44);
    private final int thumbTopBottomOffset = dp(12);
    private final int thumbHeight = dp(30);
    private final int thumbCornerRadii = dp(2);
    private final int thumbWidth = dp(5);
    private final int bubbleSize = dp(88);
    private final int bubbleBottomOffset = dp(46);
    private final int thumbSideGap = dp(10);

    private boolean pressed;
    private StaticLayout letterLayout;
    private StaticLayout oldLetterLayout;
    private String currentLetter;
    private Path path = new Path();
    private long lastUpdateTime;
    private float textX;
    private float textY;
    private float bubbleProgress;
    private float progress;
    private float lastY;

    private int scrollX;

    public static void wrap(@NonNull RecyclerView recyclerView) {
        new FastScroller(recyclerView);
    }

    private FastScroller(@NonNull RecyclerView recyclerView) {
        super(recyclerView.getContext());
        setId(R.id.id_recyclerViewFastScroller);
        this.recyclerView = recyclerView;
        isRtl = recyclerView.getLayoutDirection() == LAYOUT_DIRECTION_RTL;
        scrollX = isRtl ? thumbSideGap : dp(117);

        Arrays.fill(radii, defaultRadius);

        initWithRecyclerView(recyclerView);
        applyStyle();

        ViewGroup parent = (ViewGroup) recyclerView.getParent();
        if (parent != null) {
            ((ViewGroup) recyclerView.getParent()).addView(this);

            if (parent instanceof SwipeRefreshLayout) {
                recyclerView.addOnLayoutChangeListener(this);
            }
        }
    }

    private void initWithRecyclerView(@NonNull final RecyclerView recyclerView) {
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            boolean scrollingByUser;

            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                scrollingByUser = newState == RecyclerView.SCROLL_STATE_DRAGGING || newState == RecyclerView.SCROLL_STATE_SETTLING;
            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                if (scrollingByUser) {
                    float range = recyclerView.computeVerticalScrollRange();
                    float offset = recyclerView.computeVerticalScrollOffset();
                    float extent = recyclerView.computeVerticalScrollExtent();
                    setProgress(offset / (range - extent));
                }
            }
        });
    }

    @SuppressWarnings("ResourceType")
    private void applyStyle() {
        TypedArray array = null;
        try {
            int[] attr = {
                    R.attr.fastScrollTextColor,
                    R.attr.fastScrollActiveColor,
                    R.attr.fastScrollInactiveColor,
                    R.attr.fastScrollTextSize
            };
            array = getContext().obtainStyledAttributes(R.style.FastScrollStyle, attr);
            setLetterTextColor(array.getColor(0, Color.WHITE));
            setColors(array.getColor(1, Color.BLUE), array.getColor(2, 0xff636363));
            letterPaint.setTextSize(array.getDimensionPixelSize(3, dp(45)));
        } finally {
            if (array != null) {
                array.recycle();
            }
        }
    }

    private void setLetterTextColor(@ColorInt int color) {
        letterPaint.setColor(color);
    }

    private void setColors(@ColorInt int colorActive, @ColorInt int colorInactive) {
        paint.setColor(colorInactive);

        colors[0] = Color.red(colorInactive);
        colors[1] = Color.red(colorActive);

        colors[2] = Color.green(colorInactive);
        colors[3] = Color.green(colorActive);

        colors[4] = Color.blue(colorInactive);
        colors[5] = Color.blue(colorActive);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                float x = event.getX();
                lastY = event.getY();

                if (isRtl && x > dp(25)
                        || !isRtl && x < dp(107)
                        || lastY < thumbRect.top
                        || lastY > thumbRect.bottom
                        || lastY <= thumbTopBottomOffset
                        || lastY >= getMeasuredHeight() - thumbTopBottomOffset) {
                    return false;
                }

                pressed = true;
                lastUpdateTime = System.currentTimeMillis();

                stopScroll();

                getCurrentLetter();
                postInvalidateOnAnimation();
                return true;

            case MotionEvent.ACTION_MOVE:
                if (!pressed) {
                    return true;
                }

                float newY = event.getY();
                float minY = thumbTopBottomOffset;
                float maxY = getMeasuredHeight() - thumbTopBottomOffset;
                if (newY < minY) {
                    newY = minY;
                } else if (newY > maxY) {
                    newY = maxY;
                }
                float dy = newY - lastY;
                lastY = newY;
                progress += dy / (getMeasuredHeight() - thumbTopBottomOffset * 2 - thumbHeight);
                if (progress < 0) {
                    progress = 0;
                } else if (progress > 1) {
                    progress = 1;
                }
                getCurrentLetter();
                postInvalidateOnAnimation();
                return true;

            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                pressed = false;
                lastUpdateTime = System.currentTimeMillis();
                postInvalidateOnAnimation();
                return true;
        }
        return super.onTouchEvent(event);
    }

    private void getCurrentLetter() {
        RecyclerView.LayoutManager layoutManager = recyclerView.getLayoutManager();
        if (layoutManager instanceof LinearLayoutManager) {
            LinearLayoutManager linearLayoutManager = (LinearLayoutManager) layoutManager;
            if (linearLayoutManager.getOrientation() == LinearLayoutManager.VERTICAL) {
                RecyclerView.Adapter adapter = recyclerView.getAdapter();
                if (adapter instanceof FastScrollAdapter) {
                    FastScrollAdapter fastScrollAdapter = (FastScrollAdapter) adapter;
                    int position = fastScrollAdapter.getPositionForScrollProgress(progress);
                    linearLayoutManager.scrollToPositionWithOffset(position, 0);
                    String newLetter = fastScrollAdapter.getLetter(position);
                    if (newLetter == null) {
                        if (letterLayout != null) {
                            oldLetterLayout = letterLayout;
                        }
                        letterLayout = null;
                    } else if (!newLetter.equals(currentLetter)) {
                        letterLayout = new StaticLayout(newLetter, letterPaint, 1000, Layout.Alignment.ALIGN_NORMAL, 1.0f, 0.0f, false);
                        oldLetterLayout = null;
                        if (letterLayout.getLineCount() > 0) {
                            if (isRtl) {
                                textX = thumbSideGap + (bubbleSize - (letterLayout.getLineWidth(0) - letterLayout.getLineLeft(0))) / 2;
                            } else {
                                textX = (bubbleSize - (letterLayout.getLineWidth(0) - letterLayout.getLineLeft(0))) / 2;
                            }
                            textY = (bubbleSize - letterLayout.getHeight()) / 2;
                        }
                    }
                    currentLetter = newLetter;
                }
            }
        }
    }

    @Override
    public void onLayoutChange(View v,
                               int left, int top, int right, int bottom,
                               int oldLeft, int oldTop, int oldRight, int oldBottom) {
        setMeasuredDimension(REQUIRED_WIDTH_PX, v.getMeasuredHeight());
        layout(left, top, right, bottom);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        setMeasuredDimension(
                REQUIRED_WIDTH_PX,
                recyclerView.getMeasuredHeight()
        );
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (!shouldDraw()) {
            return;
        }
        updatePaintColors();

        int y = (int) Math.ceil((getMeasuredHeight() - thumbTopBottomOffset * 2 - thumbHeight) * progress);

        drawThumb(canvas, y);

        if ((pressed || bubbleProgress != 0)) {
            drawBubble(canvas, y);
        }

        if ((pressed && letterLayout != null && bubbleProgress < 1.0f) || (!pressed || letterLayout == null) && bubbleProgress > 0.0f) {
            long newTime = System.currentTimeMillis();
            long dt = (newTime - lastUpdateTime);
            if (dt < 0 || dt > 17) {
                dt = 17;
            }
            lastUpdateTime = newTime;
            postInvalidateOnAnimation();
            if (pressed && letterLayout != null) {
                bubbleProgress += dt / 120.0f;
                if (bubbleProgress > 1.0f) {
                    bubbleProgress = 1.0f;
                }
            } else {
                bubbleProgress -= dt / 120.0f;
                if (bubbleProgress < 0.0f) {
                    bubbleProgress = 0.0f;
                }
            }
        }
    }

    private boolean shouldDraw() {
        return recyclerView != null
                && recyclerView.getAdapter() != null
                && recyclerView.getLayoutManager() != null
                && recyclerView.getLayoutManager().getChildCount() != recyclerView.getAdapter().getItemCount();
    }

    private void updatePaintColors() {
        paint.setColor(
                Color.argb(
                        NO_ALPHA,
                        colors[0] + (int) ((colors[1] - colors[0]) * bubbleProgress),
                        colors[2] + (int) ((colors[3] - colors[2]) * bubbleProgress),
                        colors[4] + (int) ((colors[5] - colors[4]) * bubbleProgress)
                )
        );
    }

    private void drawThumb(@NonNull Canvas canvas, int y) {
        thumbRect.set(scrollX, thumbTopBottomOffset + y, scrollX + thumbWidth, thumbTopBottomOffset + thumbHeight + y);
        canvas.drawRoundRect(thumbRect, thumbCornerRadii, thumbCornerRadii, paint);
    }

    private final int maxDiff = dp(29);
    private final int minRadii = dp(4);
    private final int maxRadii = dp(40);

    private void drawBubble(@NonNull Canvas canvas, int y) {
        Paint tmpPaint = paint;
        Path tmpPath = path;
        float[] tmpRadii = radii;
        float tmpBubbleSize = bubbleSize;
        float tmpThumbSideGap = thumbSideGap;
        boolean tmpRtl = isRtl;

        tmpPaint.setAlpha((int) (NO_ALPHA * bubbleProgress));
        int progressY = y + thumbHeight;
        y -= bubbleBottomOffset;

        float diff = 0;
        if (y <= thumbTopBottomOffset) {
            diff = thumbTopBottomOffset - y;
            y = thumbTopBottomOffset;
        }
        float raduisTop;
        float raduisBottom;
        canvas.translate(tmpThumbSideGap, y);
        if (diff <= maxDiff) {
            raduisTop = defaultRadius;
            raduisBottom = minRadii + (diff / maxDiff) * maxRadii;
        } else {
            diff -= maxDiff;
            raduisBottom = defaultRadius;
            raduisTop = minRadii + (1.0f - diff / maxDiff) * maxRadii;
        }
        if (tmpRtl && (tmpRadii[0] != raduisTop || tmpRadii[6] != raduisBottom) || !tmpRtl && (tmpRadii[2] != raduisTop || tmpRadii[4] != raduisBottom)) {
            if (tmpRtl) {
                tmpRadii[0] = tmpRadii[1] = raduisTop;
                tmpRadii[6] = tmpRadii[7] = raduisBottom;
            } else {
                tmpRadii[2] = tmpRadii[3] = raduisTop;
                tmpRadii[4] = tmpRadii[5] = raduisBottom;
            }
            tmpPath.reset();
            if (tmpRtl) {
                rect.set(tmpThumbSideGap, 0, tmpBubbleSize + tmpThumbSideGap, tmpBubbleSize);
            } else {
                rect.set(0, 0, tmpBubbleSize, tmpBubbleSize);
            }
            tmpPath.addRoundRect(rect, tmpRadii, Path.Direction.CW);
            tmpPath.close();
        }
        StaticLayout layoutToDraw = letterLayout != null ? letterLayout : oldLetterLayout;
        if (layoutToDraw != null) {
            canvas.save();
            canvas.scale(bubbleProgress, bubbleProgress, scrollX, progressY - y);
            canvas.drawPath(tmpPath, tmpPaint);
            canvas.translate(textX, textY);
            layoutToDraw.draw(canvas);
            canvas.restore();
        }
    }

    @Override
    public void layout(int l, int t, int r, int b) {
        if (isRtl) {
            super.layout(0, t, getMeasuredWidth(), t + getMeasuredHeight());
        } else {
            super.layout(
                    recyclerView.getMeasuredWidth() - getMeasuredWidth(),
                    t,
                    recyclerView.getMeasuredWidth(),
                    t + getMeasuredHeight()
            );
        }
    }

    private void stopScroll() {
        if (recyclerView != null) {
            try {
                recyclerView.stopScroll();
            } catch (Exception ignored) {
                //Who cares...
            }
        }
    }

    private void setProgress(float value) {
        progress = value;
        postInvalidateOnAnimation();
    }

    @Override
    protected void onDetachedFromWindow() {
        if (recyclerView != null) {
            recyclerView.removeOnLayoutChangeListener(this);
        }
        super.onDetachedFromWindow();
    }

    private static int dp(float value) {
        if (value == 0) {
            return 0;
        }
        return (int) Math.ceil(DENSITY * value);
    }

    @Override
    protected Parcelable onSaveInstanceState() {
        Bundle savedState = new Bundle();
        savedState.putParcelable("ARG_SUPER_STATE", super.onSaveInstanceState());
        savedState.putFloat("ARG_PROGRESS", progress);
        return savedState;
    }

    @Override
    protected void onRestoreInstanceState(Parcelable state) {
        if (state instanceof Bundle) {
            Bundle savedState = (Bundle) state;
            super.onRestoreInstanceState(savedState.getParcelable("ARG_SUPER_STATE"));
            progress = savedState.getFloat("ARG_PROGRESS");
        } else {
            super.onRestoreInstanceState(state);
        }
    }
}
