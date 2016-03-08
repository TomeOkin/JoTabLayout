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

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import java.util.ArrayList;

public class MainListFragment extends Fragment {
  private ArrayList<ListItem> mList;
  private RecyclerView mRecyclerView;

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container,
      Bundle savedInstanceState) {

    mList = new ArrayList<>();
    mList.add(new ListItem("setAlpha 及其传递性测试", AlphaTestActivity.class));
    mList.add(new ListItem("TabView function", TabViewActivity.class));
    mList.add(new ListItem("JoTabLayout sample", TabLayoutActivity.class));

    View v = inflater.inflate(R.layout.fragment_main_list, container, false);
    mRecyclerView = (RecyclerView) v.findViewById(R.id.recyclerView);
    mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
    mRecyclerView.setAdapter(new MainListAdapter(mList));
    return v;
  }

  class MainListAdapter extends RecyclerView.Adapter<MainListAdapter.ViewHolder> {

    class ViewHolder extends RecyclerView.ViewHolder {
      TextView mTextView;

      public ViewHolder(View itemView) {
        super(itemView);
        mTextView = (TextView) itemView;
      }
    }

    private ArrayList<ListItem> mList;

    public MainListAdapter(ArrayList<ListItem> list) {
      this.mList = list;
    }

    public void setLists(ArrayList<ListItem> list) {
      this.mList = list;
      notifyDataSetChanged();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
      LayoutInflater layoutInflater = LayoutInflater.from(getActivity());
      View view = layoutInflater.inflate(android.R.layout.simple_list_item_1, parent, false);
      final ViewHolder holder = new ViewHolder(view);
      holder.itemView.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View v) {
          final int position = (int) v.getTag();
          startActivity(new Intent(getActivity(), mList.get(position).clazz));
        }
      });
      return holder;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
      holder.itemView.setTag(position);
      holder.mTextView.setText(mList.get(position).name);
    }

    @Override
    public int getItemCount() {
      return mList.size();
    }
  }

  class ListItem {
    public String name;
    public Class clazz;

    public ListItem(String name, Class clazz) {
      this.name = name;
      this.clazz = clazz;
    }
  }
}
