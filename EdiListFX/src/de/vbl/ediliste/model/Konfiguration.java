package de.vbl.ediliste.model;

import static javax.persistence.GenerationType.IDENTITY;

import java.util.ArrayList;
import java.util.Collection;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;

/**
 * Entity implementation class for Entity: Szenario
 * 
 */ 
@Entity
public class Konfiguration {
	private StringProperty name;
	private long id;
	private Integration integration;
	private Collection<EdiEintrag> ediEintrag;
	private String beschreibung;
	
	private StringProperty integrationName;

	public Konfiguration() {
		name = new SimpleStringProperty();
		integrationName = new SimpleStringProperty("");
	}
	
	public Konfiguration(String name) {
		this();
		this.setName(name);
		ediEintrag = new ArrayList<EdiEintrag>();
	}

	// ------------------------------------------------------------------------
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
	@ManyToOne
	@JoinColumn(referencedColumnName = "id")
	public Integration getIntegration() {
		return integration;
	}

	public void setIntegration(Integration param) {
		
		this.integration = param;
		if (param != null) {
			this.integrationName.unbind();
			this.integrationName.bind(param.nameProperty());
		}
	}
 
	// ------------------------------------------------------------------------
	@OneToMany(mappedBy = "konfiguration")
	public Collection<EdiEintrag> getEdiEintrag() {
		return ediEintrag;
	}

	public void setEdiEintrag(Collection<EdiEintrag> ediEintrag) {
		this.ediEintrag = ediEintrag;
	}
	
//	public void addEdiEintrag(EdiEintrag e) {
//		this.ediEintrag.add(e);
//	}
//	
//	public void removeEdiEintrag(EdiEintrag e) {
//		this.ediEintrag.remove(e);
//	}
	

	// ------------------------------------------------------------------------
	public String getBeschreibung() { 
		return beschreibung;
	}
	
	public void setBeschreibung(String param) {
		this.beschreibung = param;
	}

	public StringProperty integrationNameProperty () {
		return integrationName;
	}
	
}
