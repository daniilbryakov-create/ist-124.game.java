package com.aimtrainer.scenes;

import javafx.animation.AnimationTimer;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

import com.aimtrainer.GameType;
import com.aimtrainer.game.Crosshair;
import com.aimtrainer.game.Target;
import com.aimtrainer.ui.SceneManager;

/**
 * FALLING_CATCH — падающие шарики.
 * Зелёные — ловить (платформой-корзинкой).
 * Фиолетовые — избегать.
 * Игра до 3 ошибок:
 * - зелёный улетел вниз = ошибка
 * - поймал фиолетовый = ошибка
 */
public class FallingCatchScene {

    private static final double WIDTH = 1024;
    private static final double HEIGHT = 768;
    private static final int MAX_ERRORS = 3;
    private static final double BASKET_Y = HEIGHT - 40;
    private static final double RADIUS = 18;
    private static final double SPAWN_INTERVAL = 1.2; // секунд между спавнами

    private final SceneManager sceneManager;
    private final Random random = new Random();

    private Pane arena;
    private Crosshair.BasketCrosshair basket;
    private Label scoreLabel;
    private Label errorsLabel;
    private AnimationTimer gameLoop;

    private final List<Target.FallingTarget> balls = new ArrayList<>();
    private int score = 0;
    private int errors = 0;
    private boolean gameOver = false;
    private long gameStartNano;
    private long lastSpawnNano;

    private double mouseX = WIDTH / 2;

    /**
     * Создаёт сцену для режима "Падающие шарики".
     * 
     * @param sceneManager менеджер сцен для переключения меню
     */
    public FallingCatchScene(SceneManager sceneManager) {
        this.sceneManager = sceneManager;
    }

    /**
     * Создаёт и настраивает сцену игры с падающими шариками.
     * Инициализирует арену, корзинку, HUD и запускает игровой цикл.
     * 
     * @return Scene - готовая сцена для отображения
     */
    public Scene buildScene() {
        arena = new Pane();
        arena.setPrefSize(WIDTH, HEIGHT);
        arena.setStyle("-fx-background-color: #0d1b2a;");

        basket = new Crosshair.BasketCrosshair(WIDTH / 2, BASKET_Y);
        arena.getChildren().add(basket.getNode());

        HBox hud = buildHUD();

        StackPane root = new StackPane();
        root.getChildren().addAll(arena, hud);
        StackPane.setAlignment(hud, Pos.TOP_CENTER);

        Scene scene = new Scene(root, WIDTH, HEIGHT);
        scene.setCursor(Cursor.NONE);

        root.setFocusTraversable(true);
        scene.windowProperty().addListener((obs, oldW, newW) -> {
            if (newW != null) {
                newW.requestFocus();
                root.requestFocus();
            }
        });

        // Корзинка следует за мышью только по горизонтали
        scene.setOnMouseMoved(e -> mouseX = e.getX());
        scene.setOnMouseDragged(e -> mouseX = e.getX());
        scene.setOnMousePressed(e -> {
            root.requestFocus();
            scene.setCursor(Cursor.NONE);
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
     * Создаёт интерфейс игрока с счётом, счётчиком ошибок, легендой и кнопками.
     * 
     * @return HBox - контейнер с элементами интерфейса
     */
    private HBox buildHUD() {
        scoreLabel = new Label("Caught: 0");
        scoreLabel.getStyleClass().add("hud-label");

        errorsLabel = new Label("❌ 0/" + MAX_ERRORS);
        errorsLabel.getStyleClass().add("hud-label");
        errorsLabel.setStyle("-fx-text-fill: #FF6B6B;");

        // Легенда
        Label legendGood = new Label("● Поймать");
        legendGood.setStyle("-fx-text-fill: #00E676; -fx-font-size: 12px;");
        Label legendBad = new Label("● Избегать");
        legendBad.setStyle("-fx-text-fill: #CE93D8; -fx-font-size: 12px;");

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

        HBox hud = new HBox(16, scoreLabel, errorsLabel, legendGood, legendBad, new HBox(10, restartBtn, menuBtn));
        hud.setAlignment(Pos.CENTER);
        hud.setPadding(new Insets(10));
        hud.setMaxHeight(40);
        hud.setPickOnBounds(false);
        return hud;
    }

    /**
     * Инициализирует игру и запускает основной игровой цикл.
     * Цикл отвечает за:
     * - Движение корзинки вслед за мышью
     * - Периодический спавн новых шариков
     * - Обновление позиции падающих шариков
     * - Проверку коллизий (поимка, конец игры)
     */
    private void startGame() {
        score = 0;
        errors = 0;
        gameOver = false;
        balls.clear();
        gameStartNano = System.nanoTime();
        lastSpawnNano = gameStartNano;
        updateHUD();

        gameLoop = new AnimationTimer() {
            @Override
            public void handle(long now) {
                if (gameOver)
                    return;

                // Двигаем корзинку
                basket.setX(clamp(mouseX, Crosshair.BasketCrosshair.WIDTH / 2 + 6,
                        WIDTH - Crosshair.BasketCrosshair.WIDTH / 2 - 6));

                // Спавн новых шариков
                double sinceSpawn = (now - lastSpawnNano) / 1_000_000_000.0;
                if (sinceSpawn >= SPAWN_INTERVAL) {
                    spawnBall();
                    lastSpawnNano = now;
                }

                // Обновление шариков
                Iterator<Target.FallingTarget> it = balls.iterator();
                while (it.hasNext()) {
                    Target.FallingTarget ball = it.next();
                    if (ball.isCaught())
                        continue;
                    ball.fall();

                    // Пересечение с корзинкой
                    if (ball.isOverlapping(basket.getX(), basket.getY(),
                            Crosshair.BasketCrosshair.WIDTH,
                            Crosshair.BasketCrosshair.HEIGHT)) {
                        ball.setCaught(true);
                        arena.getChildren().remove(ball.getShape());
                        it.remove();

                        if (ball.getKind() == Target.FallingTarget.Kind.GOOD) {
                            score++;
                        } else {
                            // Поймали плохой
                            addError();
                            if (gameOver)
                                return;
                        }
                        updateHUD();
                        continue;
                    }

                    // Улетел вниз
                    if (ball.isOffScreen(HEIGHT)) {
                        arena.getChildren().remove(ball.getShape());
                        it.remove();
                        if (ball.getKind() == Target.FallingTarget.Kind.GOOD) {
                            // Не поймал хороший
                            addError();
                            if (gameOver)
                                return;
                            updateHUD();
                        }
                        // Плохой улетел — ничего не происходит
                    }
                }
            }
        };
        gameLoop.start();
    }

    /**
     * Создаёт новый падающий шарик в случайной позиции сверху арены.
     * 35% вероятность создать фиолетовый (плохой) шарик, остальное - зелёные
     * (хорошие).
     * Скорость падения случайна в диапазоне 2.5-4.5 пиксели за кадр.
     */
    private void spawnBall() {
        double padding = RADIUS + 10;
        double x = padding + random.nextDouble() * (WIDTH - 2 * padding);
        // 35% шанс плохого шарика
        Target.FallingTarget.Kind kind = (random.nextDouble() < 0.35)
                ? Target.FallingTarget.Kind.BAD
                : Target.FallingTarget.Kind.GOOD;
        double speed = 2.5 + random.nextDouble() * 2.0;
        Target.FallingTarget ball = new Target.FallingTarget(x, RADIUS, kind, speed);
        balls.add(ball);
        arena.getChildren().add(arena.getChildren().size() - 1, ball.getShape()); // под корзинкой
    }

    /**
     * Увеличивает счётчик ошибок на 1 и проверяет, достигнут ли лимит (3 ошибки).
     * Если лимит достигнут, игра завершается.
     */
    private void addError() {
        errors++;
        updateHUD();
        if (errors >= MAX_ERRORS)
            endGame();
    }

    /**
     * Обновляет текст на экране со статистикой: количество пойманных шариков и
     * ошибок.
     */
    private void updateHUD() {
        scoreLabel.setText("Caught: " + score);
        errorsLabel.setText("❌ " + errors + "/" + MAX_ERRORS);
    }

    /**
     * Завершает игру, останавливает цикл и показывает экран результатов.
     * Отображает количество пойманных шариков, время прохождения и ошибки.
     */
    private void endGame() {
        gameOver = true;
        gameLoop.stop();
        double elapsed = (System.nanoTime() - gameStartNano) / 1_000_000_000.0;

        VBox box = new VBox(15);
        box.setAlignment(Pos.CENTER);
        box.getStyleClass().add("result-box");

        Label title = new Label("GAME OVER");
        title.getStyleClass().add("result-title");
        Label scoreLbl = new Label("Поймано: " + score);
        scoreLbl.getStyleClass().add("result-score");
        Label timeLbl = new Label(String.format("Time: %.2fs", elapsed));
        timeLbl.getStyleClass().add("result-score");
        Label errLbl = new Label("Ошибки: " + errors + "/" + MAX_ERRORS);
        errLbl.getStyleClass().add("result-score");
        errLbl.setStyle("-fx-text-fill: #FF6B6B;");

        Button again = new Button("Play Again");
        again.getStyleClass().add("menu-button");
        again.setOnAction(e -> restart());
        Button menu = new Button("Main Menu");
        menu.getStyleClass().add("menu-button");
        menu.setOnAction(e -> sceneManager.showMenu());

        box.getChildren().addAll(title, scoreLbl, timeLbl, errLbl, again, menu);

        StackPane overlay = new StackPane(box);
        overlay.setStyle("-fx-background-color: rgba(0,0,0,0.7);");
        overlay.setPrefSize(WIDTH, HEIGHT);
        arena.getChildren().add(overlay);
        arena.getScene().setCursor(Cursor.DEFAULT);
    }

    /**
     * Перезапускает текущий режим игры.
     * Останавливает игровой цикл, очищает список шариков и запускает новый
     * экземпляр.
     */
    private void restart() {
        if (gameLoop != null)
            gameLoop.stop();
        // Очищаем шарики
        for (Target.FallingTarget b : balls)
            arena.getChildren().remove(b.getShape());
        balls.clear();
        sceneManager.startGame(GameType.FALLING_CATCH);
    }

    /**
     * Ограничивает значение в указанном диапазоне [min, max].
     * Используется для удержания корзинки в пределах арены.
     * 
     * @param val значение для ограничения
     * @param min минимальное значение
     * @param max максимальное значение
     * @return ограниченное значение
     */
    private double clamp(double val, double min, double max) {
        return Math.max(min, Math.min(max, val));
    }
}
