package de.vbl.im.model;

import java.util.ArrayList;
import java.util.Collection;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Transient;
import de.vbl.im.model.Konfiguration;
import javax.persistence.JoinColumn;
import javax.persistence.GeneratedValue;
import static javax.persistence.GenerationType.IDENTITY;

@Entity
public class Integration { 
	public static final int EDI_NR_MIN_LEN = 3;
	public static final String FORMAT_EDINR = " %03d";
	private long id;
	private IntegerProperty ediNr;
	private StringProperty bezeichnung;
	private StringProperty beschreibung;
	private StringProperty seitDatum;
	private StringProperty bisDatum;
	private EdiKomponente ediKomponente;
	private Collection<EdiEmpfaenger> ediEmpfaenger;
	private String laeDatum; 
	private String laeUser;
	
	private StringProperty senderName;
	private StringProperty iszenarioName;
	private StringProperty konfigurationName;
	private Intervall intervall;
	private Konfiguration konfiguration;
	public Integration() {
		ediNr = new SimpleIntegerProperty();
		bezeichnung = new SimpleStringProperty();
		beschreibung = new SimpleStringProperty();
		ediEmpfaenger = new ArrayList<EdiEmpfaenger>();
		seitDatum = new SimpleStringProperty();
		bisDatum = new SimpleStringProperty();
		senderName = new SimpleStringProperty();
		iszenarioName = new SimpleStringProperty("");
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

	@OneToMany(mappedBy = "integration")
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

	public StringProperty konfigurationNameProperty () {
		return konfigurationName;
	}
	
	public StringProperty iszenarioNameProperty () {
		return iszenarioName;
	}

	// Return : <Iszenario> - <Sender> - <Geschäftsobjekt> 
    @Transient
    public static String autobezeichnung(Konfiguration konfiguration,
    							  EdiKomponente sender,
    							  GeschaeftsObjekt gObjekt) {
    	String iszenarioName = "<Iszenario>";
    	String senderName = "<Sender>";
    	String gObjektName = "<Geschäftsobjekt>";
    	if (konfiguration != null) {
    		if (konfiguration.getIszenario() != null) {
    			if (konfiguration.getIszenario().getName() != null)
    				iszenarioName = konfiguration.getIszenario().getName();
    		}
    	}	
    	if (sender != null) {
    		senderName = sender.getFullname();
    	}
    	if (gObjekt != null) {
    		gObjektName = gObjekt.getName();
    	}
    	return iszenarioName + TrennStr() + senderName + TrennStr() + gObjektName;
    }
    	
	private static String TrennStr() {
		return "  |  ";
	}
//	private String ASCIItoStr(int a) {
//		byte[] b = { (byte) a };
//		String ret = new String(b);
//		return " " + ret + " ";
//	}


	@ManyToOne
	public Intervall getIntervall() {
	    return intervall;
	}

	public void setIntervall(Intervall param) {
	    this.intervall = param;
	}

	@ManyToOne
	@JoinColumn(name = "konfiguration_id", referencedColumnName = "ID")
	public Konfiguration getKonfiguration() {
	    return konfiguration;
	}

	public void setKonfiguration(Konfiguration param) {
	    this.konfiguration = param;
	}
}
