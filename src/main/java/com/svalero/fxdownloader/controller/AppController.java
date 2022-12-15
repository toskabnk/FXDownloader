package com.svalero.fxdownloader.controller;


import com.svalero.fxdownloader.util.R;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class AppController {

    //Elementos de la mainWindows a los que vamos a acceder
    public Button button;
    public TextField tfUrl;
    public TabPane tpDownloads;
    private Map<String, DownloadController> allDownloads;

    String logPath = "C:\\myFolder\\myFile.txt";

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
    public void openLog(ActionEvent actionEvent){
        System.out.println(System.getProperty("user.dir"));
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
