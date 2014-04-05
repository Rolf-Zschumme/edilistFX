package de.vbl.ediliste.model;

import java.io.Serializable;
import java.util.Collection;

import javafx.beans.property.StringProperty;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import de.vbl.ediliste.model.Anbindung;
import javax.persistence.ManyToOne;
import javax.persistence.GeneratedValue;
import static javax.persistence.GenerationType.IDENTITY;

/**
 * Entity implementation class for Entity: Szenario
 *
 */
@Entity
public class Szenario implements Serializable {

	private long id;
	private StringProperty name;
	private static final long serialVersionUID = 1L;
	private Collection<EdiEintrag> ediEintrag;
	private Anbindung anbindung;
	public Szenario() {
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

	@OneToMany(mappedBy = "szenario")
	public Collection<EdiEintrag> getEdiEintrag() {
	    return ediEintrag;
	}

	public void setEdiEintrag(Collection<EdiEintrag> param) {
	    this.ediEintrag = param;
	}

	@ManyToOne
	public Anbindung getAnbindung() {
	    return anbindung;
	}

	public void setAnbindung(Anbindung param) {
	    this.anbindung = param;
	}
   
}
