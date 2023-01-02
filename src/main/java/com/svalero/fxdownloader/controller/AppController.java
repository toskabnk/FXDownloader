package com.svalero.fxdownloader.controller;


import com.svalero.fxdownloader.util.R;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class AppController {

    //Elementos de la mainWindows a los que vamos a acceder
    public Button button;
    public TextField tfUrl;
    public TabPane tpDownloads;
    private Map<String, DownloadController> allDownloads;

    public AppController(){
        allDownloads = new HashMap<>();
    }

    //Boton de Descargar
    @FXML
    public void startDownload(){
        String url = tfUrl.getText();
        tfUrl.clear();
        tfUrl.requestFocus();
        launchDownload(url);
    }

    @FXML
    public void stopAllDownloads(ActionEvent actionEvent){
        for(DownloadController downloadController : allDownloads.values()){
            downloadController.stop();
        }
    }

    @FXML
    public void openLog(ActionEvent actionEvent) throws IOException, IllegalArgumentException {
        if (Desktop.isDesktopSupported()) {
            try {
                File file = new File("log" + File.separator + "fxdownloader.log");
                Desktop.getDesktop().open(file);
            } catch (IOException ioe) {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setContentText("Ha habido un error.");
                alert.show();
            } catch (IllegalArgumentException iae){
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setContentText("Ha habido un error al abrir el log. Es posible que no exista.");
                alert.show();
            }
        } else {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setContentText("No soportado.");
            alert.show();
        }

    }

    private void launchDownload(String url){
        try {
            //Cargamos la ventana de la descarga
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(R.getUI("downloadWindows.fxml"));

            DownloadController downloadController = new DownloadController(url);
            loader.setController(downloadController);
            VBox downloadBox = loader.load();

            String fileName = url.substring(url.lastIndexOf("/") + 1);
            tpDownloads.getTabs().add(new Tab(fileName, downloadBox));

            allDownloads.put(url, downloadController);
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }
}
