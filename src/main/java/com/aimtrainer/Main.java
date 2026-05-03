package com.aimtrainer;

import com.aimtrainer.ui.SceneManager;

import javafx.application.Application;
import javafx.stage.Stage;

/**
 * Точка входа в приложение и запуск самого JavaFX.
 * Запускает JavaFX и передаёт управление SceneManager.
 */
public class Main extends Application {

    /**
     * Начало работы JavaFX приложения.
     * Устанавливает размер окна (1024x768), дизайн приложения,
     * создаёт SceneManager и показывает главное меню.
     * 
     * @param primaryStage основная сцена JavaFX приложения
     */
    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Java mini-games");
        primaryStage.setWidth(1024);
        primaryStage.setHeight(768);
        primaryStage.setResizable(false);

        // SceneManager управляет переключением между меню и уровнями
        SceneManager sceneManager = new SceneManager(primaryStage);
        sceneManager.showMenu();

        primaryStage.show();
    }

    /**
     * Точка входа в приложение.
     * Запускает JavaFX приложение.
     * 
     * @param args аргументы командной строки
     */
    public static void main(String[] args) {
        launch(args);
    }
}
