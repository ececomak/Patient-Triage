package com.ece.ertriage;

import com.ece.ertriage.ui.DashboardView;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class MainApp extends Application {
    @Override
    public void start(Stage stage) {
        DashboardView root = new DashboardView();
        Scene scene = new Scene(root, 1200, 720);

        stage.setTitle("Acil Servis Triaj — Hasta Önceliklendirme");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}