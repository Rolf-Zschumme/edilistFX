
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

public class InEmpfaenger {

	private StringProperty geschaeftsObjektName;
	private long id;
	private InKomponente inKomponente;
	private Integration integration;
	private GeschaeftsObjekt geschaeftsObjekt;

	public InEmpfaenger() {
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
	public InKomponente getKomponente() {
	    return inKomponente;
	}

	public void setKomponente(InKomponente kompo) {
	    this.inKomponente = kompo;
	}

	@ManyToOne
	public Integration getIntegration() {
	    return integration;
	}

	public void setIntegration(Integration param) {
	    this.integration = param;
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
			geschaeftsObjekt.getInEmpfaenger().remove(this);
		}
	    geschaeftsObjekt = newGeOb;
	    if (geschaeftsObjekt.getId() > 0 && !geschaeftsObjekt.getInEmpfaenger().contains(this)) {
	    	geschaeftsObjekt.getInEmpfaenger().add(this);
	    }
    	geschaeftsObjektName.set(newGeOb.getName());
	}
	
	// ------------------------------------------------------------------------
	public IntegerProperty getInNrProperty() {
		return integration.inNrProperty();
	}
	
	public StringProperty senderNameProperty() {
		return integration.senderNameProperty();
	}

	public StringProperty empfaengerNameProperty() {
		return inKomponente.fullnameProperty();
	}
	
	// ------------------------------------------------------------------------
	public boolean equaels (InEmpfaenger empf) {
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
