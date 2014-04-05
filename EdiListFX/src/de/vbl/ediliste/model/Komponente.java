package de.vbl.ediliste.model;

import static javax.persistence.GenerationType.IDENTITY;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import de.vbl.ediliste.model.EdiEintrag;
import java.util.Collection;
import javax.persistence.OneToMany;

@Entity
public class Komponente {
	private StringProperty name = new SimpleStringProperty();
	private StringProperty fullname = new SimpleStringProperty();
	private long id;
	private System system;
	private Collection<EdiEintrag> ediEintrag;
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

	public void setName(String param) {
		name.set(param);
	}

	// - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - --
	public StringProperty fullnameProperty() {
		return fullname;
	}
	
	public String getFullname() {
		String fullName = system == null ? "-?-" : system.getFullname();
		return fullName + "  " + name.get();
	}

	// ------------------------------------------------------------------------
	@ManyToOne
	public System getSystem() {
	    return system;
	}

	public void setSystem(System param) {
	    this.system = param;
	}

	@OneToMany(mappedBy = "komponente")
	public Collection<EdiEintrag> getEdiEintrag() {
	    return ediEintrag;
	}

	public void setEdiEintrag(Collection<EdiEintrag> param) {
	    this.ediEintrag = param;
	}
}

