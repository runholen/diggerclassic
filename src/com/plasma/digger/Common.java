package com.plasma.digger;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;

public class Common {

	public static void showSelectionDialog(Activity a, SelectionChooser s, int id, Object object, String title, String message, String[] selections, int initialSelection, boolean mayCancel){
		new Common().showSelectionNow(a,s,id,object,title,message,selections,initialSelection,mayCancel);
	}
	
	private void showSelectionNow(Activity a, SelectionChooser s, int id, Object object, String title, String message, String[] selections, int initialSelection, boolean mayCancel) {
		SelectionDisplayer sd = new SelectionDisplayer(a,s,id,object,title,message,selections,initialSelection,mayCancel);
		a.runOnUiThread(sd);
	}

	public class SelectionDisplayer implements Runnable{
		Activity a;
		SelectionChooser s;
		int id;
		Object object;
		String title;
		String[] selections;
		boolean mayCancel;
		String message;
		int initialSelection;
		
		public SelectionDisplayer(Activity a, SelectionChooser s, int id, Object object, String title, String message, String[] selections, int initialSelection, boolean mayCancel) {
			this.a = a;
			this.s = s;
			this.id = id;
			this.object = object;
			this.title = title;
			this.message = message;
			this.selections = selections;
			this.mayCancel = mayCancel;
			this.initialSelection = initialSelection;
		}
		
		@Override
		public void run(){
			AlertDialog.Builder builder = new AlertDialog.Builder(a);
	        builder.setTitle(title);
	        builder.setCancelable(mayCancel);
	        if (message != null && false) builder.setMessage(message);
	        //builder.setItems(selections, new DialogInterface.OnClickListener(){
	        //   public void onClick(DialogInterface dialogInterface, int item) {
	        //        s.selectionChoosed(id,object,item);
	        //    }
	        //});
	        //if (initialSelection > 0){
	        builder.setAdapter(new MyListAdapter(a,android.R.layout.simple_spinner_dropdown_item, selections, initialSelection),new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int itemIndex) {
					String value = null;
					if (itemIndex >= 0) value = selections[itemIndex];
					s.selectionChoosed(id,object,itemIndex,value);
				}
			});
	        	//lv.getAdapter().setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
	        	
	        	//View v = lv.getAdapter().getView(initialSelection, null, null);
	        	
	        	//v.setSelected(true);
	        AlertDialog ad = builder.create();
	        ad.show();
	        if (initialSelection >= 0){
	        	ListView lv = ad.getListView();
	        	lv.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
	        	lv.setItemsCanFocus(false);
	        	lv.setItemChecked(initialSelection, true);
	        }
		}
	}
	
	public class MyListAdapter extends ArrayAdapter<String> implements ListAdapter{

		private Drawable background = null;
		private Drawable filtered = null;
		private int initialSelection;
		private String[] objects;
		
		public MyListAdapter(Context context, int textViewResourceId, String[] objects, int initialSelection) {
			super(context, textViewResourceId, objects);
			this.initialSelection = initialSelection;
			this.objects = objects;
		}
		
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			View v = super.getView(position, convertView, parent);
	        return v;
	    }
		
		@Override
		public boolean areAllItemsEnabled(){
			return false;
		}
		@Override
		public boolean isEnabled(int position){
			return true;
		}
		
	}

}
