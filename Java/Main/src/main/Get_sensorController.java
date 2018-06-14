package main;

import java.net.URL;
import java.sql.SQLException;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.text.Font;
import javafx.util.Callback;

/*
 * Get_semsorController.java
 *
 * This is the window for retiving as sensor for a sepcific node
 * 
 * Created: 2018/02/27
 * @author Rilind Hasanaj <rilind.hasanaj0018@stud.hkr.se>
 */
public class Get_sensorController implements Initializable {


    @FXML
    private ListView lv;
    static int s;
    private ObservableList<String> list = null;

    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        System.out.println("url: " + url.getPath());
        
        // connect to Mysql database
        SQL sql = new SQL();
        int a =0;
        try {
            a = sql.start(MainController.UserName, MainController.UserPass, MainController.PortNr, MainController.IP_address, MainController.Schema);
        } catch (ClassNotFoundException | SQLException ex) {
            Logger.getLogger(Get_sensorController.class.getName()).log(Level.SEVERE, null, ex);
        }
        if(a!=-1){
        try {
            list = sql.list(s);// get sensor list
        } catch (SQLException ex) {
            Logger.getLogger(Get_sensorController.class.getName()).log(Level.SEVERE, null, ex);
        }
        for (int i = 0; i < list.size(); i++) {
            System.out.println(list.get(i));
        }
        
        // set ListView element to the sensor list from the database
        lv.setItems(list); 

        Font anyfont = new Font("Consolas", 12);
        lv.setCellFactory(new CallbackImpl(anyfont));
        System.out.println("node: " + getNode());
        }else{
            lv.setItems(FXCollections.observableArrayList("No DB connection!"));
        }
    }
    // get sensor node
    public static int getNode() {
        return s;
    }

    @FXML
    private void search(ActionEvent event) {

    }

    // Button handler for the Get button, when pressed it will return a selected sensor from the list
    // to a sepcifc node
    @FXML
    private void get(ActionEvent event) {
        String ss = (String) lv.getSelectionModel().getSelectedItem();
        if(ss!=null){
        String[] array = ss.split("¤");
        MainController.sensor_on[s - 1] = true;
        MainController.id[s - 1] = Integer.parseInt(array[0].trim());
        System.out.println(Integer.parseInt(array[0].trim()));
        MainController.list_string_a[s - 1] = array[1];
        ((Node) (event.getSource())).getScene().getWindow().hide();
        }

    }

    // removes a sensor from a specific node
    @FXML
    private void remove(ActionEvent event) {
        String ss = (String) lv.getSelectionModel().getSelectedItem();
        MainController.sensor_on[s - 1] = false;
        MainController.list_string_a[s - 1] = "No sensor!";
        ((Node) (event.getSource())).getScene().getWindow().hide();

    }

    // adds a list to the ListView
    private class CallbackImpl implements Callback<ListView<Object>, ListCell<Object>> {

        private final Font anyfont;

        public CallbackImpl(Font anyfont) {
            this.anyfont = anyfont;
        }

        // @Override
        @Override
        public ListCell<Object> call(final ListView<Object> param) {
            final ListCell<Object> cell = new ListCell<Object>() {
                @Override
                protected void updateItem(final Object item, final boolean empty) {
                    super.updateItem(item, empty);
                    setFont(anyfont);
                    setText((String) item);
                }
            };
            return cell;
        }
    }

}
