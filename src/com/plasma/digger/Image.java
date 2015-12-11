package com.plasma.digger;

import android.graphics.Bitmap;

public class Image {

	MemoryImageSource mis;
	Bitmap bm = null;
	
	public Image(MemoryImageSource mis){
		this.mis = mis;
	}

	public Bitmap getBitmap() {
		if (mis.updated){
			int[] pix = new int[mis.pixels.length];
			//int[] count = new int[4];
			for (int t = 0; t < pix.length; t++){
				int i = mis.pixels[t];
				byte red = Pc.pal[mis.index][0][i];
				byte green = Pc.pal[mis.index][1][i];
				byte blue = Pc.pal[mis.index][2][i];
				//if (t <= 10){
				//	System.out.println(t+" "+i+" : "+red+" "+green+" "+blue);
				//}
				pix[t] = (red << 16) | ((green&0xff) << 8) | (blue);
			}
			//String s = "";
			//for (int t = 0; t < count.length; t++){
			//	s += count[t]+" ";
			//}
			//while(s.endsWith(" 0 ")) s = s.substring(0,s.length()-2);
			//System.out.println("Count: "+s);
			bm = Bitmap.createBitmap(pix, mis.width, mis.height, Bitmap.Config.RGB_565);
			mis.updated = false;
			//long sum = 0;
			//for (int i : mis.pixels) sum += i;
			//System.out.println("New image: "+sum);
		}
		else{
			//System.out.println("Keeping old image");
		}
		return bm;
	}
}
