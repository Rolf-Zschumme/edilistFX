package de.vbl.ediliste.model;

import static javax.persistence.GenerationType.IDENTITY;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

/**
 * Entity implementation class for Entity: Empfaenger
 *
 */
@Entity 

public class EdiEmpfaenger {

	private StringProperty empfaengerName;
	private StringProperty geschaeftsObjektName;
	private long id;
	private EdiKomponente ediKomponente;
	private EdiEintrag ediEintrag;
	private GeschaeftsObjekt geschaeftsObjekt;

	public EdiEmpfaenger() {
		empfaengerName = new SimpleStringProperty();
		geschaeftsObjektName = new SimpleStringProperty();
	}

//	public EdiEmpfaenger(EdiEintrag param) {
//		this();
//		ediEintrag = param;
//	}
	
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
		String fullName = (kompo == null) ? "?e?" : kompo.getFullname(); 
		this.empfaengerName.set(fullName);
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
	@JoinColumn(name = "geschaeftsObjekt_id", referencedColumnName = "id")
	public GeschaeftsObjekt getGeschaeftsObjekt() {
	    return geschaeftsObjekt;
	}
	public void setGeschaeftsObjekt(GeschaeftsObjekt param) {
		this.geschaeftsObjektName.set(param.getName());
//		if (ediKomponente == null ) 
//			System.out.println("GeschaeftsobjektName(E="+this+").set("+param.getName()+")");
//		else
//			System.out.println("GeschaeftsobjektName(E="+ ediKomponente.getFullname()+").set("+param.getName()+")");
	    this.geschaeftsObjekt = param;
	}
	
	// ------------------------------------------------------------------------
	public IntegerProperty ediNrProperty() {
		return ediEintrag.ediNrProperty();
	}
	
	public StringProperty senderNameProperty() {
		return ediEintrag.senderNameProperty();
	}

	public StringProperty empfaengerNameProperty() {
		return this.empfaengerName;
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
