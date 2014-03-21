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


@Entity
public class EdiEintrag {
	private long id;
	private Komponente senderKomponente;
	private IntegerProperty ediNr = new SimpleIntegerProperty();
	private StringProperty kurzBez = new SimpleStringProperty();
	private StringProperty senderName = new SimpleStringProperty();

	public IntegerProperty ediNrProperty() {
		return ediNr;
	}
	public StringProperty kurzBez() {
		return kurzBez;
	}
	public StringProperty senderName() {
		return senderName;
	}
	
	@Id
	@GeneratedValue(strategy = IDENTITY)
	public long getId() {
		return id;
	}
	public void setId(long id) {
		this.id = id;
	}

	public Integer getEdiNr() {
		return ediNr.get();
	}

	public void setEdiNr(Integer param) {
		ediNr.set(param);
	}

	@Column(length = 30)
	public String getKurzBez() {
		return kurzBez.get();
	}
	public void setKurzBez(String param) {
		kurzBez.set(param);
	}
	@OneToOne
	@PrimaryKeyJoinColumn
	public Komponente getKomponente() {
	    return senderKomponente;
	}
	public void setKomponente(Komponente param) {
	    this.senderKomponente = param;
	}
	
	public void setSenderName(String param) {
		senderName.set(param);
	}
	public String getSenderName () {
		return senderName.get();
	}

}
