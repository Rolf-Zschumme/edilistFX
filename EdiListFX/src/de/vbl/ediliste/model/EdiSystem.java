package de.vbl.ediliste.model;

import static javax.persistence.GenerationType.IDENTITY;

import java.util.Collection;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;

@Entity
public class EdiSystem {
	private StringProperty name = new SimpleStringProperty();
	private StringProperty fullname = new SimpleStringProperty();

	private long id;
	private EdiPartner ediPartner;
	private Collection<EdiKomponente> ediKomponente;
	private IntegerProperty anzKomponenten = new SimpleIntegerProperty();
	private String beschreibung;

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
		return partnerName + ASCIItoStr(42) + name.get();   // 151
	}
	private String ASCIItoStr(int a) {
		byte[] b = { (byte) a };
		String ret = new String(b);
		return " " + ret + " ";
	}

	@ManyToOne
	@JoinColumn(referencedColumnName = "id")
	public EdiPartner getEdiPartner() {
		return ediPartner;
	}

	public void setEdiPartner(EdiPartner param) {
		this.ediPartner = param;
	}

	// - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - --
	@OneToMany(mappedBy = "ediSystem")
	public Collection<EdiKomponente> getEdiKomponente() {
		return ediKomponente;
	}

	public void setEdiKomponente(Collection<EdiKomponente> param) {
		this.ediKomponente = param;
	}

	// - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - --
	public IntegerProperty anzKomponentenProperty() {
		return anzKomponenten;
	}

	public Integer getAnzKomponenten() {
		return ediKomponente.size();
	}

	public String getPartnerName() {
		return ediPartner.getName();
	}

	public String getBeschreibung() {
		return beschreibung;
	}

	public void setBeschreibung(String param) {
		this.beschreibung = param;
	}
}
