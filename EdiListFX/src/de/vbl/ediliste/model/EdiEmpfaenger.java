package de.vbl.ediliste.model;

import static javax.persistence.GenerationType.IDENTITY;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import de.vbl.ediliste.model.EdiEintrag;
import de.vbl.ediliste.model.GeschaeftsObjekt;
import javax.persistence.JoinColumn;

/**
 * Entity implementation class for Entity: Empfaenger
 *
 */
@Entity

public class EdiEmpfaenger {

	private StringProperty bemerkung = new SimpleStringProperty();
	private long id;
	private EdiKomponente ediKomponente;
	private EdiEintrag ediEintrag;
	private GeschaeftsObjekt geschaeftsObjekt;

	public EdiEmpfaenger() {
	}
	public EdiEmpfaenger(EdiEintrag param) {
		ediEintrag = param;
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
	public StringProperty bemerkungProperty() {
		return bemerkung;
	}
	
	public String getBemerkung() {
		return bemerkung.get();
	}

	public void setBemerkung(String param) {
		bemerkung.set(param);
	}

	@ManyToOne
	public EdiKomponente getKomponente() {
	    return ediKomponente;
	}

	public void setKomponente(EdiKomponente param) {
	    this.ediKomponente = param;
	}

	@ManyToOne
	public EdiEintrag getEdiEintrag() {
	    return ediEintrag;
	}

	public void setEdiEintrag(EdiEintrag param) {
	    this.ediEintrag = param;
	}
	@ManyToOne
	@JoinColumn(name = "geschaeftsObjekt_id", referencedColumnName = "id")
	public GeschaeftsObjekt getGeschaeftsObjekt() {
	    return geschaeftsObjekt;
	}
	public void setGeschaeftsObjekt(GeschaeftsObjekt param) {
	    this.geschaeftsObjekt = param;
	}


}
