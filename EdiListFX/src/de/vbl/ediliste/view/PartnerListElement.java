package de.vbl.ediliste.view;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.StringProperty;
import de.vbl.ediliste.model.EdiPartner;
import de.vbl.ediliste.model.EdiSystem;

public class PartnerListElement {
	EdiPartner partner;
	StringProperty name;
	IntegerProperty anzSysteme;
	IntegerProperty anzKomponenten;
	
	
	public PartnerListElement(EdiPartner partner) 
	{
		this.partner = partner;
		this.name = partner.nameProperty();
		
		int anzK = 0;
		for ( EdiSystem s : partner.getEdiSystem() ) {
			anzK += s.getEdiKomponente().size();
		}
		this.anzSysteme = new SimpleIntegerProperty(partner.getEdiSystem().size());
		this.anzKomponenten = new SimpleIntegerProperty(anzK);
	}
	
	public EdiPartner getPartner() {
		return partner;
	}

	public StringProperty nameProperty() {
		return name;
	}

	public IntegerProperty anzSystemeProperty() {
		return anzSysteme;
	}

	public IntegerProperty anzKomponentenProperty() {
		return anzKomponenten;
	}
}
