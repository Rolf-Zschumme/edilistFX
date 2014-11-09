package de.vbl.im.model;

import static javax.persistence.GenerationType.TABLE;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Collection;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Transient;

/**
 * Entity implementation class for Entity: KontaktPerson
 *
 */
@Entity 

public class KontaktPerson implements Serializable {
	
	private long id;
	private StringProperty nummer;
	private StringProperty nachname;
	private StringProperty vorname;
	private StringProperty art;       // F=Fachlich T=Fechnisch oder Blank
	private StringProperty abteilung;
	private StringProperty telefon;
	private StringProperty mail;
	private static final long serialVersionUID = 1L;

	public KontaktPerson() {
		super();
		nummer    = new SimpleStringProperty();
		nachname  = new SimpleStringProperty();
		vorname   = new SimpleStringProperty();
		art       = new SimpleStringProperty();
		abteilung = new SimpleStringProperty();
		telefon   = new SimpleStringProperty();
		mail      = new SimpleStringProperty();
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
	public StringProperty artProperty() {
		return art;
	}
	@Deprecated
	@Column(length = 1, columnDefinition = "F=Fachlich T=Technisch oder ' '")
	public String getArt() {
		return art.get();
	}
	public String getArtSafe() {
		return art.getValueSafe();
	}
	public void setArt(String abteilung) {
		this.art.set (abteilung==null || abteilung.length() == 0 ? " ": abteilung.substring(0, 1));
	}

	public static Collection<String> valuesOfArt = Arrays.asList(" ","Fachlich","Technisch"); 

	public String getArtLong() {
		char art = this.getArtSafe().charAt(0); 
		for (String value : valuesOfArt) {
			if (value.charAt(0) == art) return value; 
		}
		return "???";
	}
	// ----------------------------------
	public StringProperty abteilungProperty() {
		return abteilung;
	}
	@Deprecated
	public String getAbteilung() {
		return abteilung.get();
	}
	public String getAbteilungSafe() {
		return abteilung.getValueSafe();
	}
	public void setAbteilung(String abteilung) {
		this.abteilung.set(abteilung);
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
	
	/* ------------------------------------------------------------------------
	/ return: <Art:> Vorname Nachname <(Nummer | Firma )> zurück
	 * "F: Hans Meier (Post)"
	 * "T: Günther Test (P01234)"
	 * "   Gerlinde Oberfeld" 
	 * ----------------------------------------------------------------------*/
	@Transient
	public String getArtVornameNachnameFirma() {
		String prefix = art.getValueSafe();
		if (prefix.equals(" ")) {
			prefix = "   "; 
		} else {
			prefix += ": ";
		}
		String ret = prefix + vorname.getValueSafe() + " " + nachname.getValueSafe();
		String suffix = nummer.getValueSafe();
		if(suffix.equals("")) {
			suffix = abteilung.getValueSafe();
		}
		if (suffix.equals("") == false) {
			ret += " (" + suffix + ")";
		}
		return ret;
	}
}
