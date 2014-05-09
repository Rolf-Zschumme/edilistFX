package de.vbl.ediliste.model;

import java.io.Serializable;
import java.lang.String;
import javax.persistence.*;
import static javax.persistence.GenerationType.IDENTITY;
import de.vbl.ediliste.model.Integration;
import java.util.Collection;

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
	@JoinTable(joinColumns = @JoinColumn(name = "ediSzenario_id", referencedColumnName = "ID"))
	public Collection<Integration> getEdiAnbindung() {
		return integration;
	}

	public void setEdiAnbindung(Collection<Integration> param) {
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
