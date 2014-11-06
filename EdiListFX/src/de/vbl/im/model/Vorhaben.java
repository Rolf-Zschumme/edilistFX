package de.vbl.im.model;

import static javax.persistence.GenerationType.IDENTITY;

import java.io.Serializable;
import java.util.Collection;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;

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
	private Collection<Integration> integration;

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

	/* ====================================================================== */
	@ManyToMany
	@JoinTable(joinColumns = @JoinColumn(name = "vorhaben_id", referencedColumnName = "id"))
	public Collection<Integration> getIntegration() {
		return integration;
	}

	public void setIntegration(Collection<Integration> param) {
		this.integration = param;
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
