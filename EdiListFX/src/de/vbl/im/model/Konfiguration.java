package de.vbl.im.model;

import static javax.persistence.AccessType.PROPERTY;

import java.util.Set;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

import javax.persistence.Access;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Transient;
import de.vbl.im.model.Iszenario;
import javax.persistence.ManyToOne;
import javax.persistence.JoinColumn;

/**
 * Entity implementation class for Entity: Szenario
 * 
 */ 
@Entity
@Access(PROPERTY)
public class Konfiguration {
	private long id;
	private StringProperty name;
	private String beschreibung;
	@Transient
	private StringProperty iSzenarioName;
	private Set<Integration> integration;
	private Iszenario iszenario;
	public Konfiguration() {
		name = new SimpleStringProperty();
		iSzenarioName = new SimpleStringProperty("");
	}
	
	public Konfiguration(String name) {
		this();
		this.setName(name);
//		integration = new ArrayList<Integration>();
	}

	// ------------------------------------------------------------------------
	@Id 
	@GeneratedValue
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

	
	
//	public void addIntegration(Integration e) {
//		this.integration.add(e);
//	}
//	
//	public void removeIntegration(Integration e) {
//		this.integration.remove(e);
//	}
	

	// ------------------------------------------------------------------------
	public String getBeschreibung() { 
		return beschreibung;
	}
	
	public void setBeschreibung(String param) {
		this.beschreibung = param;
	}

	public StringProperty iSzenarioNameProperty () {
		return iSzenarioName;
	}

	@OneToMany(mappedBy = "konfiguration")
	public Set<Integration> getIntegration() {
	    return integration;
	}

	public void setIntegration(Set<Integration> param) {
	    this.integration = param;
	}

	@ManyToOne
	@JoinColumn(name = "iszenario_id", referencedColumnName = "ID")
	public Iszenario getIszenario() {
	    return iszenario;
	}

	public void setIszenario(Iszenario param) {
	    this.iszenario = param;
	}

}
