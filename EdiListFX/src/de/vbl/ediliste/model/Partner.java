package de.vbl.ediliste.model;

import static javax.persistence.GenerationType.IDENTITY;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

/**
 * Entity implementation class for Entity: Partner
 *
 */
@Entity

public class Partner {
	private StringProperty name = new SimpleStringProperty();

	private long id;

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
	
	public String getName() {
		return name.get();
	}

	public void setName(String param) {
		name.set(param);
	}

	// ------------------------------------------------------------------------
}
