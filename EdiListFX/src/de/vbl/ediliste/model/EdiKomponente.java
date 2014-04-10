package de.vbl.ediliste.model;

import static javax.persistence.GenerationType.IDENTITY;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;

@Entity
public class EdiKomponente {
	private StringProperty name = new SimpleStringProperty();
	private StringProperty fullname = new SimpleStringProperty();
	private long id;
	private EdiSystem ediSystem;
	
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
	
//	public String getFullname() {
//		String fullName = ediSystem == null ? "-?-" : ediSystem.getFullname();
//		return fullName + "  " + name.get();
//	}

	@ManyToOne
	public EdiSystem getSystem() {
	    return ediSystem;
	}

	public void setSystem(EdiSystem param) {
	    this.ediSystem = param;
	}
}

