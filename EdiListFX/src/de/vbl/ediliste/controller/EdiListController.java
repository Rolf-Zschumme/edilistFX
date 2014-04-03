package de.vbl.ediliste.controller;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
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

import de.vbl.ediliste.main.EdiListMain;
import de.vbl.ediliste.model.EdiEintrag;
import de.vbl.ediliste.model.EdiNrListElement;

public class EdiListController {
	private static final String PERSISTENCE_UNIT_NAME = "EdiListFX";

    @FXML
    private ResourceBundle resources;

    @FXML
    private URL location;

    @FXML
    private TableColumn<EdiNrListElement, String> ediNrCol;

    @FXML
    private TitledPane ediEintragPane;

    @FXML
    private TextField senderName;

    @FXML
    private TextField datenart;

    @FXML
    private TextField ediLastChange;

    @FXML
    private TableColumn<EdiNrListElement, String> ediKurzbezCol;

    @FXML
    private TableView<EdiNrListElement> ediNrTable;

    @FXML
    private TitledPane szenarioPane;

    @FXML
    private TextField ediBezeichnung;

    @FXML
    private TextField empfaengerName;

    @FXML
    private TitledPane anbindungPane;

    private EdiListMain mainApp;
    private EntityManager em;
    private ObservableList<EdiNrListElement> ediNrArrayList = FXCollections.observableArrayList();
    private int maxEdiNr;
    private Stage primaryStage;

    public void setMainApp(EdiListMain mainApp) {
    	this.mainApp = mainApp;
    }
    
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
    }
    
    
    private void setupBindings() {
    	
    	ediNrTable.setItems(ediNrArrayList);
    	
    	ediNrCol.setCellValueFactory(new PropertyValueFactory<EdiNrListElement,String>("ediNr"));
    	ediKurzbezCol.setCellValueFactory(new PropertyValueFactory<EdiNrListElement,String>("ediKurzBez"));
		
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
    void xnewEdiNr(ActionEvent event) {

    	em.getTransaction().begin();
    	
    	EdiEintrag ediEintrag = new EdiEintrag();
    	ediEintrag.setEdiNr(getHighestEdiNr()+1);
    	
      	em.persist(ediEintrag);
    	em.getTransaction().commit();
    	
    	ediNrArrayList.add(new EdiNrListElement(ediEintrag.getId(),
    										   ediEintrag.getEdiNr(),	
    										   ediEintrag.getKurzBez() ));
    	if (ediEintrag.getEdiNr() > maxEdiNr)
    		maxEdiNr = ediEintrag.getEdiNr();
    }

    
    @FXML
    void newEdiNr(ActionEvent event) {
    
    	FXMLLoader loader = new FXMLLoader();
    	loader.setLocation(getClass().getResource("../view/NeuerEdiEintrag.fxml"));
    	try {
			loader.load();
		} catch (IOException e) {
			e.printStackTrace();
		}
    	NeuerEdiEintragController controller = loader.getController();
    	
    	Stage modal_dialog = new Stage(StageStyle.DECORATED);

    	Parent root = loader.getRoot();
    	Scene scene = new Scene(root);

    	modal_dialog.initModality(Modality.APPLICATION_MODAL);
    	modal_dialog.initOwner(primaryStage);
    	modal_dialog.setScene(scene);
    	modal_dialog.showAndWait();
    	
    	
    	
    	System.out.println(getClass().getName() + ".newEdiNr wurde beendet.");
    }    
    
    
    
    
    
    
    
    
    
    private Integer getHighestEdiNr() {
//    	Query query = em.createNativeQuery("SELECT ediNr FROM EdiEintrag WHERE ediNr=(SELECT MAX(ediNr) FROM EdiEintrag");
//    	Object o = query.getSingleResult();
    	return maxEdiNr;
    }
    
    private void checkFieldFromView() {
		assert ediNrCol != null : "fx:id=\"ediNrCol\" was not injected: check your FXML file 'EdiListe.fxml'.";
		assert ediEintragPane != null : "fx:id=\"ediEintragPane\" was not injected: check your FXML file 'EdiListe.fxml'.";
		assert senderName != null : "fx:id=\"senderName\" was not injected: check your FXML file 'EdiListe.fxml'.";
		assert datenart != null : "fx:id=\"datenart\" was not injected: check your FXML file 'EdiListe.fxml'.";
		assert ediLastChange != null : "fx:id=\"ediLastChange\" was not injected: check your FXML file 'EdiListe.fxml'.";
		assert ediKurzbezCol != null : "fx:id=\"ediKurzbezCol\" was not injected: check your FXML file 'EdiListe.fxml'.";
		assert ediNrTable != null : "fx:id=\"ediNrTable\" was not injected: check your FXML file 'EdiListe.fxml'.";
		assert szenarioPane != null : "fx:id=\"szenarioPane\" was not injected: check your FXML file 'EdiListe.fxml'.";
		assert ediBezeichnung != null : "fx:id=\"ediBezeichnung\" was not injected: check your FXML file 'EdiListe.fxml'.";
		assert empfaengerName != null : "fx:id=\"empfaengerName\" was not injected: check your FXML file 'EdiListe.fxml'.";
		assert anbindungPane != null : "fx:id=\"anbindungPane\" was not injected: check your FXML file 'EdiListe.fxml'.";
	}
}
