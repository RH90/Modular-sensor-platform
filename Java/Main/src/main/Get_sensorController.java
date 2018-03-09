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
        lv.setStyle("-fx-font-name:Consolas;-fx-font-size:12");
        // FXMLDocumentController.Label_list_a.get(0).setText("hellp");
        SQL sql = new SQL();

        try {
            sql.start();
        } catch (ClassNotFoundException | SQLException ex) {
            Logger.getLogger(Get_sensorController.class.getName()).log(Level.SEVERE, null, ex);
        }
        try {
            list = sql.list(s);
        } catch (SQLException ex) {
            Logger.getLogger(Get_sensorController.class.getName()).log(Level.SEVERE, null, ex);
        }
        lv.setItems(list);
//        VirtualFlow ch = (VirtualFlow) lv.getChildrenUnmodifiable();
//        for (int i = 0; i < ch.getCellCount(); i++) {
//            System.out.println("he");
//            Cell cell = ch.getCell(i);
//            Font anyfont = new Font("Consolas", 12);
//            cell.setFont(anyfont);
//        }
        Font anyfont = new Font("Consolas", 12);
        lv.setCellFactory(new Callback<ListView<Object>, ListCell<Object>>() {

            // @Override
            public ListCell<Object> call(final ListView<Object> param) {
                final ListCell<Object> cell = new ListCell<Object>() {
                    @Override
                    protected void updateItem(final Object item, final boolean empty) {
                        if (isEmpty()) {
                            setFont(anyfont);
                            setText((String) item);
                            super.updateItem(item, empty);

                            //  setStyle("-fx-font-name: Consolas");
                        }
                    }
                };
                return cell;
            }
        });
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

}
