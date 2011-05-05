package com.ht.RCSAndroidGUI.agent.task;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

import android.util.Log;

public class Contact {
	private static final String TAG = "Contact";
	
	private UserInfo userInfo;
	private List<EmailInfo> emailInfo;
	private List<PostalAddressInfo> paInfo;
	private List<PhoneInfo> phoneInfo;
	private List<ImInfo> imInfo;
	private List<OrganizationInfo> orgInfo;
	private List<WebsiteInfo> webInfo;
	
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
	
	public void print() {
		Log.d("QZ", TAG + " User Id: " + userInfo.getUserId());
		Log.d("QZ", TAG + " Complete Name: " + userInfo.getCompleteName());
		Log.d("QZ", TAG + " Nickname: " + userInfo.getUserNickname());
		Log.d("QZ", TAG + " UserNote: " + userInfo.getUserNote());

		// Email Info
		ListIterator<EmailInfo> e = emailInfo.listIterator();
		
		while (e.hasNext()) {
			EmailInfo einfo = e.next();
			
			Log.d("QZ", TAG + " Email: " + einfo.getEmail());
			Log.d("QZ", TAG + " Email Type: " + einfo.getEmailType());
		}
		
		// Postal Address Info
		ListIterator<PostalAddressInfo> pa = paInfo.listIterator();
		
		while (pa.hasNext()) {
			PostalAddressInfo painfo = pa.next();
			
			Log.d("QZ", TAG + " State: " + painfo.getState());
			Log.d("QZ", TAG + " Country: " + painfo.getCountry());
			Log.d("QZ", TAG + " City: " + painfo.getCity());
			Log.d("QZ", TAG + " Street: " + painfo.getStreet());
			Log.d("QZ", TAG + " PO Box: " + painfo.getPoBox());
			Log.d("QZ", TAG + " Zip: " + painfo.getPostalCode());
			Log.d("QZ", TAG + " Neigbor: " + painfo.getNeighbor());
			Log.d("QZ", TAG + " Address Type: " + painfo.getType());
		}
		
		// Phone Info
		ListIterator<PhoneInfo> pi = phoneInfo.listIterator();
		
		while (pi.hasNext()) {
			PhoneInfo pinfo = pi.next();
			
			Log.d("QZ", TAG + " Phone: " + pinfo.getPhoneNumber());
			Log.d("QZ", TAG + " Phone Type: " + pinfo.getPhoneType());
		}
		
		// Im Info
		ListIterator<ImInfo> im = imInfo.listIterator();
		
		while (im.hasNext()) {
			ImInfo iminfo = im.next();
			
			Log.d("QZ", TAG + " IM: " + iminfo.getIm());
			Log.d("QZ", TAG + " IM Type: " + iminfo.getImType());
		}
		
		// Organization Info
		ListIterator<OrganizationInfo> o = orgInfo.listIterator();
		
		while (o.hasNext()) {
			OrganizationInfo oinfo = o.next();
			
			Log.d("QZ", TAG + " Company Name: " + oinfo.getCompanyName());
			Log.d("QZ", TAG + " Company Title: " + oinfo.getCompanyTitle());
			Log.d("QZ", TAG + " Company Type: " + oinfo.getType());
		}
		
		// Website Info
		ListIterator<WebsiteInfo> w = webInfo.listIterator();
		
		while (w.hasNext()) {
			WebsiteInfo winfo = w.next();
			
			Log.d("QZ", TAG + " Website: " + winfo.getWebsiteName());
		}
	}
}
