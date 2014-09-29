package de.vbl.ediliste.model;

import static javax.persistence.GenerationType.TABLE;

import java.io.Serializable;

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
	private String nummer;
	private String nachname;
	private String vorname;
	private String abteilung;
	private String telefon;
	private String mail;
	private static final long serialVersionUID = 1L;
	public KontaktPerson() {
		super();
	}   
	@Id    
	@GeneratedValue(strategy = TABLE)
	public long getId() {
		return this.id;
	}

	public void setId(long id) {
		this.id = id;
	}   
	public String getIdStr() {
		return this.idStr;
	}
	public void setIdStr(String idStr) {
		this.idStr = idStr;
	}
	
	// ----------------------------------
	public String getNummer() {
		return nummer;
	}
	public void setNummer(String nummer) {
		this.nummer = nummer;
	}
	
	// ----------------------------------
	public String getNachname() {
		return this.nachname;
	}
	public void setNachname(String name) {
		this.nachname = name;
	}
	
	// ----------------------------------
	public String getVorname() {
		return vorname;
	}
	public void setVorname(String vorname) {
		this.vorname = vorname;
	}
	
	// ----------------------------------
	public String getAbteilung() {
		return abteilung;
	}
	public void setAbteilung(String abteilung) {
		this.abteilung = abteilung;
	}

	// ----------------------------------
	public String getTelefon() {
		return telefon;
	}
	public void setTelefon(String telefon) {
		this.telefon = telefon;
	}

	// ----------------------------------
	public String getMail() {
		return mail;
	}
	public void setMail(String mail) {
		this.mail = mail;
	}
}
