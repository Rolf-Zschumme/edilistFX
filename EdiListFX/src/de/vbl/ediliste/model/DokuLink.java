package de.vbl.ediliste.model;

import static javax.persistence.GenerationType.IDENTITY;

import java.io.Serializable;
import java.util.Date;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Temporal;
import static javax.persistence.TemporalType.TIMESTAMP;

/**
 * Entity implementation class for Entity: Dokumentation
 *  
 */

@Entity
public class DokuLink implements Serializable {
	private static final long serialVersionUID = 1L;
	public static enum DokuStatus { OHNE_VORHABEN, NUR_VORHABEN, ABGENOMMEN, ALT_VORHABEN, NEU_VORHABEN };
	
	private StringProperty name;
	private Long id;
	private StringProperty pfad;
	private Date datum;
	private long revision;
	private Repository repository;
	private StringProperty vorhaben;
	private DokuStatus status; 


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
		if (name==null) {
			name = new SimpleStringProperty(param);
		} 
		else {
			name.set(param);
		}	
	}

	// ------------------------------------------------------------------------
	public StringProperty pfadProperty() {
		return pfad;
	}

	public String getPfad() {
		return pfad.get();
	}

	public void setPfad(String param) {
		if (pfad==null) {
			pfad = new SimpleStringProperty(param);
		}
		else {
			pfad.set(param);
		}
	}

	// ------------------------------------------------------------------------
	public StringProperty vorhabenProperty() {
		return vorhaben;
	}

	public String getVorhaben() {
		return vorhaben.get();
	}

	public void setVorhaben(String param) {
		if (vorhaben==null) {
			vorhaben = new SimpleStringProperty(param);
		}
		else {
			vorhaben.set(param);
		}
	}

	// ------------------------------------------------------------------------
	@Temporal(TIMESTAMP) 
	public Date getDatum() {
		return datum;
	}

	public void setDatum(Date param) {
		this.datum = param;
	}

	public long getRevision() {
		return revision;
	}

	public void setRevision(long param) {
		this.revision = param;
	}

	@ManyToOne
	public Repository getRepository() {
	    return repository;
	}

	public void setRepository(Repository param) {
	    this.repository = param;
	}

	public DokuStatus getStatus() {
		return status;
	}

	public void setStatus(DokuStatus status) {
		this.status = status;
	}
}
