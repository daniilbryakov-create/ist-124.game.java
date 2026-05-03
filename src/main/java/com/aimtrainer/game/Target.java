package com.aimtrainer.game;

import javafx.scene.Group;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.RadialGradient;
import javafx.scene.paint.Stop;
import javafx.scene.shape.Arc;
import javafx.scene.shape.ArcType;
import javafx.scene.shape.Circle;

public class Target {

    protected final Circle shape;
    protected double velocityX;
    protected double velocityY;
    protected final long spawnTimeNano;

    /**
     * Создаёт базовую мишень (шарик) с градиентом красного цвета.
     * Записывает время создания для отслеживания возраста мишени.
     * 
     * @param x      координата X центра мишени
     * @param y      координата Y центра мишени
     * @param radius радиус мишени
     */
    public Target(double x, double y, double radius) {
        RadialGradient gradient = new RadialGradient(
                0, 0, 0.3, 0.3, 1.0, true, CycleMethod.NO_CYCLE,
                new Stop(0.0, Color.web("#FF6B6B")),
                new Stop(0.6, Color.web("#E63946")),
                new Stop(1.0, Color.web("#9B1B30")));
        shape = new Circle(x, y, radius);
        shape.setFill(gradient);
        shape.setStroke(Color.WHITE);
        shape.setStrokeWidth(2);
        spawnTimeNano = System.nanoTime();
    }

    /**
     * Проверяет, попала ли точка (x, y) в область мишени.
     * Использует вычисление расстояния от центра мишени.
     * 
     * @param x координата X точки
     * @param y координата Y точки
     * @return true если точка внутри мишени, false иначе
     */
    public boolean isHit(double x, double y) {
        double dx = x - shape.getCenterX();
        double dy = y - shape.getCenterY();
        return Math.sqrt(dx * dx + dy * dy) <= shape.getRadius();
    }

    /**
     * Обновляет позицию мишени на основе скорости.
     * Отскакивает от границ арены (зеркальное отражение).
     * 
     * @param canvasWidth  ширина арены
     * @param canvasHeight высота арены
     */
    public void update(double canvasWidth, double canvasHeight) {
        double newX = shape.getCenterX() + velocityX;
        double newY = shape.getCenterY() + velocityY;
        double r = shape.getRadius();
        if (newX - r < 0 || newX + r > canvasWidth) {
            velocityX = -velocityX;
            newX = shape.getCenterX() + velocityX;
        }
        if (newY - r < 0 || newY + r > canvasHeight) {
            velocityY = -velocityY;
            newY = shape.getCenterY() + velocityY;
        }
        shape.setCenterX(newX);
        shape.setCenterY(newY);
    }

    /**
     * Возвращает время (в секундах) прошедшее с момента создания мишени.
     * 
     * @return возраст мишени в секундах
     */
    public double getAgeSeconds() {
        return (System.nanoTime() - spawnTimeNano) / 1_000_000_000.0;
    }

    /**
     * Возвращает объект Circle (форму) мишени для добавления на экран.
     * 
     * @return Circle - визуальное представление мишени
     */
    public Circle getShape() {
        return shape;
    }

    /**
     * Устанавливает скорость движения мишени по осям X и Y.
     * 
     * @param vx скорость по оси X (пиксели за кадр)
     * @param vy скорость по оси Y (пиксели за кадр)
     */
    public void setVelocity(double vx, double vy) {
        this.velocityX = vx;
        this.velocityY = vy;
    }

    // -----------------------------------------------------------------------
    // MovingHoldTarget
    // -----------------------------------------------------------------------

    /**
     * Мишень для режима MOVING_HOLD с индикатором прогресса удержания.
     * Показывает дугу вокруг шарика, которая заполняется по мере удержания.
     * Меняет цвет с красного на жёлтый по мере заполнения.
     */
    public static class MovingHoldTarget extends Target {

        public static final double HOLD_DURATION = 1.0;

        private double holdProgress = 0.0;
        private final Arc progressArc;
        private final Group node;

        /**
         * Создаёт движущуюся мишень с индикатором прогресса.
         * 
         * @param x      координата X центра мишени
         * @param y      координата Y центра мишени
         * @param radius радиус мишени
         */
        public MovingHoldTarget(double x, double y, double radius) {
            super(x, y, radius);

            progressArc = new Arc(x, y, radius + 4, radius + 4, 90, 0);
            progressArc.setType(ArcType.OPEN);
            progressArc.setFill(Color.TRANSPARENT);
            progressArc.setStroke(Color.LIME);
            progressArc.setStrokeWidth(3);

            node = new Group(shape, progressArc);
            node.setMouseTransparent(true);
        }

        /**
         * Добавляет время удержания к прогрессу.
         * Обновляет цвет мишени (красный → жёлтый) и заполнение дуги.
         * 
         * @param delta время в секундах для добавления к прогрессу
         * @return true если достигнуто время удержания (мишень уничтожена), false иначе
         */
        public boolean addHold(double delta) {
            holdProgress = Math.min(holdProgress + delta, HOLD_DURATION);
            double ratio = holdProgress / HOLD_DURATION;

            Color fill = interpolateColor(ratio);
            RadialGradient gradient = new RadialGradient(
                    0, 0, 0.3, 0.3, 1.0, true, CycleMethod.NO_CYCLE,
                    new Stop(0.0, fill.brighter()),
                    new Stop(0.6, fill),
                    new Stop(1.0, fill.darker().darker()));
            shape.setFill(gradient);
            progressArc.setLength(-360 * ratio);
            return holdProgress >= HOLD_DURATION;
        }

        /**
         * Сбрасывает прогресс удержания и возвращает мишень в исходное состояние.
         * Очищает дугу прогресса и восстанавливает красный цвет.
         */
        public void resetHold() {
            holdProgress = 0.0;
            progressArc.setLength(0);
            RadialGradient gradient = new RadialGradient(
                    0, 0, 0.3, 0.3, 1.0, true, CycleMethod.NO_CYCLE,
                    new Stop(0.0, Color.web("#FF6B6B")),
                    new Stop(0.6, Color.web("#E63946")),
                    new Stop(1.0, Color.web("#9B1B30")));
            shape.setFill(gradient);
        }

        /**
         * Обновляет позицию мишени и синхронизирует положение индикатора прогресса.
         * 
         * @param canvasWidth  ширина арены
         * @param canvasHeight высота арены
         */
        @Override
        public void update(double canvasWidth, double canvasHeight) {
            super.update(canvasWidth, canvasHeight);
            progressArc.setCenterX(shape.getCenterX());
            progressArc.setCenterY(shape.getCenterY());
        }

        /**
         * Возвращает группу узлов (шарик + индикатор) для добавления на экран.
         * 
         * @return Group - группа с мишенью и индикатором прогресса
         */
        public Group getNode() {
            return node;
        }

        /**
         * Вычисляет цвет мишени на основе прогресса удержания.
         * От 0% до 50%: красный → жёлтый (увеличивается зелень)
         * От 50% до 100%: жёлтый → зелёный (уменьшается красный)
         * 
         * @param t значение от 0.0 до 1.0 (прогресс от 0% до 100%)
         * @return Color - интерполированный цвет
         */
        private Color interpolateColor(double t) {
            if (t < 0.5) {
                double tt = t * 2;
                return Color.color(1.0, tt, 0.0);
            } else {
                double tt = (t - 0.5) * 2;
                return Color.color(1.0 - tt, 1.0, 0.0);
            }
        }
    }

    // -----------------------------------------------------------------------
    // FallingTarget
    // -----------------------------------------------------------------------

    /**
     * Мишень для режима FALLING_CATCH - падающий шарик.
     * Может быть зелёным (нужно ловить) или фиолетовым (нужно избегать).
     * Падает вниз с заданной скоростью.
     */
    public static class FallingTarget {

        /**
         * Тип падающего шарика.
         * GOOD - зелёный (нужно ловить, добавляет очко)
         * BAD - фиолетовый (нужно избегать, добавляет ошибку при поимке)
         */
        public enum Kind {
            GOOD, BAD
        }

        private final Circle shape;
        private final Kind kind;
        private final double speed;
        private boolean caught = false;

        /**
         * Создаёт падающий шарик с указанными параметрами.
         * Цвет зависит от типа: зелёный для GOOD, фиолетовый для BAD.
         * 
         * @param x      координата X начальной позиции (выше арены)
         * @param radius радиус шарика
         * @param kind   тип шарика (GOOD или BAD)
         * @param speed  скорость падения (пиксели за кадр)
         */
        public FallingTarget(double x, double radius, Kind kind, double speed) {
            this.kind = kind;
            this.speed = speed;

            Color base = (kind == Kind.GOOD) ? Color.web("#00E676") : Color.web("#CE93D8");
            Color mid = (kind == Kind.GOOD) ? Color.web("#00C853") : Color.web("#AB47BC");
            Color dark = (kind == Kind.GOOD) ? Color.web("#006622") : Color.web("#6A1B9A");

            RadialGradient gradient = new RadialGradient(
                    0, 0, 0.35, 0.35, 1.0, true, CycleMethod.NO_CYCLE,
                    new Stop(0.0, base),
                    new Stop(0.6, mid),
                    new Stop(1.0, dark));

            shape = new Circle(x, -radius, radius);
            shape.setFill(gradient);
            shape.setStroke(Color.WHITE);
            shape.setStrokeWidth(1.5);
        }

        /**
         * Перемещает шарик вниз на величину скорости.
         * Вызывается каждый кадр для анимации падения.
         */
        public void fall() {
            shape.setCenterY(shape.getCenterY() + speed);
        }

        /**
         * Проверяет пересечение шарика с прямоугольной платформой (корзинкой).
         * Использует AABB (Axis-Aligned Bounding Box) коллизию.
         * 
         * @param platformX центр платформы по оси X
         * @param platformY центр платформы по оси Y
         * @param platformW ширина платформы
         * @param platformH высота платформы
         * @return true если шарик пересекается с платформой
         */
        public boolean isOverlapping(double platformX, double platformY, double platformW, double platformH) {
            double cx = shape.getCenterX();
            double cy = shape.getCenterY();
            double r = shape.getRadius();
            return cx + r > platformX - platformW / 2 &&
                    cx - r < platformX + platformW / 2 &&
                    cy + r > platformY - platformH / 2 &&
                    cy - r < platformY + platformH / 2;
        }

        /**
         * Проверяет, ушёл ли шарик за нижнюю границу экрана.
         * 
         * @param height высота экрана
         * @return true если шарик ниже нижней границы
         */
        public boolean isOffScreen(double height) {
            return shape.getCenterY() - shape.getRadius() > height;
        }

        /**
         * Возвращает тип шарика (GOOD или BAD).
         * 
         * @return тип шарика
         */
        public Kind getKind() {
            return kind;
        }

        /**
         * Возвращает визуальное представление шарика.
         * 
         * @return Circle - форма шарика
         */
        public Circle getShape() {
            return shape;
        }

        /**
         * Проверяет, был ли шарик пойман/удален с арены.
         * 
         * @return true если шарик уже обработан (пойман или улетел)
         */
        public boolean isCaught() {
            return caught;
        }

        /**
         * Помечает шарик как пойманный/удаленный.
         * 
         * @param v true если шарик пойман
         */
        public void setCaught(boolean v) {
            this.caught = v;
        }
    }
}
