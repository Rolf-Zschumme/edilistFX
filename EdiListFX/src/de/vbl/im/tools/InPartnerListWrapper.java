package de.vbl.im.tools;

import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import de.vbl.im.model.InPartner;

@XmlRootElement(name = "partnerlist")
public class InPartnerListWrapper {

	private List<InPartner> inPartnerList;

	
	@XmlElement(name = "partner")
	public List<InPartner> getInPartnerList() {
		return inPartnerList;
	}

	public void setInPartnerList(List<InPartner> inPartnerList) {
		System.out.println("set");
		this.inPartnerList = inPartnerList;
	}
}
