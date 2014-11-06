package de.vbl.im.model;

import static javax.persistence.GenerationType.TABLE;

import java.io.Serializable;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

/**
 * Entity implementation class for Entity: KontaktPerson
 *
 */
@Entity 

public class KontaktPerson implements Serializable {
	
	private long id;
	private String idStr;
	private StringProperty nummer;
	private StringProperty nachname;
	private StringProperty vorname;
	private StringProperty abteilung;
	private StringProperty telefon;
	private StringProperty mail;
	private static final long serialVersionUID = 1L;
	public KontaktPerson() {
		super();
		nummer = new SimpleStringProperty();
		nachname = new SimpleStringProperty();
		vorname = new SimpleStringProperty();
		abteilung = new SimpleStringProperty();
		telefon = new SimpleStringProperty();
		mail = new SimpleStringProperty();
	}
	
	@Id    
	@GeneratedValue(strategy = TABLE)
	public long getId() {
		return this.id;
	}
	public void setId(long id) {
		this.id = id;
	}   
	// ----------------------------------
	public String getIdStr() {
		return this.idStr;
	}
	public void setIdStr(String idStr) {
		this.idStr = idStr;
	}
	
	// ----------------------------------
	public StringProperty nummerProperty() {
		return nummer;
	}
	public String getNummer() {
		return nummer.get();
	}
	public void setNummer(String nummer) {
		this.nummer.set(nummer);
	}
	
	// ----------------------------------
	public StringProperty nachnameProperty() {
		return nachname;
	}
	public String getNachname() {
		return this.nachname.get();
	}
	public void setNachname(String name) {
		this.nachname.set(name);
	}
	
	// ----------------------------------
	public StringProperty vornameProperty() {
		return vorname;
	}
	public String getVorname() {
		return vorname.get();
	}
	public void setVorname(String vorname) {
		this.vorname.set(vorname);
	}
	
	// ----------------------------------
	public StringProperty abteilungProperty() {
		return abteilung;
	}
	@Deprecated
	public String getAbteilung() {
		return abteilung.get();
	}
	public void setAbteilung(String abteilung) {
		this.abteilung.set(abteilung);
	}
	public String getAbteilungSafe() {
		return abteilung.getValueSafe();
	}

	// ----------------------------------
	public StringProperty telefonProperty() {
		return telefon;
	}
	public String getTelefon() {
		return telefon.get();
	}
	public void setTelefon(String telefon) {
		this.telefon.set(telefon);
	}

	// ----------------------------------
	public StringProperty mailProperty() {
		return mail;
	}
	public String getMail() {
		return mail.get();
	}
	public void setMail(String mail) {
		this.mail.set(mail);
	}
}
