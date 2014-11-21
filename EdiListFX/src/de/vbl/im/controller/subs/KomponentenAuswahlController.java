package de.vbl.im.controller.subs;

import java.util.List;
import java.util.Optional;
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

import org.controlsfx.dialog.Dialog.Actions;
import org.controlsfx.dialog.Dialogs;

import de.vbl.im.model.InKomponente;
import de.vbl.im.model.InPartner;
import de.vbl.im.model.InSystem;

/**
 * @author p01023
 * 
 *	20.08.2014 RZ EntityManager wird stets neu erstellt -> aktuelle Tabelleninhalte
 *  25.08.2014 RZ EntityManager wird vom rufenden Programm übergeben!
 *  27.08.2014 RZ getSelectionModel() ergänzt
 */
public class KomponentenAuswahlController {
	public static enum KomponentenTyp {	SENDER, RECEIVER };
	private static final String APPL_TITLE = "Integration-Manager";
	private EntityManager entityManager; 
    
    @FXML private ResourceBundle resources;

    @FXML private ComboBox<InPartner> partnerCB; 
    @FXML private ComboBox<InSystem> systemCB;
    @FXML private ComboBox<InKomponente> komponenteCB;
    
    @FXML private Button btnOK;
    @FXML private Button btnNewSystem;
    @FXML private Button btnNewPartner;
    @FXML private Button btnNewKomponente;

    @FXML private Label lbFehlertext;
    @FXML private Label lbSenderReseiver;


	private LongProperty inKomponentenId;
	
	private ObservableList<InPartner>      partnerList;     //  = FXCollections.observableArrayList();
	private ObservableList<InSystem> 		systemList;      //  = FXCollections.observableArrayList(); 
	private ObservableList<InKomponente>   komponentenList; //  = FXCollections.observableArrayList(); 

	public Actions getResponse () {
		if (inKomponentenId == null || inKomponentenId.get()==0L)
			return Actions.CANCEL;
		else
			return Actions.OK;
	}

//	public Long getSelectedKomponentenId() {
//		return inKomponentenId.get();
//	}

	public InKomponente getSelectedKomponente() {
		return komponenteCB.getSelectionModel().getSelectedItem();
	}
	
	public void setKomponente(KomponentenTyp typ, InKomponente komponente, EntityManager em) {
    	System.out.println(getClass().getName()+".setKomponenten called");
    	entityManager = em;
        readPartnerData();
    	
		if (typ == KomponentenTyp.SENDER)
			lbSenderReseiver.setText("Sender");
		else
			lbSenderReseiver.setText("Empfänger");

		if (komponente != null) {
			inKomponentenId.set(komponente.getId());
				
			partnerCB.getSelectionModel().select(komponente.getInSystem().getinPartner());
			systemCB.getSelectionModel().select(komponente.getInSystem());
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
    	
    	inKomponentenId = new SimpleLongProperty();
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
    	// alle InPartner lesen
    	TypedQuery<InPartner> tq = entityManager.createQuery(
    		  "SELECT p FROM InPartner p ORDER BY p.name", InPartner.class);
    	List<InPartner> resultList = tq.getResultList();
    	for (InPartner inPartner : resultList) {
    		partnerList.add(inPartner);
    	}
    }
    
    private void readSystemData(Long partnerId) {
    	systemList.clear();
		TypedQuery<InSystem> tq = entityManager.createQuery(
				"SELECT s FROM InSystem s ORDER BY s.name", InSystem.class);
		List<InSystem> resultList = tq.getResultList();
		for (InSystem inSystem : resultList) {
			if (partnerId == inSystem.getinPartner().getId()) {
				systemList.add(inSystem);
			}
		}
    }

    private void readKomponentenData(Long systemId) {
    	komponentenList.clear();
		TypedQuery<InKomponente> tq = entityManager.createQuery(
		  "SELECT k FROM InKomponente k ORDER BY k.name",InKomponente.class);
		List<InKomponente> resultList = tq.getResultList();
		for (InKomponente inKomponente : resultList) {
			if (systemId == inKomponente.getInSystem().getId()) {
				komponentenList.add(inKomponente);
			}
		}
    }
    
    private void setupBindings() {
    	btnOK.disableProperty().bind(Bindings.equal(0L, inKomponentenId));
    	
    	btnNewSystem.disableProperty().bind(Bindings.isNull(partnerCB.getSelectionModel().selectedItemProperty()));
    	systemCB.disableProperty().bind(Bindings.isNull(partnerCB.getSelectionModel().selectedItemProperty()));
    	btnNewKomponente.disableProperty().bind(Bindings.isNull(systemCB.getSelectionModel().selectedItemProperty()));
    	komponenteCB.disableProperty().bind(Bindings.isNull(systemCB.getSelectionModel().selectedItemProperty()));
    	
    	partnerCB.setCellFactory(new Callback<ListView<InPartner>,ListCell<InPartner>> () {
    		@Override
    		public ListCell<InPartner> call(ListView<InPartner> param) {
    			return new ListCell<InPartner>() {
    				@Override
    				protected void updateItem(InPartner inPartner, boolean empty) {
    					super.updateItem(inPartner, empty);
    					if(inPartner != null) {
    						setText(inPartner.getName());
    					}
    				}
    			};
    		}
    	});
    	partnerCB.setButtonCell(new ListCell<InPartner> () {
    		@Override
    		protected void updateItem(InPartner inPartner, boolean empty) {
    			super.updateItem(inPartner, empty);
    			setText( (empty) ? "?" : inPartner.getName());
    		}    		
    	});
    	partnerCB.getSelectionModel().selectedItemProperty().addListener(
    			new ChangeListener<InPartner>() {
    				@Override
    				public void changed (
    						ObservableValue<? extends InPartner> observable,
    							InPartner oldPartner, InPartner newPartner) {
    					readSystemData( (newPartner==null) ? -1L : newPartner.getId());
    					komponenteCB.getSelectionModel().select(null);
    				}
    			}
    	);
    	
    	systemCB.setCellFactory(new Callback<ListView<InSystem>,ListCell<InSystem>> () {
    		@Override
    		public ListCell<InSystem> call(ListView<InSystem> param) {
    			return new ListCell<InSystem>() {
    				@Override
    				protected void updateItem(InSystem system, boolean empty) {
    					super.updateItem(system, empty);
    					if(system != null) {
    						setText(system.getName());
    					}
    				}
    			};
    		}
    	}); 
    	systemCB.setButtonCell(new ListCell<InSystem> () {
    		@Override
    		protected void updateItem(InSystem system, boolean empty) {
    			super.updateItem(system, empty);
    			setText( (empty) ? "?" : system.getName());
    		}    		
    	});
    	systemCB.getSelectionModel().selectedItemProperty().addListener(
    			new ChangeListener<InSystem>() {
    				@Override
    				public void changed (
    						ObservableValue<? extends InSystem> observable,
    							InSystem oldSystem, InSystem newSystem) {
    					readKomponentenData( (newSystem==null) ? -1L : newSystem.getId());
    				}
    			}
    	);

    	komponenteCB.setCellFactory(new Callback<ListView<InKomponente>,ListCell<InKomponente>> () {
    		@Override
    		public ListCell<InKomponente> call(ListView<InKomponente> param) {
    			return new ListCell<InKomponente>() {
    				@Override
    				protected void updateItem(InKomponente komponente, boolean empty) {
    					super.updateItem(komponente, empty);
    					if(komponente != null) {
    						setText(komponente.getName());
    					}
    				}
    			};
    		}
    	});
    	
    	komponenteCB.setButtonCell(new ListCell<InKomponente> () {
    		@Override
    		protected void updateItem(InKomponente komponente, boolean empty) {
    			super.updateItem(komponente, empty);
    			setText( (empty) ? "?" : komponente.getName());
    		}    		
    	});
    	komponenteCB.getSelectionModel().selectedItemProperty().addListener(
    			new ChangeListener<InKomponente>() {
    				@Override
    				public void changed (
    						ObservableValue<? extends InKomponente> observable,
    							InKomponente oldKomponente, InKomponente newKomponente) {
    					inKomponentenId.set((newKomponente==null)? 0L : newKomponente.getId());
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
    	inKomponentenId.set(0L);
    	komponenteCB.getSelectionModel().clearSelection();
		unbind();
    	close(event);
    }
    
    @FXML
    void newPartnerPressed(ActionEvent event) {
    	Node source = (Node) event.getSource();
    	Stage stage = (Stage) source.getScene().getWindow();
    	String aktName = "";
    	while(true) {
			Optional<String> newName = Dialogs.create().owner(stage).title(APPL_TITLE)
					.masthead("Neuen Partner anlegen")
					.message("Bezeichnung des Partners:")
					.showTextInput(aktName);
    		if (!newName.isPresent()) {
    			break;
    		}
			aktName = newName.get().trim();
    		if (aktName.length() < 3) {
    			Dialogs.create().owner(stage).title(APPL_TITLE)
    				.masthead("Korrektur erforderlich")
    				.message("Bitte einen Namen mit mind. 3 Zeichen eingeben")
    				.showWarning();
    			continue;
    		}
    		if (partnerNameExist(aktName)) {
    			Dialogs.create().owner(stage).title(APPL_TITLE)
    				.masthead("Duplikatsprüfung")
    				.message("Ein Partner mit diesem Namen ist bereits vorhanden")
    				.showWarning();
    			continue;
    		}
    		InPartner inPartner = new InPartner(aktName);
    		try {
    			entityManager.getTransaction().begin();
    			entityManager.persist(inPartner);
    			entityManager.getTransaction().commit();
    			partnerList.add(inPartner);
    			partnerCB.getSelectionModel().select(inPartner);
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
    	for (InPartner p : partnerList) {
    		if (name.equalsIgnoreCase(p.getName()))
    			return true;
    	}
		return false;
	}

    
    @FXML
    void newSystemPressed(ActionEvent event) {
    	Node source = (Node) event.getSource();
    	Stage stage = (Stage) source.getScene().getWindow();
    	String aktName = "";
    	InPartner aktPartner = partnerCB.getSelectionModel().getSelectedItem();
    	String partnerName = aktPartner.getName();
    	while(true) {
			Optional<String> newName = Dialogs.create().owner(stage).title(APPL_TITLE)
					.masthead("Neues System für Partner " + partnerName + " anlegen")
					.message("Bezeichnung des neuen Systems:")
					.showTextInput(aktName);
    		if (newName.isPresent() == false) {
    			break;
    		}
			aktName = newName.get().trim();
    		if (aktName.length() < 3) {
    			Dialogs.create().owner(stage).title(APPL_TITLE)
					.masthead("Korrektur erforderlich")
					.message("Der System-Name muß mindestens 3 Zeichen lang sein")
					.showWarning();
    			continue;
    		}
    		if (systemNameExist(aktName)) {
    			Dialogs.create().owner(stage).title(APPL_TITLE)
					.masthead("Duplikatsprüfung")
					.message("Für den Partner " + partnerName + " ist bereits ein gleichnamiges System vorhanden")
					.showWarning();
    			continue;
    		}
    		InSystem inSystem = new InSystem(aktName, aktPartner);
    		try {
    			entityManager.getTransaction().begin();
    			entityManager.persist(inSystem);
    			entityManager.getTransaction().commit();
    			System.out.println("commit-->" + inSystem.getinPartner().getInSystem().contains(inSystem));
    		} catch (RuntimeException e) {
    			Dialogs.create().owner(stage).title(APPL_TITLE)
					.masthead("Datenbankfehler")
					.message("Fehler beim Anlegen des Systems")
					.showException(e);
    			continue;
    		}
    		systemList.add(inSystem);
    		systemCB.getSelectionModel().select(inSystem);
    		break;
    	}	
    }

    private boolean systemNameExist(String name) {
    	for (InSystem p : systemList) {
    		if (name.equalsIgnoreCase(p.getName()))
    			return true;
    	}
		return false;
	}
    
    @FXML
    void newKomponentePressed(ActionEvent event) {
    	Node source = (Node) event.getSource();
    	Stage stage = (Stage) source.getScene().getWindow();
    	String aktName = "";
    	InSystem aktSystem = systemCB.getSelectionModel().getSelectedItem();
    	String systemName = aktSystem.getFullname();
    	while(true) {
			Optional<String> newName = Dialogs.create().owner(stage).title(APPL_TITLE)
					.masthead("Neue Komponente für das System " + systemName + " anlegen")
					.message("Bezeichnung der neuen Komponente:")
					.showTextInput(aktName);
    		if (newName.isPresent() == false) {
    			break;
    		}
			aktName = newName.get().trim();
    		if (aktName.length() < 2) {
    			Dialogs.create().owner(stage).title(APPL_TITLE)
					.masthead("Korrektur erforderlich")
					.message("Der Komponenten-Name muß mindestens 2 Zeichen lang sein")
					.showWarning();
    			continue;
    		}
    		if (komponentenNameExist(aktName)) {
    			Dialogs.create().owner(stage).title(APPL_TITLE)
					.masthead("Duplikatsprüfung")
					.message("Für das System " + systemName + " ist bereits eine gleichnamige Komponente vorhanden")
					.showWarning();
    			continue;
    		} 
    		InKomponente inKomponente = new InKomponente(aktName, aktSystem);
    		try {
    			entityManager.getTransaction().begin();
    			entityManager.persist(inKomponente);
    			entityManager.getTransaction().commit();
    		} catch (RuntimeException e) {
    			Dialogs.create().owner(stage).title(APPL_TITLE)
					.masthead("Datenbankfehler")
					.message("Fehler beim Anlegen der Komponente")
					.showException(e);
    			continue;
    		}
    		komponentenList.add(inKomponente);
    		komponenteCB.getSelectionModel().select(inKomponente);
    		break;
    	}	
    }

    private boolean komponentenNameExist(String name) {
    	for (InKomponente k : komponentenList) {
    		if (name.equalsIgnoreCase(k.getName()))
    			return true;
    	}
		return false;
	}

    private void unbind() {
//    	tfInNr.textProperty().unbindBidirectional(integration.inNrProperty());
//    	tfKurzbez.textProperty().unbindBidirectional(integration.kurzBezProperty());
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