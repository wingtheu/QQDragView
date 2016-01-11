package com.wingtheu.qqdragview;

import android.graphics.PointF;

public class GeometryUtil {

	public static float getDistanceBetween2Points(PointF p0, PointF p1) {
		float distance = (float) Math.sqrt(Math.pow(p0.y - p1.y, 2) + Math.pow(p0.x - p1.x, 2));
		return distance;
	}

	public static PointF getMiddlePoint(PointF p1, PointF p2) {
		return new PointF((p1.x + p2.x) / 2.0f, (p1.y + p2.y) / 2.0f);
	}

	public static PointF getPointByPercent(PointF p1, PointF p2, float percent) {
		return new PointF(evaluateValue(percent, p1.x, p2.x), evaluateValue(percent, p1.y, p2.y));
	}

	public static float evaluateValue(float fraction, Number start, Number end) {
		return start.floatValue() + (end.floatValue() - start.floatValue()) * fraction;
	}

	public static PointF[] getIntersectionPoints(PointF pMiddle, float radius, Double lineK) {
		PointF[] points = new PointF[2];

		float radian, xOffset = 0, yOffset = 0;
		if (lineK != null) {
			radian = (float) Math.atan(lineK);
			xOffset = (float) (Math.sin(radian) * radius);
			yOffset = (float) (Math.cos(radian) * radius);
		} else {
			xOffset = radius;
			yOffset = 0;
		}
		points[0] = new PointF(pMiddle.x + xOffset, pMiddle.y - yOffset);
		points[1] = new PointF(pMiddle.x - xOffset, pMiddle.y + yOffset);

		return points;
	}
}