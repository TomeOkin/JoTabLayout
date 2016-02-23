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

public class OneFragment extends Fragment {
  //private static final String ARG_INDEX = "index";
  //private static final String ARG_TEXT = "text";

  private int mIndex;
  private String mText;

  public static OneFragment newInstance(String text, int index) {
    //Bundle args = new Bundle();
    //args.putInt(ARG_INDEX, index);
    //args.putString(ARG_TEXT, text);
    OneFragment fragment = new OneFragment();
    fragment.mIndex = index;
    fragment.mText = text;
    //fragment.setArguments(args);
    return fragment;
  }

  @Override
  public void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    //Bundle arguments = getArguments();
    //mIndex = arguments.getInt(ARG_INDEX, 0);
    //mText = arguments.getString(ARG_TEXT, "");
  }

  @Nullable
  @Override
  public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
      @Nullable Bundle savedInstanceState) {
    View view = inflater.inflate(R.layout.fragment_one, container, false);
    TextView textView = (TextView) view.findViewById(R.id.text);
    textView.setText(mText + " 第" + mIndex + "页");
    return view;
  }
}
