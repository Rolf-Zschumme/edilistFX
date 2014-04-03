package de.vbl.ediliste.model;

import java.io.Serializable;
import java.lang.Long;

import javafx.beans.property.StringProperty;

import javax.persistence.*;
import de.vbl.ediliste.model.EdiEintrag;
import java.util.Collection;

/**
 * Entity implementation class for Entity: Dokumentation
 *
 */
@Entity
public class Dokument implements Serializable {

	private Long id;
	private StringProperty name;
	private static final long serialVersionUID = 1L;
	private Collection<EdiEintrag> ediEintrag;

	public Dokument() {
		super();
	}   

	@Id
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

	@ManyToMany(mappedBy = "dokument")
	public Collection<EdiEintrag> getEdiEintrag() {
	    return ediEintrag;
	}

	public void setEdiEintrag(Collection<EdiEintrag> param) {
	    this.ediEintrag = param;
	}
}
