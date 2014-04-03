package de.vbl.ediliste.model;

import static javax.persistence.GenerationType.IDENTITY;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import de.vbl.ediliste.model.System;
import java.util.Collection;
import javax.persistence.OneToMany;

/**
 * Entity implementation class for Entity: Partner
 *
 */
@Entity

public class Partner {
	private StringProperty name = new SimpleStringProperty();

	private Long id;

	private Collection<System> system;
	
	// ------------------------------------------------------------------------
	@Id
	@GeneratedValue(strategy = IDENTITY)	
	public Long getId() {
		return this.id;
	}

	public void setId(Long id) {
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

	@OneToMany(mappedBy = "partner")
	public Collection<System> getSystem() {
	    return system;
	}

	public void setSystem(Collection<System> param) {
	    this.system = param;
	}

	// ------------------------------------------------------------------------
}
