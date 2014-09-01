package de.vbl.ediliste.model;

import static javax.persistence.GenerationType.IDENTITY;

import java.util.Collection;

import javafx.beans.binding.Bindings;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Transient;

@Entity
public class EdiSystem {
	private long id;
	private StringProperty name;
	private EdiPartner ediPartner;
	private ObservableList<EdiKomponente> ediKomponente;
	private String beschreibung;
	
	private StringProperty fullname;
	private IntegerProperty anzKomponenten;

	public EdiSystem() {
		name = new SimpleStringProperty();
		fullname = new SimpleStringProperty();
		anzKomponenten = new SimpleIntegerProperty();
	}

	public EdiSystem(String name, EdiPartner ediPartner) {
		this();
		this.name.setValue(name);
		this.setEdiPartner(ediPartner);
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
	public StringProperty nameProperty() {
		return name;
	}

	public String getName() {
		return name.get();
	}
	
	public String getNameSafe() {
		return name.getValueSafe();
	}

	public void setName(String param) {
		name.set(param);
		
	}

	// - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - --
	public StringProperty fullnameProperty() {
		return fullname;
	}
	public String getFullname () {
		return fullname.get();
	}

//	public String getFullname() {
//		String partnerName = ediPartner == null ? "-?-" : ediPartner.getName();
//		return partnerName + ASCIItoStr(42) + name.get();   // 151
//		return fullname.get();
//	}
	private String ASCIItoStr(int a) {
		byte[] b = { (byte) a };
		String ret = new String(b);
		return " " + ret + " ";
	}

	@ManyToOne
	@JoinColumn(referencedColumnName = "id")
	public EdiPartner getEdiPartner() {
		return ediPartner;
	}

	public void setEdiPartner(EdiPartner param) {
		if (ediPartner != null) {
			fullname.unbind();
		}
		ediPartner = param;
		fullname.bind(Bindings.concat(ediPartner.nameProperty(), ASCIItoStr(42), name));
	}

	// - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - --
	@OneToMany(mappedBy = "ediSystem")
	public Collection<EdiKomponente> getEdiKomponente() {
		return ediKomponente;
	}

	public void setEdiKomponente(Collection<EdiKomponente> komponenten) {
		if (ediKomponente != null) {
			anzKomponenten.unbind();
			System.out.println("setEdiKomponente UNBOUND");
		}
		if (ediKomponente != null) {
			System.out.println("setEdiKomponente " + ediKomponente.size());
		}
		ediKomponente = FXCollections.observableArrayList(komponenten);
		anzKomponenten.bind(Bindings.size(ediKomponente));

//		System.out.println("setKomponente f�r " + this.getFullname() + " mit "  + komponenten );
		anzKomponenten.addListener(new ChangeListener<Number>() {
			@Override
			public void changed(ObservableValue<? extends Number> observable,
					Number oldValue, Number newValue) {
				System.out.println("------->   Value changed from " + oldValue + " to " + newValue + " for " + observable.toString());
			}
		} );
	}

	// - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - --
	@Transient
	public IntegerProperty anzKomponentenProperty() {
//		System.out.println("EdiSystem.anzKomponentenProperty() called for " + this.getNameSafe());
		return anzKomponenten;
	}

//	public Integer getAnzKomponenten() {
//		System.out.println("EdiSystem.getAnzKomponenten() called for " + this.getNameSafe());
//		return ediKomponente.size();
//	}

	public String getPartnerName() {
		return ediPartner.getName();
	}

	public String getBeschreibung() {
		return beschreibung;
	}

	public void setBeschreibung(String param) {
		this.beschreibung = param;
	}
}
