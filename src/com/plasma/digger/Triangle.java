package com.plasma.digger;

import android.graphics.Point;
import android.graphics.Rect;

public class Triangle{
	Point p1, p2, p3;
	Point[] points;
	Rect bounds;
	public Triangle(Point p1, Point p2, Point p3, Rect bounds){
		this.p1 = p1; this.p2 = p2; this.p3 = p3;
		points = new Point[]{p1,p2,p3};
		this.bounds = bounds;
	}
	public Triangle(Rect bounds, int dir){
		this.bounds = bounds;
		if (dir == 8){
			p1 = new Point(bounds.left+bounds.width()/2,bounds.top);
			p2 = new Point(bounds.left,bounds.bottom);
			p3 = new Point(bounds.right,bounds.bottom);
		}
		if (dir == 4){
			p1 = new Point(bounds.left,bounds.top+bounds.height()/2);
			p2 = new Point(bounds.right,bounds.top);
			p3 = new Point(bounds.right,bounds.bottom);
		}
		if (dir == 6){
			p1 = new Point(bounds.right,bounds.top+bounds.height()/2);
			p2 = new Point(bounds.left,bounds.top);
			p3 = new Point(bounds.left,bounds.bottom);
		}
		if (dir == 2){
			p1 = new Point(bounds.left+bounds.width()/2,bounds.bottom);
			p2 = new Point(bounds.left,bounds.top);
			p3 = new Point(bounds.right,bounds.top);
		}
		points = new Point[]{p1,p2,p3};
	}
	public boolean contains(int x, int y) {
		return bounds.contains(x, y);
	}
}
