package de.vbl.ediliste.model;

import java.io.Serializable;
import java.lang.Long;

import javafx.beans.property.StringProperty;

import javax.persistence.*;
import de.vbl.ediliste.model.EdiEintrag;
import de.vbl.ediliste.model.Dokument;

/**
 * Entity implementation class for Entity: EdiEintragDokument
 *
 */
@Entity
public class EdiEintragDokument implements Serializable {

	private static final long serialVersionUID = 1L;
	private Long id;
	private StringProperty name;
	private EdiEintrag ediEintrag;
	private Dokument dokument;

	public EdiEintragDokument() {
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

	@ManyToOne
	public EdiEintrag getEdiEintrag() {
	    return ediEintrag;
	}

	public void setEdiEintrag(EdiEintrag param) {
	    this.ediEintrag = param;
	}

	@ManyToOne
	public Dokument getDokument() {
	    return dokument;
	}

	public void setDokument(Dokument param) {
	    this.dokument = param;
	}
   
}
