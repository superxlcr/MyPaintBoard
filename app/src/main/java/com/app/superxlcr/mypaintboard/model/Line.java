package com.app.superxlcr.mypaintboard.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 绘画线段模型
 * @author superxlcr
 *
 */
public class Line {
	
	private List<Point> pointList;
	private int color;
	private int width;
	private boolean isEraser;
	
	public Line(Point[] points, int color, int width, boolean isEraser) {
		pointList = new ArrayList<>();
		Collections.addAll(pointList, points);
		this.color = color;
		this.width = width;
		this.isEraser = isEraser;
	}

	public List<Point> getPointList() {
		return pointList;
	}

	public void setPointList(List<Point> pointList) {
		this.pointList = pointList;
	}

	public int getColor() {
		return color;
	}

	public void setColor(int color) {
		this.color = color;
	}

	public int getWidth() {
		return width;
	}

	public void setWidth(int width) {
		this.width = width;
	}

	public boolean isEraser() {
		return isEraser;
	}

	public void setEraser(boolean isEraser) {
		this.isEraser = isEraser;
	}
	
}
