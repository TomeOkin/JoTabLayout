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
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class ContentFragment extends Fragment {
  private static final String ARG_INDEX = "index";
  private static final String ARG_TEXT = "text";

  private String mText;
  private int mIndex;

  private TextView mTextView;

  public static ContentFragment newInstance(String text, int index) {
    ContentFragment fragment = new ContentFragment();
    //Bundle args = new Bundle();
    //args.putInt(ARG_INDEX, index);
    //args.putString(ARG_TEXT, text);
    //fragment.setArguments(args);
    fragment.mText = text;
    fragment.mIndex = index;
    return fragment;
  }

  @Override
  public void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    //setRetainInstance(true);
  }

  @Nullable
  @Override
  public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
      @Nullable Bundle savedInstanceState) {
    if (savedInstanceState != null) {
      mIndex = savedInstanceState.getInt(ARG_INDEX, 0);
      mText = savedInstanceState.getString(ARG_TEXT, "");
    }

    View view = inflater.inflate(R.layout.fragment_two, container, false);
    mTextView = (TextView) view.findViewById(R.id.two_text);
    mTextView.setText(mText + " 第" + mIndex + "页");
    return view;
  }

  @Override
  public void onSaveInstanceState(Bundle outState) {
    super.onSaveInstanceState(outState);
    outState.putInt(ARG_INDEX, mIndex);
    outState.putString(ARG_TEXT, mText);
  }

  @Override
  public void onViewStateRestored(@Nullable Bundle savedInstanceState) {
    super.onViewStateRestored(savedInstanceState);

    //if (savedInstanceState != null) {
    //  mIndex = savedInstanceState.getInt(ARG_INDEX, 0);
    //  mText = savedInstanceState.getString(ARG_TEXT, "");
    //}
    //
    //mTextView.setText(mText + " 第" + mIndex + "页");
  }
}
