package com.aimtrainer.game;

import javafx.scene.Group;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;

/**
 * Crosshair.java содержит два класса прицела:
 *
 *  1. Crosshair      — классический крест (MOVING_HOLD и DISAPPEARING)
 *  2. BasketCrosshair — платформа-корзинка (FALLING_CATCH)
 */
/**
 * Содержит два типа прицелов для разных режимов игры.
 * 1. Crosshair - классический крест (для MOVING_HOLD и DISAPPEARING)
 * 2. BasketCrosshair - платформа-корзинка (для FALLING_CATCH)
 */
public class Crosshair {

    private final Group group;
    private static final double SIZE = 12;
    private static final double GAP = 4;
    private static final double THICKNESS = 2;

    /**
     * Создаёт классический крестообразный прицел.
     * Состоит из центральной точки и четырёх линий (вверх, вниз, влево, вправо).
     * Цвет: лайм (зелёный).
     */
    public Crosshair() {
        group = new Group();
        Circle dot = new Circle(0, 0, 1.5, Color.LIME);
        Line top = makeLine(0, -GAP, 0, -GAP - SIZE);
        Line bottom = makeLine(0, GAP, 0, GAP + SIZE);
        Line left = makeLine(-GAP, 0, -GAP - SIZE, 0);
        Line right = makeLine(GAP, 0, GAP + SIZE, 0);
        group.getChildren().addAll(dot, top, bottom, left, right);
        group.setMouseTransparent(true);
    }

    /**
     * Вспомогательный метод для создания линии прицела.
     * 
     * @param x1 стартовая координата X
     * @param y1 стартовая координата Y
     * @param x2 конечная координата X
     * @param y2 конечная координата Y
     * @return Line - созданная линия
     */
    private Line makeLine(double x1, double y1, double x2, double y2) {
        Line line = new Line(x1, y1, x2, y2);
        line.setStroke(Color.LIME);
        line.setStrokeWidth(THICKNESS);
        return line;
    }

    /**
     * Устанавливает позицию прицела на экране.
     * 
     * @param x координата X
     * @param y координата Y
     */
    public void setPosition(double x, double y) {
        group.setLayoutX(x);
        group.setLayoutY(y);
    }

    /**
     * Возвращает визуальный узел прицела для добавления на экран.
     * 
     * @return Group - группа с линиями и точкой прицела
     */
    public Group getNode() {
        return group;
    }

    /**
     * Корзинка-прицел для режима FALLING_CATCH.
     * Горизонтальная платформа с бортиками по краям для ловли падающих шариков.
     * Движется только по оси X, позиция Y зафиксирована внизу арены.
     */
    public static class BasketCrosshair {

        public static final double WIDTH = 90;
        public static final double HEIGHT = 14;

        private final Group group;
        private double x;
        private final double y;

        /**
         * Создаёт корзинку-платформу для ловли шариков.
         * Состоит из трёх частей: основная платформа и два бортика.
         * 
         * @param startX начальная позиция по оси X (обычно центр экрана)
         * @param fixedY зафиксированная позиция по оси Y (обычно внизу арены)
         */
        public BasketCrosshair(double startX, double fixedY) {
            this.x = startX;
            this.y = fixedY;

            // Основная платформа
            Rectangle base = new Rectangle(-WIDTH / 2, -HEIGHT / 2, WIDTH, HEIGHT);
            base.setFill(Color.web("#29B6F6"));
            base.setStroke(Color.WHITE);
            base.setStrokeWidth(1.5);
            base.setArcWidth(8);
            base.setArcHeight(8);

            // Левый бортик
            Rectangle left = new Rectangle(-WIDTH / 2 - 6, -HEIGHT / 2 - 10, 6, HEIGHT + 10);
            left.setFill(Color.web("#0288D1"));
            left.setStroke(Color.WHITE);
            left.setStrokeWidth(1);
            left.setArcWidth(4);
            left.setArcHeight(4);

            // Правый бортик
            Rectangle right = new Rectangle(WIDTH / 2, -HEIGHT / 2 - 10, 6, HEIGHT + 10);
            right.setFill(Color.web("#0288D1"));
            right.setStroke(Color.WHITE);
            right.setStrokeWidth(1);
            right.setArcWidth(4);
            right.setArcHeight(4);

            group = new Group(base, left, right);
            group.setMouseTransparent(true);
            group.setLayoutX(x);
            group.setLayoutY(y);
        }

        /**
         * Перемещает корзинку по горизонтали (ось X).
         * Позиция Y остаётся неизменной.
         * 
         * @param newX новая координата X
         */
        public void setX(double newX) {
            this.x = newX;
            group.setLayoutX(newX);
        }

        /**
         * Возвращает текущую позицию корзинки по оси X.
         * 
         * @return координата X
         */
        public double getX() {
            return x;
        }

        /**
         * Возвращает зафиксированную позицию корзинки по оси Y.
         * 
         * @return координата Y
         */
        public double getY() {
            return y;
        }

        /**
         * Возвращает визуальный узел корзинки для добавления на экран.
         * 
         * @return Group - группа с платформой и бортиками
         */
        public Group getNode() {
            return group;
        }
    }
}
