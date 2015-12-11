package com.plasma.digger;

import android.graphics.Rect;

public class Circle{
	int x, y, r;
	Rect bounds;
	public Circle(int x, int y, int r){
		this.x = x; this.y = y; this.r = r;
		this.bounds = new Rect(x-r,y-r,x+r,y+r);
	}
	public boolean contains(int x, int y) {
		return bounds.contains(x, y);
	}
}