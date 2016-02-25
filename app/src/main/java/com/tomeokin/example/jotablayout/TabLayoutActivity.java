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
package com.tomeokin.example.jotablayout;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.View;
import com.tomeokin.common.activity.BaseActivity;
import com.tomeokin.widget.jotablayout.JoTabLayout;
import com.tomeokin.widget.jotablayout.listener.OnTabSelectedListener;
import java.util.ArrayList;

public class TabLayoutActivity extends BaseActivity {
  private JoTabLayout mTabLayout;
  private ViewPager mViewPager;

  private ArrayList<Fragment> mFragments = new ArrayList<>();

  @Override
  protected void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_tablayout);

    mViewPager = (ViewPager) findViewById(R.id.viewPager);
    mTabLayout = (JoTabLayout) findViewById(R.id.tabLayout);
    final JoTabLayout tabLayout1 = (JoTabLayout) findViewById(R.id.tabLayout1);
    final JoTabLayout tabLayout2 = (JoTabLayout) findViewById(R.id.tabLayout2);
    final JoTabLayout tabLayout3 = (JoTabLayout) findViewById(R.id.tabLayout3);

    for (int i = 0; i < mTabLayout.getTabCount(); i++) {
      mFragments.add(TwoFragment.newInstance("FrameLayout 2 ", i));
    }

    // 需要指出的是，由于此处的写法，每个 JoTabLayout 的点击会通过 ViewPager 对其他 JoTabLayout 产生影响
    // 具体表现为：更改选中的 tab，试试接下去的点击，和 Bounce 的滑动、点击效果有关
    OnTabSelectedListener listener = new OnTabSelectedListener() {
      @Override
      public void onTabSelect(View view, int position) {
        // Snackbar.make(view, "onTabSelect", Snackbar.LENGTH_SHORT).show();
        if (mViewPager != null) {
          mViewPager.setCurrentItem(position, false);
        }
      }

      @Override
      public void onTabReselect(View view, int position) {
        // Snackbar.make(view, "onTabReselect", Snackbar.LENGTH_SHORT).show();
      }
    };

    mTabLayout.setOnTabSelectedListener(new OnTabSelectedListener() {
      @Override
      public void onTabSelect(View view, int position) {
        if (mViewPager != null) {
          mViewPager.setCurrentItem(position, false);
        }
        setCurrentFragment(position);
      }

      @Override
      public void onTabReselect(View view, int position) {

      }
    });
    tabLayout1.setOnTabSelectedListener(listener);
    tabLayout2.setOnTabSelectedListener(listener);
    tabLayout3.setOnTabSelectedListener(listener);

    mTabLayout.applyConfigurationWithViewPager(mViewPager, false);
    tabLayout1.applyConfigurationWithViewPager(mViewPager, false);
    tabLayout2.applyConfigurationWithViewPager(mViewPager, true);
    tabLayout3.applyConfigurationWithViewPager(mViewPager, true);

    //mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
    //  @Override
    //  public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
    //    //mTabLayout.scrollTabTo(position, positionOffset);
    //    //mTabLayout.scrollIndicatorTo(position, positionOffset);
    //
    //    tabLayout1.scrollIndicatorTo(position, positionOffset);
    //    tabLayout2.scrollTabTo(position, positionOffset);
    //    tabLayout3.scrollTabTo(position, positionOffset);
    //    tabLayout3.scrollIndicatorTo(position, positionOffset);
    //  }
    //
    //  @Override
    //  public void onPageSelected(int position) {
    //    // Log.i("take", "onPageSelected");
    //    mTabLayout.setCurrentTab(position);
    //    tabLayout1.setCurrentTab(position);
    //    tabLayout2.setCurrentTab(position);
    //    tabLayout3.setCurrentTab(position);
    //  }
    //
    //  @Override
    //  public void onPageScrollStateChanged(int state) {
    //    //mTabLayout.updateState();
    //
    //    tabLayout1.updateState();
    //    tabLayout3.updateState();
    //  }
    //});
    mViewPager.setAdapter(new MainAdapter(getSupportFragmentManager()));
    mViewPager.setCurrentItem(1);

    // 由于 mViewPager.setCurrentItem(0); 不会导致 onPageSelected 被调用，因此需要手动调用一次
    // 当显示的页面不是 0 时，onPageSelected 会被调用
    mTabLayout.setCurrentTab(1);
    setCurrentFragment(1);
    tabLayout1.setCurrentTab(0);
    tabLayout2.setCurrentTab(0);
    tabLayout3.setCurrentTab(0);
  }

  @Override
  public int getFragmentContainerId() {
    return R.id.frameLayout;
  }

  @Override
  public ArrayList<Fragment> getFragments() {
    return mFragments;
  }

  private class MainAdapter extends FragmentPagerAdapter {
    private ArrayList<Fragment> fragments;

    public MainAdapter(FragmentManager fm) {
      super(fm);
      fragments = new ArrayList<>();
      for (int i = 0; i < mTabLayout.getTabCount(); i++) {
        fragments.add(TwoFragment.newInstance("ViewPager", i));
      }
    }

    @Override
    public Fragment getItem(int position) {
      return fragments.get(position);
    }

    @Override
    public int getCount() {
      return fragments.size();
    }
  }
}
