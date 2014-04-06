package de.vbl.ediliste.model;

import static javax.persistence.GenerationType.IDENTITY;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
 
@Entity 
public class System {
	private StringProperty name = new SimpleStringProperty();
	private StringProperty fullname = new SimpleStringProperty();
	
	private long id;
	private Partner partner;

	// ------------------------------------------------------------------------
	@Id
	@GeneratedValue(strategy = IDENTITY)
	public long getId() {
		return id;
	}
	public void setId(long id) {
		this.id = id;
	}

	// ------------------------------------------------------------------------
	public StringProperty nameProperty() {
		return name;
	}
	
	public String getName() {
		return name.get();
	}

	public void setName(String param) {
		name.set(param);
	}

	// - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - --
	public StringProperty fullnameProperty() {
		return fullname;
	}
	
	public String getFullname() {
		String partnerName = partner == null ? "-?-" : partner.getName();
		return partnerName + "  " + name.get();
	}
	@ManyToOne
	public Partner getPartner() {
	    return partner;
	}
	public void setPartner(Partner param) {
	    this.partner = param;
	}



}
