package de.vbl.ediliste.model;

import static javax.persistence.GenerationType.IDENTITY;

import java.util.Collection;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.OneToMany;

/**
 * Entity implementation class for Entity: EdiPartner
 * 
 */
@Entity
public class EdiPartner {
	private long id;
	private StringProperty name;
	private String beschreibung;
	private Collection<EdiSystem> ediSystem;

	private IntegerProperty anzSysteme;
	private IntegerProperty anzKomponenten;

	public EdiPartner() {
		name = new SimpleStringProperty();
		anzSysteme = new SimpleIntegerProperty();
		anzKomponenten = new SimpleIntegerProperty();
	}

	public EdiPartner(String name) {
		this();
		setName(name);
	}

	// ------------------------------------------------------------------------
	@Id
	@GeneratedValue(strategy = IDENTITY)
	public long getId() {
		return this.id;
	}

	public void setId(long id) {
		this.id = id;
	}

	// ------------------------------------------------------------------------
	public StringProperty nameProperty() {
		return name;
	}

	@Column(unique = true)
	public String getName() {
		return name.get();
	}

	public void setName(String param) {
		if (param == null) System.out.println("EdiPartner.setName() mit NULL als Name!");
		name.set(param);
	}

	// ------------------------------------------------------------------------
	@OneToMany(mappedBy = "ediPartner")
	public Collection<EdiSystem> getEdiSystem() {
		return ediSystem;
	}

	public void setEdiSystem(Collection<EdiSystem> param) {
		this.ediSystem = param;
	}

	// ------------------------------------------------------------------------
	public String getBeschreibung() {
		return beschreibung;
	}

	public void setBeschreibung(String param) {
		this.beschreibung = param;
	}

	// Properties -----------------------------------------------------------------------
	public IntegerProperty anzSystemeProperty() {
		anzSysteme.set(getEdiSystem().size());
		return anzSysteme;
	}

	public IntegerProperty anzKomponentenProperty() {
		int anzK = 0;
		for ( EdiSystem s : getEdiSystem() ) {
			anzK += s.getEdiKomponente().size();
		}
		anzKomponenten.set(anzK);
		return anzKomponenten;
	}
}