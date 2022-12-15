package com.svalero.fxdownloader.controller;

import com.svalero.fxdownloader.task.DownloadTask;
import javafx.concurrent.Worker;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.swing.filechooser.FileSystemView;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ResourceBundle;

public class DownloadController implements Initializable {

    public Label lbName;
    public TextField tfURL;

    public TextField tfFileName;
    public Button btStart;
    public Button btPause;
    public ProgressBar pbProgress;
    public Label lbStatus;
    public Label lbProgressSize;
    public Label lbSize;
    private String urlTxt;
    private DownloadTask downloadTask;
    private File file;
    private DirectoryChooser directoryChooser = new DirectoryChooser();


    private static final Logger logger = LogManager.getLogger(DownloadController.class);

    public DownloadController(String urlTxt){
        logger.info("Descarga " + urlTxt + " iniciada.");
        this.urlTxt = urlTxt;
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        //Conseguimos el directorio del usuario y añadimos la carpeta de descargas
        String downloadsFolder = System.getProperty("user.home") + "\\Downloads\\";

        //Extraemos el nombre del archivo a descargar
        String fileName = urlTxt.substring(urlTxt.lastIndexOf("/") + 1);

        //Escribimos el nombre del archivo en el cuadro de texto
        tfFileName.setText(fileName);

        //Escribimos el directorio donde se va a guardar el archivo
        tfURL.setText(downloadsFolder);
        directoryChooser.setInitialDirectory(new File(downloadsFolder));

        //Desactivamos el boton de pausa ya que no hay descarga iniciada
        btPause.setDisable(true);

        //Añadimos un evento si pulsamos el cuadro de texto del directorio de descarga
        tfURL.setOnMouseClicked(new EventHandler<MouseEvent>() {
            public void handle(MouseEvent event) {
                //Conseguimos el stage y abrimos un cuadro de dialogo de seleccion de directorio
                Stage stage = (Stage) tfURL.getScene().getWindow();
                file = directoryChooser.showDialog(stage);
                if(file != null){
                    tfURL.setText(file.getAbsolutePath());
                } else{
                    directoryChooser.setInitialDirectory(new File(downloadsFolder));
                }

            }
        });
    }

    @FXML
    public void startDownload(ActionEvent actionEvent){
        try {

            //Creamos el archivo donde se va a descargar con el directorio y nombre del archivo
            File downloadFile = new File(directoryChooser.getInitialDirectory(), tfFileName.getText());

            //Si es archivo es null no hacemos nada
            if (downloadFile == null) {
                //Desactivamos el boton de pausa
                btPause.setDisable(true);
                return;
            }

            //Desactivamos el boton de Iniciar y activamos el de Pausa
            btPause.setDisable(false);
            btStart.setDisable(true);

            //Creamos la tarea de la descarga
            downloadTask = new DownloadTask(urlTxt, downloadFile);

            //Bindeamos la barra de progreso a el progreso de la tarea
            pbProgress.progressProperty().unbind();
            pbProgress.progressProperty().bind(downloadTask.progressProperty());

            //Estados de la tarea
            downloadTask.stateProperty().addListener((observableValue, oldState, newState) -> {
                logger.info(observableValue.toString());
                if (newState == Worker.State.SUCCEEDED) {
                    Alert alert = new Alert(Alert.AlertType.INFORMATION);
                    alert.setContentText("La descarga " + tfFileName.getText() + " ha finalizado correctamente.");
                    alert.show();
                    btStart.setDisable(false);
                    btPause.setDisable(true);
                }

            });

            //Vamos actualizado el progreso de la descarga
            downloadTask.messageProperty().addListener((observableValue, oldValue, newValue) -> {
                lbStatus.setText(newValue);
            });

            //Creamos el hilo de la descarga
            new Thread(downloadTask).start();

        } catch (MalformedURLException murle){
            murle.printStackTrace();
            logger.error("URL no valida " + murle.fillInStackTrace());
        }
    }

    @FXML
    public void pauseDownload(ActionEvent actionEvent){
        stop();
    }

    @FXML
    public void openSaveDialog(ActionEvent actionEvent){

    }

    public void stop(){
        if(downloadTask != null){
            downloadTask.cancel();
            btStart.setDisable(false);
        }
    }
}
