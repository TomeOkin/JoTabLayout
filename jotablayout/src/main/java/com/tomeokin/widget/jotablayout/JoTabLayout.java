/*
 * Copyright 2016 TomeOkin
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.tomeokin.widget.jotablayout;

import android.animation.TypeEvaluator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.util.SparseArray;
import android.util.TypedValue;
import android.view.View;
import android.view.animation.OvershootInterpolator;
import android.widget.LinearLayout;
import com.tomeokin.widget.jotablayout.listener.OnTabSelectedListener;

public class JoTabLayout extends LinearLayout implements ValueAnimator.AnimatorUpdateListener {
  // Gravity
  private static final int GRAVITY_TOP = 1;
  private static final int GRAVITY_BOTTOM = 3;

  // Indicator Shape
  private static final int SHAPE_NONE = 0;
  private static final int SHAPE_LINE = 1;
  private static final int SHAPE_TRIANGLE = 2;
  private static final int SHAPE_SQUARE = 3;

  private int mTabCount;
  private int mCurrentTab = 0;
  private int mLastTab = -1;

  // underline
  private int mUnderlineColor = Color.parseColor("#cfd8dc");
  private float mUnderlineHeight = dp2px(0.5f);
  private int mUnderlineGravity = GRAVITY_TOP;
  private Paint mUnderlinePaint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG);

  // divider
  private int mDividerColor = Color.parseColor("#bdbdbd");
  private float mDividerWidth = 0;
  private int mDividerVerticalPadding = (int) dp2px(8);
  private Paint mDividerPaint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG);

  // indicator
  private int mIndicatorColor;
  private float mIndicatorWidth = -1; // equal with tab width
  private float mIndicatorHeight = -1;
  private float mIndicatorCornerRadius = 0;
  private int mIndicatorGravity = GRAVITY_TOP;
  private int mIndicatorShape = SHAPE_NONE;
  private int mIndicatorAnimOffset = 0; // indicator horizontal offset when animation

  private int mIndicatorAnimDuration = 0;
  private boolean mIndicatorAnimEnabled = true;
  private boolean mIndicatorBounceEnabled = true;
  private ValueAnimator mValueAnimator;
  private OvershootInterpolator mInterpolator = new OvershootInterpolator(1.5f);
  private IndicatorPoint mCurrentP = new IndicatorPoint();
  private IndicatorPoint mLastP = new IndicatorPoint();

  private Rect mIndicatorRect = new Rect();
  private Paint mTrianglePaint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG);
  private Path mTrianglePath = new Path();
  private GradientDrawable mIndicatorDrawable = new GradientDrawable();

  public JoTabLayout(Context context) {
    this(context, null);
  }

  public JoTabLayout(Context context, AttributeSet attrs) {
    this(context, attrs, 0);
  }

  public JoTabLayout(Context context, AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);

    setWillNotDraw(false); // 没有重载 onMeasure 时，需要手动设置，否则 onDraw 不会被调用
    setClipChildren(false);
    setClipToPadding(false);

    // obtain user defined attr values
    TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.JoTabLayout, defStyleAttr, 0);

    // underline
    mUnderlineColor = a.getColor(R.styleable.JoTabLayout_underlineColor, mUnderlineColor);
    mUnderlineHeight = a.getDimension(R.styleable.JoTabLayout_underlineHeight, mUnderlineHeight);
    mUnderlineGravity = a.getInt(R.styleable.JoTabLayout_underlineGravity, mUnderlineGravity);

    // divider
    mDividerColor = a.getColor(R.styleable.JoTabLayout_dividerColor, mDividerColor);
    mDividerWidth = a.getDimension(R.styleable.JoTabLayout_dividerWidth, mDividerWidth);
    mDividerVerticalPadding =
        a.getDimensionPixelSize(R.styleable.JoTabLayout_dividerVerticalPadding,
            mDividerVerticalPadding);

    // indicator
    mIndicatorShape = a.getInt(R.styleable.JoTabLayout_indicatorShape, mIndicatorShape);

    // init default indicator attrs again according to mIndicatorShape
    if (mIndicatorShape == SHAPE_TRIANGLE) {
      mIndicatorWidth = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 10,
          getResources().getDisplayMetrics());
      mIndicatorHeight = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 3,
          getResources().getDisplayMetrics());
    } else {
      mIndicatorWidth = 0;
      mIndicatorHeight = 0;
    }
    mIndicatorColor = (mIndicatorShape == SHAPE_SQUARE) ? Color.parseColor("#e0e0e0")
        : Color.parseColor("#0277bd");

    mIndicatorColor = a.getColor(R.styleable.JoTabLayout_indicatorColor, mIndicatorColor);
    mIndicatorWidth = a.getDimension(R.styleable.JoTabLayout_indicatorWidth, mIndicatorWidth);
    mIndicatorHeight = a.getDimension(R.styleable.JoTabLayout_indicatorHeight, mIndicatorHeight);
    mIndicatorCornerRadius =
        a.getDimension(R.styleable.JoTabLayout_indicatorCornerRadius, mIndicatorCornerRadius);
    mIndicatorGravity = a.getInt(R.styleable.JoTabLayout_indicatorGravity, mIndicatorGravity);

    mIndicatorAnimDuration =
        a.getInt(R.styleable.JoTabLayout_indicatorAnimDuration, mIndicatorAnimDuration);
    mIndicatorAnimEnabled =
        a.getBoolean(R.styleable.JoTabLayout_indicatorAnimEnabled, mIndicatorAnimEnabled);
    mIndicatorBounceEnabled =
        a.getBoolean(R.styleable.JoTabLayout_indicatorBounceEnabled, mIndicatorBounceEnabled);

    a.recycle();

    mValueAnimator = ValueAnimator.ofObject(new PointEvaluator(), mLastP, mCurrentP);
    mValueAnimator.addUpdateListener(this);
  }

  /**
   * 初始化状态部分
   */
  @Override
  protected void onFinishInflate() {
    super.onFinishInflate();
    mTabCount = getChildCount();

    initTabsWithListener();
  }

  private void initTabsWithListener() {
    View tabView;
    for (int i = 0; i < mTabCount; i++) {
      tabView = getChildAt(i);
      tabView.setTag(i);
      tabView.setOnClickListener(null);
    }

    setCurrentTab(0);
  }

  public void applyConfigurationWithViewPager(@NonNull final ViewPager viewPager,
      final boolean alphaTransformEnabled) {
    viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
      @Override
      public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
        if (mIndicatorShape != SHAPE_NONE && mIndicatorAnimEnabled) {
          scrollIndicatorTo(position, positionOffset);
        }

        if (alphaTransformEnabled) {
          scrollTabTo(position, positionOffset);
        }
      }

      @Override
      public void onPageSelected(int position) {
        setCurrentTab(position);
      }

      @Override
      public void onPageScrollStateChanged(int state) {
        if (mIndicatorShape != SHAPE_NONE && mIndicatorAnimEnabled && mIndicatorBounceEnabled) {
          updateState();
        }
      }
    });

    // 如果没有设置监听器，则设置一个默认的监听器
    if (mListener == null) {
      mListener = new OnTabSelectedListener() {
        @Override
        public void onTabSelect(View view, int position) {
          viewPager.setCurrentItem(position, false);
        }

        @Override
        public void onTabReselect(View view, int position) {

        }
      };
      addOnTabSelectedListener();
    }
  }

  /**
   * 添加监听器部分
   */
  private OnTabSelectedListener mListener;

  public void setOnTabSelectedListener(OnTabSelectedListener listener) {
    mListener = listener;
    addOnTabSelectedListener();
  }

  private boolean mTabClickTrigger = true;

  private void addOnTabSelectedListener() {
    View tabView;
    for (int i = 0; i < mTabCount; i++) {
      tabView = getChildAt(i);
      tabView.setOnClickListener(new OnClickListener() {
        @Override
        public void onClick(View v) {
          int position = (int) v.getTag();

          if (mCurrentTab != position) {
            mTabClickTrigger = true;
            setCurrentTab(position);
            if (mListener != null) {
              mListener.onTabSelect(v, position);
            }
          } else {
            if (mListener != null) {
              mListener.onTabReselect(v, position);
            }
          }
        }
      });
    }
  }

  public int getCurrentTab() {
    return mCurrentTab;
  }

  /**
   * 更新选中状态部分
   */
  public void setCurrentTab(int index) {
    if (index >= mTabCount) {
      throw new IllegalArgumentException("index must be smaller than tabCount");
    }

    if (mLastTab != -1 && index == mCurrentTab) {
      return;
    }

    resetState();
    mLastTab = mCurrentTab;
    mCurrentTab = index;
    View currentTabView = getChildAt(mCurrentTab);
    currentTabView.setAlpha(1);

    // change indicator
    if (mIndicatorShape != SHAPE_NONE && mIndicatorAnimEnabled) {
      calIndicatorOffset();
    } else {
      invalidate();
    }
  }

  private void resetState() {
    View view;
    for (int i = 0; i < mTabCount; i++) {
      view = getChildAt(mCurrentTab);
      view.setAlpha(0);
    }
  }

  private void calIndicatorOffset() {
    final View currentTabView = getChildAt(mCurrentTab);
    mCurrentP.left = currentTabView.getLeft();
    mCurrentP.right = currentTabView.getRight();

    final View lastTabView = getChildAt(mLastTab);
    mLastP.left = lastTabView.getLeft();
    mLastP.right = lastTabView.getRight();

    if (mLastP.left == mCurrentP.left && mLastP.right == mCurrentP.right) {
      invalidate();
    } else {
      mValueAnimator.setObjectValues(mLastP, mCurrentP);

      if (mIndicatorBounceEnabled) {
        mValueAnimator.setInterpolator(mInterpolator);
      }

      if (mIndicatorAnimDuration <= 0) {
        mIndicatorAnimDuration = mIndicatorBounceEnabled ? 500 : 250;
      }

      mValueAnimator.setDuration(mIndicatorAnimDuration);
      mValueAnimator.start();
    }
  }

  @Override
  public void onAnimationUpdate(ValueAnimator animation) {
    View currentTabView = getChildAt(mCurrentTab);
    IndicatorPoint p = (IndicatorPoint) animation.getAnimatedValue();
    mIndicatorAnimOffset = (int) p.left - currentTabView.getLeft();
    invalidate();
  }

  /**
   * 当需要 tab alpha transform 时在 onPageScrolled 使用，参数命名相对应
   */
  public void scrollTabTo(int position, float positionOffset) {
    if (position + 1 > mTabCount) {
      throw new IllegalArgumentException("position must be smaller than tabCount");
    }

    // onScroll: position = min(source, dest), positionOffset = [0, 1]
    // from 0 to 1: position = 0
    // from 1 to 0: position = 0
    View view;
    if (positionOffset > 0) {
      view = getChildAt(position);
      view.setAlpha(1 - positionOffset);
      view = getChildAt(position + 1);
      view.setAlpha(positionOffset);
    }
  }

  /**
   * 如果需要指示器滑动效果，在 onPageScrolled 中调用，参数命名相对应
   */
  public void scrollIndicatorTo(int position, float positionOffset) {
    View tabView = getChildAt(position);
    View currentView = getChildAt(mCurrentTab);

    // 修正默认的增量
    mIndicatorAnimOffset = tabView.getLeft()
        + (int) ((tabView.getWidth() - mIndicatorWidth) / 2)
        + (int) (tabView.getWidth() * positionOffset)
        - currentView.getLeft()
        - (int) ((currentView.getWidth() - mIndicatorWidth) / 2);

    if (!mTabClickTrigger) {
      mValueAnimator.setInterpolator(null);
      mValueAnimator.setDuration(250);
    }

    invalidate();
  }

  /**
   * 启用抖动效果时，需要在 onPageScrollStateChanged 中调用，否则滑动页面时也会有抖动效果
   * onPageScrollStateChanged 仅在页面发生滑动时才会被调用。
   */
  public void updateState() {
    mTabClickTrigger = false;
  }

  public int getTabCount() {
    return getChildCount();
  }

  @Override
  protected void onDraw(Canvas canvas) {
    super.onDraw(canvas);

    // draw underline
    if (mUnderlineHeight > 0) {
      mUnderlinePaint.setColor(mUnderlineColor);

      // 如果值不为 GRAVITY_BOTTOM，则默认在顶部绘制
      // getMeasuredHeight 包括 view 超出部分的高度
      // getHeight 不包括超出部分的高度，由于默认 android:clipToPadding="true"，故使用 getHeight()

      if (mUnderlineGravity == GRAVITY_BOTTOM) {
        canvas.drawRect(getPaddingLeft(), getHeight() - mUnderlineHeight,
            getWidth() + getPaddingLeft(), getHeight(), mUnderlinePaint);
      } else {
        canvas.drawRect(getPaddingLeft(), 0, getWidth() + getPaddingLeft(), mUnderlineHeight,
            mUnderlinePaint);
      }
    }

    // draw divider
    if (mDividerWidth > 0) {
      mDividerPaint.setStrokeWidth(mDividerWidth);
      mDividerPaint.setColor(mDividerColor);
      View tab;
      for (int i = 0; i < mTabCount - 1; i++) {
        tab = getChildAt(i);
        canvas.drawLine(getPaddingLeft() + tab.getRight(), mDividerVerticalPadding,
            getPaddingLeft() + tab.getRight(), getHeight() - mDividerVerticalPadding,
            mDividerPaint);
      }
    }

    calIndicatorRect();
    mIndicatorRect.left += mIndicatorAnimOffset;
    mIndicatorRect.right = mIndicatorRect.left + (int) mIndicatorWidth;

    if (mIndicatorShape != SHAPE_NONE) {
      if (mIndicatorShape == SHAPE_TRIANGLE) {
        mTrianglePaint.setColor(mIndicatorColor);
        mTrianglePath.reset();
        mTrianglePath.moveTo(mIndicatorRect.left, getHeight());
        mTrianglePath.lineTo(mIndicatorRect.left / 2 + mIndicatorRect.right / 2,
            getHeight() - mIndicatorHeight);
        mTrianglePath.lineTo(mIndicatorRect.right, getHeight());
        mTrianglePath.close();
        canvas.drawPath(mTrianglePath, mTrianglePaint);
      } else if (mIndicatorShape == SHAPE_LINE) {
        mIndicatorDrawable.setColor(mIndicatorColor);
        if (mIndicatorGravity == GRAVITY_BOTTOM) {
          mIndicatorDrawable.setBounds(mIndicatorRect.left, getHeight() - (int) mIndicatorHeight,
              mIndicatorRect.right, getHeight());
        } else {
          mIndicatorDrawable.setBounds(mIndicatorRect.left, 0, mIndicatorRect.right,
              (int) mIndicatorHeight);
        }
        mIndicatorDrawable.setCornerRadius(mIndicatorCornerRadius);
        mIndicatorDrawable.draw(canvas);
      } else if (mIndicatorShape == SHAPE_SQUARE) {
        if (mIndicatorCornerRadius <= 0 || mIndicatorCornerRadius > mIndicatorHeight / 2) {
          mIndicatorCornerRadius = mIndicatorHeight / 2;
        }
        mIndicatorRect.top = (int) ((getHeight() - mIndicatorHeight) / 2);
        mIndicatorRect.bottom = mIndicatorRect.top + (int) mIndicatorHeight;

        mIndicatorDrawable.setColor(mIndicatorColor);
        mIndicatorDrawable.setBounds(mIndicatorRect.left, mIndicatorRect.top, mIndicatorRect.right,
            mIndicatorRect.bottom);
        mIndicatorDrawable.setCornerRadius(mIndicatorCornerRadius);
        mIndicatorDrawable.draw(canvas);
      }
    }
  }

  private void calIndicatorRect() {
    View view = getChildAt(mCurrentTab);
    if (mIndicatorWidth <= 0 || mIndicatorHeight <= 0) {
      if (mIndicatorShape == SHAPE_SQUARE) {
        mIndicatorWidth =
            view.getWidth() - TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 8,
                getResources().getDisplayMetrics());
        mIndicatorHeight =
            view.getHeight() - TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 8,
                getResources().getDisplayMetrics());
      } else if (mIndicatorShape == SHAPE_LINE) {
        mIndicatorWidth = view.getWidth();
        mIndicatorHeight = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 1.5f,
            getResources().getDisplayMetrics());
      }
    }

    mIndicatorRect.left = view.getLeft() + (int) ((view.getWidth() - mIndicatorWidth) / 2);
    mIndicatorRect.right = mIndicatorRect.left + (int) mIndicatorWidth;
  }

  @Override
  protected Parcelable onSaveInstanceState() {
    Bundle bundle = new Bundle();
    bundle.putParcelable("parentInstanceState", super.onSaveInstanceState());
    bundle.putInt("mCurrentTab", mCurrentTab);
    SparseArray<Parcelable> childInstanceState = new SparseArray<>();
    for (int i = 0; i < getChildCount(); i++) {
      getChildAt(i).saveHierarchyState(childInstanceState);
    }
    bundle.putSparseParcelableArray("childInstanceState", childInstanceState);
    return bundle;
  }

  @Override
  protected void onRestoreInstanceState(Parcelable state) {
    if (state instanceof Bundle) {
      Bundle bundle = (Bundle) state;
      mCurrentTab = bundle.getInt("mCurrentTab");
      state = bundle.getParcelable("parentInstanceState");
      super.onRestoreInstanceState(state);
      SparseArray<Parcelable> childInstanceStates =
          bundle.getSparseParcelableArray("childInstanceState");
      for (int i = 0; i < getChildCount(); i++) {
        getChildAt(i).restoreHierarchyState(childInstanceStates);
      }
    }
  }

  @Override
  protected void dispatchSaveInstanceState(SparseArray<Parcelable> container) {
    dispatchFreezeSelfOnly(container);
  }

  @Override
  protected void dispatchRestoreInstanceState(SparseArray<Parcelable> container) {
    dispatchThawSelfOnly(container);
  }

  private float dp2px(float dp) {
    return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp,
        getResources().getDisplayMetrics());
  }

  class IndicatorPoint {
    public float left;
    public float right;
  }

  class PointEvaluator implements TypeEvaluator<IndicatorPoint> {
    @Override
    public IndicatorPoint evaluate(float fraction, IndicatorPoint startValue,
        IndicatorPoint endValue) {
      float left = startValue.left + fraction * (endValue.left - startValue.left);
      float right = startValue.right + fraction * (endValue.right - startValue.right);
      IndicatorPoint point = new IndicatorPoint();
      point.left = left;
      point.right = right;
      return point;
    }
  }
}
