package de.vbl.im.model;

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
import javax.persistence.ManyToMany;
import javax.persistence.OneToMany;
import javax.persistence.Transient;


/**
 * Entity implementation class for Entity: InPartner
 * 
 */
@Entity
public class InPartner {
	private long id;
	private StringProperty name;
	private String beschreibung;
//	private Collection<InSystem> inSystem;
	private ObservableList<InSystem> inSystem;
	private Collection<Ansprechpartner> ansprechpartner;
	

	private IntegerProperty anzSysteme;
	private IntegerProperty anzKomponenten;

	public InPartner() {
		name = new SimpleStringProperty();
		anzSysteme = new SimpleIntegerProperty();
		anzKomponenten = new SimpleIntegerProperty();
	}

	public InPartner(String name) {
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
//	@OneToMany(mappedBy = "inPartner")
//	public Collection<InSystem> getInSystem() {
//		return inSystem;
//	}
//
//	public void setInSystem(Collection<InSystem> param) {
//		this.inSystem = param;
//	}
	
	
	@OneToMany(mappedBy = "inPartner")
	public Collection<InSystem> getInSystem() {
		return inSystem;
	}

	public void setInSystem(Collection<InSystem> systems) {
		if (inSystem != null) {
			anzSysteme.unbind();
			anzKomponenten.unbind();
		}
		this.inSystem = FXCollections.observableArrayList(systems);
		anzSysteme.bind(Bindings.size(inSystem));
		
//		ObservableIntegerArray array = FXCollections.observableIntegerArray(); 
//		for ( InSystem s : getInSystem() ) {
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
		for ( InSystem s : getInSystem() ) {
//			anzK += s.getInKomponente().size();
			anzK += s.anzKomponentenProperty().get();
		}
		anzKomponenten.set(anzK);
		return anzKomponenten;
	}
	// - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - --
	@ManyToMany
	public Collection<Ansprechpartner> getAnsprechpartner() {
	    return ansprechpartner;
	}

	public void setAnsprechpartner(Collection<Ansprechpartner> param) {
	    this.ansprechpartner = param;
	}
	
}