package de.vbl.im.model;

import static javax.persistence.GenerationType.IDENTITY;

import java.io.Serializable;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

//import org.apache.logging.log4j.LogManager;
//import org.apache.logging.log4j.Logger;

/**
 * Entity implementation class for Entity: GeschaeftsObjekt
 *
 */

@Entity

public class GeschaeftsObjekt implements Serializable {
	/**
	 * 
	 */
//	private static final Logger logger = LogManager.getLogger(GeschaeftsObjekt.class.getName()); 
	private static final long serialVersionUID = 6212861741600895810L;

	private long id;
	private StringProperty name;
	private String beschreibung;
//	private ObservableList<InEmpfaenger> inEmpfaenger;
//	private IntegerProperty anzVerwendungen;
	
	public GeschaeftsObjekt() {
		name = new SimpleStringProperty();
//		inEmpfaenger = FXCollections.observableArrayList();
//		anzVerwendungen = new SimpleIntegerProperty();
	}   
	public GeschaeftsObjekt(String name) {
		this();
		this.name.set(name);
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
	public StringProperty nameProperty() {
		return name;
	}
	
	@Column(unique = true, nullable = false)
	public String getName() {
		return this.name.get();
	}

	public void setName(String name) {
		this.name.set(name);
	}
	
	// ------------------------------------------------------------------------
	public String getBeschreibung() { 
		return beschreibung;
	}

	public void setBeschreibung(String param) {
		this.beschreibung = param;
	}
	
//	@OneToMany(mappedBy = "geschaeftsObjekt")
//	public Collection<InEmpfaenger> getInEmpfaenger() {
//	    return inEmpfaenger;
//	}
//	public void setInEmpfaenger(Collection<InEmpfaenger> param) {
//		anzVerwendungen.unbind();
//		inEmpfaenger = FXCollections.observableArrayList(param);
//		logger.info("name:" + this.getName() + " id:" + this.id + " param.size:" + param.size() + " param:"+param);
//		anzVerwendungen.bind(Bindings.size(inEmpfaenger));
//	}
	
//	public IntegerProperty anzVerwendungenProperty () {
//		return anzVerwendungen;
//	}
}
