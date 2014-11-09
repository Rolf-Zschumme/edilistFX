
package de.vbl.im.model;

import static javax.persistence.GenerationType.IDENTITY;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;

/**
 * Entity implementation class for Entity: Empfaenger
 *
 */
@Entity 

public class EdiEmpfaenger {

	private StringProperty geschaeftsObjektName;
	private long id;
	private EdiKomponente ediKomponente;
	private EdiEintrag ediEintrag;
	private GeschaeftsObjekt geschaeftsObjekt;

	public EdiEmpfaenger() {
		geschaeftsObjektName = new SimpleStringProperty();
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

	public void setKomponente(EdiKomponente kompo) {
	    this.ediKomponente = kompo;
	}

	@ManyToOne
	public EdiEintrag getEdiEintrag() {
	    return ediEintrag;
	}

	public void setEdiEintrag(EdiEintrag param) {
	    this.ediEintrag = param;
	}
	
	public StringProperty geschaeftsObjektNameProperty () {
		return this.geschaeftsObjektName;
	}
	
	@ManyToOne
	public GeschaeftsObjekt getGeschaeftsObjekt() {
	    return geschaeftsObjekt; 
	}
	
	public void setGeschaeftsObjekt(GeschaeftsObjekt newGeOb) {
		if (geschaeftsObjekt != null) {
			geschaeftsObjekt.getEdiEmpfaenger().remove(this);
		}
	    geschaeftsObjekt = newGeOb;
	    if (geschaeftsObjekt.getId() > 0 && !geschaeftsObjekt.getEdiEmpfaenger().contains(this)) {
	    	geschaeftsObjekt.getEdiEmpfaenger().add(this);
	    }
    	geschaeftsObjektName.set(newGeOb.getName());
	}
	
	// ------------------------------------------------------------------------
	public IntegerProperty getEdiNrProperty() {
		return ediEintrag.ediNrProperty();
	}
	
	public StringProperty senderNameProperty() {
		return ediEintrag.senderNameProperty();
	}

	public StringProperty empfaengerNameProperty() {
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
