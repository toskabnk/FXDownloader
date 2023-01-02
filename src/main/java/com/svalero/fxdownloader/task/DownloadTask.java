package com.svalero.fxdownloader.task;

import com.svalero.fxdownloader.controller.DownloadController;
import javafx.concurrent.Task;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONArray;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.net.URLConnection;

public class DownloadTask extends Task<Integer> {

    private URL url;
    private File file;

    private JSONArray message = new JSONArray();

    private static final Logger logger = LogManager.getLogger(DownloadController.class);

    public DownloadTask(String textURL, File file) throws MalformedURLException{
        url = new URL(textURL);
        this.file = file;
    }

    @Override
    protected Integer call() throws Exception {
        logger.trace("Descarga " + url.toString() + " iniciada");
        updateMessage("Conectando con el servidor...");

        URLConnection urlConnection = url.openConnection();
        double tamanio = urlConnection.getContentLength();
        double tamanioMb = tamanio / 1048576;
        message.put(0,"/" + Math.round(tamanioMb * 100) / 100d + "Mb");

        //Guardamos la descarga en el buffer
        BufferedInputStream inputStream = new BufferedInputStream(url.openStream());

        //Archivo donde guardamos la descarga
        FileOutputStream fileOutputStream = new FileOutputStream(file);
        byte[] dataBuffer = new byte[4096];
        int bytesRead;
        int totalRead = 0;
        double progress = 0.;

        //Leemos una secuenca de bytes del archivo y lo almacenamos en el buffer
        while ((bytesRead = inputStream.read(dataBuffer)) != -1){

            //Para que la descarga no sea tan rapida
            Thread.sleep(1);

            //Escribe la secuencia de bytes en el archivo local
            fileOutputStream.write(dataBuffer, 0, bytesRead);

            //Actualizamos el total leido
            totalRead += bytesRead;

            //Progreso de la descarga
            progress = Math.round(((double) totalRead / tamanio) * 100) / 100d;
            double totalReadMb = totalRead / 1048576;
            message.put(1,totalReadMb);
            message.put(2,progress * 100 + " %");
            updateProgress(progress, 1);
            updateMessage(String.valueOf(message));

            if(isCancelled()) {
                logger.info("Descarga " + url.toString() + " cancelada.");
                updateProgress(0, 0);
                message.put(2, "Cancelada");
                updateMessage(String.valueOf(message));
                return null;
            }
        }

        updateProgress(1, 1);
        updateMessage("100 %");

        logger.trace("Descarga " + url.toString() + "finalizada.");
        return null;
    }
}
