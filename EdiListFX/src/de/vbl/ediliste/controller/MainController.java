package de.vbl.ediliste.controller;

import javafx.scene.control.Dialogs;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

import javafx.beans.binding.Bindings;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Dialogs.DialogOptions;
import javafx.scene.control.Dialogs.DialogResponse;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
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
import de.vbl.ediliste.model.Komponente;
import de.vbl.ediliste.view.EdiNrListElement;

public class MainController {
	private static final String PERSISTENCE_UNIT_NAME = "EdiListFX";
	private static final String SICHERHEITSABFRAGE = "Sicherheitsabfrage";

    @FXML private ResourceBundle resources;
    @FXML private URL location;

    @FXML private TableView<EdiNrListElement> ediNrTable;
    @FXML private TableColumn<EdiNrListElement, String> ediNrCol;
    @FXML private TableColumn<EdiNrListElement, String> ediKurzbezCol;

    @FXML private TitledPane szenarioPane;
    @FXML private TitledPane anbindungPane;
    @FXML private TitledPane ediEintragPane;
    
    @FXML private TextField ediBezeichnung;
    @FXML private TextField datenart;
    @FXML private TextField ediLastChange;

    @FXML private Button btnEmpfaenger1;
    @FXML private Button btnSender;
    @FXML private Button btnNewEdiNr;
    @FXML private Button btnDeleteEdiEintrag;


    
    private EntityManager em;
    private ObservableList<EdiNrListElement> ediNrArrayList = FXCollections.observableArrayList();
    private int maxEdiNr;
    private Stage primaryStage;

    private BooleanProperty senderIsSelected = new SimpleBooleanProperty(false);
    
    private EdiEintrag aktEdi;
    
    public void setStage(Stage temp) {
    	primaryStage = temp;
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
        
        ediNrTable.getSelectionModel().selectedItemProperty().addListener(
        		new ChangeListener<EdiNrListElement>() {
        			@Override
        			public void changed(
        					ObservableValue<? extends EdiNrListElement> observable,
        					EdiNrListElement oldValue, EdiNrListElement newValue) {
        				System.out.println("oldValue=" + ((oldValue == null) ? "null" : oldValue.ediNrProperty().get()) 
        							   + "  newValue=" + ((newValue == null) ? "null" : newValue.ediNrProperty().get()) ); 
        				final EdiEintrag defEdi = new EdiEintrag();
        				if (oldValue != null) {
        					ediBezeichnung.textProperty().unbindBidirectional(defEdi.kurzBezProperty());
        					ediBezeichnung.textProperty().unbindBidirectional(aktEdi.kurzBezProperty());;
        				}
        				if (newValue != null) {
        					aktEdi = em.find(EdiEintrag.class, newValue.getEdiId());
        					ediBezeichnung.textProperty().bindBidirectional(aktEdi.kurzBezProperty());
        				}
        				else {
        					ediBezeichnung.textProperty().bindBidirectional(defEdi.kurzBezProperty());
        				}
        			}
				}
        );
    }
    
    private void setupBindings() {
    	
    	ediNrTable.setItems(ediNrArrayList);
    	
    	ediNrCol.setCellValueFactory(new PropertyValueFactory<EdiNrListElement,String>("ediNr"));
    	ediKurzbezCol.setCellValueFactory(new PropertyValueFactory<EdiNrListElement,String>("kurzBez"));
    	
    	btnDeleteEdiEintrag.disableProperty().bind(Bindings.isNull(ediNrTable.getSelectionModel().selectedItemProperty()));
    	btnSender.disableProperty().bind(Bindings.isNull(ediNrTable.getSelectionModel().selectedItemProperty()));
    	btnEmpfaenger1.disableProperty().bind(Bindings.not(senderIsSelected));

    	//		szenarioPane.textProperty().bind(ediEintrag.szenarioNameProperty());
	}

	private void loadEdiNrListData() {
    	Query query = em.createQuery("SELECT e.id, e.ediNr, e.kurzBez FROM EdiEintrag e ORDER BY e.ediNr");

    	ediNrArrayList.clear();
    	Integer max = 0;
    	for (Object zeile  : query.getResultList()) {
    		Object[] obj = (Object[]) zeile;
			ediNrArrayList.add(new EdiNrListElement( (Long) obj[0], (Integer) obj[1], (String) obj[2]));
			max = (Integer) obj[1]; 
    	}	
    	maxEdiNr = max;
	}
    	
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
			EdiNrListElement newListElement = new EdiNrListElement(newEE.getId(),newEE.getEdiNr(),newEE.getKurzBez());
			ediNrArrayList.add(newListElement);
			if (newEE.getEdiNr() > maxEdiNr) 
				maxEdiNr = newEE.getEdiNr();
			ediNrTable.getSelectionModel().select(newListElement);
    	}
    }    
    
    @FXML
    void deleteEdiEintrag(ActionEvent event) {
    	EdiNrListElement selectedlistElement = ediNrTable.getSelectionModel().getSelectedItem();
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
    			ediNrTable.getSelectionModel().clearSelection();
    		}
    	}
    }
    
    @FXML
    void senderButton(ActionEvent event) {
    	Stage dialog = new Stage(StageStyle.UTILITY);
    	FXMLLoader loader = loadKomponentenAuswahl(dialog); 

    	KomponentenAuswahlController komponentenAuswahlController = loader.getController();
    	komponentenAuswahlController.setKomponente(KomponentenTyp.SENDER, aktEdi.getKomponente());
  //  	System.out.println(getClass().getName() + ".senderButton --> vor dialogshowAndWait");

    	dialog.showAndWait();
    	
    	Komponente selKomponente = komponentenAuswahlController.selectedKomponenten();
    	if (aktEdi.getKomponente() != selKomponente) {
    		aktEdi.setKomponente(selKomponente);
    		btnSender.setText(selKomponente.getFullname());
    		senderIsSelected.set(true);
    	}
    }

    private FXMLLoader loadKomponentenAuswahl(Stage dialog) {
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
    	dialog.setX(primaryStage.getX() + 100);
    	dialog.setY(primaryStage.getY() + 250);
		return loader;
	}

	@FXML
    void empfaengerButton(ActionEvent event) {
    	Stage dialog = new Stage(StageStyle.UTILITY);
    	FXMLLoader loader = loadKomponentenAuswahl(dialog); 

    	KomponentenAuswahlController komponentenAuswahlController = loader.getController();
    	komponentenAuswahlController.setKomponente(KomponentenTyp.RECEIVER, aktEdi.getKomponente());
    	
    	dialog.showAndWait();
    	
    	Komponente selKomponente = komponentenAuswahlController.selectedKomponenten();
    	if (aktEdi.getKomponente() != selKomponente) {
    		aktEdi.setKomponente(selKomponente);
    	}

    }
    
    private void checkFieldFromView() {
        assert ediNrCol != null : "fx:id=\"ediNrCol\" was not injected: check your FXML file 'Main.fxml'.";
        assert datenart != null : "fx:id=\"datenart\" was not injected: check your FXML file 'Main.fxml'.";
        assert ediLastChange != null : "fx:id=\"ediLastChange\" was not injected: check your FXML file 'Main.fxml'.";
        assert ediKurzbezCol != null : "fx:id=\"ediKurzbezCol\" was not injected: check your FXML file 'Main.fxml'.";
        assert ediNrTable != null : "fx:id=\"ediNrTable\" was not injected: check your FXML file 'Main.fxml'.";
        assert ediBezeichnung != null : "fx:id=\"ediBezeichnung\" was not injected: check your FXML file 'Main.fxml'.";
        assert btnEmpfaenger1 != null : "fx:id=\"btnEmpfaenger1\" was not injected: check your FXML file 'Main.fxml'.";
        assert anbindungPane != null : "fx:id=\"anbindungPane\" was not injected: check your FXML file 'Main.fxml'.";
        assert ediEintragPane != null : "fx:id=\"ediEintragPane\" was not injected: check your FXML file 'Main.fxml'.";
        assert btnNewEdiNr != null : "fx:id=\"btnNewEdiNr\" was not injected: check your FXML file 'Main.fxml'.";
        assert btnDeleteEdiEintrag != null : "fx:id=\"btnDeleteEdiEintrag\" was not injected: check your FXML file 'Main.fxml'.";
        assert szenarioPane != null : "fx:id=\"szenarioPane\" was not injected: check your FXML file 'Main.fxml'.";
        assert btnSender != null : "fx:id=\"btnSender\" was not injected: check your FXML file 'Main.fxml'.";
	}
}
