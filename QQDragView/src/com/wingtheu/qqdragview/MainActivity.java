package com.wingtheu.qqdragview;

import android.app.Activity;
import android.graphics.PointF;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;

public class MainActivity extends Activity implements OnClickListener {

	private TextView mRed_point;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_main);
		initView();
	}

	private void initView() {
		TextView title = (TextView) findViewById(R.id.title);
		mRed_point = (TextView) findViewById(R.id.red_point);
		Button click = (Button) findViewById(R.id.click);
		click.setOnClickListener(this);
		initRedPoint();
	}

	private void initRedPoint() {
		mRed_point.setText(String.valueOf(1));
		mRed_point.setTag(1);
		DragViewListener mGooListener = new DragViewListener(MainActivity.this,
				mRed_point) {
			@Override
			public void onDisappear(PointF mDragCenter) {
				super.onDisappear(mDragCenter);
				mRed_point.setVisibility(View.GONE);
				mRed_point.invalidate();
			}

			@Override
			public void onReset(boolean isOutOfRange) {
				super.onReset(isOutOfRange);
				mRed_point.setVisibility(View.VISIBLE);
				mRed_point.invalidate();
			}
		};
		mRed_point.setOnTouchListener(mGooListener);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.click:
			mRed_point.setVisibility(View.VISIBLE);
			initRedPoint();
			break;
		}
	}
}
