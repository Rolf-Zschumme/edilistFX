package de.vbl.im.model;

import java.io.Serializable;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

import static javax.persistence.GenerationType.IDENTITY;


/**
 * Entity implementation class for Entity: Intervall
 *
 */
@Entity

public class Intervall implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = -7384250256056710054L;
	private long id;
	private StringProperty name;

	// ========================================================================
	public Intervall() {
		name = new SimpleStringProperty("");
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
		return name.getValueSafe();
	}

	public void setName(String name) {
		this.name.setValue(name);
	}
   
}
