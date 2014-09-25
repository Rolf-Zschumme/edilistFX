package de.vbl.ediliste.model;

import static javax.persistence.GenerationType.IDENTITY;

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
import javax.persistence.Transient;


/**
 * Entity implementation class for Entity: EdiPartner
 * 
 */
@Entity
public class EdiPartner {
	private long id;
	private StringProperty name;
	private String beschreibung;
//	private Collection<EdiSystem> ediSystem;
	private ObservableList<EdiSystem> ediSystem;

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
		name.set(param);
	}

//	// ------------------------------------------------------------------------
//	@OneToMany(mappedBy = "ediPartner")
//	public Collection<EdiSystem> getEdiSystem() {
//		return ediSystem;
//	}
//
//	public void setEdiSystem(Collection<EdiSystem> param) {
//		this.ediSystem = param;
//	}
	
	
	@OneToMany(mappedBy = "ediPartner")
	public Collection<EdiSystem> getEdiSystem() {
		return ediSystem;
	}

	public void setEdiSystem(Collection<EdiSystem> systems) {
		if (ediSystem != null) {
			anzSysteme.unbind();
			anzKomponenten.unbind();
		}
		this.ediSystem = FXCollections.observableArrayList(systems);
		anzSysteme.bind(Bindings.size(ediSystem));
		
//		ObservableIntegerArray array = FXCollections.observableIntegerArray(); 
//		for ( EdiSystem s : getEdiSystem() ) {
// 			IntegerProperty tmp = new SimpleIntegerProperty();
//			tmp.bind(anzKomponenten);
//			anzKomponenten.bind(Bindings.add(tmp, s.anzKomponentenProperty()));
//		}
	}

	// ------------------------------------------------------------------------
	public String getBeschreibung() {
		return beschreibung;
	}

	public void setBeschreibung(String param) {
		this.beschreibung = param;
	}

	// ------------------------------------------------------------------------
	@Transient
	public IntegerProperty anzSystemeProperty() {
		return anzSysteme;
	}

	// ------------------------------------------------------------------------
	@Transient
	public IntegerProperty anzKomponentenProperty() {
		int anzK = 0;
		for ( EdiSystem s : getEdiSystem() ) {
//			anzK += s.getEdiKomponente().size();
			anzK += s.anzKomponentenProperty().get();
		}
		anzKomponenten.set(anzK);
		return anzKomponenten;
	}
}