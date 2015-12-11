package com.plasma.digger;

import android.graphics.Point;
import android.graphics.Rect;

public class Square{
	Rect bounds;
	boolean menu;
	
	public Square(Rect bounds, boolean menu){
		this.bounds = bounds;
		this.menu = menu;
	}
	public boolean contains(int x, int y) {
		return bounds.contains(x, y);
	}
}
