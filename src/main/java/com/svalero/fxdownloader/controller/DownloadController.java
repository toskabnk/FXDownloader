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
import org.json.JSONArray;

import javax.swing.filechooser.FileSystemView;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.concurrent.atomic.AtomicBoolean;

public class DownloadController implements Initializable {

    public TextField tfURL;
    public TextField tfFileName;
    public TextField tfDelay;
    public Button btStart;
    public Button btPause;
    public Button btCancel;
    public ProgressBar pbProgress;
    public Label lbStatus;
    public Label lbProgressSize;
    public Label lbSize;
    private String urlTxt;
    private DownloadTask downloadTask;
    private File file;
    private DirectoryChooser directoryChooser = new DirectoryChooser();
    private JSONArray message;
    private AtomicBoolean downloadPaused = new AtomicBoolean(false);

    private static final Logger logger = LogManager.getLogger(DownloadController.class);

    public DownloadController(String urlTxt){
        logger.info("Descarga " + urlTxt + " creada.");
        this.urlTxt = urlTxt;
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        //Conseguimos el directorio del usuario y añadimos la carpeta de descargas
        String downloadsFolder = System.getProperty("user.home") + File.separator + "Downloads" + File.separator;

        //Extraemos el nombre del archivo a descargar
        String fileName = urlTxt.substring(urlTxt.lastIndexOf("/") + 1);

        //Escribimos el nombre del archivo en el cuadro de texto
        tfFileName.setText(fileName);

        //Escribimos el directorio donde se va a guardar el archivo
        tfURL.setText(downloadsFolder);
        directoryChooser.setInitialDirectory(new File(downloadsFolder));

        //Desactivamos el boton de pausa ya que no hay descarga iniciada
        btPause.setDisable(true);
        btCancel.setDisable(true);

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
            File downloadFile = new File(tfURL.getText(), tfFileName.getText());

            //Si es archivo es null no hacemos nada
            if (downloadFile == null) {
                //Desactivamos el boton de pausa
                btPause.setDisable(true);
                btCancel.setDisable(true);
                return;
            }

            //Desactivamos el boton de Iniciar y activamos el de Pausa
            btPause.setDisable(false);
            btStart.setDisable(true);
            btCancel.setDisable(false);

            //Creamos la tarea de la descarga
            downloadTask = new DownloadTask(urlTxt, downloadFile, downloadPaused);

            //Bindeamos la barra de progreso a el progreso de la tarea
            pbProgress.progressProperty().unbind();
            pbProgress.progressProperty().bind(downloadTask.progressProperty());

            //Estados de la tarea
            downloadTask.stateProperty().addListener((observableValue, oldState, newState) -> {
                //logger.info(observableValue.toString());
                if (newState == Worker.State.SUCCEEDED) {
                    Alert alert = new Alert(Alert.AlertType.INFORMATION);
                    logger.info("Descarga " + tfFileName.getText() + " ha sido finalizada con exito.");
                    alert.setContentText("La descarga " + tfFileName.getText() + " ha finalizado correctamente.");
                    alert.show();
                    btStart.setDisable(true);
                    btPause.setDisable(true);
                    btCancel.setDisable(true);
                } else if(newState == Worker.State.FAILED){
                    logger.info("Descarga " + tfFileName.getText() + " fallida.");
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setContentText("Ha habido un error con la descarga " + tfFileName.getText());
                    alert.show();
                    btStart.setDisable(false);
                    btPause.setDisable(true);
                    btCancel.setDisable(true);
                } else if (newState == Worker.State.CANCELLED) {
                    btStart.setDisable(false);
                    btPause.setDisable(true);
                    btCancel.setDisable(true);
                }

            });

            //Vamos actualizado el progreso de la descarga
            downloadTask.messageProperty().addListener((observableValue, oldValue, newValue) -> {
                message = new JSONArray(newValue);
                lbStatus.setText(message.get(2).toString());
                lbSize.setText(message.get(0).toString());
                lbProgressSize.setText(message.get(1).toString());

            });
            Integer delay = 0;
            try{
                delay = Integer.parseInt(tfDelay.getText());
            } catch (NumberFormatException nfe){
                delay = 0;
            }

            //Creamos el hilo de la descarga
            new java.util.Timer().schedule(
                    new java.util.TimerTask() {
                        @Override
                        public void run() {
                            new Thread(downloadTask).start();
                        }
                    },
                    1000 * delay);


        } catch (MalformedURLException murle){
            murle.printStackTrace();
            logger.error("URL no valida " + murle.fillInStackTrace());
        }
    }

    @FXML
    public void pauseDownload(ActionEvent actionEvent) throws InterruptedException {
        if(downloadPaused.get()){
            btPause.setText("Pausar");
            downloadPaused.set(false);
            synchronized (downloadTask) {
                downloadTask.notify();
            }
        } else {
            btPause.setText("Reanudar");
            downloadPaused.set(true);
        }
    }

    @FXML
    public void openSaveDialog(ActionEvent actionEvent){

    }

    @FXML
    public void cancelDownload(ActionEvent actionEvent){
        stop();
    }

    public void stop(){
        if(downloadTask != null){
            downloadTask.cancel();
            btStart.setDisable(false);
            lbStatus.setText("");
            lbSize.setText("");
            lbProgressSize.setText("");
        }
    }
}
