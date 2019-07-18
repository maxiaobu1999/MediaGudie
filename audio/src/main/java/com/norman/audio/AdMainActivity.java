package com.norman.audio;

import android.app.Activity;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

public class AdMainActivity extends AppCompatActivity {
    public static final String TAG = "AdMainActivity+++:";
    private Activity mActivity;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ad_main);
        mActivity = this;

        addButton("..").setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            }
        });
    }

    private Button addButton(String string) {
        Button button = new Button(mActivity);
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 300);
        params.topMargin = 50;
        button.setLayoutParams(params);
        button.setText(string);
        LinearLayout ll = findViewById(R.id.ll_container);
        ll.addView(button);
        return button;
    }
}
