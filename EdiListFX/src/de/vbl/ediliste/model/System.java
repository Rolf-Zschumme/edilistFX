package de.vbl.ediliste.model;

import static javax.persistence.GenerationType.IDENTITY;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import de.vbl.ediliste.model.Partner;
import de.vbl.ediliste.model.Komponente;
import javax.persistence.ManyToOne;
import java.util.Collection;
import javax.persistence.OneToMany;
 
@Entity 
public class System {
	private StringProperty name = new SimpleStringProperty();
	private StringProperty fullname = new SimpleStringProperty();
	
	private Long id;
	private Partner partner;
	private Collection<Komponente> komponente;

	// ------------------------------------------------------------------------
	@Id
	@GeneratedValue(strategy = IDENTITY)
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}

	// ------------------------------------------------------------------------
	public StringProperty nameProperty() {
		return name;
	}
	
	public String getName() {
		return name.get();
	}

	public void setName(String param) {
		name.set(param);
	}

	// - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - --
	public StringProperty fullnameProperty() {
		return fullname;
	}
	
	public String getFullname() {
		String partnerName = partner == null ? "-?-" : partner.getName();
		return partnerName + "  " + name.get();
	}
	@ManyToOne
	public Partner getPartner() {
	    return partner;
	}
	public void setPartner(Partner param) {
	    this.partner = param;
	}
	@OneToMany(mappedBy = "system")
	public Collection<Komponente> getKomponente() {
	    return komponente;
	}
	public void setKomponente(Collection<Komponente> param) {
	    this.komponente = param;
	}



}
