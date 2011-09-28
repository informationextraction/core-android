package com.android.service.test;

import java.io.IOException;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import com.android.service.GeneralException;
import com.android.service.Status;
import com.android.service.conf.ConfEvent;
import com.android.service.conf.Configuration;
import com.android.service.event.EventManager;
import com.android.service.util.Utils;

import android.app.Instrumentation;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.content.res.Resources.NotFoundException;
import android.test.AndroidTestCase;
import android.test.InstrumentationTestCase;

public class ConfigurationTest extends InstrumentationTestCase {
Status status;
	
	@Override
	public void setUp() {
		status = Status.self();
		Status.setAppContext(getInstrumentation().getTargetContext());
		status.clean();
		status.unTriggerAll();
	}

	public void testJSON() throws GeneralException, JSONException, NotFoundException, IOException {
		
		AssetManager assets = getInstrumentation().getContext().getAssets();
		String[] list = assets.list("/");
		list = assets.list("/assets");
		list = assets.list("/res");
		
		Resources resources = getInstrumentation().getContext().getResources();
		int sizeConfig = resources.openRawResource(R.raw.config).available();
		int sizeMessages = resources.openRawResource(R.raw.messages).available();
		int sizeJson = resources.openRawResource(R.raw.json_config_mobile).available();
		
		String jsonConf = Utils.inputStreamToString(
				resources.openRawResource(R.raw.json_config_mobile));
			
		assertNotNull(jsonConf);
		assertTrue(jsonConf.length() > 0);
		
		JSONTokener tokenizer = new JSONTokener(jsonConf);
		assertNotNull(tokenizer);
		JSONObject conf = (JSONObject) tokenizer.nextValue();
	
		assertNotNull(conf);
	}
	
	public void testConfiguration() throws GeneralException, JSONException, IOException {
		Resources resources = getInstrumentation().getContext().getResources();
		String jsonConf = Utils.inputStreamToString(resources.openRawResource(R.raw.json_config_mobile));
		
		Configuration conf = new Configuration(jsonConf);
		assertNotNull(conf);
		
		boolean ret = conf.loadConfiguration(false);
		assertTrue(ret);
	}
}
