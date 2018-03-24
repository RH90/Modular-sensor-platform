/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package main;

import com.sun.javafx.scene.control.skin.VirtualFlow;
import java.net.URL;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.ResourceBundle;
import java.util.concurrent.Callable;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Cell;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.text.Font;
import javafx.util.Callback;

/**
 * FXML Controller class
 *
 * @author Rilind
 */
public class Get_sensorController implements Initializable {
    private String UserName = "";
    private String UserPass = "";
    private int PortNr = 0;
    private String IP_address = "";
    private String Schema = "";

    @FXML
    private ListView lv;
    static int s;
    private ObservableList<String> list = null;

    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // TODO
//        getString();
        System.out.println("url: " + url.getPath());
        SQL sql = new SQL();

        try {
            sql.start(UserName,UserPass,PortNr,IP_address,Schema);
        } catch (ClassNotFoundException | SQLException ex) {
            Logger.getLogger(Get_sensorController.class.getName()).log(Level.SEVERE, null, ex);
        }
        try {
            list = sql.list(s);
        } catch (SQLException ex) {
            Logger.getLogger(Get_sensorController.class.getName()).log(Level.SEVERE, null, ex);
        }
        for (int i = 0; i < list.size(); i++) {
            System.out.println(list.get(i));
        }
        lv.setItems(list);
        
        Font anyfont = new Font("Consolas", 12);
        lv.setCellFactory(new CallbackImpl(anyfont));
        System.out.println("node: " + getNode());
    }

    public void sensor_node(int s) {
        Get_sensorController.s = s;
    }

    public static int getNode() {
        return s;
    }

    @FXML
    private void search(ActionEvent event) {

    }

    @FXML
    private void get(ActionEvent event) {
        String ss = (String) lv.getSelectionModel().getSelectedItem();
        String[] array = ss.split("¤");
        FXMLDocumentController.sensor_on[s - 1] = true;
        FXMLDocumentController.id[s - 1] = Integer.parseInt(array[0].trim());
        System.out.println(Integer.parseInt(array[0].trim()));
        FXMLDocumentController.list_string_a[s - 1] = array[1];
        ((Node) (event.getSource())).getScene().getWindow().hide();

    }

    @FXML
    private void remove(ActionEvent event) {
        String ss = (String) lv.getSelectionModel().getSelectedItem();
        FXMLDocumentController.sensor_on[s - 1] = false;
        FXMLDocumentController.list_string_a[s - 1] = "No sensor!";
        ((Node) (event.getSource())).getScene().getWindow().hide();

    }

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
