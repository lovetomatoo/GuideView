package com.ghx.guideviewdemo.activity;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.ghx.guideviewdemo.R;
import com.ghx.guideviewdemo.weiget.GuideView;

/**
 * Created by qmtv on 2016/7/5.
 */
public class SecondActivity extends AppCompatActivity {

    private ImageView mIvLeft;
    private ImageView mIvRight;
    private TextView mTvMid;
    private GuideView mGuideViewRight;
    private GuideView mGuideViewLeft;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_second);

        initView();
        setGuideView();
    }

    private void initView() {
        mIvLeft = (ImageView) findViewById(R.id.iv_left);
        mIvRight = (ImageView) findViewById(R.id.iv_right);
        mTvMid = (TextView) findViewById(R.id.tv_mid);
    }

    private void setGuideView() {

        //来张图片
        ImageView imageView1 = new ImageView(this);
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                                                                                ViewGroup.LayoutParams.WRAP_CONTENT);
        imageView1.setImageResource(R.mipmap.pinbi_tip_icon);
        imageView1.setLayoutParams(params);

        mGuideViewRight = GuideView.Builder.newInstance(this)
                .setTargetView(mIvRight)
                .setCustomGuideView(imageView1)
                .setDirction(GuideView.Direction.LEFT_BOTTOM)
                .setOnclickListener(new GuideView.OnClickCallback() {
                    @Override
                    public void onClickedGuideView() {
                        mGuideViewRight.hide();
//                        mGuideViewLeft.show();
                    }
                })
                .build();

//        mGuideViewLeft = GuideView.Builder.newInstance(this)
//                .setTargetView(mIvLeft)
//                .setCustomGuideView(imageView1)
//                .setDirction(GuideView.Direction.LEFT_BOTTOM)
//                .setOnclickListener(new GuideView.OnClickCallback() {
//                    @Override
//                    public void onClickedGuideView() {
//                        mGuideViewLeft.hide();
//                    }
//                })
//                .build();


        mGuideViewRight.show();
    }
}
