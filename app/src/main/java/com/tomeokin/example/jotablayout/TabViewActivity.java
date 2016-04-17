package com.tomeokin.example.jotablayout;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;
import com.tomeokin.widget.jotablayout2.TabView;

public class TabViewActivity extends AppCompatActivity {

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_tabview);

    final TabView tabView = (TabView) findViewById(R.id.tabView);

    final SeekBar seekBar = (SeekBar) findViewById(R.id.seekBar);
    seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
      @Override
      public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        tabView.setAlpha(progress / 255.0f);
      }

      @Override
      public void onStartTrackingTouch(SeekBar seekBar) {

      }

      @Override
      public void onStopTrackingTouch(SeekBar seekBar) {

      }
    });

    final Button toggle = (Button) findViewById(R.id.toggle);
    toggle.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        tabView.setAlphaTransformEnabled(!tabView.isAlphaTransformEnabled());
      }
    });

    final Button changeAttr = (Button) findViewById(R.id.changeAttr);
    changeAttr.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        tabView.setTitleAttr("hi", tabView.getTextSize(), tabView.getTextColorNormal(),
            tabView.getTextColorSelected());
      }
    });
  }
}
