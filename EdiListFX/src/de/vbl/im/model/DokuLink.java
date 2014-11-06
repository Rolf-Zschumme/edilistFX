package de.vbl.im.model;

import static javax.persistence.GenerationType.IDENTITY;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

import javafx.beans.property.LongProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleLongProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Transient;
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
	
	private long id;
	private StringProperty name;
	private StringProperty pfad;
	private ObjectProperty<LocalDateTime> datumProp;
	private Date datum;
	private LongProperty revision;
	private Repository repository;
	private StringProperty vorhaben;
	private DokuStatus status; 

	public DokuLink() {
		vorhaben = new SimpleStringProperty();
		name = new SimpleStringProperty();
		pfad = new SimpleStringProperty();
		revision = new SimpleLongProperty();
		datumProp = new SimpleObjectProperty<LocalDateTime>();
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

	// ------------------------------------------------------------------------
	public StringProperty pfadProperty() {
		return pfad;
	}

	public String getPfad() {
		return pfad.get();
	}

	public void setPfad(String param) {
		pfad.set(param);
	}

	// ------------------------------------------------------------------------
	public StringProperty vorhabenProperty() {
		return vorhaben;
	}

	public String getVorhaben() {
		return vorhaben.get();
	}

	public void setVorhaben(String param) {
		vorhaben.set(param);
	}

	// ------------------------------------------------------------------------
	@Transient
	public ObjectProperty<LocalDateTime> datumProperty() {
		return datumProp;
	}
	
	@Temporal(TIMESTAMP)
	public Date getDatum() {
		return datum;
	}

	public void setDatum(Date param) {
		if (param != null) {
			LocalDateTime ld = LocalDateTime.ofInstant(param.toInstant(), ZoneId.systemDefault());
//			if (datumProp == null) {
//				datumProp = new SimpleObjectProperty<LocalDateTime>(ld);
//			} else {
			    datumProp.set(ld);
//			}
		}
		datum = param;
	}

	// ------------------------------------------------------------------------
	@Transient
	public LongProperty revisionProperty() {
		return revision;
	}
	public long getRevision() {
		return revision.get();
	}

	public void setRevision(long param) {
		revision.set(param);
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
