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

import java.util.Random;

import com.aimtrainer.GameType;
import com.aimtrainer.game.Crosshair;
import com.aimtrainer.game.Target;
import com.aimtrainer.ui.SceneManager;

/**
 * MOVING_HOLD — движущийся шарик.
 * Нужно навести прицел и удерживать ЛКМ ~1 секунду.
 * Шарик меняет цвет по мере заполнения прогресса.
 */
public class MovingHoldScene {

    private static final double WIDTH = 1024;
    private static final double HEIGHT = 768;
    private static final double ARENA_TOP = 50;
    private static final int TOTAL = 20;
    private static final double RADIUS = 26;

    private final SceneManager sceneManager;
    private final Random random = new Random();

    private Pane arena;
    private Crosshair crosshair;
    private Label scoreLabel;
    private Label timerLabel;
    private AnimationTimer gameLoop;

    private Target.MovingHoldTarget currentTarget;
    private int score = 0;
    private boolean gameOver = false;
    private long gameStartNano;
    private long lastFrameNano;

    private double mouseX = WIDTH / 2;
    private double mouseY = HEIGHT / 2;
    private boolean mouseDown = false;

    /**
     * Создаёт сцену для режима "Движущийся шарик".
     * 
     * @param sceneManager менеджер сцен для переключения меню
     */
    public MovingHoldScene(SceneManager sceneManager) {
        this.sceneManager = sceneManager;
    }

    /**
     * Создаёт и настраивает сцену игры с движущимся шариком.
     * Инициализирует арену, прицел, HUD, обработчики мыши/клавиш и запускает
     * игровой цикл.
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
            if (e.getButton() == MouseButton.PRIMARY)
                mouseDown = true;
        });
        scene.setOnMouseReleased(e -> {
            if (e.getButton() == MouseButton.PRIMARY) {
                mouseDown = false;
                if (currentTarget != null)
                    currentTarget.resetHold();
            }
        });
        scene.setOnMouseEntered(e -> {
            if (!gameOver) {
                root.requestFocus();
                scene.setCursor(Cursor.NONE);
            }
        });

        lastFrameNano = System.nanoTime();
        startGame();
        return scene;
    }

    /**
     * Создаёт интерфейс игрока со счётом времени и кнопками меню.
     * 
     * @return HBox - контейнер с элементами интерфейса
     */
    private HBox buildHUD() {
        scoreLabel = new Label("Score: 0/" + TOTAL);
        scoreLabel.getStyleClass().add("hud-label");

        timerLabel = new Label("Time: 0.0s");
        timerLabel.getStyleClass().add("hud-label");

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

        HBox hud = new HBox(20, scoreLabel, timerLabel, new HBox(10, restartBtn, menuBtn));
        hud.setAlignment(Pos.CENTER);
        hud.setPadding(new Insets(10));
        hud.setMaxHeight(40);
        hud.setPickOnBounds(false);
        return hud;
    }

    /**
     * Инициализирует игру, создаёт первую мишень и запускает основной игровой цикл.
     * Цикл отвечает за:
     * - Обновление позиции прицела
     * - Обновление позиции и ротации мишени
     * - Проверку удержания ЛКМ и накопление времени удержания
     * - Обновление дисплея времени
     * - Проверку победы (20 мишеней уничтожено)
     */
    private void startGame() {
        score = 0;
        gameOver = false;
        gameStartNano = System.nanoTime();
        lastFrameNano = gameStartNano;
        scoreLabel.setText("Score: 0/" + TOTAL);
        spawnTarget();

        gameLoop = new AnimationTimer() {
            @Override
            public void handle(long now) {
                if (gameOver)
                    return;
                double delta = (now - lastFrameNano) / 1_000_000_000.0;
                lastFrameNano = now;

                crosshair.setPosition(mouseX, mouseY);

                if (currentTarget != null) {
                    currentTarget.update(WIDTH, HEIGHT - ARENA_TOP);

                    // Проверяем наведение + зажатие ЛКМ
                    if (mouseDown && currentTarget.isHit(mouseX, mouseY)) {
                        boolean destroyed = currentTarget.addHold(delta);
                        if (destroyed) {
                            removeTarget();
                            score++;
                            scoreLabel.setText("Score: " + score + "/" + TOTAL);
                            if (score >= TOTAL) {
                                endGame();
                                return;
                            }
                            spawnTarget();
                        }
                    } else {
                        // Прицел ушёл или ЛКМ отпущена — сбрасываем
                        currentTarget.resetHold();
                    }
                }

                double elapsed = (now - gameStartNano) / 1_000_000_000.0;
                timerLabel.setText(String.format("Time: %.1fs", elapsed));
            }
        };
        gameLoop.start();
    }

    /**
     * Создаёт новую движущуюся мишень в случайной позиции на арене.
     * Задаёт мишени случайную скорость (2.5-4.5 пиксели за кадр) и направление.
     */
    private void spawnTarget() {
        double padding = RADIUS + 10;
        double x = padding + random.nextDouble() * (WIDTH - 2 * padding);
        double y = ARENA_TOP + padding + random.nextDouble() * (HEIGHT - ARENA_TOP - 2 * padding);
        currentTarget = new Target.MovingHoldTarget(x, y, RADIUS);
        double speed = 2.5 + random.nextDouble() * 2;
        double angle = random.nextDouble() * 2 * Math.PI;
        currentTarget.setVelocity(Math.cos(angle) * speed, Math.sin(angle) * speed);
        arena.getChildren().add(currentTarget.getNode());
    }

    /**
     * Удаляет текущую мишень с арены.
     */
    private void removeTarget() {
        if (currentTarget != null) {
            arena.getChildren().remove(currentTarget.getNode());
            currentTarget = null;
        }
    }

    /**
     * Завершает игру и показывает экран результатов.
     * Вычисляет время прохождения и передаёт данные в showResult.
     */
    private void endGame() {
        gameOver = true;
        gameLoop.stop();
        double elapsed = (System.nanoTime() - gameStartNano) / 1_000_000_000.0;
        showResult(score, TOTAL, elapsed);
    }

    /**
     * Отображает экран результатов после окончания игры.
     * Показывает финальный счёт, время прохождения и кнопки для повтора или
     * возврата в меню.
     * 
     * @param sc      достигнутый счёт
     * @param total   целевой счёт (обычно 20)
     * @param elapsed время прохождения в секундах
     */
    private void showResult(int sc, int total, double elapsed) {
        VBox box = new VBox(15);
        box.setAlignment(Pos.CENTER);
        box.getStyleClass().add("result-box");

        Label title = new Label("ROUND OVER");
        title.getStyleClass().add("result-title");
        Label scoreLbl = new Label("Score: " + sc + "/" + total);
        scoreLbl.getStyleClass().add("result-score");
        Label timeLbl = new Label(String.format("Time: %.2fs", elapsed));
        timeLbl.getStyleClass().add("result-score");

        Button again = new Button("Play Again");
        again.getStyleClass().add("menu-button");
        again.setOnAction(e -> restart());
        Button menu = new Button("Main Menu");
        menu.getStyleClass().add("menu-button");
        menu.setOnAction(e -> sceneManager.showMenu());

        box.getChildren().addAll(title, scoreLbl, timeLbl, again, menu);

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
        sceneManager.startGame(GameType.MOVING_HOLD);
    }
}
