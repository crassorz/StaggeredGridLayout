package cc.gu.android.view.staggeredgridlayout;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewDebug;
import android.widget.FrameLayout;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by cc on 2016/11/17.
 */

public class StaggeredGridLayout extends FrameLayout {
    public static final int HORIZONTAL = 0;
    public static final int VERTICAL = 1;

    public StaggeredGridLayout(Context context) {
        super(context);
        init(null, 0, R.style.StaggeredGridLayout);
    }

    public StaggeredGridLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs, 0, R.style.StaggeredGridLayout);
    }

    public StaggeredGridLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(attrs, defStyleAttr, R.style.StaggeredGridLayout);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public StaggeredGridLayout(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(attrs, defStyleAttr, defStyleRes);
    }

    private void init(AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        final TypedArray a = getContext().obtainStyledAttributes(
                attrs, R.styleable.StaggeredGridLayout, defStyleAttr, defStyleRes);
        int index;
        index = a.getInt(R.styleable.StaggeredGridLayout_android_orientation, -1);
        if (index >= 0) {
            setOrientation(index);
        }
        setFullable(a.getBoolean(R.styleable.StaggeredGridLayout_fullable, true));
        setStaggered(a.getDimensionPixelSize(R.styleable.StaggeredGridLayout_unit_size, 1), a.getInt(R.styleable.StaggeredGridLayout_count, 0));
        index = a.getInt(R.styleable.StaggeredGridLayout_android_gravity, -1);
        if (index >= 0) {
            setGravity(index);
        }
    }

    @ViewDebug.ExportedProperty(category = "measurement")
    private int staggeredSize = 1, staggeredCount = 0;
    @ViewDebug.ExportedProperty(category = "measurement")
    private boolean fullable = true;
    @ViewDebug.ExportedProperty(category = "measurement")
    private int mOrientation = VERTICAL;
    @ViewDebug.ExportedProperty(category = "measurement")
    private int gravity = 0;

    public void setFullable(boolean fullable) {
        if (this.fullable != fullable) {
            this.fullable = fullable;
            requestLayout();
        }
    }

    public boolean isFullable() {
        return fullable;
    }

    public void setStaggered(int staggeredSize, int staggeredCount) {
        staggeredSize = Math.max(1, staggeredSize);
        staggeredCount = Math.max(0, staggeredCount);
        if (this.staggeredSize != staggeredSize || this.staggeredCount != staggeredCount) {
            this.staggeredSize = staggeredSize;
            this.staggeredCount = staggeredCount;
            requestLayout();
        }
    }

    public void setStaggeredSize(int staggeredSize) {
        setStaggered(staggeredSize, staggeredCount);
    }

    public int getStaggeredSize() {
        return staggeredSize;
    }

    public void setStaggeredCount(int staggeredCount) {
        setStaggered(staggeredSize, staggeredCount);
    }

    public int getStaggeredCount() {
        return staggeredCount;
    }


    /**
     * Should the layout be a column or a row.
     *
     * @param orientation Pass {@link #HORIZONTAL} or {@link #VERTICAL}. Default
     *                    value is {@link #HORIZONTAL}.
     * @attr ref android.R.styleable#LinearLayout_orientation
     */
    public void setOrientation(int orientation) {
        if (mOrientation != orientation) {
            mOrientation = orientation;
            requestLayout();
        }
    }

    public int getOrientation() {
        return mOrientation;
    }

    public void setGravity(int gravity) {
        if (this.gravity != gravity) {
            this.gravity = gravity;
            requestLayout();
        }
    }

    public int getGravity() {
        return gravity;
    }

    @Override
    protected LayoutParams generateDefaultLayoutParams() {
        return new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
    }

    private static Rect[] getLayouts(List<View> mMatchParentChildren, Rect output, boolean isVertical, boolean fullable, int groupSize, int groupCount) {
        final boolean v = isVertical;
        Comparator<Rect> spaceSort = new Comparator<Rect>() {
            @Override
            public int compare(Rect lhs, Rect rhs) {
                int i;
                if (v) {
                    i = lhs.top - rhs.top;
                } else {
                    i = lhs.left - rhs.left;
                }
                if (i != 0) {
                    return i;
                }
                if (v) {
                    i = lhs.left - rhs.left;
                } else {
                    i = lhs.top - rhs.top;
                }
                return i;
            }
        };
        LinkedList<Rect> spaces = new LinkedList<>();
        int root = output.width();
        if (v) {
            spaces.add(new Rect(0, 0, root, Integer.MAX_VALUE));
        } else {
            spaces.add(new Rect(0, 0, Integer.MAX_VALUE, root));
        }
        output.set(0, 0, 0, 0);
        int[] mix = new int[root];
        Rect[] layouts = new Rect[mMatchParentChildren.size()];
        for (int i = 0; i < layouts.length; i++) {
            final View child = mMatchParentChildren.get(i);
            final LayoutParams lp = (LayoutParams) child.getLayoutParams();
            Rect layout = new Rect();
            int w = child.getMeasuredWidth() + lp.leftMargin + lp.rightMargin;
            w = Math.min(root, w);
            int h = child.getMeasuredHeight() + lp.topMargin + lp.bottomMargin;
            checkSpase:
            {
                for (int j = 0; j < spaces.size(); j++) {
                    Rect space = spaces.get(j);
                    if (w <= space.width() && h <= space.height()) {
                        layout.set(space.left, space.top, space.left + w, space.top + h);
                        break checkSpase;
                    }
                }
                if (v) {
                    int top = output.bottom;
                    layout.set(0, top, w, top + h);
                } else {
                    int left = output.right;
                    layout.set(left, 0, left + w, h);
                }
            }
            if (groupCount > 0) {
                double g = root * 1f / groupCount;
                if (v) {
                    layout.right = (int) (Math.ceil(layout.right / g) * g);
                } else {
                    layout.bottom = (int) (Math.ceil(layout.bottom / g) * g);
                }
            }
            if (v) {
                layout.bottom = (int) Math.ceil(layout.bottom * 1f / groupSize) * groupSize;
            } else {
                layout.right = (int) Math.ceil(layout.right * 1f / groupSize) * groupSize;
            }
            output.bottom = Math.max(output.bottom, layout.bottom);
            output.right = Math.max(output.right, layout.right);
            Log.w("AutoGrid", String.format("layout %s (%s)", layout, output));

            layouts[i] = layout;
            if (v) {
                for (int j = layout.left; j < layout.right && j < mix.length; j++) {
                    mix[j] = Math.max(mix[j], layout.bottom);
                }
            } else {
                for (int j = layout.top; j < layout.bottom && j < mix.length; j++) {
                    mix[j] = Math.max(mix[j], layout.right);
                }
            }
            for (int j = 0; j < spaces.size(); j++) {
                Rect space = spaces.get(j);
                if (space.width() <= 0 || space.height() <= 0) {
                    Log.e("AutoGrid", String.format("remove %s", space));
                    spaces.remove(j);
                    j--;
                } else if (Rect.intersects(layout, space)) {
                    spaces.remove(j);
                    Log.e("AutoGrid", String.format("remove %s", space));
                    if (layout.contains(space)) {
                        j--;
                        continue;
                    }
                    int k = j;
                    j--;
                    Rect rect;
                    if (fullable) {
                        if (layout.left > space.left) {
                            rect = new Rect(space.left, space.top, layout.left, space.bottom);
                            spaces.add(k, rect);
                            Log.e("AutoGrid", String.format("add %s", rect));
                        }
                        if (layout.top > space.top) {
                            rect = new Rect(space.left, space.top, space.right, layout.top);
                            spaces.add(k, rect);
                            Log.e("AutoGrid", String.format("add %s", rect));
                        }
                    }
                    if (layout.right < space.right) {
                        rect = new Rect(layout.right, space.top, space.right, space.bottom);
                        if (v) {
                            spaces.add(k, rect);
                        } else {
                            spaces.add(rect);
                        }
                        Log.e("AutoGrid", String.format("add %s", rect));
                    }
                    if (layout.bottom < space.bottom) {
                        rect = new Rect(space.left, layout.bottom, space.right, space.bottom);
                        if (v) {
                            spaces.add(rect);
                        } else {
                            spaces.add(k, rect);
                        }
                        Log.e("AutoGrid", String.format("add %s", rect));
                    }
                } else {
                    for (int k = j + 1; k < spaces.size(); k++) {
                        if (space.contains(spaces.get(k))) {
                            spaces.remove(k);
                            k--;
                        }
                    }
                }
            }
            Collections.sort(spaces, spaceSort);
        }
        return layouts;
    }

    private Rect[] layouts = {};
    private List<View> mMatchParentChildren = new ArrayList<View>();

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int count = getChildCount();
        mMatchParentChildren.clear();
        int childState = 0;


        for (int i = 0; i < count; i++) {
            final View child = getChildAt(i);
            if (child.getVisibility() != GONE) {
                measureChildWithMargins(child, widthMeasureSpec, 0, heightMeasureSpec, 0);
                final LayoutParams lp = (LayoutParams) child.getLayoutParams();
                childState = combineMeasuredStates(childState, child.getMeasuredState());
                mMatchParentChildren.add(child);
                final int childWidthMeasureSpec;
                if (lp.width == LayoutParams.MATCH_PARENT) {
                    final int width = Math.max(0, MeasureSpec.getSize(widthMeasureSpec)
                            - getPaddingLeft() - getPaddingRight()
                            - lp.leftMargin - lp.rightMargin);
                    childWidthMeasureSpec = MeasureSpec.makeMeasureSpec(
                            width, MeasureSpec.EXACTLY);
                } else {
                    childWidthMeasureSpec = getChildMeasureSpec(widthMeasureSpec,
                            getPaddingLeft() + getPaddingRight() +
                                    lp.leftMargin + lp.rightMargin,
                            lp.width);
                }

                final int childHeightMeasureSpec;
                if (lp.height == LayoutParams.MATCH_PARENT) {
                    final int height = Math.max(0, MeasureSpec.getSize(heightMeasureSpec)
                            - getPaddingTop() - getPaddingBottom()
                            - lp.topMargin - lp.bottomMargin);
                    childHeightMeasureSpec = MeasureSpec.makeMeasureSpec(
                            height, MeasureSpec.EXACTLY);
                } else {
                    childHeightMeasureSpec = getChildMeasureSpec(heightMeasureSpec,
                            getPaddingTop() + getPaddingBottom() +
                                    lp.topMargin + lp.bottomMargin,
                            lp.height);
                }

                child.measure(childWidthMeasureSpec, childHeightMeasureSpec);
            }
        }
        Rect layout = new Rect(0, 0, MeasureSpec.getSize(widthMeasureSpec) - getPaddingLeft() - getPaddingRight(), MeasureSpec.getSize(heightMeasureSpec) - getPaddingTop() - getPaddingBottom());
        Log.w("AutoGrid", String.format("onMeasure %s %s", mMatchParentChildren.size(), layout));
        layouts = getLayouts(mMatchParentChildren, layout, mOrientation == VERTICAL, fullable, staggeredSize, staggeredCount);
        int maxWidth = layout.width();
        int maxHeight = layout.height();
        // Account for padding too
        maxWidth += getPaddingLeft() + getPaddingRight();
        maxHeight += getPaddingTop() + getPaddingBottom();

        // Check against our minimum height and width
        maxHeight = Math.max(maxHeight, getSuggestedMinimumHeight());
        maxWidth = Math.max(maxWidth, getSuggestedMinimumWidth());

        // Check against our foreground's minimum height and width
        final Drawable drawable = getBackground();
        if (drawable != null) {
            maxHeight = Math.max(maxHeight, drawable.getMinimumHeight());
            maxWidth = Math.max(maxWidth, drawable.getMinimumWidth());
        }
        setMeasuredDimension(resolveSizeAndState(maxWidth, widthMeasureSpec, childState),
                resolveSizeAndState(maxHeight, heightMeasureSpec,
                        childState << MEASURED_HEIGHT_STATE_SHIFT));
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        Log.w("AutoGrid", String.format("layoutChildren %s", mMatchParentChildren.size()));

        final int layoutDirection = getGravity();

        for (int i = 0; i < layouts.length; i++) {
            Rect rect = layouts[i];
            final View child = mMatchParentChildren.get(i);
            final LayoutParams lp = (LayoutParams) child.getLayoutParams();
            final int parentLeft = rect.left + getPaddingLeft();
            final int parentTop = rect.top + getPaddingTop();
            final int parentRight = rect.right + getPaddingLeft();
            final int parentBottom = rect.bottom + getPaddingTop();
            final int width = child.getMeasuredWidth();
            final int height = child.getMeasuredHeight();
            int childLeft;
            int childTop;

            int gravity = lp.gravity;
            if (gravity == -1) {
                gravity = this.gravity;
            }
            int absoluteGravity = Gravity.getAbsoluteGravity(gravity, layoutDirection);
            final int verticalGravity = gravity & Gravity.VERTICAL_GRAVITY_MASK;

            switch (absoluteGravity & Gravity.HORIZONTAL_GRAVITY_MASK) {
                case Gravity.CENTER_HORIZONTAL:
                    childLeft = parentLeft + (parentRight - parentLeft - width) / 2 +
                            lp.leftMargin - lp.rightMargin;
                    break;
                case Gravity.RIGHT:
                    childLeft = parentRight - width - lp.rightMargin;
                    break;
                case Gravity.LEFT:
                default:
                    childLeft = parentLeft + lp.leftMargin;
            }

            switch (verticalGravity) {
                case Gravity.TOP:
                    childTop = parentTop + lp.topMargin;
                    break;
                case Gravity.CENTER_VERTICAL:
                    childTop = parentTop + (parentBottom - parentTop - height) / 2 +
                            lp.topMargin - lp.bottomMargin;
                    break;
                case Gravity.BOTTOM:
                    childTop = parentBottom - height - lp.bottomMargin;
                    break;
                default:
                    childTop = parentTop + lp.topMargin;
            }
            child.layout(childLeft, childTop, childLeft + width, childTop + height);
        }
    }

}
