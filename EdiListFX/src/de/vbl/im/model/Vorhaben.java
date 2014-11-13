package de.vbl.im.model;

import static javax.persistence.GenerationType.IDENTITY;

import java.io.Serializable;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

/**
 * Entity implementation class for Entity: Vorhaben
 * 
 */
@Entity
public class Vorhaben implements Serializable {

	private long id;
	private String nummer;
	private String name;
	private static final long serialVersionUID = 1L;
	public Vorhaben() {
		super();
	}

	@Id
	@GeneratedValue(strategy = IDENTITY)
	public long getId() {
		return this.id;
	}

	public void setId(long id) {
		this.id = id;
	}

	/* ---------------------------------------------------------------------- */
	public String getNummer() {
		return nummer;
	}

	public void setNummer(String param) {
		this.nummer = param;
	}

	/* ---------------------------------------------------------------------- */
	public String getName() {
		return this.name;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
}
