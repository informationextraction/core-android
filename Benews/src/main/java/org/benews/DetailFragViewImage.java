package org.benews;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;

import java.io.File;
import java.util.Date;

/**
 * Created by zad on 30/10/14.
 */
public class DetailFragViewImage extends DetailFragView {

	private final String TAG = "DetailFragViewImage";

	@Override
	public void onActivityCreated(@Nullable Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		if (item_path != null && item_type != null) {
			if (item_type.equals(BeNewsArrayAdapter.TYPE_IMG_DIR)) {
				File imgFile = new File(item_path);
				if (imgFile.exists()) {
					try {
						Bitmap myBitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath());
						ImageView imageView = (ImageView) media;
						if ( myBitmap.getHeight() > 600 )
							myBitmap = BitmapHelper.scaleToFitHeight(myBitmap,400);
						imageView.setImageBitmap(myBitmap);
					} catch (Exception e) {

					}
				}
			}
			if (item_title != null && title != null) {
				((TextView) title).setText(item_title);
			}
			if (item_headline != null && headline != null) {
				((TextView) headline).setText(item_headline);
			}
			if (item_content != null && content != null) {
				((TextView) content).setText(item_content);
			}
			if (item_date != null && date != null) {
				try {
					Date date_f = new Date();
					long epoch = Long.parseLong(item_date);
					date_f.setTime(epoch * 1000L);
					//Log.d(TAG,"date "+date +" long=" + epoch);
					((TextView) date).setText(BeNewsArrayAdapter.dateFormatter.format(date_f));
				} catch (Exception e) {
					Log.d(TAG, "Invalid date " + item_date);
					((TextView) date).setText("--/--/----");
				}
			}

		}

	}
}
