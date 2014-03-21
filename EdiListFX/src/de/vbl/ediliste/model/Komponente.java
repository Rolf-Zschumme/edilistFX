package de.vbl.ediliste.model;

import static javax.persistence.GenerationType.IDENTITY;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import de.vbl.ediliste.model.System;
import javax.persistence.ManyToOne;

import javax.persistence.JoinColumn;


@Entity
public class Komponente {
	private long id;
	private System system;
	private StringProperty name = new SimpleStringProperty();

	public StringProperty nameProperty() {
		return name;
	}

	@Id
	@GeneratedValue(strategy = IDENTITY)
	public long getId() {
		return id;
	}
	
	public void setId(long id) {
		this.id = id;
	}
	
	public String getName() {
		return name.get();
	}

	public void setName(String param) {
		name.set(param);
	}

	@ManyToOne
	@JoinColumn(name = "id", referencedColumnName = "id")
	public System getSystem() {
	    return system;
	}

	public void setSystem(System param) {
	    this.system = param;
	}
	
}
