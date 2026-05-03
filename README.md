# 🎯 CS:GO Aim Trainer — JavaFX

## Структура проекта

```
aimtrainer/
├── pom.xml
└── src/main/
    ├── java/
    │   ├── module-info.java
    │   └── com/aimtrainer/
    │       ├── Main.java            — точка входа
    │       ├── SceneManager.java    — переключение сцен
    │       ├── MenuScreen.java      — главное меню
    │       ├── GameScene.java       — основная игровая логика + AnimationTimer
    │       ├── LevelConfig.java     — параметры каждого уровня
    │       ├── Target.java          — мишень (круг, попадание, движение)
    │       └── Crosshair.java       — прицел
    └── resources/
        └── style.css                — стили (CS:GO-вайб)
```

## Запуск

### Через Maven:
```bash
cd aimtrainer
mvn javafx:run
```

### Через IntelliJ IDEA:
1. File → Open → выбери папку `aimtrainer`
2. IDEA подтянет Maven зависимости
3. Run → Main.java

> Нужна Java 17+ и Maven.

## Уровни

| Level | Цели       | Движение | Исчезают | Таймер |
|-------|-----------|----------|----------|--------|
| 1     | Средние    | Нет      | Нет      | Да     |
| 2     | Поменьше   | Да       | Нет      | Да     |
| 3     | Маленькие  | Нет      | Через 2с | Нет    |
