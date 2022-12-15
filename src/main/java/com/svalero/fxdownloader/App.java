package com.svalero.fxdownloader;

import com.svalero.fxdownloader.controller.AppController;
import com.svalero.fxdownloader.util.R;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class App extends Application {

    public void init() throws Exception {
        super.init();
    }

    @Override
    public void start(Stage stage) throws Exception {
        FXMLLoader loader = new FXMLLoader();
        loader.setLocation(R.getUI("mainWindows.fxml"));
        loader.setController(new AppController());
        VBox vbox = loader.load();

        Scene scene = new Scene(vbox);
        stage.setScene(scene);
        stage.setTitle("FXDownloader");
        stage.show();
    }

    public void stop() throws Exception{
        super.stop();
    }

    public static void main(String[] args) {
        launch();
    }
}
