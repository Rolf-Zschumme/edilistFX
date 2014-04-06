package de.vbl.ediliste.controller;

import java.util.List;
import java.util.ResourceBundle;

import javafx.beans.binding.Bindings;
import javafx.beans.property.LongProperty;
import javafx.beans.property.SimpleLongProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Dialogs;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.stage.Stage;
import javafx.util.Callback;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.persistence.TypedQuery;

import de.vbl.ediliste.model.Komponente;
import de.vbl.ediliste.model.Partner;
import de.vbl.ediliste.model.PartnerSystem;

public class KomponentenAuswahlController {
	private static final String APPL_TITLE = "EdiListe";
	private static final String PERSISTENCE_UNIT_NAME = "EdiListFX";
    
    @FXML private ResourceBundle resources;

    @FXML private ComboBox<String> komponenteCB;
    @FXML private ComboBox<PartnerSystem> systemCB;
    @FXML private ComboBox<Partner> partnerCB;
    
    @FXML private Button newSystemButton;
    @FXML private Button newPartnerButton;
    @FXML private Button newKomponenteButton;

    @FXML private Label fehlertext;

	private EntityManager em; 
	private Komponente komponente;
	private LongProperty selectedPartnerId = new SimpleLongProperty();

	private ObservableList<Partner>       partnerList = FXCollections.observableArrayList();
	private ObservableList<PartnerSystem> systemList  = FXCollections.observableArrayList(); 
	
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
    		partnerList.add(partner);
    	}
    }
    
    private void getSystemData() {
    	Long partnerId = selectedKomponenten().getId();
    	systemList.clear();
		TypedQuery<PartnerSystem> tq = em.createQuery(
				"SELECT system FROM PartnerSystem system ORDER BY system.system", 
				PartnerSystem.class);
		List<PartnerSystem> ediList = tq.getResultList();
		for (PartnerSystem ediSystem : ediList) {
			if (partnerId == ediSystem.getPartner().getId()) {
				systemList.add(ediSystem);
			}
		}
    }
    
    
    private void setupBindings() {
    	newSystemButton.disableProperty().bind(Bindings.isNull(partnerCB.getSelectionModel().selectedItemProperty()));
    	systemCB.disableProperty().bind(Bindings.isNull(partnerCB.getSelectionModel().selectedItemProperty()));
    	newKomponenteButton.disableProperty().bind(Bindings.isNull(systemCB.getSelectionModel().selectedItemProperty()));
    	komponenteCB.disableProperty().bind(Bindings.isNull(systemCB.getSelectionModel().selectedItemProperty()));
    	
    	partnerCB.setCellFactory(new Callback<ListView<Partner>,ListCell<Partner>> () {
    		@Override
    		public ListCell<Partner> call(ListView<Partner> param) {
    			return new ListCell<Partner>() {
    				@Override
    				protected void updateItem(Partner partner, boolean empty) {
    					super.updateItem(partner, empty);
    					if(partner != null) {
    						setText(partner.getName());
//    						textProperty().bind(Bindings.format("%s", partner.nameProperty()));
    					}
    				}
    			};
    		}
    	});
    	partnerCB.setButtonCell(new ListCell<Partner> () {
    		@Override
    		protected void updateItem(Partner partner, boolean empty) {
    			super.updateItem(partner, empty);
    			if (empty) {
    				setText("?");
    			} else {
    				setText(partner.getName());
    			}
    		}    		
    	});
    	partnerCB.getSelectionModel().selectedItemProperty().addListener(
    			new ChangeListener<Partner>() {
    				@Override
    				public void changed (
    						ObservableValue<? extends Partner> observable,
    							Partner oldPartner, Partner newPartner) {
    					System.out.println(getClass().getName() + " Partner changed to " + newPartner.getName());
    					selectedPartnerId.set(newPartner.getId());
    					
    				}
    			}
    	);
    	partnerCB.setItems(partnerList);
    	
    	systemCB.setCellFactory(new Callback<ListView<PartnerSystem>,ListCell<PartnerSystem>> () {
    		@Override
    		public ListCell<PartnerSystem> call(ListView<PartnerSystem> param) {
    			return new ListCell<PartnerSystem>() {
    				@Override
    				protected void updateItem(PartnerSystem system, boolean empty) {
    					super.updateItem(system, empty);
    					if(system != null) {
    						setText(system.getName());
    					}
    				}
    			};
    		}
    	});
    	systemCB.setButtonCell(new ListCell<PartnerSystem> () {
    		@Override
    		protected void updateItem(PartnerSystem system, boolean empty) {
    			super.updateItem(system, empty);
    			if (empty) {
    				setText("?");
    			} else {
    				setText(system.getName());
    			}
    		}    		
    	});
    	systemCB.getSelectionModel().selectedItemProperty().addListener(
    			new ChangeListener<PartnerSystem>() {
    				@Override
    				public void changed (
    						ObservableValue<? extends PartnerSystem> observable,
    							PartnerSystem oldSystem, PartnerSystem newSystem) {
    					System.out.println(getClass().getName() + " System changed to " + newSystem.getName());
//    					selectedPartnerId.set(newPartner.getId());
    					
    				}
    			}
    	);
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
    	Node source = (Node) event.getSource();
    	Stage stage = (Stage) source.getScene().getWindow();
    	String name = "";
    	while(true) {
    		name = Dialogs.showInputDialog(stage,
    				"Bezeichnung des Partners:",
    				"Neuen Partner eingeben",APPL_TITLE,name);
    		if (name == null)	
    			break;
    		if (name.length() < 3) {
    			Dialogs.showInformationDialog(stage, 
    					"Bitte einen Namen mit mind. 3 Zeichen eingeben",
    					"Korrektur erforderlich",APPL_TITLE);
    			continue;
    		}
    		if (partnerNameExist(name)) {
    			Dialogs.showInformationDialog(stage, 
    					"Ein Partner mit diesem Namen ist bereits vorhanden",
    					"Duplikatsprüfung",APPL_TITLE);
    			continue;
    		}
    		Partner partner = new Partner(name);
    		try {
    			em.getTransaction().begin();
    			em.persist(partner);
    			em.getTransaction().commit();
    		} catch (RuntimeException e) {
    			Dialogs.showErrorDialog(stage,
    					"Fehler beim speichern des Partners",
    					"Datenbankfehler",APPL_TITLE,e);
    			continue;
    		}
    		partnerList.add(partner);
    		partnerCB.getSelectionModel().select(partner);
    		break;
    	}	
    }
    
    private boolean partnerNameExist(String name) {
    	for (Partner p : partnerList) {
    		if (name.equalsIgnoreCase(p.getName()))
    			return false;
    	}
		return false;
	}

    
    @FXML
    void newSystemPressed(ActionEvent event) {
    	Node source = (Node) event.getSource();
    	Stage stage = (Stage) source.getScene().getWindow();
    	String name = "";
    	Partner aktPartner = partnerCB.getSelectionModel().getSelectedItem();
    	String partnerName = aktPartner.getName();
    	while(true) {
    		name = Dialogs.showInputDialog(stage,
    				"Bezeichnung des neuen Systems:",
    				"Neues System für Partner " + partnerName + " eingeben" ,APPL_TITLE,name);
    		if (name == null)	
    			break;
    		if (name.length() < 3) {
    			Dialogs.showInformationDialog(stage, 
    					"Der System-Name muß mindestens 3 Zeichen lang sein",
    					"Korrektur erforderlich",APPL_TITLE);
    			continue;
    		}
    		if (partnerNameExist(name)) {
    			Dialogs.showInformationDialog(stage, 
    					"Für den Partner " + partnerName + " ist bereits ein gleichnamiges System vorhanden",
    					"Duplikatsprüfung",APPL_TITLE);
    			continue;
    		}
    		PartnerSystem partnerSystem = new PartnerSystem(name, aktPartner);
    		try {
    			em.getTransaction().begin();
    			em.persist(partnerSystem);
    			em.getTransaction().commit();
    		} catch (RuntimeException e) {
    			Dialogs.showErrorDialog(stage,
    					"Fehler beim Anlegen des Systems",
    					"Datenbankfehler",APPL_TITLE,e);
    			continue;
    		}
//    		partnerList.add(name);
//    		partnerIdList.add(partner.getId());
//    		partnerCB.getSelectionModel().select(name);
    		systemList.add(partnerSystem);
    		systemCB.getSelectionModel().select(partnerSystem);
    		break;
    	}	

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