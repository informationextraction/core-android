package com.android.service.mock;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import com.android.service.Status;
import com.android.service.action.Action;
import com.android.service.conf.ConfAction;

public class MockActionCounter extends MockAction {
	MockSubAction sub;
	public MockActionCounter(int id) throws JSONException{
		Action action = new Action(id, "action "+id);
		String jsonConf = "{\"action\"=>\"counter\"}";
		JSONObject conf = (JSONObject) new JSONTokener(jsonConf).nextValue();

		sub = new MockSubAction(new ConfAction(0, 0, conf));
		action.addSubAction(sub);
		Status.self().addAction(action);
	}
	
	public int getTriggered(){
		return sub.triggered;
	}
}
