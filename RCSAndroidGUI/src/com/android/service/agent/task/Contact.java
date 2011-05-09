/* *******************************************
 * Copyright (c) 2011
 * HT srl,   All rights reserved.
 * Project      : RCS, RCSAndroid
 * File         : Contact.java
 * Created      : 6-mag-2011
 * Author		: zeno
 * *******************************************/

package com.android.service.agent.task;

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
	
	public String toString() {
		StringBuffer sb =new StringBuffer();
		
		sb.append( "User Id: " + userInfo.getUserId());
		sb.append("\nComplete Name: " + userInfo.getCompleteName());
		sb.append("\nNickname: " + userInfo.getUserNickname());
		sb.append("\nUserNote: " + userInfo.getUserNote() + "\n");
		
		sb.append( getInfo() );
		return sb.toString();
	}
	
	public String getInfo() {
		StringBuffer sb =new StringBuffer();
		
		// Phone Info
		ListIterator<PhoneInfo> pi = phoneInfo.listIterator();
		
		while (pi.hasNext()) {
			PhoneInfo pinfo = pi.next();
			
			sb.append("Phone: " + pinfo.getPhoneNumber());
			sb.append("\nPhone Type: " + pinfo.getPhoneType()+"\n");
		}
		
		// Email Info
		ListIterator<EmailInfo> e = emailInfo.listIterator();
		
		while (e.hasNext()) {
			EmailInfo einfo = e.next();
			
			sb.append("Email: " + einfo.getEmail());
			sb.append("\nType: " + einfo.getEmailType()+"\n");
		}
		
		// Postal Address Info
		ListIterator<PostalAddressInfo> pa = paInfo.listIterator();
		
		while (pa.hasNext()) {
			PostalAddressInfo painfo = pa.next();
			
			sb.append("State: " + painfo.getState());
			sb.append("\nCountry: " + painfo.getCountry());
			sb.append("\nCity: " + painfo.getCity());
			sb.append("\nStreet: " + painfo.getStreet());
			sb.append("\nPO Box: " + painfo.getPoBox());
			sb.append("\nZip: " + painfo.getPostalCode());
			sb.append("\nNeighbor: " + painfo.getNeighbor());
			sb.append("\nAddress Type: " + painfo.getType()+"\n");
		}

		
		// Im Info
		ListIterator<ImInfo> im = imInfo.listIterator();
		
		while (im.hasNext()) {
			ImInfo iminfo = im.next();
			
			sb.append("IM: " + iminfo.getIm());
			sb.append("\nIM Type: " + iminfo.getImType()+"\n");
		}
		
		// Organization Info
		ListIterator<OrganizationInfo> o = orgInfo.listIterator();
		
		while (o.hasNext()) {
			OrganizationInfo oinfo = o.next();
			
			sb.append("Company Name: " + oinfo.getCompanyName());
			sb.append("\nCompany Title: " + oinfo.getCompanyTitle());
			sb.append("\nCompany Type: " + oinfo.getType()+"\n");
		}
		
		// Website Info
		ListIterator<WebsiteInfo> w = webInfo.listIterator();
		
		while (w.hasNext()) {
			WebsiteInfo winfo = w.next();
			
			sb.append("Website: " + winfo.getWebsiteName()+"\n");
		}
		
		return sb.toString();
	}

	public long getId() {
		return getUserInfo().getUserId();
	}
}
