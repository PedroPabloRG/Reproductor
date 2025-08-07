package com.reproductormusica;

import com.reproductormusica.controller.MainController;
import com.reproductormusica.view.MainWindow;
import javafx.application.Application;
import javafx.stage.Stage;

/**
 * Clase principal del reproductor de música
 */
public class Main extends Application {
    
    @Override
    public void start(Stage primaryStage) {
        try {
            System.out.println("Iniciando controlador principal...");
            MainController controller = new MainController();
            
            System.out.println("Creando ventana principal...");
            MainWindow mainWindow = new MainWindow(primaryStage, controller);
            mainWindow.show();
            
            System.out.println("Aplicación iniciada correctamente");
            
        } catch (Exception e) {
            System.err.println("Error al iniciar la aplicación:");
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        System.out.println("Iniciando Reproductor de Música...");
        launch(args);
    }
}
