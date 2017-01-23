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
	private double paintWidth;
	private boolean isEraser;
	private int width;
	private int height;
	
	public Line(Point[] points, int color, double paintWidth, boolean isEraser, int width, int height) {
		pointList = new ArrayList<>();
		Collections.addAll(pointList, points);
		this.color = color;
		this.paintWidth = paintWidth;
		this.isEraser = isEraser;
		this.width = width;
		this.height = height;
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

	public double getPaintWidth() {
		return paintWidth;
	}

	public void setPaintWidth(double paintWidth) {
		this.paintWidth = paintWidth;
	}

	public int getHeight() {
		return height;
	}

	public void setHeight(int height) {
		this.height = height;
	}

	public boolean isEraser() {
		return isEraser;
	}

	public void setEraser(boolean isEraser) {
		this.isEraser = isEraser;
	}
	
}
