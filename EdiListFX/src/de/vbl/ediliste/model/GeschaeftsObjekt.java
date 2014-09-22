package de.vbl.ediliste.model;

import static javax.persistence.GenerationType.IDENTITY;

import java.io.Serializable;
import java.util.Collection;

import javafx.beans.binding.Bindings;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import javax.persistence.Column;
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
	/**
	 * 
	 */
	private static final long serialVersionUID = 6212861741600895810L;
	private long id;
	private StringProperty name;
	private String beschreibung;
	private ObservableList<EdiEmpfaenger> ediEmpfaenger;
	private IntegerProperty anzVerwendungen;
	
	public GeschaeftsObjekt() {
		name = new SimpleStringProperty();
		anzVerwendungen = new SimpleIntegerProperty();
	}   
	public GeschaeftsObjekt(String name) {
		this();
		this.name.set(name);
	}
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
	
	@Column(unique = true, nullable = false)
	public String getName() {
		return this.name.get();
	}

	public void setName(String name) {
		this.name.set(name);
	}
	
	// ------------------------------------------------------------------------
	public String getBeschreibung() { 
		return beschreibung;
	}

	public void setBeschreibung(String param) {
		this.beschreibung = param;
	}
	
	@OneToMany(mappedBy = "geschaeftsObjekt")
	public Collection<EdiEmpfaenger> getEdiEmpfaenger() {
	    return ediEmpfaenger;
	}
	public void setEdiEmpfaenger(Collection<EdiEmpfaenger> param) {
		anzVerwendungen.unbind();
		ediEmpfaenger = FXCollections.observableArrayList(param);
		anzVerwendungen.bind(Bindings.size(ediEmpfaenger));
	}
	
	public IntegerProperty anzVerwendungenProperty () {
		return anzVerwendungen;
	}
}
