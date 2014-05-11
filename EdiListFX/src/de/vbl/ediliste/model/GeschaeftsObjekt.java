package de.vbl.ediliste.model;

import static javax.persistence.GenerationType.IDENTITY;

import java.io.Serializable;
import java.util.Collection;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.OneToMany;

/**
 * Entity implementation class for Entity: GeschaeftsObjekt
 *
 */
@Entity

public class GeschaeftsObjekt implements Serializable {

	
	private long id;
	private String name;
	private static final long serialVersionUID = 1L;
	private Collection<EdiEmpfaenger> ediEmpfaenger;
	private IntegerProperty anzVerwendungen = new SimpleIntegerProperty();

	public GeschaeftsObjekt() {
		super();
	}   
	public GeschaeftsObjekt(String name) {
		this.name = name;
	}
	@Id    
	@GeneratedValue(strategy = IDENTITY)
	public long getId() {
		return this.id;
	}

	public void setId(long id) {
		this.id = id;
	}   
	public String getName() {
		return this.name;
	}

	public void setName(String name) {
		this.name = name;
	}
	@OneToMany(mappedBy = "geschaeftsObjekt")
	public Collection<EdiEmpfaenger> getEdiEmpfaenger() {
	    return ediEmpfaenger;
	}
	public void setEdiEmpfaenger(Collection<EdiEmpfaenger> param) {
	    this.ediEmpfaenger = param;
	}
   
	public IntegerProperty anzVerwendungenProperty () {
		anzVerwendungen.set(ediEmpfaenger.size());
		return anzVerwendungen;
	}
}
