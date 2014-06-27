package de.vbl.ediliste.model;

import static javax.persistence.GenerationType.IDENTITY;
import static javax.persistence.TemporalType.DATE;

import java.io.Serializable;
import java.util.Collection;
import java.util.Date;

import javafx.beans.property.StringProperty;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.OneToMany;
import javax.persistence.Temporal;

/** 
 * Entity implementation class for Entity: Dokumentation
 * 
 */
@Entity 
public class DokuLink implements Serializable {

	private StringProperty name;
	private Long id;
	private static final long serialVersionUID = 1L;
	private String pfad;
	private Date bisDatum;
	private Integer revision;
	private Collection<Integration> integration;
	private Collection<SVN_Repository> sVN_Repository;
	public DokuLink() {
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

	@ManyToMany
	@JoinTable(joinColumns = @JoinColumn(name = "vorhaben_id", referencedColumnName = "id"))
	public Collection<Integration> getIntegration() {
	    return integration;
	}

	public void setIntegration(Collection<Integration> param) {
	    this.integration = param;
	} 

	@OneToMany
	@JoinColumn
	public Collection<SVN_Repository> getSVN_Repository() {
	    return sVN_Repository;
	}

	public void setSVN_Repository(Collection<SVN_Repository> param) {
	    this.sVN_Repository = param;
	}
}
