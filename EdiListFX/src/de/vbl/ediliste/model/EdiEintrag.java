package de.vbl.ediliste.model;

import static javax.persistence.GenerationType.IDENTITY;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.OneToOne;
import javax.persistence.PrimaryKeyJoinColumn;
import de.vbl.ediliste.model.Szenario;
import javax.persistence.ManyToOne;


@Entity
public class EdiEintrag {
	private long id;
	private Komponente senderKomponente;
	private IntegerProperty ediNr = new SimpleIntegerProperty();
	private StringProperty kurzBez = new SimpleStringProperty();
	private StringProperty senderName = new SimpleStringProperty();
	private Szenario szenario;

	// ------------------------------------------------------------------------
	@Id
	@GeneratedValue(strategy = IDENTITY)
	public long getId() {
		return id;
	}
	public void setId(long id) {
		this.id = id;
	}

	// ------------------------------------------------------------------------
	public IntegerProperty ediNrProperty() {
		return ediNr;
	}

	public Integer getEdiNr() {
		return ediNr.get();
	}

	public void setEdiNr(Integer param) {
		ediNr.set(param);
	}

	// ------------------------------------------------------------------------
	public StringProperty kurzBez() {
		return kurzBez;
	}

	@Column(length = 30)
	public String getKurzBez() {
		return kurzBez.get();
	}

	public void setKurzBez(String param) {
		kurzBez.set(param);
	}

	// ------------------------------------------------------------------------
	public StringProperty senderName() {
		return senderName;
	}

//	public void setSenderName(String param) {
//		senderName.set(param);
//	}

	public String getSenderName () {
		return senderKomponente == null ? "" : senderKomponente.getName();
	}
	
	// ------------------------------------------------------------------------
	@OneToOne
	@PrimaryKeyJoinColumn
	public Komponente getSender() {
	    return senderKomponente;
	}
	public void setSender(Komponente param) {
	    this.senderKomponente = param;
	}
	@ManyToOne
	public Szenario getSzenario() {
	    return szenario;
	}
	public void setSzenario(Szenario param) {
	    this.szenario = param;
	}
	
}

