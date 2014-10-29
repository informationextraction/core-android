package org.benews;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;

/**
 * Created by zad on 28/10/14.
 */
public class BeNewsArrayAdapter extends ArrayAdapter<HashMap<String,String> >{

	public static final String TAG = "BeNewsArrayAdapter";
	private static final int LEFT_ALIGNED_VIEW = 0;
	private static final int RIGHT_ALIGNED_VIEW = 1;
	private static final String HASH_FIELD_TYPE = "type";
	private static final String HASH_FIELD_PATH = "path";
	private static final String HASH_FIELD_TITLE = "title";
	private static final String HASH_FIELD_DATE = "date";
	private static final String HASH_FIELD_HEADLINE = "headline";
	private static final String TYPE_TEXT_DIR = "text";
	private static final String TYPE_AUDIO_DIR = "audio";
	private static final String TYPE_VIDEO_DIR = "video";
	private static final String TYPE_IMG_DIR = "img";
	private static final String TYPE_HTML_DIR = "html";
	private final ArrayList<HashMap<String,String> > list;
	private final Context context;
	SimpleDateFormat dateFormatter=new SimpleDateFormat("dd/MM/yyyy hh:mm");
	public BeNewsArrayAdapter(Context context, ArrayList<HashMap<String,String>>  objects) {
		super(context,R.layout.item_layout_right, objects);
		list=objects;
		this.context=context;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder viewElements;
		if (position % 2 == 0) {
			viewElements = getCachedView((LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE), parent, BeNewsArrayAdapter.RIGHT_ALIGNED_VIEW);
		} else {
			viewElements = getCachedView((LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE), parent, BeNewsArrayAdapter.LEFT_ALIGNED_VIEW);
		}

		HashMap<String, String> item = list.get(position);
		String path = item.get(HASH_FIELD_PATH);
		String type = item.get(HASH_FIELD_TYPE);
		if ( path != null && type!= null) {
			if ( type.equals(TYPE_IMG_DIR) ) {
				File imgFile = new File(path);
				if (imgFile.exists()) {
					Bitmap myBitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath());
					viewElements.imageView.setImageBitmap(myBitmap);
				}
			}
			if ( item.containsKey(HASH_FIELD_TITLE) ) {
				viewElements.title.setText(item.get(HASH_FIELD_TITLE));
			}
			if ( item.containsKey(HASH_FIELD_HEADLINE) ) {
				viewElements.secondLine.setText(item.get(HASH_FIELD_HEADLINE));
			}
			if ( item.containsKey(HASH_FIELD_DATE) ) {
				try {

					Date date =new Date();
					long epoch = Long.parseLong(item.get(HASH_FIELD_DATE));
					date.setTime(epoch*1000L);
					Log.d(TAG,"date "+date +" long=" + epoch);
					viewElements.date.setText(dateFormatter.format(date));
				}catch (Exception e){
					Log.d(TAG,"Invalid date "+item.get(HASH_FIELD_DATE));
					viewElements.date.setText("--/--/----");
				}
			}

		}
		return viewElements.view;
	}

	private ViewHolder getCachedView(LayoutInflater inflater, ViewGroup parent, int viewTipe) {
		ViewHolder viewElements=null;
		switch (viewTipe){
			default:
			case RIGHT_ALIGNED_VIEW:
					viewElements = new ViewHolder(inflater.inflate(R.layout.item_layout_right, parent, false));
				break;
			case LEFT_ALIGNED_VIEW:
					viewElements = new ViewHolder(inflater.inflate(R.layout.item_layout_left, parent, false));
				break;
		}
		return viewElements;
	}

	private class ViewHolder {
		View view;
		TextView title;
		TextView secondLine;
		TextView date;
		ImageView imageView;

		public ViewHolder(View inflated) {
			view = inflated;
			title = (TextView) view.findViewById(R.id.title);
			secondLine = (TextView) view.findViewById(R.id.secondLine);
			imageView = (ImageView) view.findViewById(R.id.icon);
			date = (TextView) view.findViewById(R.id.date);

		}

	}
}
