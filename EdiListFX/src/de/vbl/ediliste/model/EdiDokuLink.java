package de.vbl.ediliste.model;

import static javax.persistence.GenerationType.IDENTITY;

import java.io.Serializable;
import java.util.Collection;
import java.util.Date;

import javafx.beans.property.StringProperty;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToMany;
import javax.persistence.Temporal;
import static javax.persistence.TemporalType.DATE;

/**
 * Entity implementation class for Entity: Dokumentation
 * 
 */
@Entity
public class EdiDokuLink implements Serializable {

	private StringProperty name;
	private Long id;
	private static final long serialVersionUID = 1L;
	private Collection<EdiEintrag> ediEintrag;
	private String pfad;
	private Date bisDatum;
	private Integer revision;

	public EdiDokuLink() {
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

	@ManyToMany(mappedBy = "ediDokuLink")
	public Collection<EdiEintrag> getEdiEintrag() {
		return ediEintrag;
	}

	public void setEdiEintrag(Collection<EdiEintrag> param) {
		this.ediEintrag = param;
	}

	public String getPfad() {
		return pfad;
	}

	public void setPfad(String param) {
		this.pfad = param;
	}

	@Temporal(DATE)
	public Date getBisDatum() {
		return bisDatum;
	}

	public void setBisDatum(Date param) {
		this.bisDatum = param;
	}

	public Integer getRevision() {
		return revision;
	}

	public void setRevision(Integer param) {
		this.revision = param;
	}
}
