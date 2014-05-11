package de.vbl.ediliste.model;

import static javax.persistence.GenerationType.IDENTITY;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

@Entity
public class EdiKomponente {
	private StringProperty name = new SimpleStringProperty();
	private StringProperty fullname = new SimpleStringProperty();
	private long id;
	private EdiSystem ediSystem;
	private String beschreibung;

	public EdiKomponente() {
	}

	public EdiKomponente(String name, EdiSystem system) {
		this.name.set(name);
		this.ediSystem = system;
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
		String fullName = ediSystem == null ? "-?-" : ediSystem.getFullname();
		return fullName + ASCIItoStr(42) + name.get();  // 151
	}
	
	private String ASCIItoStr(int a) {
		byte[] b = { (byte) a };
		String ret = new String(b);
		return " " + ret + " ";
	}

//	public String getBezName() {
//		String ret = "?";
//		if (ediSystem != null) {
//			if (ediSystem.getEdiPartner() != null) {
//				ret = ediSystem.getEdiPartner().getName();
//			}
//			ret += "-" + ediSystem.getName();
//		}
//		return ret + "-" + name.get();
//	}
	
	// - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - --
	@ManyToOne
	@JoinColumn(name = "ediSystem_id", referencedColumnName = "id")
	public EdiSystem getEdiSystem() {
		return ediSystem;
	}

	public void setEdiSystem(EdiSystem param) {
		this.ediSystem = param;
	}

	public String getSystemName() {
		return ediSystem.getName();
	}

	public String getPartnerName() {
		return ediSystem.getPartnerName();
	}

	public String getBeschreibung() {
		return beschreibung;
	}

	public void setBeschreibung(String param) {
		this.beschreibung = param;
	}
}
