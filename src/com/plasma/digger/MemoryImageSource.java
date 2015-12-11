package com.plasma.digger;

public class MemoryImageSource {

	protected int width, height;
	protected int[] pixels;
	protected boolean updated = false;
	protected int index;
	
	public MemoryImageSource(int index, int width, int height, int[] pixels, int offset, int stride) {
		this.index = index;
		this.width = width;
		this.height = height;
		this.pixels = pixels;
	}

	public void newPixels() {
		updated  = true;
	}

	public void newPixels(int x, int y, int width2, int height2) {
		updated = true;
	}
}
