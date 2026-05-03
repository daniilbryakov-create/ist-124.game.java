package com.aimtrainer;

import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

public class MenuScreen {

    private final VBox root;

    /**
     * Создаёт экран главного меню.
     * Показывает три кнопки для выбора режима игры:
     * - Движущийся шарик (удерживай прицел)
     * - Исчезающий шарик (кликни до исчезновения)
     * - Падающие шарики (лови корзинкой)
     * 
     * @param sceneManager менеджер сцен для переключения на выбранный режим
     */
    public MenuScreen(SceneManager sceneManager) {
        root = new VBox(20);
        root.setAlignment(Pos.CENTER);
        root.getStyleClass().add("menu-root");

        Label title = new Label("Java Mini-Games");
        title.getStyleClass().add("menu-title");

        Label subtitle = new Label("Выбери режим:");
        subtitle.getStyleClass().add("menu-subtitle");

        Button btn1 = makeBtn("🎯  Движущийся шарик  —  удерживай прицел", sceneManager, GameType.MOVING_HOLD);
        Button btn2 = makeBtn("⏱  Исчезающий шарик  —  успей кликнуть", sceneManager, GameType.DISAPPEARING);
        Button btn3 = makeBtn("🪣  Падающие шарики  —  лови корзинкой", sceneManager, GameType.FALLING_CATCH);

        root.getChildren().addAll(title, subtitle, btn1, btn2, btn3);
    }

    /**
     * Создаёт стилизованную кнопку меню с обработчиком события.
     * При клике на кнопку запускается выбранный режим игры.
     * 
     * @param text текст на кнопке
     * @param sm   менеджер сцен
     * @param type тип игры для запуска
     * @return готовая кнопка с обработчиком
     */
    private Button makeBtn(String text, SceneManager sm, GameType type) {
        Button btn = new Button(text);
        btn.getStyleClass().add("menu-button");
        btn.setOnAction(e -> sm.startGame(type));
        return btn;
    }

    /**
     * Возвращает корневой элемент меню для отображения в сцене.
     * 
     * @return VBox с элементами меню
     */
    public Parent getRoot() {
        return root;
    }
}
