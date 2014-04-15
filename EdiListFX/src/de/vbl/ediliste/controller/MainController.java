package de.vbl.ediliste.controller;

import java.io.IOException;
import java.net.URL;
import java.util.Collection;
import java.util.ResourceBundle;

import javafx.beans.binding.Bindings;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Cursor;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Dialogs;
import javafx.scene.control.Dialogs.DialogOptions;
import javafx.scene.control.Dialogs.DialogResponse;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.TitledPane;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.persistence.Query;

import de.vbl.ediliste.controller.KomponentenAuswahlController.KomponentenTyp;
import de.vbl.ediliste.model.EdiEintrag;
import de.vbl.ediliste.model.EdiEmpfaenger;
import de.vbl.ediliste.model.EdiKomponente;
import de.vbl.ediliste.model.EdiPartner;
import de.vbl.ediliste.model.EdiSystem;
import de.vbl.ediliste.view.EdiNrListElement;
import de.vbl.ediliste.view.PartnerListElement;

public class MainController {
	private static final String PERSISTENCE_UNIT_NAME = "EdiListFX";
	private static final String SICHERHEITSABFRAGE = "Sicherheitsabfrage";

    @FXML private ResourceBundle resources;
    @FXML private URL location;

    @FXML private TabPane tabPaneObjekte;    
    @FXML private Tab tabEdiNr;
    @FXML private TableView<EdiNrListElement> tableEdiNrAuswahl;
    @FXML private TableColumn<EdiNrListElement, String> tColAuswahlEdiNr;
    @FXML private TableColumn<EdiNrListElement, String> tColAuswahlEdiNrBezeichnung;

    @FXML private Tab tabPartner;
    @FXML private TableView<PartnerListElement> tablePartnerAuswahl;
    @FXML private TableColumn<PartnerListElement, String> tColAuswahlPartnerName;
    @FXML private TableColumn<PartnerListElement, Integer> tColAuswahlPartnerSysteme;
    @FXML private TableColumn<PartnerListElement, Integer> tColAuswahlPartnerKomponenten;

    @FXML private Tab tabSysteme;
    @FXML private TableView<EdiSystem> tableSystemAuswahl;
    @FXML private TableColumn<EdiSystem, String> tColSelSystemSystemName;
    @FXML private TableColumn<EdiSystem, String> tColSelSystemPartnerName;
    @FXML private TableColumn<EdiSystem, Integer> tColSelSystemKomponenten;

    @FXML private Tab tabKomponenten;
    @FXML private TableView<EdiKomponente> tableKomponentenAuswahl;   
    @FXML private TableColumn<EdiKomponente, String> tColSelKompoKomponten;
    @FXML private TableColumn<EdiKomponente, String> tColSelKompoSysteme;
    @FXML private TableColumn<EdiKomponente, String> tColSelKompoPartner;
    
    
    @FXML private TitledPane paneSzenario;
    @FXML private TitledPane paneAnbindung;
    @FXML private TitledPane paneEdiEintrag;
    
    @FXML private TextField tfEdiBezeichnung;
    @FXML private TextArea  taEdiBeschreibung;
    @FXML private TextField tfDatenart1;
    @FXML private TextField tfDatenart2;
    @FXML private TextField tfDatenart3;
    @FXML private TextField ediLastChange;

    @FXML private Button btnEdiEintragSpeichern;
    @FXML private Button btnEmpfaenger1;
    @FXML private Button btnEmpfaenger2;
    @FXML private Button btnEmpfaenger3;
    @FXML private Button btnSender;
    @FXML private Button btnNewEdiNr;
    @FXML private Button btnDeleteEdiEintrag;
    
    private EntityManager em;
    private ObservableList<EdiNrListElement> ediNrArrayList = FXCollections.observableArrayList();
    private ObservableList<PartnerListElement> ediPartnerList = FXCollections.observableArrayList();
    private ObservableList<EdiSystem> ediSystemList = FXCollections.observableArrayList();
    private ObservableList<EdiKomponente> ediKomponentenList = FXCollections.observableArrayList();
    
    private int maxEdiNr;
    private Stage primaryStage;

    private BooleanProperty ediEintragIsChanged = new SimpleBooleanProperty(false);
    private BooleanProperty senderIsSelected = new SimpleBooleanProperty(false);
    
    private EdiEintrag aktEdi;
    private EdiEmpfaenger aktEmpfaenger[] = new EdiEmpfaenger[3];
    
    public void setStage(Stage temp) {
    	primaryStage = temp;
    }

    @FXML
    void btnUeber(ActionEvent event) {
    	Dialogs.showInformationDialog(primaryStage, 
    			"Version 0.1 - 15.04.2014 mit Sender- und Empf�nger-Auswahl", 
    			"VBL-Tool zur Verwaltung der EDI-Liste", "EDI-Liste");
    }
    
    /* ------------------------------------------------------------------------
     * initialize() is the controllers "main"-method 
     * it is called after loading "EdiListe.fxml" 
     * ----------------------------------------------------------------------*/
    @FXML
    void initialize() {
    	checkFieldFromView();
        setupEntityManager();
        loadEdiNrListData();
        setupBindings();
        
        tableEdiNrAuswahl.getSelectionModel().selectedItemProperty().addListener(
        		new ChangeListener<EdiNrListElement>() {
        			@Override
        			public void changed(
        					ObservableValue<? extends EdiNrListElement> observable,
        					EdiNrListElement oldValue, EdiNrListElement newValue) {
        				System.out.println("oldValue=" + ((oldValue == null) ? "null" : oldValue.ediNrProperty().get()) 
        							   + "  newValue=" + ((newValue == null) ? "null" : newValue.ediNrProperty().get()) );
        				if (ediEintragIsChanged.get() == true) {
        					if ( Dialogs.showConfirmDialog(primaryStage, "Soll die �nderungen am EDI-Eintrag " +
        							aktEdi.getEdiNrStr() + " \"" + aktEdi.getBezeichnung() + "\"" +
        							" gespeichert werden?", SICHERHEITSABFRAGE,"EDI-Liste", 
        							DialogOptions.OK_CANCEL) == DialogResponse.OK) {
        						aktEdiEintragSpeichern();
        					}
        						
        				}
        				final EdiEintrag defEdi = new EdiEintrag();
        				if (oldValue != null) {
        					tfEdiBezeichnung.textProperty().unbindBidirectional(defEdi.bezeichnungProperty());
        					tfEdiBezeichnung.textProperty().unbindBidirectional(aktEdi.bezeichnungProperty());
        				}
        				if (newValue != null) {
        					aktEdi = em.find(EdiEintrag.class, newValue.getEdiId());
        					ediEintragIsChanged.set(false);
        					tfEdiBezeichnung.textProperty().bindBidirectional(aktEdi.bezeichnungProperty());
        					taEdiBeschreibung.setText(aktEdi.getBezeichnung());
        					btnSender.setText(aktEdi.getKomponente()==null ? "" : aktEdi.getKomponente().getFullname());
        					senderIsSelected.set(aktEdi.getKomponente()!=null);
        			    	Collection<EdiEmpfaenger> empfaengerList = aktEdi.getEdiEmpfaenger();
        					for (int i=0; i<3;++i) {
        						aktEmpfaenger[i] = null;
        						if (empfaengerList.iterator().hasNext())
        							aktEmpfaenger[i] = empfaengerList.iterator().next();
        					}
        					btnEmpfaenger1.setText(aktEmpfaenger[0]==null ? "" : aktEmpfaenger[0].getKomponente().getFullname());
        					btnEmpfaenger2.setText(aktEmpfaenger[1]==null ? "" : aktEmpfaenger[0].getKomponente().getFullname());
        					btnEmpfaenger3.setText(aktEmpfaenger[2]==null ? "" : aktEmpfaenger[0].getKomponente().getFullname());
        		        	paneEdiEintrag.textProperty().set("EDI-Eintrag " + aktEdi.getEdiNrStr());
        				}
        				else {
        					tfEdiBezeichnung.textProperty().bindBidirectional(defEdi.bezeichnungProperty());
        					taEdiBeschreibung.setText("");
        					btnSender.setText("");
        					btnEmpfaenger1.setText("");
        					btnEmpfaenger2.setText("");
        					btnEmpfaenger3.setText("");
        		    		senderIsSelected.set(false);
        		        	paneEdiEintrag.textProperty().set("EDI-Eintrag");
        				}
        			}
				}
        );
        tabPaneObjekte.getSelectionModel().selectedItemProperty().addListener(
        		new ChangeListener<Tab>() {
        			@Override
        			public void changed(ObservableValue<? extends Tab> ov, Tab talt, Tab tneu) {
        				final Tab akttab = tneu;
        				primaryStage.getScene().setCursor(Cursor.WAIT);
        				Task<Void> task = new Task<Void>() {
        					@Override
        					protected Void call() throws Exception {
        						if (akttab.equals(tabPartner)) {
        							// loadPartnerListData();
        							ediPartnerList.clear();
        					    	Query query = em.createQuery("SELECT p FROM EdiPartner p ORDER BY p.name");
        					    	for (Object p : query.getResultList()) {
        					    		ediPartnerList.add(new PartnerListElement( (EdiPartner) p));
        					    	}
        						}
        						else if(akttab.equals(tabSysteme)) {
        							// loadSystemListData();
        							ediSystemList.clear();
        					    	Query query = em.createQuery("SELECT s FROM EdiSystem s ORDER BY s.name");
        					    	for (Object s : query.getResultList()) {
        					    		ediSystemList.add((EdiSystem)s);
        					    	}
        						}
        						else if(akttab.equals(tabKomponenten)) {
        							// loadKomponentenListData();
        							ediKomponentenList.clear();
        					    	Query query = em.createQuery("SELECT k FROM EdiKomponente k ORDER BY k.name");
        					    	for (Object k : query.getResultList()) {
        					    		ediKomponentenList.add((EdiKomponente)k);
        					    	}	
        						}
        						//  
        						return null;
        					}
        				};
        				task.setOnSucceeded(new EventHandler<WorkerStateEvent>() {
							@Override
							public void handle(WorkerStateEvent event) {
		        				primaryStage.getScene().setCursor(Cursor.DEFAULT);
							}
						});
        				new Thread(task).start();
        			}
				}
        );
    }
    
    @FXML
    void ediEintragSpeichern(ActionEvent event) {
		aktEdiEintragSpeichern();
    }
    
    protected void aktEdiEintragSpeichern() {
		em.getTransaction().begin();
		em.persist(aktEdi);
		em.getTransaction().commit();
		ediEintragIsChanged.set(false);
		System.out.println("Der Eintrag " + aktEdi.getEdiNrStr() + " wurde gespeichert");
	}

    
	private void setupBindings() {
    	
    	tableEdiNrAuswahl.setItems(ediNrArrayList);
    	tColAuswahlEdiNr.setCellValueFactory(new PropertyValueFactory<EdiNrListElement,String>("ediNr"));
    	tColAuswahlEdiNrBezeichnung.setCellValueFactory(new PropertyValueFactory<EdiNrListElement,String>("bezeichnung"));
    	
    	tfEdiBezeichnung.disableProperty().bind(Bindings.isNull(tableEdiNrAuswahl.getSelectionModel().selectedItemProperty()));
    	btnDeleteEdiEintrag.disableProperty().bind(Bindings.isNull(tableEdiNrAuswahl.getSelectionModel().selectedItemProperty()));
    	btnSender.disableProperty().bind(Bindings.isNull(tableEdiNrAuswahl.getSelectionModel().selectedItemProperty()));
    	btnEmpfaenger1.disableProperty().bind(Bindings.not(senderIsSelected));
    	btnEdiEintragSpeichern.disableProperty().bind(Bindings.not(ediEintragIsChanged));

    	// paneSzenario.textProperty().bind(ediEintrag.szenarioNameProperty());
    	
    	tablePartnerAuswahl.setItems(ediPartnerList);
    	tColAuswahlPartnerName.setCellValueFactory(new PropertyValueFactory<PartnerListElement,String>("name"));
    	tColAuswahlPartnerSysteme.setCellValueFactory(new PropertyValueFactory<PartnerListElement,Integer>("anzSysteme"));
    	tColAuswahlPartnerKomponenten.setCellValueFactory(new PropertyValueFactory<PartnerListElement,Integer>("anzKomponenten"));
    	
    	tableSystemAuswahl.setItems(ediSystemList);
    	tColSelSystemSystemName.setCellValueFactory(new PropertyValueFactory<EdiSystem,String>("name"));
    	tColSelSystemPartnerName.setCellValueFactory(new PropertyValueFactory<EdiSystem,String>("partnerName"));
    	tColSelSystemKomponenten.setCellValueFactory(new PropertyValueFactory<EdiSystem,Integer>("AnzKomponenten"));
    	
    	tableKomponentenAuswahl.setItems(ediKomponentenList);
    	tColSelKompoKomponten.setCellValueFactory(new PropertyValueFactory<EdiKomponente,String>("name"));
    	tColSelKompoSysteme.setCellValueFactory(new PropertyValueFactory<EdiKomponente,String>("systemName"));
    	tColSelKompoPartner.setCellValueFactory(new PropertyValueFactory<EdiKomponente,String>("partnerName"));
	}

	private void loadEdiNrListData() {
    	Query query = em.createQuery("SELECT e.id, e.ediNr, e.bezeichnung FROM EdiEintrag e ORDER BY e.ediNr");
    	ediNrArrayList.clear();
    	Integer max = 0;
    	for (Object zeile  : query.getResultList()) {
    		Object[] obj = (Object[]) zeile;
			ediNrArrayList.add(new EdiNrListElement( (Long) obj[0], (Integer) obj[1], (String) obj[2]));
			max = (Integer) obj[1]; 
    	}	
    	maxEdiNr = max;
	}
	
//	private void loadPartnerListData() {
//		ediPartnerList.clear();
//    	Query query = em.createQuery("SELECT p FROM EdiPartner p ORDER BY p.name");
//    	for (Object p : query.getResultList()) {
//    		ediPartnerList.add(new PartnerListElement( (EdiPartner) p));
//    	}
//	}
//	
//	private void loadSystemListData() {
//		ediSystemList.clear();
//    	Query query = em.createQuery("SELECT s FROM EdiSystem s ORDER BY s.name");
//    	for (Object s : query.getResultList()) {
//    		ediSystemList.add((EdiSystem)s);
//    	}
//	}
	
//	private void loadKomponentenListData() {
//		ediKomponentenList.clear();
//    	Query query = em.createQuery("SELECT k FROM EdiKomponente k ORDER BY k.name");
//    	for (Object k : query.getResultList()) {
//    		ediKomponentenList.add((EdiKomponente)k);
////    		EdiKomponente kompo = (EdiKomponente) k;
////    		System.out.println("Komponente read: " + kompo.getName() );
//    	}
//	}
	
    private void setupEntityManager() {
    	EntityManagerFactory factory = Persistence.createEntityManagerFactory(PERSISTENCE_UNIT_NAME);
    	em = factory.createEntityManager();
    }
    
	/* *****************************************************************************
	 * 
	 * ****************************************************************************/
    @FXML
    void newEdiNr(ActionEvent event) {
    
    	FXMLLoader loader = new FXMLLoader();
    	loader.setLocation(getClass().getResource("../view/NeuerEdiEintrag.fxml"));
    	try {
			loader.load();
		} catch (IOException e) {
			e.printStackTrace();
		}
    	Parent root = loader.getRoot();
    	Scene scene = new Scene(root);
    	
    	Stage dialog = new Stage(StageStyle.UTILITY);
    	dialog.initModality(Modality.APPLICATION_MODAL);
    	dialog.initOwner(primaryStage);
    	
    	NeuerEdiEintragController dialogController = loader.getController();
    	
    	dialog.setScene(scene);
    	dialog.setX(primaryStage.getX() + 250);
    	dialog.setY(primaryStage.getY() + 100);
    	dialog.showAndWait();

    	if (dialogController.getResponse() == DialogResponse.OK) {
    		EdiEintrag newEE = dialogController.getNewEdiEintrag();
			EdiNrListElement newListElement = new EdiNrListElement(newEE.getId(),newEE.getEdiNr(),newEE.getBezeichnung());
			ediNrArrayList.add(newListElement);
			if (newEE.getEdiNr() > maxEdiNr) 
				maxEdiNr = newEE.getEdiNr();
			tableEdiNrAuswahl.getSelectionModel().select(newListElement);
    	}
    }    
    
    @FXML
    void deleteEdiEintrag(ActionEvent event) {
    	EdiNrListElement selectedlistElement = tableEdiNrAuswahl.getSelectionModel().getSelectedItem();
    	if (selectedlistElement != null) {
    		int ediNr = selectedlistElement.ediNrProperty().get();
    		DialogResponse response = Dialogs.showConfirmDialog(primaryStage, 
    				"EDI-Eintrag mit der Nr. " + ediNr + " wirklich l�schen?",
    				SICHERHEITSABFRAGE,"", DialogOptions.OK_CANCEL);
    		if (response == DialogResponse.OK) {
    			long id = selectedlistElement.getEdiId();
    			EdiEintrag ediEintrag = em.find(EdiEintrag.class, id);
    			if (ediEintrag==null) {
    				System.out.println("FEHLER: EDI-Eintrag mit der ID "+ id + " und der Nr. "+ 
    								 ediNr + " ist nicht (mehr) gespeichert");
    			}
    			else {
	        		em.getTransaction().begin();
	        		em.remove(ediEintrag);
	        		em.getTransaction().commit();
    			}	
        		ediNrArrayList.remove(selectedlistElement);
    			tableEdiNrAuswahl.getSelectionModel().clearSelection();
    		}
    	}
    }
    
    @FXML
    void senderButton(ActionEvent event) {

    	Stage dialog = new Stage(StageStyle.UTILITY);
    	FXMLLoader loader = loadKomponentenAuswahl(dialog, 100, 250); 

    	KomponentenAuswahlController komponentenAuswahlController = loader.getController();
    	Long aktSenderId = (aktEdi.getKomponente()==null) ? 0L : aktEdi.getKomponente().getId();
    	komponentenAuswahlController.setKomponente(KomponentenTyp.SENDER, aktSenderId);
    	dialog.showAndWait();
    	if (komponentenAuswahlController.getResponse() == DialogResponse.OK ) {
	    	Long selKomponentenID = komponentenAuswahlController.getSelectedKomponentenId();
    	    if (aktSenderId != selKomponentenID	) {
   	    		aktEdi.setKomponente(em.find(EdiKomponente.class, selKomponentenID));
   	    		senderIsSelected.set(true);
    	    	btnSender.setText(aktEdi.getKomponente().getFullname());
    	    	ediEintragIsChanged.set(true);
    	    }
    	}
    }

    @FXML
    void empfaengerButton1(ActionEvent event) {
    	String ret = empfaengerButton(1);
    	if (ret != null) {
			btnEmpfaenger1.setText(ret);
    	}
    }	
    @FXML
    void empfaengerButton2(ActionEvent event) {
    	String ret = empfaengerButton(2);
    	if (ret != null) {
			btnEmpfaenger2.setText(ret);
    	}
    }	
    @FXML
    void empfaengerButton3(ActionEvent event) {
    	String ret = empfaengerButton(3);
    	if (ret != null) {
			btnEmpfaenger3.setText(ret);
    	}
    }	
    	
    private String empfaengerButton(int btnNr) {
    	String ret = null;
    	Stage dialog = new Stage(StageStyle.UTILITY);
    	FXMLLoader loader = loadKomponentenAuswahl(dialog, 400, 350); 
    	
    	KomponentenAuswahlController komponentenAuswahlController = loader.getController();
    	Long aktEmpfaengerId = (aktEmpfaenger[btnNr]==null ? 0L : aktEmpfaenger[btnNr].getKomponente().getId());
    	komponentenAuswahlController.setKomponente(KomponentenTyp.RECEIVER, aktEmpfaengerId);
    	dialog.showAndWait();
    	if (komponentenAuswahlController.getResponse() == DialogResponse.OK ) {
    		Long selEmpfaengerID = komponentenAuswahlController.getSelectedKomponentenId();
    		if (aktEmpfaengerId != selEmpfaengerID) {
    			if (aktEmpfaenger[btnNr]==null) {
    				aktEmpfaenger[btnNr] = new EdiEmpfaenger(aktEdi);
    				em.persist(aktEmpfaenger[btnNr]);
    			}
    			aktEmpfaenger[btnNr].setKomponente(em.find(EdiKomponente.class,selEmpfaengerID));
    			ret = aktEmpfaenger[btnNr].getKomponente().getFullname();
    	    	ediEintragIsChanged.set(true);
    		}
    	}
    	return ret;
    }
    
    
    private FXMLLoader loadKomponentenAuswahl(Stage dialog, int xOffset, int yOffset) {
    	FXMLLoader loader = new FXMLLoader();
    	loader.setLocation(getClass().getResource("../view/KomponentenAuswahl.fxml"));
    	try {
    		loader.load();
    	} catch (IOException e) {
    		e.printStackTrace(); 
    	}
    	Parent root = loader.getRoot();
//    	System.out.println(getClass().getName() + ".loaderKomponentenAuswahl --> vor new Scene");
    	Scene scene = new Scene(root);
//    	System.out.println(getClass().getName() + ".loaderKomponentenAuswahl --> vor Modality");
    	dialog.initModality(Modality.APPLICATION_MODAL);
    	dialog.initOwner(primaryStage);
    	dialog.setTitle(primaryStage.getTitle());
    	dialog.setScene(scene);
    	dialog.setX(primaryStage.getX() + xOffset);
    	dialog.setY(primaryStage.getY() + yOffset);
		return loader;
	}

    private void checkFieldFromView() {
    	assert tabPaneObjekte != null : "fx:id=\"tabPaneObjekte\" was not injected: check your FXML file 'Main.fxml'.";

    	assert tabEdiNr != null : "fx:id=\"tabEdiNr\" was not injected: check your FXML file 'Main.fxml'.";
    	assert btnNewEdiNr != null : "fx:id=\"btnNewEdiNr\" was not injected: check your FXML file 'Main.fxml'.";
    	assert btnDeleteEdiEintrag != null : "fx:id=\"btnDeleteEdiEintrag\" was not injected: check your FXML file 'Main.fxml'.";
        assert tableEdiNrAuswahl != null : "fx:id=\"tableEdiNrAuswahl\" was not injected: check your FXML file 'Main.fxml'.";
        assert tColAuswahlEdiNr != null : "fx:id=\"tColAuswahlEdiNr\" was not injected: check your FXML file 'Main.fxml'.";
        assert tColAuswahlEdiNrBezeichnung != null : "fx:id=\"tColAuswahlEdiNrBezeichnung\" was not injected: check your FXML file 'Main.fxml'.";

        assert tabPartner != null : "fx:id=\"tabPartner\" was not injected: check your FXML file 'Main.fxml'.";
        assert tablePartnerAuswahl != null : "fx:id=\"tablePartnerAuswahl\" was not injected: check your FXML file 'Main.fxml'.";
        assert tColAuswahlPartnerName != null : "fx:id=\"tColAuswahlPartnerName\" was not injected: check your FXML file 'Main.fxml'.";
        assert tColAuswahlPartnerSysteme != null : "fx:id=\"tColAuswahlPartnerSysteme\" was not injected: check your FXML file 'Main.fxml'.";
        assert tColAuswahlPartnerKomponenten != null : "fx:id=\"tColAuswahlPartnerKomponenten\" was not injected: check your FXML file 'Main.fxml'.";

        assert tabSysteme != null : "fx:id=\"tabSysteme\" was not injected: check your FXML file 'Main.fxml'.";
        assert tableSystemAuswahl != null : "fx:id=\"tableSystemAuswahl\" was not injected: check your FXML file 'Main.fxml'.";
        assert tColSelSystemSystemName != null : "fx:id=\"tColSelSystemSystemName\" was not injected: check your FXML file 'Main.fxml'.";
        assert tColSelSystemPartnerName != null : "fx:id=\"tColSelSystemPartnerName\" was not injected: check your FXML file 'Main.fxml'.";
        assert tColSelSystemKomponenten != null : "fx:id=\"tColSelSystemKomponenten\" was not injected: check your FXML file 'Main.fxml'.";
        
        assert tabKomponenten != null : "fx:id=\"tabKomponenten\" was not injected: check your FXML file 'Main.fxml'.";
        assert tableKomponentenAuswahl != null : "fx:id=\"tableKomponentenAuswahl\" was not injected: check your FXML file 'Main.fxml'.";
        assert tColSelKompoKomponten != null : "fx:id=\"tColSelKompoKomponten\" was not injected: check your FXML file 'Main.fxml'.";
        assert tColSelKompoSysteme != null : "fx:id=\"tColSelKompoSysteme\" was not injected: check your FXML file 'Main.fxml'.";
        assert tColSelKompoPartner != null : "fx:id=\"tColSelKompoPartner\" was not injected: check your FXML file 'Main.fxml'.";

        assert paneAnbindung != null : "fx:id=\"paneAnbindung\" was not injected: check your FXML file 'Main.fxml'.";

        assert paneSzenario != null : "fx:id=\"paneSzenario\" was not injected: check your FXML file 'Main.fxml'.";

        assert paneEdiEintrag != null : "fx:id=\"paneEdiEintrag\" was not injected: check your FXML file 'Main.fxml'.";
        assert btnSender != null : "fx:id=\"btnSender\" was not injected: check your FXML file 'Main.fxml'.";
        assert tfEdiBezeichnung != null : "fx:id=\"tfEdiBezeichnung\" was not injected: check your FXML file 'Main.fxml'.";
        assert ediLastChange != null : "fx:id=\"ediLastChange\" was not injected: check your FXML file 'Main.fxml'.";
        assert tfDatenart1 != null : "fx:id=\"tfDatenart1\" was not injected: check your FXML file 'Main.fxml'.";
        assert btnEmpfaenger1 != null : "fx:id=\"btnEmpfaenger1\" was not injected: check your FXML file 'Main.fxml'.";
        assert btnEdiEintragSpeichern != null : "fx:id=\"btnEdiEintragSpeichern\" was not injected: check your FXML file 'Main.fxml'.";

    }
}
