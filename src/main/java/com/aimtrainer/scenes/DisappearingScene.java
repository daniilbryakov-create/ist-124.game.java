package com.aimtrainer.scenes;

import javafx.animation.AnimationTimer;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Arc;
import javafx.scene.shape.ArcType;

import java.util.Random;

import com.aimtrainer.GameType;
import com.aimtrainer.game.Crosshair;
import com.aimtrainer.game.Target;
import com.aimtrainer.ui.SceneManager;

/**
 * DISAPPEARING — статичный шарик, исчезает через 2 секунды.
 * Нужно кликнуть ЛКМ пока он виден.
 * Рядом с мишенью — дуга обратного отсчёта.
 */
public class DisappearingScene {

    private static final double WIDTH = 1024;
    private static final double HEIGHT = 768;
    private static final double ARENA_TOP = 50;
    private static final int TOTAL = 30;
    private static final double RADIUS = 22;
    private static final double DISAPPEAR_T = 2.0;

    private final SceneManager sceneManager;
    private final Random random = new Random();

    private Pane arena;
    private Crosshair crosshair;
    private Label scoreLabel;
    private AnimationTimer gameLoop;

    private Target currentTarget;
    private Arc timerArc; // дуга обратного отсчёта вокруг мишени
    private int score = 0;
    private int spawned = 0;
    private boolean gameOver = false;
    private long gameStartNano;

    private double mouseX = WIDTH / 2;
    private double mouseY = HEIGHT / 2;

    /**
     * Создаёт сцену для режима "Исчезающий шарик".
     * 
     * @param sceneManager менеджер сцен для переключения меню
     */
    public DisappearingScene(SceneManager sceneManager) {
        this.sceneManager = sceneManager;
    }

    /**
     * Создаёт и настраивает сцену игры.
     * Инициализирует арену, прицел, HUD, обработчики мыши и запускает игровой цикл.
     * 
     * @return Scene - готовая сцена для отображения
     */
    public Scene buildScene() {
        arena = new Pane();
        arena.setPrefSize(WIDTH, HEIGHT);
        arena.setStyle("-fx-background-color: #1a1a2e;");

        HBox hud = buildHUD();

        StackPane root = new StackPane();
        root.getChildren().addAll(arena, hud);
        StackPane.setAlignment(hud, Pos.TOP_CENTER);

        crosshair = new Crosshair();
        crosshair.setPosition(WIDTH / 2, HEIGHT / 2);

        Pane crosshairLayer = new Pane();
        crosshairLayer.setPrefSize(WIDTH, HEIGHT);
        crosshairLayer.setMouseTransparent(true);
        crosshairLayer.getChildren().add(crosshair.getNode());

        root.getChildren().add(1, crosshairLayer);

        Scene scene = new Scene(root, WIDTH, HEIGHT);
        scene.setCursor(Cursor.NONE);

        root.setFocusTraversable(true);
        scene.windowProperty().addListener((obs, oldW, newW) -> {
            if (newW != null) {
                newW.requestFocus();
                root.requestFocus();
            }
        });

        scene.setOnMouseMoved(e -> {
            mouseX = e.getX();
            mouseY = e.getY();
        });
        scene.setOnMouseDragged(e -> {
            mouseX = e.getX();
            mouseY = e.getY();
        });
        scene.setOnMousePressed(e -> {
            root.requestFocus();
            scene.setCursor(Cursor.NONE);
            if (e.getButton() == MouseButton.PRIMARY && !gameOver)
                handleClick(e.getX(), e.getY());
        });
        scene.setOnMouseEntered(e -> {
            if (!gameOver) {
                root.requestFocus();
                scene.setCursor(Cursor.NONE);
            }
        });

        startGame();
        return scene;
    }

    /**
     * Создаёт интерфейс игрока (HUD) с текущим счётом и кнопками меню.
     * 
     * @return HBox - контейнер с элементами интерфейса
     */
    private HBox buildHUD() {
        scoreLabel = new Label("Score: 0/" + TOTAL);
        scoreLabel.getStyleClass().add("hud-label");

        Button restartBtn = new Button("⟳ Restart");
        restartBtn.getStyleClass().add("hud-button");
        restartBtn.setFocusTraversable(false);
        restartBtn.setOnAction(e -> restart());

        Button menuBtn = new Button("☰ Menu");
        menuBtn.getStyleClass().add("hud-button");
        menuBtn.setFocusTraversable(false);
        menuBtn.setOnAction(e -> {
            if (gameLoop != null)
                gameLoop.stop();
            sceneManager.showMenu();
        });

        HBox hud = new HBox(20, scoreLabel, new HBox(10, restartBtn, menuBtn));
        hud.setAlignment(Pos.CENTER);
        hud.setPadding(new Insets(10));
        hud.setMaxHeight(40);
        hud.setPickOnBounds(false);
        return hud;
    }

    /**
     * Инициализирует игру, создаёт первую мишень и запускает основной игровой цикл.
     * Цикл обновляет позицию прицела, отслеживает время исчезновения и проверяет
     * окончание игры.
     */
    private void startGame() {
        score = 0;
        spawned = 0;
        gameOver = false;
        gameStartNano = System.nanoTime();
        scoreLabel.setText("Score: 0/" + TOTAL);
        spawnTarget();

        gameLoop = new AnimationTimer() {
            @Override
            public void handle(long now) {
                if (gameOver)
                    return;
                crosshair.setPosition(mouseX, mouseY);

                if (currentTarget != null) {
                    double age = currentTarget.getAgeSeconds();
                    // Обновляем дугу обратного отсчёта
                    if (timerArc != null) {
                        double ratio = 1.0 - (age / DISAPPEAR_T);
                        timerArc.setLength(360 * ratio);
                        // Цвет: зелёный → красный
                        double r = Math.min(1.0, 2.0 * (1.0 - ratio));
                        double g = Math.min(1.0, 2.0 * ratio);
                        timerArc.setStroke(Color.color(r, g, 0));
                    }

                    if (age >= DISAPPEAR_T) {
                        removeTarget();
                        if (spawned < TOTAL)
                            spawnTarget();
                        else
                            endGame();
                    }
                }
            }
        };
        gameLoop.start();
    }

    /**
     * Обрабатывает клик левой кнопкой мыши.
     * Проверяет попадание в текущую мишень, увеличивает счёт и спаривает новую
     * мишень.
     * 
     * @param x координата X клика
     * @param y координата Y клика
     */
    private void handleClick(double x, double y) {
        if (currentTarget == null)
            return;
        if (currentTarget.isHit(x, y)) {
            score++;
            scoreLabel.setText("Score: " + score + "/" + TOTAL);
            removeTarget();
            if (score >= TOTAL) {
                endGame();
                return;
            }
            if (spawned < TOTAL)
                spawnTarget();
        }
    }

    /**
     * Создаёт новую мишень в случайной позиции на арене.
     * Также создаёт дугу обратного отсчёта вокруг мишени.
     */
    private void spawnTarget() {
        double padding = RADIUS + 10;
        double x = padding + random.nextDouble() * (WIDTH - 2 * padding);
        double y = ARENA_TOP + padding + random.nextDouble() * (HEIGHT - ARENA_TOP - 2 * padding);
        currentTarget = new Target(x, y, RADIUS);
        spawned++;

        // Дуга обратного отсчёта
        timerArc = new Arc(x, y, RADIUS + 5, RADIUS + 5, 90, 360);
        timerArc.setType(ArcType.OPEN);
        timerArc.setFill(Color.TRANSPARENT);
        timerArc.setStroke(Color.LIMEGREEN);
        timerArc.setStrokeWidth(3);

        arena.getChildren().addAll(currentTarget.getShape(), timerArc);
    }

    /**
     * Удаляет текущую мишень и индикатор времени с арены.
     */
    private void removeTarget() {
        if (currentTarget != null) {
            arena.getChildren().remove(currentTarget.getShape());
            currentTarget = null;
        }
        if (timerArc != null) {
            arena.getChildren().remove(timerArc);
            timerArc = null;
        }
    }

    /**
     * Завершает игру, останавливает цикл и показывает экран результатов.
     * Отображает итоговый счёт, время прохождения и точность попадания.
     */
    private void endGame() {
        gameOver = true;
        gameLoop.stop();
        double elapsed = (System.nanoTime() - gameStartNano) / 1_000_000_000.0;

        VBox box = new VBox(15);
        box.setAlignment(Pos.CENTER);
        box.getStyleClass().add("result-box");

        Label title = new Label("ROUND OVER");
        title.getStyleClass().add("result-title");
        Label scoreLbl = new Label("Score: " + score + "/" + TOTAL);
        scoreLbl.getStyleClass().add("result-score");
        Label timeLbl = new Label(String.format("Time: %.2fs", elapsed));
        timeLbl.getStyleClass().add("result-score");
        double acc = spawned > 0 ? score * 100.0 / spawned : 0;
        Label accLbl = new Label(String.format("Accuracy: %.0f%%", acc));
        accLbl.getStyleClass().add("result-score");

        Button again = new Button("Play Again");
        again.getStyleClass().add("menu-button");
        again.setOnAction(e -> restart());
        Button menu = new Button("Main Menu");
        menu.getStyleClass().add("menu-button");
        menu.setOnAction(e -> sceneManager.showMenu());

        box.getChildren().addAll(title, scoreLbl, timeLbl, accLbl, again, menu);

        StackPane overlay = new StackPane(box);
        overlay.setStyle("-fx-background-color: rgba(0,0,0,0.7);");
        overlay.setPrefSize(WIDTH, HEIGHT);
        arena.getChildren().add(overlay);
        arena.getScene().setCursor(Cursor.DEFAULT);
    }

    /**
     * Перезапускает текущий режим игры.
     * Останавливает текущий игровой цикл и запускает новый экземпляр режима.
     */
    private void restart() {
        if (gameLoop != null)
            gameLoop.stop();
        sceneManager.startGame(GameType.DISAPPEARING);
    }
}
