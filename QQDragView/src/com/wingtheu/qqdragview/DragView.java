package com.wingtheu.qqdragview;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.animation.ValueAnimator.AnimatorUpdateListener;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.Path;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.PathShape;
import android.support.v4.view.MotionEventCompat;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.OvershootInterpolator;

public class DragView extends View {

	interface OnDisappearListener {
		void onDisappear(PointF mDragCenter);

		void onReset(boolean isOutOfRange);
	}

	private PointF mInitCenter;
	private PointF mDragCenter;
	private PointF mStickCenter;
	float dragCircleRadius = 0;
	float stickCircleRadius = 0;
	float stickCircleMinRadius = 0;
	float stickCircleTempRadius = stickCircleRadius;
	float farest = 0;
	String text = "";

	private Paint mPaintRed;
	private Paint mTextPaint;
	private ValueAnimator mAnim;
	private boolean isOutOfRange = false;
	private boolean isDisappear = false;

	private OnDisappearListener mListener;
	private Rect rect;
	private int mStatusBarHeight;

	private float resetDistance;

	public DragView(Context context) {
		super(context);

		rect = new Rect(0, 0, 50, 50);

		stickCircleRadius = DragUtils.dip2Dimension(10.0f, context);
		dragCircleRadius = DragUtils.dip2Dimension(10.0f, context);
		stickCircleMinRadius = DragUtils.dip2Dimension(3.0f, context);
		farest = DragUtils.dip2Dimension(80.0f, context);
		resetDistance = DragUtils.dip2Dimension(40.0f, getContext());

		mPaintRed = new Paint(Paint.ANTI_ALIAS_FLAG);
		mPaintRed.setColor(Color.RED);

		mTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		mTextPaint.setTextAlign(Align.CENTER);
		mTextPaint.setColor(Color.WHITE);
		mTextPaint.setTextSize(dragCircleRadius * 1.2f);
	}

	public void setDargCircleRadius(float r) {
		dragCircleRadius = r;
	}

	public void setStickCircleRadius(float r) {
		stickCircleRadius = r;
	}

	public void setNumber(int num) {
		text = String.valueOf(num);
	}

	public void initCenter(float x, float y) {
		mDragCenter = new PointF(x, y);
		mStickCenter = new PointF(x, y);
		mInitCenter = new PointF(x, y);
		invalidate();
	}

	private void updateDragCenter(float x, float y) {
		this.mDragCenter.x = x;
		this.mDragCenter.y = y;
		invalidate();
	}

	private ShapeDrawable drawGooView() {
		Path path = new Path();

		float distance = (float) GeometryUtil.getDistanceBetween2Points(mDragCenter, mStickCenter);
		stickCircleTempRadius = getCurrentRadius(distance);

		float xDiff = mStickCenter.x - mDragCenter.x;
		Double dragLineK = null;
		if (xDiff != 0) {
			dragLineK = (double) ((mStickCenter.y - mDragCenter.y) / xDiff);
		}

		PointF[] dragPoints = GeometryUtil.getIntersectionPoints(mDragCenter, dragCircleRadius, dragLineK);
		PointF[] stickPoints = GeometryUtil.getIntersectionPoints(mStickCenter, stickCircleTempRadius, dragLineK);

		PointF pointByPercent = GeometryUtil.getPointByPercent(mDragCenter, mStickCenter, 0.618f);

		path.moveTo((float) stickPoints[0].x, (float) stickPoints[0].y);
		path.quadTo((float) pointByPercent.x, (float) pointByPercent.y, (float) dragPoints[0].x, (float) dragPoints[0].y);
		path.lineTo((float) dragPoints[1].x, (float) dragPoints[1].y);
		path.quadTo((float) pointByPercent.x, (float) pointByPercent.y, (float) stickPoints[1].x, (float) stickPoints[1].y);
		path.close();

		ShapeDrawable shapeDrawable = new ShapeDrawable(new PathShape(path, 50f, 50f));
		shapeDrawable.getPaint().setColor(Color.RED);
		return shapeDrawable;
	}

	private float getCurrentRadius(float distance) {

		distance = Math.min(distance, farest);

		float fraction = 0.2f + 0.8f * distance / farest;

		float evaluateValue = (float) GeometryUtil.evaluateValue(fraction, stickCircleRadius, stickCircleMinRadius);
		return evaluateValue;
	}

	@Override
	public boolean dispatchTouchEvent(MotionEvent event) {
		if (isAnimRunning()) {
			return false;
		}
		return super.dispatchTouchEvent(event);
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {

		int actionMasked = MotionEventCompat.getActionMasked(event);
		switch (actionMasked) {
		case MotionEvent.ACTION_DOWN: {

			if (isAnimRunning()) {
				return false;
			}

			isDisappear = false;
			isOutOfRange = false;
			updateDragCenter(event.getRawX(), event.getRawY());

			break;
		}
		case MotionEvent.ACTION_MOVE: {

			PointF p0 = new PointF(mDragCenter.x, mDragCenter.y);
			PointF p1 = new PointF(mStickCenter.x, mStickCenter.y);
			if (GeometryUtil.getDistanceBetween2Points(p0, p1) > farest) {
				isOutOfRange = true;

				updateDragCenter(event.getRawX(), event.getRawY());
				return false;
			}

			updateDragCenter(event.getRawX(), event.getRawY());
			break;
		}
		case MotionEvent.ACTION_UP: {
			handleActionUp();

			break;
		}
		default: {
			isOutOfRange = false;
			break;
		}
		}
		return true;
	}

	private boolean isAnimRunning() {
		if (mAnim != null && mAnim.isRunning()) {
			return true;
		}
		return false;
	}

	private void disappeared() {
		isDisappear = true;
		invalidate();

		if (mListener != null) {
			mListener.onDisappear(mDragCenter);
		}
	}

	private void handleActionUp() {
		if (isOutOfRange) {
			if (GeometryUtil.getDistanceBetween2Points(mDragCenter, mInitCenter) < resetDistance) {
				if (mListener != null)
					mListener.onReset(isOutOfRange);
				return;
			}

			disappeared();
		} else {
			mAnim = ValueAnimator.ofFloat(1.0f);
			mAnim.setInterpolator(new OvershootInterpolator(4.0f));

			final PointF startPoint = new PointF(mDragCenter.x, mDragCenter.y);
			final PointF endPoint = new PointF(mStickCenter.x, mStickCenter.y);
			mAnim.addUpdateListener(new AnimatorUpdateListener() {

				@Override
				public void onAnimationUpdate(ValueAnimator animation) {
					float fraction = animation.getAnimatedFraction();
					PointF pointByPercent = GeometryUtil.getPointByPercent(startPoint, endPoint, fraction);
					updateDragCenter((float) pointByPercent.x, (float) pointByPercent.y);
				}
			});
			mAnim.addListener(new AnimatorListenerAdapter() {
				@Override
				public void onAnimationEnd(Animator animation) {
					if (mListener != null)
						mListener.onReset(isOutOfRange);
				}
			});
			if (GeometryUtil.getDistanceBetween2Points(startPoint, endPoint) < 10) {
				mAnim.setDuration(10);
			} else {
				mAnim.setDuration(500);
			}
			mAnim.start();
		}
	}

	@Override
	protected void onDraw(Canvas canvas) {

		canvas.save();
		canvas.translate(0, -mStatusBarHeight);
		if (!isDisappear) {
			if (!isOutOfRange) {
				ShapeDrawable drawGooView = drawGooView();
				drawGooView.setBounds(rect);
				drawGooView.draw(canvas);

				canvas.drawCircle(mStickCenter.x, mStickCenter.y, stickCircleTempRadius, mPaintRed);
			}
			canvas.drawCircle(mDragCenter.x, mDragCenter.y, dragCircleRadius, mPaintRed);
			canvas.drawText(text, mDragCenter.x, mDragCenter.y + dragCircleRadius / 2f, mTextPaint);
		}
		canvas.restore();

	}

	public OnDisappearListener getOnDisappearListener() {
		return mListener;
	}

	public void setOnDisappearListener(OnDisappearListener mListener) {
		this.mListener = mListener;
	}

	public void setStatusBarHeight(int statusBarHeight) {
		this.mStatusBarHeight = statusBarHeight;
	}

}
