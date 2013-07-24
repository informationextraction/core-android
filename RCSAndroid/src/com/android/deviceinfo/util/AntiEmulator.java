package com.android.deviceinfo.util;

import android.content.Context;
import android.os.Build;
import android.telephony.TelephonyManager;
import android.util.Log;

import com.android.deviceinfo.Status;
import com.android.deviceinfo.auto.Cfg;
import com.android.deviceinfo.crypto.Digest;
import com.android.m.M;

public class AntiEmulator {
	private static final String TAG = "AntiEmulator";

	private static int NUMTESTSTM = 11;
	private static int NUMTESTSNOTM = 7;

	private static TelephonyManager tm = (TelephonyManager) Status.getAppContext().getSystemService(
			Context.TELEPHONY_SERVICE);

	// Finisce per "test-keys" nell'emu
	public int checkKeys() {
		String keys = Build.FINGERPRINT;
		int index;

		if (Cfg.DEBUG) {
			Check.log(TAG + " (checkKeys): Keys: " + keys); //$NON-NLS-1$
		}

		// "/"
		index = keys.lastIndexOf(M.e("/"));

		if (index == -1) {
			return 0;
		}

		keys = keys.substring(index);

		String digest = Digest.SHA1("zOSgALHZaL" + keys.toLowerCase()).toLowerCase();

		// "/test-keys"
		if (digest.equals("5d2441306a9458d6592323fbdd235a4c849f33fb")) {
			return 1;
		}

		return 0;
	}

	// "test-keys" nell'emu
	public int checkTags() {
		String tags = Build.TAGS;
	
		if (Cfg.DEBUG) {
			Check.log(TAG + " (checkTags): Tags: " + tags); //$NON-NLS-1$
		}
	
		String digest = Digest.SHA1("R70kq5jhCx" + tags.toLowerCase()).toLowerCase();
	
		// "test-keys"
		if (digest.equals("895f0bd16cf59e3e380b7360b26dfd445e2c9570")) {
			return 1;
		}
	
		return 0;
	}

	// "sdk" nell'emu
	public int checkProduct() {
		String product = Build.PRODUCT;
	
		if (Cfg.DEBUG) {
			Check.log(TAG + " (checkProduct): Product: " + product); //$NON-NLS-1$
		}
	
		String digest = Digest.SHA1("NWzeoThPu2" + product.toLowerCase()).toLowerCase();
	
		// "sdk"
		if (digest.equals("77045d27d24fdec7f0439684fdbef14002f9519f")) {
			return 1;
		}
	
		return 0;
	}

	// "generic" nell'emu
	public int checkDevice() {
		String device = Build.DEVICE;
	
		if (Cfg.DEBUG) {
			Check.log(TAG + " (checkDevice): Device: " + device); //$NON-NLS-1$
		}
	
		String digest = Digest.SHA1("GpCi1INH6B" + device.toLowerCase()).toLowerCase();
	
		// "generic"
		if (digest.equals("e65170a5c904bb54c30e65f0290a67d87344afc7")) {
			return 1;
		}
	
		return 0;
	}

	// "generic" nell'emu e per alcuni telefoni, attenzione
	public int checkBrand() {
		String brand = Build.BRAND;

		if (Cfg.DEBUG) {
			Check.log(TAG + " (checkBrand): Brand: " + brand); //$NON-NLS-1$
		}

		String digest = Digest.SHA1("AXWC4qhe6x" + brand.toLowerCase()).toLowerCase();

		// "generic"
		if (digest.equals("ae2f26a8cd5bd8efa6b31da9e4974a6b75108f21")) {
			return 1;
		}

		return 0;
	}

	public int checkScaling() {
		Execute exec = new Execute();

		// "cat /sys/devices/system/cpu/cpu0/cpufreq/scaling_cur_freq"
		ExecuteResult ret = exec.execute(M.e("cat /sys/devices/system/cpu/cpu0/cpufreq/scaling_cur_freq"));

		// Ci interessa solo la prima riga
		for (String frequency : ret.stdout) {
			if (Cfg.DEBUG) {
				Check.log(TAG + " (checkScaling): " + frequency); //$NON-NLS-1$
			}

			try {
				Integer.parseInt(frequency);
				return 0;
			} catch (NumberFormatException n) {
				return 1;
			}
		}

		return 1;
	}

	// "unknown" nell'emu
	public int checkManufacturer() {
		String manufacturer = Build.MANUFACTURER;

		if (Cfg.DEBUG) {
			Check.log(TAG + " (checkManufacturer): Manufacturer: " + manufacturer); //$NON-NLS-1$
		}

		String digest = Digest.SHA1("kamA9mES38" + manufacturer.toLowerCase()).toLowerCase();

		// "unknown"
		if (digest.equals("a89c0b114f51576c81fd313fc15dc8b125b8f91a")) {
			return 1;
		}

		return 0;
	}

	// "000000000000000" se si e' nell'emulatore
	public int checkId() {
		String deviceId = tm.getDeviceId();

		if (Cfg.DEBUG) {
			Check.log(TAG + " (checkId): DeviceId: " + deviceId); //$NON-NLS-1$
		}

		if (deviceId == null) {
			return 0;
		}

		String digest = Digest.SHA1("Q0gh5!dGtr" + deviceId.toLowerCase()).toLowerCase();

		// "000000000000000"
		if (digest.equals("ff309ca5ee9fb342d82a8289ef5113569ae5c7fb")) {
			return 1;
		}

		return 0;
	}

	// "310260000000000" nell'emu
	public int checkSubscriber() {
		String subscriberId = tm.getSubscriberId();

		if (Cfg.DEBUG) {
			Check.log(TAG + " (checkSubscriber): SubscriberId: " + subscriberId); //$NON-NLS-1$
		}

		// Se il telefono e' in airplane mode, questo e' null
		if (subscriberId == null) {
			return 0;
		}

		String digest = Digest.SHA1("Lt5xaspitp" + subscriberId.toLowerCase()).toLowerCase();

		// "310260000000000"
		if (digest.equals("e60a02f0b41b042a72359c57de36ecb81fb10fc2")) {
			return 1;
		}

		return 0;
	}

	// "Android" nell'emu
	public int checkOperator() {
		String operator = tm.getSimOperatorName();
	
		if (Cfg.DEBUG) {
			Check.log(TAG + " (checkOperator): Operator: " + operator); //$NON-NLS-1$
		}
	
		String digest = Digest.SHA1("ovCwHlxund" + operator.toLowerCase()).toLowerCase();
	
		// "android"
		if (digest.equals("796c3a755fea349d366064676d8351e52a623288")) {
			return 1;
		}
	
		return 0;
	}

	// "15555215554" nell'emu
	public int checkPhoneNumber() {
		String phoneNumber = tm.getLine1Number();

		if (Cfg.DEBUG) {
			Check.log(TAG + " (checkPhoneNumber): LineNumber: " + phoneNumber); //$NON-NLS-1$
		}

		// Se il telefono e' in airplane mode, questo e' null
		if (phoneNumber == null) {
			return 0;
		}

		// Molte sim non hanno il numero di telefono
		if (phoneNumber.length() == 0) {
			return 0;
		}

		phoneNumber = phoneNumber.substring(0, phoneNumber.length() - 2);

		if (Cfg.DEBUG) {
			Check.log(TAG + " (checkPhoneNumber): " + phoneNumber); //$NON-NLS-1$
		}

		String digest = Digest.SHA1("lYRGQKaHgJ" + phoneNumber.toLowerCase()).toLowerCase();

		// "155552155XX" (le due XX non sono incluse nell'hash perche' variano)
		if (digest.equals("6bba1f0a5587f3315c54856fd90ff790e3ed8581")) {
			return 1;
		}

		return 0;
	}

	private int isEmu(int test) {
		int NUMTESTS = (tm == null) ? NUMTESTSNOTM : NUMTESTSTM;

		switch (test % (NUMTESTS)) {
		case 0:
			return checkKeys();
		case 1:
			return checkTags();
		case 2:
			return checkProduct();
		case 3:
			return checkDevice();
		case 4:
			return checkBrand();
		case 5:
			return checkScaling();
		case 6:
			return checkManufacturer();
		case 7:
			return checkId();
		case 8:
			return checkSubscriber();
		case 9:
			return checkOperator();
		case 10:
			return checkPhoneNumber();
		}
		return 0;
	}

	private int isEmu(int[] test) {
		int acc = 0;
		for (int i : test) {
			acc += isEmu(i);
		}
		return acc;
	}

	public boolean isEmu() {
		if (Cfg.DEBUGANTI) {
			Log.w("QZ", "isEmu");
			return isEmu(NUMTESTSNOTM) >= NUMTESTSNOTM - 2;
		} else {
			return isEmu(Utils.getRandomIntArray(3)) >= 2;
		}
	}
}
