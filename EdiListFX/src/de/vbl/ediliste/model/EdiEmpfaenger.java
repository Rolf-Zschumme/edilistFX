package de.vbl.ediliste.model;

import javafx.beans.property.StringProperty;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import static javax.persistence.GenerationType.IDENTITY;

/**
 * Entity implementation class for Entity: Empfaenger
 *
 */
@Entity 

public class EdiEmpfaenger {

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
	
	// ------------------------------------------------------------------------
	public StringProperty senderNameProperty() {
		return ediEintrag.senderNameProperty();
	}

	public StringProperty empfaengerNameProperty() {
		System.out.println("ediKomponente.fullname"+ ediKomponente.fullnameProperty().getName());
		return ediKomponente.fullnameProperty();
	}
	
	
	// ------------------------------------------------------------------------
	public boolean equaels (EdiEmpfaenger empf) {
		if ( this.getId() == empf.getId()) {
			Long eKompId = empf.getKomponente() == null ? -1 : empf.getKomponente().getId();
			Long tKompId = this.getKomponente() == null ? -1 : this.getKomponente().getId();
			Long eGeObId = empf.getGeschaeftsObjekt() == null ? -1 : empf.getGeschaeftsObjekt().getId();
			Long tGeObId = this.getGeschaeftsObjekt() == null ? -1 : this.getGeschaeftsObjekt().getId();
			if ((eKompId == tKompId) &&
				(eGeObId == tGeObId)  ) {
				return true;
			}
		}
		return false;
	}
}
