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
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.stage.Stage;
import javafx.util.Callback;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;

import org.controlsfx.dialog.Dialogs;
import org.controlsfx.dialog.Dialog.Actions;

import de.vbl.ediliste.model.EdiKomponente;
import de.vbl.ediliste.model.EdiPartner;
import de.vbl.ediliste.model.EdiSystem;

/**
 * @author p01023
 * 
 *	20.08.2014 RZ EntityManager wird stets neu erstellt -> aktuelle Tabelleninhalte
 *  25.08.2014 RZ EntityManager wird vom rufenden Programm übergeben!
 */
public class KomponentenAuswahlController {
	public static enum KomponentenTyp {	SENDER, RECEIVER };
	private static final String APPL_TITLE = "EdiListe";
//	private static final String PERSISTENCE_UNIT_NAME = "EdiListFX";
	private EntityManager entityManager; 
    
    @FXML private ResourceBundle resources;

    @FXML private ComboBox<EdiPartner> partnerCB; 
    @FXML private ComboBox<EdiSystem> systemCB;
    @FXML private ComboBox<EdiKomponente> komponenteCB;
    
    @FXML private Button btnOK;
    @FXML private Button btnNewSystem;
    @FXML private Button btnNewPartner;
    @FXML private Button btnNewKomponente;

    @FXML private Label lbFehlertext;
    @FXML private Label lbSenderReseiver;


	private LongProperty ediKomponentenId;
	
	private ObservableList<EdiPartner>      partnerList;     //  = FXCollections.observableArrayList();
	private ObservableList<EdiSystem> 		systemList;      //  = FXCollections.observableArrayList(); 
	private ObservableList<EdiKomponente>   komponentenList; //  = FXCollections.observableArrayList(); 

	public Actions getResponse () {
		if (ediKomponentenId == null || ediKomponentenId.get()==0L)
			return Actions.CANCEL;
		else
			return Actions.OK;
	}

	public Long getSelectedKomponentenId() {
		return ediKomponentenId.get();
	}
	
	public void setKomponente(KomponentenTyp typ, Long komponentenID, EntityManager em) {
    	System.out.println(getClass().getName()+".setKomponenten called");
    	entityManager = em;
        readPartnerData();
    	
		if (typ == KomponentenTyp.SENDER)
			lbSenderReseiver.setText("Sender");
		else
			lbSenderReseiver.setText("Empfänger");

		ediKomponentenId.set(komponentenID);
		if (komponentenID > 0L) {
			EdiKomponente komponente = entityManager.find(EdiKomponente.class, komponentenID);
				
			partnerCB.getSelectionModel().select(komponente.getEdiSystem().getEdiPartner());
			systemCB.getSelectionModel().select(komponente.getEdiSystem());
			komponenteCB.getSelectionModel().select(komponente);
		}
		
		setupBindings();
	}
	
	/* ------------------------------------------------------------------------
     * initialize() is the controllers "main"-method 
     * it is called after loading "....fxml" 
     * ----------------------------------------------------------------------*/
    @FXML
    void initialize() {
    	System.out.println(getClass().getName()+".initialize called");
    	
    	ediKomponentenId = new SimpleLongProperty();
  	    partnerList      = FXCollections.observableArrayList();
		systemList       = FXCollections.observableArrayList();
		komponentenList  = FXCollections.observableArrayList();
    	
    	checkFieldFromView();
//		entityManager = Persistence.createEntityManagerFactory(PERSISTENCE_UNIT_NAME).createEntityManager();

		setupBindings();
		partnerCB.setItems(partnerList);
		systemCB.setItems(systemList);
		komponenteCB.setItems(komponentenList);
    }
    
    private void readPartnerData() {
    	// alle EdiPartner lesen
    	TypedQuery<EdiPartner> tq = entityManager.createQuery(
    		  "SELECT p FROM EdiPartner p ORDER BY p.name", EdiPartner.class);
    	List<EdiPartner> ediList = tq.getResultList();
    	for (EdiPartner ediPartner : ediList) {
    		partnerList.add(ediPartner);
    	}
    }
    
    private void readSystemData(Long partnerId) {
    	systemList.clear();
		TypedQuery<EdiSystem> tq = entityManager.createQuery(
				"SELECT s FROM EdiSystem s ORDER BY s.name", EdiSystem.class);
		List<EdiSystem> ediList = tq.getResultList();
		for (EdiSystem ediSystem : ediList) {
			if (partnerId == ediSystem.getEdiPartner().getId()) {
				systemList.add(ediSystem);
			}
		}
    }

    private void readKomponentenData(Long systemId) {
    	komponentenList.clear();
		TypedQuery<EdiKomponente> tq = entityManager.createQuery(
		  "SELECT k FROM EdiKomponente k ORDER BY k.name",EdiKomponente.class);
		List<EdiKomponente> resultList = tq.getResultList();
		for (EdiKomponente ediKomponente : resultList) {
			if (systemId == ediKomponente.getEdiSystem().getId()) {
				komponentenList.add(ediKomponente);
			}
		}
    }
    
    private void setupBindings() {
    	btnOK.disableProperty().bind(Bindings.equal(0L, ediKomponentenId));
    	
    	btnNewSystem.disableProperty().bind(Bindings.isNull(partnerCB.getSelectionModel().selectedItemProperty()));
    	systemCB.disableProperty().bind(Bindings.isNull(partnerCB.getSelectionModel().selectedItemProperty()));
    	btnNewKomponente.disableProperty().bind(Bindings.isNull(systemCB.getSelectionModel().selectedItemProperty()));
    	komponenteCB.disableProperty().bind(Bindings.isNull(systemCB.getSelectionModel().selectedItemProperty()));
    	
    	partnerCB.setCellFactory(new Callback<ListView<EdiPartner>,ListCell<EdiPartner>> () {
    		@Override
    		public ListCell<EdiPartner> call(ListView<EdiPartner> param) {
    			return new ListCell<EdiPartner>() {
    				@Override
    				protected void updateItem(EdiPartner ediPartner, boolean empty) {
    					super.updateItem(ediPartner, empty);
    					if(ediPartner != null) {
    						setText(ediPartner.getName());
    					}
    				}
    			};
    		}
    	});
    	partnerCB.setButtonCell(new ListCell<EdiPartner> () {
    		@Override
    		protected void updateItem(EdiPartner ediPartner, boolean empty) {
    			super.updateItem(ediPartner, empty);
    			setText( (empty) ? "?" : ediPartner.getName());
    		}    		
    	});
    	partnerCB.getSelectionModel().selectedItemProperty().addListener(
    			new ChangeListener<EdiPartner>() {
    				@Override
    				public void changed (
    						ObservableValue<? extends EdiPartner> observable,
    							EdiPartner oldPartner, EdiPartner newPartner) {
    					readSystemData( (newPartner==null) ? -1L : newPartner.getId());
    					komponenteCB.getSelectionModel().select(null);
    				}
    			}
    	);
    	
    	systemCB.setCellFactory(new Callback<ListView<EdiSystem>,ListCell<EdiSystem>> () {
    		@Override
    		public ListCell<EdiSystem> call(ListView<EdiSystem> param) {
    			return new ListCell<EdiSystem>() {
    				@Override
    				protected void updateItem(EdiSystem system, boolean empty) {
    					super.updateItem(system, empty);
    					if(system != null) {
    						setText(system.getName());
    					}
    				}
    			};
    		}
    	}); 
    	systemCB.setButtonCell(new ListCell<EdiSystem> () {
    		@Override
    		protected void updateItem(EdiSystem system, boolean empty) {
    			super.updateItem(system, empty);
    			setText( (empty) ? "?" : system.getName());
    		}    		
    	});
    	systemCB.getSelectionModel().selectedItemProperty().addListener(
    			new ChangeListener<EdiSystem>() {
    				@Override
    				public void changed (
    						ObservableValue<? extends EdiSystem> observable,
    							EdiSystem oldSystem, EdiSystem newSystem) {
    					readKomponentenData( (newSystem==null) ? -1L : newSystem.getId());
    				}
    			}
    	);

    	komponenteCB.setCellFactory(new Callback<ListView<EdiKomponente>,ListCell<EdiKomponente>> () {
    		@Override
    		public ListCell<EdiKomponente> call(ListView<EdiKomponente> param) {
    			return new ListCell<EdiKomponente>() {
    				@Override
    				protected void updateItem(EdiKomponente komponente, boolean empty) {
    					super.updateItem(komponente, empty);
    					if(komponente != null) {
    						setText(komponente.getName());
    					}
    				}
    			};
    		}
    	});
    	
    	komponenteCB.setButtonCell(new ListCell<EdiKomponente> () {
    		@Override
    		protected void updateItem(EdiKomponente komponente, boolean empty) {
    			super.updateItem(komponente, empty);
    			setText( (empty) ? "?" : komponente.getName());
    		}    		
    	});
    	komponenteCB.getSelectionModel().selectedItemProperty().addListener(
    			new ChangeListener<EdiKomponente>() {
    				@Override
    				public void changed (
    						ObservableValue<? extends EdiKomponente> observable,
    							EdiKomponente oldKomponente, EdiKomponente newKomponente) {
    					ediKomponentenId.set((newKomponente==null)? 0L : newKomponente.getId());
    				}
    			}
    	);
    }	
    	
//    private void setupEntityManager() {
//    	if (em == null) {
//    		EntityManagerFactory factory = Persistence.createEntityManagerFactory(PERSISTENCE_UNIT_NAME);
//    		em = factory.createEntityManager();
//    	}	
//    }
    
    @FXML
    void okPressed(ActionEvent event) {
		unbind();
		close(event);
    }

    @FXML
    void escapePressed(ActionEvent event) {
    	ediKomponentenId.set(0L);;
		unbind();
    	close(event);
    }
    
    @FXML
    void newPartnerPressed(ActionEvent event) {
    	Node source = (Node) event.getSource();
    	Stage stage = (Stage) source.getScene().getWindow();
    	System.out.println("KomponentenAuswahlController.newPartnerPressed() stage:"+stage);
    	String name = "";
    	while(true) {
			name = Dialogs.create().owner(stage).title(APPL_TITLE)
					.masthead("Neuen EdiPartner anlegen")
					.message("Bezeichnung des Partners:")
					.showTextInput(name);
    		if (name == null)	
    			break;
    		if (name.length() < 3) {
    			Dialogs.create().owner(stage).title(APPL_TITLE)
    				.masthead("Korrektur erforderlich")
    				.message("Bitte einen Namen mit mind. 3 Zeichen eingeben")
    				.showWarning();
    			continue;
    		}
    		if (partnerNameExist(name)) {
    			Dialogs.create().owner(stage).title(APPL_TITLE)
    				.masthead("Duplikatsprüfung")
    				.message("Ein Partner mit diesem Namen ist bereits vorhanden")
    				.showWarning();
    			continue;
    		}
    		EdiPartner ediPartner = new EdiPartner(name);
    		try {
    			entityManager.getTransaction().begin();
    			entityManager.persist(ediPartner);
    			entityManager.getTransaction().commit();
    			partnerList.add(ediPartner);
    			partnerCB.getSelectionModel().select(ediPartner);
    		} catch (RuntimeException e) {
    			Dialogs.create().owner(stage).title(APPL_TITLE)
					.masthead("Datenbankfehler")
					.message("Fehler beim Anlegen des Partners")
					.showException(e);
    			continue;
    		}
    		break;
    	}	
    }
    
    private boolean partnerNameExist(String name) {
    	for (EdiPartner p : partnerList) {
    		if (name.equalsIgnoreCase(p.getName()))
    			return true;
    	}
		return false;
	}

    
    @FXML
    void newSystemPressed(ActionEvent event) {
    	Node source = (Node) event.getSource();
    	Stage stage = (Stage) source.getScene().getWindow();
    	String name = "";
    	EdiPartner aktPartner = partnerCB.getSelectionModel().getSelectedItem();
    	String partnerName = aktPartner.getName();
    	while(true) {
			name = Dialogs.create().owner(stage).title(APPL_TITLE)
					.masthead("Neues System für Partner " + partnerName + " anlegen")
					.message("Bezeichnung des neuen Systems:")
					.showTextInput(name);
    		if (name == null)	
    			break;
    		if (name.length() < 3) {
    			Dialogs.create().owner(stage).title(APPL_TITLE)
					.masthead("Korrektur erforderlich")
					.message("Der System-Name muß mindestens 3 Zeichen lang sein")
					.showWarning();
    			continue;
    		}
    		if (systemNameExist(name)) {
    			Dialogs.create().owner(stage).title(APPL_TITLE)
					.masthead("Duplikatsprüfung")
					.message("Für den Partner " + partnerName + " ist bereits ein gleichnamiges System vorhanden")
					.showWarning();
    			continue;
    		}
    		EdiSystem ediSystem = new EdiSystem(name, aktPartner);
    		try {
    			entityManager.getTransaction().begin();
    			entityManager.persist(ediSystem);
    			entityManager.getTransaction().commit();
    		} catch (RuntimeException e) {
    			Dialogs.create().owner(stage).title(APPL_TITLE)
					.masthead("Datenbankfehler")
					.message("Fehler beim Anlegen des Systems")
					.showException(e);
    			continue;
    		}
    		systemList.add(ediSystem);
    		systemCB.getSelectionModel().select(ediSystem);
    		break;
    	}	
    }

    private boolean systemNameExist(String name) {
    	for (EdiSystem p : systemList) {
    		if (name.equalsIgnoreCase(p.getName()))
    			return true;
    	}
		return false;
	}
    
    @FXML
    void newKomponentePressed(ActionEvent event) {
    	Node source = (Node) event.getSource();
    	Stage stage = (Stage) source.getScene().getWindow();
    	String name = "";
    	EdiSystem aktSystem = systemCB.getSelectionModel().getSelectedItem();
    	String systemName = aktSystem.getFullname();
    	while(true) {
			name = Dialogs.create().owner(stage).title(APPL_TITLE)
					.masthead("Neue Komponente für das System " + systemName + " anlegen")
					.message("Bezeichnung der neuen Komponente:")
					.showTextInput(name);
    		if (name == null)	
    			break;
    		if (name.length() < 2) {
    			Dialogs.create().owner(stage).title(APPL_TITLE)
					.masthead("Korrektur erforderlich")
					.message("Der Komponenten-Name muß mindestens 2 Zeichen lang sein")
					.showWarning();
    			continue;
    		}
    		if (komponentenNameExist(name)) {
    			Dialogs.create().owner(stage).title(APPL_TITLE)
					.masthead("Duplikatsprüfung")
					.message("Für das System " + systemName + " ist bereits eine gleichnamige Komponente vorhanden")
					.showWarning();
    			continue;
    		} 
    		EdiKomponente ediKomponente = new EdiKomponente(name, aktSystem);
    		try {
    			entityManager.getTransaction().begin();
    			entityManager.persist(ediKomponente);
    			entityManager.getTransaction().commit();
    		} catch (RuntimeException e) {
    			Dialogs.create().owner(stage).title(APPL_TITLE)
					.masthead("Datenbankfehler")
					.message("Fehler beim Anlegen der Komponente")
					.showException(e);
    			continue;
    		}
    		komponentenList.add(ediKomponente);
    		komponenteCB.getSelectionModel().select(ediKomponente);
    		break;
    	}	

    }

    private boolean komponentenNameExist(String name) {
    	for (EdiKomponente k : komponentenList) {
    		if (name.equalsIgnoreCase(k.getName()))
    			return true;
    	}
		return false;
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
    
    /* *****************************************************************************
	 * 
	 * ****************************************************************************/
    
    private void checkFieldFromView() {
    	assert partnerCB != null : "fx:id=\"partnerCB\" was not injected: check your FXML file 'KomponentenAuswahl.fxml'.";
        assert systemCB != null : "fx:id=\"systemCB\" was not injected: check your FXML file 'KomponentenAuswahl.fxml'.";
        assert komponenteCB != null : "fx:id=\"komponenteCB\" was not injected: check your FXML file 'KomponentenAuswahl.fxml'.";
        assert btnNewSystem != null : "fx:id=\"btnNewSystem\" was not injected: check your FXML file 'KomponentenAuswahl.fxml'.";
        assert btnNewPartner != null : "fx:id=\"btnNewPartner\" was not injected: check your FXML file 'KomponentenAuswahl.fxml'.";
        assert btnNewKomponente != null : "fx:id=\"btnNewKomponente\" was not injected: check your FXML file 'KomponentenAuswahl.fxml'.";
        assert lbFehlertext != null : "fx:id=\"fehlertext\" was not injected: check your FXML file 'KomponentenAuswahl.fxml'.";
	}
}