package com.aimtrainer;

import javafx.scene.Scene;
import javafx.stage.Stage;

public class SceneManager {

    private final Stage stage;

    /**
     * Создаёт менеджер сцен для управления переключением между экранами.
     * 
     * @param stage основная сцена приложения
     */
    public SceneManager(Stage stage) {
        this.stage = stage;
    }

    /**
     * Отображает главное меню на экране.
     * Создаёт новый объект MenuScreen, устанавливает CSS стили и показывает сцену.
     */
    public void showMenu() {
        MenuScreen menu = new MenuScreen(this);
        Scene scene = new Scene(menu.getRoot(), 1024, 768);
        scene.getStylesheets().add(getClass().getResource("/style.css").toExternalForm());
        stage.setScene(scene);
    }

    /**
     * Запускает режим игры в зависимости от выбранного типа.
     * Создаёт соответствующую сцену (MovingHoldScene, DisappearingScene или
     * FallingCatchScene),
     * применяет CSS стили и показывает на экране.
     * 
     * @param type тип игры (MOVING_HOLD, DISAPPEARING, FALLING_CATCH)
     * @throws IllegalArgumentException если передан неизвестный тип игры
     */
    public void startGame(GameType type) {
        Scene scene;
        switch (type) {
            case MOVING_HOLD:
                scene = new MovingHoldScene(this).buildScene();
                break;
            case DISAPPEARING:
                scene = new DisappearingScene(this).buildScene();
                break;
            case FALLING_CATCH:
                scene = new FallingCatchScene(this).buildScene();
                break;
            default:
                throw new IllegalArgumentException("Unknown game type: " + type);
        }
        scene.getStylesheets().add(getClass().getResource("/style.css").toExternalForm());
        stage.setScene(scene);
    }

    /**
     * Запускает режим игры по номеру уровня (для обратной совместимости).
     * Преобразует номер уровня в тип игры и запускает соответствующий режим.
     * 
     * @param level номер уровня (1 = MOVING_HOLD, 2 = DISAPPEARING, 3 =
     *              FALLING_CATCH)
     * @throws IllegalArgumentException если передан неизвестный номер уровня
     * @deprecated используйте {@link #startGame(GameType)} вместо этого
     */
    public void startLevel(int level) {
        switch (level) {
            case 1:
                startGame(GameType.MOVING_HOLD);
                break;
            case 2:
                startGame(GameType.DISAPPEARING);
                break;
            case 3:
                startGame(GameType.FALLING_CATCH);
                break;
            default:
                throw new IllegalArgumentException("Unknown level: " + level);
        }
    }

    /**
     * Возвращает основную сцену приложения.
     * 
     * @return Stage - сцена JavaFX
     */
    public Stage getStage() {
        return stage;
    }
}
