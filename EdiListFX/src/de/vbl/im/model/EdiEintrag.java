package de.vbl.im.model;

import static javax.persistence.GenerationType.IDENTITY;

import java.util.ArrayList;
import java.util.Collection;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.JoinColumn;

import static javax.persistence.CascadeType.ALL;
import de.vbl.im.model.EdiIntervall;

@Entity
public class EdiEintrag {
	public static final int EDI_NR_MIN_LEN = 3;
	public static final String FORMAT_EDINR = " %03d";
	private long id;
	private IntegerProperty ediNr;
	private StringProperty bezeichnung;
	private StringProperty beschreibung;
	private StringProperty seitDatum;
	private StringProperty bisDatum;
	private Konfiguration konfiguration;
	private EdiKomponente ediKomponente;
	private Collection<EdiEmpfaenger> ediEmpfaenger;
	private String laeDatum; 
	private String laeUser;
	
	private StringProperty senderName;
	private StringProperty integrationName;
	private StringProperty konfigurationName;
	private EdiIntervall ediIntervall;

	public EdiEintrag() {
		ediNr = new SimpleIntegerProperty();
		bezeichnung = new SimpleStringProperty();
		beschreibung = new SimpleStringProperty();
		ediEmpfaenger = new ArrayList<EdiEmpfaenger>();
		seitDatum = new SimpleStringProperty();
		bisDatum = new SimpleStringProperty();
		senderName = new SimpleStringProperty();
		integrationName = new SimpleStringProperty("");
		konfigurationName = new SimpleStringProperty("");
	}	

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
	
	public String getEdiNrStr() {
		String ret = Integer.toString(getEdiNr());
		while(ret.length()<EDI_NR_MIN_LEN) ret = "0"+ ret;
		return ret;
	}

	// ------------------------------------------------------------------------
	public StringProperty bezeichnungProperty() {
		return bezeichnung;
	}

	public String getBezeichnung() {
		return bezeichnung.get();
	}

	public void setBezeichnung(String param) {
		bezeichnung.set(param);
	}

	// ------------------------------------------------------------------------
	public StringProperty beschreibungProperty() {
		return beschreibung;
	}
	public String getBeschreibung() {
		return beschreibung.get();
	}
	
	public void setBeschreibung(String param) {
		beschreibung.set(param);
	}
	
	// ------------------------------------------------------------------------
	public StringProperty senderNameProperty() {
		return senderName;
	}

	 public void setSenderName(String param) {
		 senderName.set(param);
	 }
	 
	// ------------------------------------------------------------------------
	@ManyToOne(cascade = ALL)
	@JoinColumn(referencedColumnName = "id")
	public Konfiguration getKonfiguration() {
		return konfiguration; 
	}

	public void setKonfiguration(Konfiguration param) {
		this.konfiguration = param;
		if (param != null) {
			this.konfigurationName.unbind();
			this.konfigurationName.bind(param.nameProperty());
			this.integrationName.unbind();
			this.integrationName.bind(param.integrationNameProperty());
		}	
	}

	@ManyToOne
	public EdiKomponente getEdiKomponente() {
		return ediKomponente;
	}

	public void setEdiKomponente(EdiKomponente kompo) {
		if (ediKomponente != null) {
			senderName.unbind();
		}
		ediKomponente = kompo;
		if (kompo != null) {
			senderName.bind(kompo.fullnameProperty());
		}	
	}

	@OneToMany(mappedBy = "ediEintrag", cascade = ALL)
	public Collection<EdiEmpfaenger> getEdiEmpfaenger() {
		return ediEmpfaenger;
	}

	public void setEdiEmpfaenger(Collection<EdiEmpfaenger> param) {
		this.ediEmpfaenger = param;
	}
 
	public StringProperty seitDatumProperty() {
		return seitDatum;
	}
	public String getSeitDatum() {
		return seitDatum.get();
	}
	public void setSeitDatum(String param) {
		seitDatum.set(param);
	}

	public StringProperty bisDatumProperty() {
		return bisDatum;
	}
	public String getBisDatum() {
		return bisDatum.get();
	}
	public void setBisDatum(String param) {
		bisDatum.set(param);
	}
	
	public String getLaeDatum() {
		return laeDatum;
	}

	public void setLaeDatum(String laeDatum) {
		this.laeDatum = laeDatum;
	}
	
	public String getLaeUser() {
		return laeUser;
	}

	public void setLaeUser(String laeUser) {
		this.laeUser = laeUser;
	}

	public StringProperty konfigurationName () {
		return konfigurationName;
	}
	
	public StringProperty intregrationName () {
		return integrationName;
	}

    public String autoBezeichnung() {
    	String intSzeName = "I??";
    	String empf01Name = "E??";
    	String geOb01Name = "G??";
    	if (ediEmpfaenger.size() > 0) {
    		EdiEmpfaenger e01 = ediEmpfaenger.iterator().next();
    		if (e01.getKomponente() != null) {
    			empf01Name = e01.getKomponente().getFullname();
    		}
    		if (e01.getGeschaeftsObjekt() != null) {
    			geOb01Name = e01.getGeschaeftsObjekt().getName();
    		}
    	}
    	if (konfiguration != null) {
    		if (konfiguration.getIntegration() != null) {
    			if (konfiguration.getIntegration().getName() != null)
    				intSzeName = konfiguration.getIntegration().getName();
    		}
    	}	
    	return intSzeName + "  [" + senderName.get() + "  >>  " + empf01Name + ": " + geOb01Name + "]";
    }

	@ManyToOne
	public EdiIntervall getEdiIntervall() {
	    return ediIntervall;
	}

	public void setEdiIntervall(EdiIntervall param) {
	    this.ediIntervall = param;
	}
}
