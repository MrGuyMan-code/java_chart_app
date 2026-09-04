package com.application;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.net.URL;

public class MainApp extends Application {

    @Override
    public void start(Stage stage) throws Exception {
        // 1. Încarcă fișierul FXML
        FXMLLoader loader = new FXMLLoader();
        URL fxmlUrl = getClass().getResource("/com/application/main-page.FXML");
        loader.setLocation(fxmlUrl);
        Parent root = loader.load();

        // 2. Creează scena
        Scene scene = new Scene(root, 1000, 750);

        // 3. (Opțional) Adaugă CSS
        scene.getStylesheets().add(
            getClass().getResource("/com/application/styles.css").toExternalForm()
        );

        // 4. Configurează și afișează fereastra
        stage.setTitle("Crypto Candlestick Chart");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}