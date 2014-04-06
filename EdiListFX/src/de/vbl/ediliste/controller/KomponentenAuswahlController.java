package de.vbl.ediliste.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import javafx.beans.binding.Bindings;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.stage.Stage;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.persistence.TypedQuery;

import de.vbl.ediliste.model.Komponente;
import de.vbl.ediliste.model.Partner;

public class KomponentenAuswahlController {
	private static final String PERSISTENCE_UNIT_NAME = "EdiListFX";
    
    @FXML private ResourceBundle resources;

    @FXML private ComboBox<String> komponenteCB;
    @FXML private ComboBox<String> systemCB;
    @FXML private ComboBox<String> partnerCB;
    
    @FXML private Button newSystemButton;
    @FXML private Button newPartnerButton;
    @FXML private Button newKomponenteButton;

    @FXML private Label fehlertext;

	private EntityManager em; 
	private Komponente komponente;

	private ObservableList<String> partnerList =  FXCollections.observableArrayList();
	private List<Long> partnerIdList = new ArrayList<Long>();
	
	
	public Komponente selectedKomponenten() {
		return komponente;
	}
	/* ------------------------------------------------------------------------
     * initialize() is the controllers "main"-method 
     * it is called after loading "....fxml" 
     * ----------------------------------------------------------------------*/
    @FXML
    void initialize() {
    	System.out.println(getClass().getName()+".initialize called");
    	checkFieldFromView();
        setupEntityManager();

        getPartnerData();
		setupBindings();
    }
    
    private void getPartnerData() {
    	// alle Partner lesen
    	TypedQuery<Partner> tq = em.createQuery(
    			"SELECT partner FROM Partner partner ORDER BY partner.name",Partner.class);
    	List<Partner> ediList = tq.getResultList();
    	for (Partner partner : ediList) {
    		partnerList.add(partner.getName());
    		partnerIdList.add(partner.getId());
    	}
		partnerList.add("Beteiligte");			partnerIdList.add(1L);
		partnerList.add("Postrentendienst");	partnerIdList.add(2L);
		partnerList.add("Bankhaus Metzler");	partnerIdList.add(3L);
		partnerList.add("Versicherte");			partnerIdList.add(4L);
		partnerList.add("Siteforum");			partnerIdList.add(4L);
    }
    
    
    
    private void setupBindings() {
    	newSystemButton.disableProperty().bind(Bindings.isNull(partnerCB.getSelectionModel().selectedItemProperty()));
    	systemCB.disableProperty().bind(Bindings.isNull(partnerCB.getSelectionModel().selectedItemProperty()));
    	newKomponenteButton.disableProperty().bind(Bindings.isNull(systemCB.getSelectionModel().selectedItemProperty()));
    	komponenteCB.disableProperty().bind(Bindings.isNull(systemCB.getSelectionModel().selectedItemProperty()));
    	
    	partnerCB.setItems(partnerList);
	}
    	
    private void setupEntityManager() {
    	EntityManagerFactory factory = Persistence.createEntityManagerFactory(PERSISTENCE_UNIT_NAME);
    	em = factory.createEntityManager();
    }
    
    @FXML
    void okPressed(ActionEvent event) {
//    	if (isEdiNrUsed(ediNr)) {
//    		fehlertext.setText("Die Nummer " + ediNr + " ist bereits vergeben - bitte ändern");
//    	}
//    	else if (ediEintrag.getKurzBez()=="") {
//    		fehlertext.setText("Bitte eine Kurzbezeichnung eingeben");
//    	}
//    	else {
    		em.getTransaction().begin();
//    		em.persist(ediEintrag);
    		em.getTransaction().commit();
    		unbind();
    		close(event);
//    	}
    }

    @FXML
    void escapePressed(ActionEvent event) {
		unbind();
//    	ediEintrag = null;
    	close(event);
    }
    
    private void unbind() {
//    	tfEdiNr.textProperty().unbindBidirectional(ediEintrag.ediNrProperty());
//    	tfKurzbez.textProperty().unbindBidirectional(ediEintrag.kurzBezProperty());
    }
    
    private void close(ActionEvent event) {
    	Node source = (Node) event.getSource();
    	Stage stage = (Stage) source.getScene().getWindow();
    	stage.close();
    }

    @FXML
    void newPartnerPressed(ActionEvent event) {

    }

    @FXML
    void newSystemPressed(ActionEvent event) {

    }

    @FXML
    void newKomponentePressed(ActionEvent event) {

    }

    @FXML
    void ff0808(ActionEvent event) {

    }

    
    /* *****************************************************************************
	 * 
	 * ****************************************************************************/
    
    private void checkFieldFromView() {
        assert komponenteCB != null : "fx:id=\"komponenteCB\" was not injected: check your FXML file 'KomponentenAuswahl.fxml'.";
        assert systemCB != null : "fx:id=\"systemCB\" was not injected: check your FXML file 'KomponentenAuswahl.fxml'.";
        assert newSystemButton != null : "fx:id=\"newSystemButton\" was not injected: check your FXML file 'KomponentenAuswahl.fxml'.";
        assert newPartnerButton != null : "fx:id=\"newPartnerButton\" was not injected: check your FXML file 'KomponentenAuswahl.fxml'.";
        assert fehlertext != null : "fx:id=\"fehlertext\" was not injected: check your FXML file 'KomponentenAuswahl.fxml'.";
        assert newKomponenteButton != null : "fx:id=\"newKomponenteButton\" was not injected: check your FXML file 'KomponentenAuswahl.fxml'.";
        assert partnerCB != null : "fx:id=\"partnerCB\" was not injected: check your FXML file 'KomponentenAuswahl.fxml'.";
	}
}