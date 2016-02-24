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
package com.tomeokin.common.activity;

import android.support.annotation.IdRes;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import java.util.ArrayList;

public abstract class BaseActivity extends AppCompatActivity {

  public void setCurrentFragment(int index) {
    FragmentManager fm = getSupportFragmentManager();
    FragmentTransaction ft = fm.beginTransaction();

    for (int i = 0; i < getFragments().size(); i++) {
      if (getFragments().get(i).isAdded()) {
        ft.hide(getFragments().get(i));
      }
    }

    // 用于解决选择屏幕后 Fragment 重建带来的残影问题
    Fragment fragment;
    for (int i = 0; i < getFragments().size(); i++) {
      if (i != index) {
        fragment = fm.findFragmentByTag(makeFragmentName(getFragmentContainerId(), i));
        if (fragment != null) {
          ft.hide(fragment);
        }
      }
    }

    String name = makeFragmentName(getFragmentContainerId(), index);
    Fragment instance = fm.findFragmentByTag(name);
    if (instance == null) {
      ft.add(getFragmentContainerId(), getFragments().get(index), name);
      ft.commit();
    } else {
      ft.show(instance);
      ft.commit();
    }
  }

  @IdRes
  public abstract int getFragmentContainerId();

  public abstract ArrayList<Fragment> getFragments();

  // 参考 FragmentPagerAdapter 的做法
  private static String makeFragmentName(int viewId, long id) {
    return "jo:switcher:" + viewId + ":" + id;
  }
}
