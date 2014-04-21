package de.vbl.ediliste.model;

import static javax.persistence.GenerationType.IDENTITY;
import static javax.persistence.TemporalType.DATE;

import java.io.Serializable;
import java.util.Date;

import javafx.beans.property.StringProperty;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Temporal;
import de.vbl.ediliste.model.EdiAnbindung;
import java.util.Collection;
import javax.persistence.ManyToMany;
import javax.persistence.JoinTable;
import javax.persistence.JoinColumn;
import de.vbl.ediliste.model.SVNrepository;
import javax.persistence.OneToMany;

/** 
 * Entity implementation class for Entity: Dokumentation
 * 
 */
@Entity
public class EdiDokuLink implements Serializable {

	private StringProperty name;
	private Long id;
	private static final long serialVersionUID = 1L;
	private String pfad;
	private Date bisDatum;
	private Integer revision;
	private Collection<EdiAnbindung> ediAnbindung;
	private Collection<SVNrepository> sVNrepository;
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
	@JoinTable(joinColumns = @JoinColumn(name = "ediSzenario_id", referencedColumnName = "ID"))
	public Collection<EdiAnbindung> getEdiAnbindung() {
	    return ediAnbindung;
	}

	public void setEdiAnbindung(Collection<EdiAnbindung> param) {
	    this.ediAnbindung = param;
	}

	@OneToMany
	@JoinTable(name = "SVN_Repostitory", joinColumns = @JoinColumn(name = "EdiDokuLink_id", referencedColumnName = "ID"))
	public Collection<SVNrepository> getSVNrepository() {
	    return sVNrepository;
	}

	public void setSVNrepository(Collection<SVNrepository> param) {
	    this.sVNrepository = param;
	}
}
