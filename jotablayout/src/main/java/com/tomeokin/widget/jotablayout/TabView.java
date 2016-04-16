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

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;

public class TabView extends View {
  private static final int GRAVITY_LEFT = 0;
  private static final int GRAVITY_TOP = 1;
  private static final int GRAVITY_RIGHT = 2;
  private static final int GRAVITY_BOTTOM = 3;

  // icon
  private int mIconNormalId; // 默认图标 id
  private int mIconSelectedId; // 选中图标 id
  private Bitmap mIconNormal = null; // 默认图标
  private Bitmap mIconSelected = null; // 选中图标
  private int mIconGravity;

  // title
  private String mTitle;
  private int mTextSize = (int) sp2px(10);
  private int mTextColorNormal = Color.parseColor("#424242");
  private int mTextColorSelected = Color.parseColor("#0288de");

  // common
  private int mInternalPadding = (int) dp2px(4);
  private boolean mAlphaTransformEnabled = false;

  // tools
  private float mAlpha; // 当前的透明度
  private Paint mIconPaint = new Paint(); // 背景的画笔
  private Rect mIconRect = new Rect(); // 图标绘制区域
  private Paint mTextPaint = new Paint(); // 描述文本的画笔
  private Rect mTextBound; // 描述文本矩形测量大小

  public TabView(Context context) {
    this(context, null);
  }

  public TabView(Context context, AttributeSet attrs) {
    this(context, attrs, 0);
  }

  public TabView(Context context, AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);

    // obtain user defined attr values
    TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.TabView, defStyleAttr, 0);

    // icon
    mIconNormalId = a.getResourceId(R.styleable.TabView_tabIconNormal, -1);
    if (mIconNormalId != -1) {
      mIconNormal = BitmapFactory.decodeResource(getResources(), mIconNormalId);
    }
    mIconSelectedId = a.getResourceId(R.styleable.TabView_tabIconSelected, -1);
    if (mIconSelectedId != -1) {
      mIconSelected = BitmapFactory.decodeResource(getResources(), mIconSelectedId);
    }

    mIconGravity = a.getInt(R.styleable.TabView_tabIconGravity, GRAVITY_TOP);

    // title
    mTitle = a.getString(R.styleable.TabView_tabTitle);
    mTextSize = a.getDimensionPixelSize(R.styleable.TabView_tabTextSize, mTextSize);
    mTextColorNormal = a.getColor(R.styleable.TabView_textColorNormal, mTextColorNormal);
    mTextColorSelected = a.getColor(R.styleable.TabView_textColorSelected, mTextColorSelected);

    // common
    mInternalPadding =
        a.getDimensionPixelSize(R.styleable.TabView_internalPadding, mInternalPadding);
    mAlphaTransformEnabled =
        a.getBoolean(R.styleable.TabView_alphaTransformEnabled, mAlphaTransformEnabled);

    a.recycle();

    initText();
  }

  /**
   * 如果有设置文字就获取文字的区域大小
   */
  private void initText() {
    if (mTitle != null) {
      mTextBound = new Rect();
      mTextPaint.setTextSize(mTextSize);
      mTextPaint.setAntiAlias(true); // 抗锯齿
      mIconPaint.setFilterBitmap(true); // 绘制动画时忽略抗锯齿
      mTextPaint.setDither(true); // 使用抖动处理
      mTextPaint.getTextBounds(mTitle, 0, mTitle.length(), mTextBound);
    }
  }

  @Override
  protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
    super.onMeasure(widthMeasureSpec, heightMeasureSpec);

    updateDrawBorder();
  }

  private void updateDrawBorder() {
    initText();

    if (TextUtils.isEmpty(mTitle) && mIconNormal == null) {
      throw new IllegalArgumentException("title 和 icon 至少要设置一个");
    }

    if (!TextUtils.isEmpty(mTitle) && mIconNormal != null) {
      // 获取图标应该绘制的大小
      int availableWidth = getMeasuredWidth() - getPaddingLeft() - getPaddingRight();
      int availableHeight = getMeasuredHeight() - getPaddingTop() - getPaddingBottom();

      if (mIconGravity == GRAVITY_LEFT || mIconGravity == GRAVITY_RIGHT) {
        availableWidth -= (mTextBound.width() + mInternalPadding);
      } else {
        availableHeight -= (mTextBound.height() + mInternalPadding);
      }

      int iconWH = Math.min(availableWidth, availableHeight);

      // 计算绘制区域，实现居中效果
      if (mIconGravity == GRAVITY_LEFT) {
        // 计算图标绘制区域
        mIconRect.left = getPaddingLeft();
        mIconRect.top = getPaddingTop() + (availableHeight - iconWH) / 2;
        mIconRect.right = mIconRect.left + iconWH;
        mIconRect.bottom = mIconRect.top + iconWH;

        // 计算标题绘制区域
        int textLeft = mIconRect.right + mInternalPadding;
        int textTop = getPaddingTop() + (availableHeight - mTextBound.height()) / 2;
        mTextBound.set(textLeft, textTop, textLeft + mTextBound.width(),
            textTop + mTextBound.height());
      } else if (mIconGravity == GRAVITY_TOP) {
        // 计算图标绘制区域
        mIconRect.left = getPaddingLeft() + (availableWidth - iconWH) / 2;
        mIconRect.top = getPaddingTop();
        mIconRect.right = mIconRect.left + iconWH;
        mIconRect.bottom = mIconRect.top + iconWH;

        // 计算标题绘制区域
        int textLeft = getPaddingLeft() + (availableWidth - mTextBound.width()) / 2;
        int textTop = mIconRect.bottom + mInternalPadding;
        mTextBound.set(textLeft, textTop, textLeft + mTextBound.width(),
            textTop + mTextBound.height());
      } else if (mIconGravity == GRAVITY_RIGHT) {
        // 计算标题绘制区域
        int textLeft = getPaddingLeft();
        int textTop = getPaddingTop() + (availableHeight - mTextBound.height()) / 2;
        mTextBound.set(textLeft, textTop, textLeft + mTextBound.width(),
            textTop + mTextBound.height());

        // 计算图标绘制区域
        mIconRect.left = mTextBound.right + mInternalPadding;
        mIconRect.top = getPaddingTop() + (availableHeight - iconWH) / 2;
        mIconRect.right = mIconRect.left + iconWH;
        mIconRect.bottom = mIconRect.top + iconWH;
      } else if (mIconGravity == GRAVITY_BOTTOM) {
        // 计算标题绘制区域
        int textLeft = getPaddingLeft() + (availableWidth - mTextBound.width()) / 2;
        int textTop = getPaddingTop();
        mTextBound.set(textLeft, textTop, textLeft + mTextBound.width(),
            textTop + mTextBound.height());

        // 计算图标绘制区域
        mIconRect.left = getPaddingLeft() + (availableWidth - iconWH) / 2;
        mIconRect.top = mTextBound.bottom + mInternalPadding;
        mIconRect.right = mIconRect.left + iconWH;
        mIconRect.bottom = mIconRect.top + iconWH;
      }
    } else if (!TextUtils.isEmpty(mTitle)) {
      // 计算可用绘制区域
      int availableWidth = getMeasuredWidth() - getPaddingLeft() - getPaddingRight();
      int availableHeight = getMeasuredHeight() - getPaddingTop() - getPaddingBottom();

      // 计算标题绘制区域
      int textLeft = getPaddingLeft() + (availableWidth - mTextBound.width()) / 2;
      int textTop = getPaddingTop() + (availableHeight - mTextBound.height()) / 2;
      mTextBound.set(textLeft, textTop, textLeft + mTextBound.width(),
          textTop + mTextBound.height());
    } else if (mIconNormal == null) {
      // 计算可用绘制区域
      int availableWidth = getMeasuredWidth() - getPaddingLeft() - getPaddingRight();
      int availableHeight = getMeasuredHeight() - getPaddingTop() - getPaddingBottom();

      // 获取图标应该绘制的大小
      int iconWH = Math.min(availableWidth, availableHeight);

      // 计算图标绘制区域
      mIconRect.left = getPaddingLeft() + (availableWidth - iconWH) / 2;
      mIconRect.top = getPaddingTop() + (availableHeight - iconWH) / 2;
      mIconRect.right = mIconRect.left + iconWH;
      mIconRect.bottom = mIconRect.top + iconWH;
    }
  }

  @Override
  protected void onDraw(Canvas canvas) {
    super.onDraw(canvas);

    int alpha = (int) Math.ceil(mAlpha * 255);
    if (!mAlphaTransformEnabled && alpha < 255) {
      alpha = 0;
    }

    // 绘制图标
    if (mIconNormal != null) {
      // 静止状态下减少一次重绘
      if (alpha != 255) {
        mIconPaint.reset();
        mIconPaint.setAntiAlias(true); // 设置抗锯齿
        mIconPaint.setFilterBitmap(true); // 绘制动画时忽略抗锯齿
        mIconPaint.setAlpha(255 - alpha); // setAlpha 应该在 setColor 之后设置
        canvas.drawBitmap(mIconNormal, null, mIconRect, mIconPaint);
      }

      if (mIconSelected != null && alpha != 0) {
        mIconPaint.reset();
        mIconPaint.setAntiAlias(true); // 设置抗锯齿
        mIconPaint.setFilterBitmap(true); // 绘制动画时忽略抗锯齿
        mIconPaint.setAlpha(alpha); // setAlpha 应该在 setColor 之后设置
        canvas.drawBitmap(mIconSelected, null, mIconRect, mIconPaint);
      }
    }

    // 绘制标题
    if (mTitle != null) {
      mTextPaint.setColor(mTextColorNormal);
      mTextPaint.setAlpha(255 - alpha); // setAlpha 应该在 setColor 之后设置

      // 默认情况下，textAlign 为 Paint.Align.LEFT，绘制文本时，x 为左上角 x 坐标，y 为 baseline 值
      // 如果 textAlign 为 Paint.Align.CENTER，绘制文本时，x 为文本的水平中心 x 坐标，y 为 baseline 值
      // see [amulyakhare/TextDrawable](https://github.com/amulyakhare/TextDrawable)
      // TextDrawable.onDraw()
      canvas.drawText(mTitle, mTextBound.left, (mTextBound.bottom + mTextBound.top) / 2
          - (mTextPaint.descent() + mTextPaint.ascent()) / 2, mTextPaint);

      mTextPaint.setColor(mTextColorSelected);
      mTextPaint.setAlpha(alpha); // setAlpha 应该在 setColor 之后设置
      //canvas.drawText(mTitle, mTextBound.left, mTextBound.bottom - mFmi.bottom / 2, mTextPaint);
      canvas.drawText(mTitle, mTextBound.left, (mTextBound.bottom + mTextBound.top) / 2
          - (mTextPaint.descent() + mTextPaint.ascent()) / 2, mTextPaint);
    }
  }

  @Override
  public void setAlpha(float alpha) {
    if (alpha < 0 || alpha > 1) {
      throw new IllegalArgumentException("alpha value must between 0.0f to 1.0f");
    }

    mAlpha = alpha;
    invalidate();
  }

  public boolean isAlphaTransformEnabled() {
    return mAlphaTransformEnabled;
  }

  public void setAlphaTransformEnabled(boolean enabled) {
    mAlphaTransformEnabled = enabled;
    invalidate();
  }

  /**
   * 根据当前所在线程更新界面
   */
  //private void invalidateView() {
  //  if (Looper.getMainLooper() == Looper.myLooper()) {
  //    invalidate();
  //  } else {
  //    if (Build.VERSION.SDK_INT > 16) {
  //      postInvalidateOnAnimation();
  //    } else {
  //      postInvalidate();
  //    }
  //  }
  //}
  public void setTitleAttr(String title, int size, int normalColor, int selectedColor) {
    mTitle = title;
    mTextSize = size;
    mTextColorNormal = normalColor;
    mTextColorSelected = selectedColor;
    updateDrawBorder();
    invalidate();
  }

  public String getTitle() {
    return mTitle;
  }

  public int getTextSize() {
    return mTextSize;
  }

  public int getTextColorNormal() {
    return mTextColorNormal;
  }

  public int getTextColorSelected() {
    return mTextColorSelected;
  }

  public void setIcon(int normal, int selected) {
    mIconNormalId = normal;
    mIconSelectedId = selected;

    if (mIconNormalId != -1) {
      mIconNormal = BitmapFactory.decodeResource(getResources(), mIconNormalId);
    }
    if (mIconSelectedId != -1) {
      mIconSelected = BitmapFactory.decodeResource(getResources(), mIconSelectedId);
    }
  }

  private float dp2px(float dp) {
    return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp,
        getResources().getDisplayMetrics());
  }

  private float sp2px(float sp) {
    return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, sp,
        getResources().getDisplayMetrics());
  }

  @Override
  protected Parcelable onSaveInstanceState() {
    Parcelable superState = super.onSaveInstanceState();
    SavedState ss = new SavedState(superState);
    ss.alpha = mAlpha;
    ss.showAlphaTransform = mAlphaTransformEnabled;
    ss.title = mTitle;
    ss.textSize = mTextSize;
    ss.textNormalColor = mTextColorNormal;
    ss.textSelectedColor = mTextColorSelected;
    ss.normalIconId = mIconNormalId;
    ss.selectedIconId = mIconSelectedId;
    return ss;
  }

  @Override
  protected void onRestoreInstanceState(Parcelable state) {
    SavedState ss = (SavedState) state;
    super.onRestoreInstanceState(ss.getSuperState());
    mAlpha = ss.alpha;
    mAlphaTransformEnabled = ss.showAlphaTransform;
    mTitle = ss.title;
    mTextSize = ss.textSize;
    mTextColorNormal = ss.textNormalColor;
    mTextColorSelected = ss.textSelectedColor;
    mIconNormalId = ss.normalIconId;
    mIconSelectedId = ss.selectedIconId;
  }

  static class SavedState extends BaseSavedState {
    float alpha;
    boolean showAlphaTransform;
    String title;
    int textSize;
    int textNormalColor;
    int textSelectedColor;
    int normalIconId;
    int selectedIconId;

    public SavedState(Parcel source) {
      super(source);
      alpha = source.readFloat();
      showAlphaTransform = source.readByte() == 1;
      title = source.readString();
      textSize = source.readInt();
      textNormalColor = source.readInt();
      textSelectedColor = source.readInt();
      normalIconId = source.readInt();
      selectedIconId = source.readInt();
    }

    public SavedState(Parcelable superState) {
      super(superState);
    }

    @Override
    public void writeToParcel(Parcel out, int flags) {
      super.writeToParcel(out, flags);
      out.writeFloat(alpha);
      out.writeByte((byte) (showAlphaTransform ? 1 : 0));
      out.writeString(title);
      out.writeInt(textSize);
      out.writeInt(textNormalColor);
      out.writeInt(textSelectedColor);
      out.writeInt(normalIconId);
      out.writeInt(selectedIconId);
    }

    public static final Parcelable.Creator<SavedState> CREATOR =
        new Parcelable.Creator<SavedState>() {
          @Override
          public SavedState createFromParcel(Parcel source) {
            return new SavedState(source);
          }

          @Override
          public SavedState[] newArray(int size) {
            return new SavedState[size];
          }
        };
  }
}
