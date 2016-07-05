package com.ghx.guideviewdemo.activity;

import android.content.Intent;
import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.ghx.guideviewdemo.R;
import com.ghx.guideviewdemo.weiget.GuideView;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private TextView mTvMain;
    private Button mBtnMain;
    private ImageView mIvMain;
    private GuideView mTvGuideView;
    private GuideView mBtnGuideView;
    private GuideView mIvGuideView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initView();
        setGuideView();
    }

    private void initView() {
        mTvMain = (TextView) findViewById(R.id.tv_main);
        mBtnMain = (Button) findViewById(R.id.btn_main);
        mIvMain = (ImageView) findViewById(R.id.iv_main);
        Button mBtnNextPage = (Button) findViewById(R.id.btn_nextpage);
        mBtnNextPage.setOnClickListener(this);
    }


    private void setGuideView() {

        TextView tv1 = new TextView(this);
        tv1.setText("我是一个TextView，请叫我二蛋");
        tv1.setTextColor(Color.RED);
        tv1.setTextSize(18);

        TextView tv2 = new TextView(this);
        tv2.setText("我是一个Buttom，就不叫");
        tv2.setTextColor(Color.BLUE);
        tv2.setTextSize(18);

        ImageView iv = new ImageView(this);
        iv.setImageResource(R.mipmap.ic_launcher);
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        iv.setLayoutParams(params);

        mTvGuideView = GuideView.Builder.newInstance(this)
                .setTargetView(mTvMain)
                .setCustomGuideView(tv1)
                .setBgColor(R.color.guide_shadow)
                .setDirction(GuideView.Direction.BOTTOM)
                .setShape(GuideView.TargetViewShape.CIRCULAR)
                .setOnclickListener(new GuideView.OnClickCallback() {
                    @Override
                    public void onClickedGuideView() {
                        mTvGuideView.hide();
                        mBtnGuideView.show();
                    }
                })
                .build();

        mBtnGuideView = GuideView.Builder.newInstance(this)
                .setTargetView(mBtnMain)
                .setCustomGuideView(tv2)
                .setBgColor(R.color.guide_shadow)
                .setDirction(GuideView.Direction.TOP)
                .setShape(GuideView.TargetViewShape.ELLIPSE)
                .setOvalParameter(new int[]{10, 5, 10, 5})
                .setOnclickListener(new GuideView.OnClickCallback() {
                    @Override
                    public void onClickedGuideView() {
                        mBtnGuideView.hide();
                        mIvGuideView.show();
                    }
                })
                .build();

        mIvGuideView = GuideView.Builder.newInstance(this)
                .setTargetView(mIvMain)
                .setDirction(GuideView.Direction.RIGHT_BOTTOM)
                .setCustomGuideView(iv)
                .setBgColor(R.color.guide_shadow)
                .setShape(GuideView.TargetViewShape.RECTANGULAR)
                .setRoundRecTParameter(new int[] {30, 30})
                .setOnclickListener(new GuideView.OnClickCallback() {
                    @Override
                    public void onClickedGuideView() {
                        mIvGuideView.hide();
                    }
                })
                .build();

        mTvGuideView.show();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
                    case R.id.btn_nextpage:
                        Intent intent = new Intent(this, SecondActivity.class);
                        startActivity(intent);
                        
                        break;
                }
    }
}
