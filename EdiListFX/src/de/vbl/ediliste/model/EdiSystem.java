package de.vbl.ediliste.model;

import static javax.persistence.GenerationType.IDENTITY;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
 
@Entity 
public class EdiSystem {
	private StringProperty name = new SimpleStringProperty();
	private StringProperty fullname = new SimpleStringProperty();
	
	private long id;
	private EdiPartner ediPartner;
	
	public EdiSystem() {
		super();
	}
	public EdiSystem(String name, EdiPartner ediPartner) {
		super();
		this.name.setValue(name);
		this.ediPartner = ediPartner;
	}
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
		String partnerName = ediPartner == null ? "-?-" : ediPartner.getName();
		return partnerName + "  " + name.get();
	}
	@ManyToOne
	public EdiPartner getPartner() {
	    return ediPartner;
	}
	public void setPartner(EdiPartner param) {
	    this.ediPartner = param;
	}



}
