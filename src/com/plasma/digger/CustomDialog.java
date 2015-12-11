package com.plasma.digger;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;

public class CustomDialog implements Runnable, OnDismissListener{
	Activity a;
	String title;
	String text;
	boolean exit;
	Exception ex;
	public CustomDialog(Activity a, String title, Exception ex, String text, boolean exit) {
		this.a = a; 
		this.title = title;
		this.ex = ex;
		this.text = text;
		this.exit = exit;
	}
	@Override
	public void run() {
		AlertDialog alert = new AlertDialog.Builder(a).create();
		alert.setTitle(title);
		if (ex != null){
			String s = ex.toString();
			//s += getStackTrace(ex);
			alert.setMessage(s);
		}
		else alert.setMessage(text);
		alert.show();
		if (exit) alert.setOnDismissListener(this);
	}
	@Override
	public void onDismiss(DialogInterface dialog) {
		a.finish();
	}
}
