package com.example.agario.client.controllers;

import javafx.animation.Interpolator;
import javafx.animation.ParallelTransition;
import javafx.animation.ScaleTransition;
import javafx.animation.TranslateTransition;
import javafx.scene.shape.Circle;
import javafx.util.Duration;

public class AnimationController {
    private void animatePlayerMovement(Circle player, double dx, double dy) {
        double movementIntensity = Math.sqrt(dx * dx + dy * dy) / 50;
        movementIntensity = Math.min(movementIntensity, 1);

        double scaleFactor = 1 + movementIntensity * 0.1;

        ScaleTransition scaleTransition = new ScaleTransition(Duration.millis(10), player);
        scaleTransition.setToX(scaleFactor);
        scaleTransition.setToY(scaleFactor);
        scaleTransition.setAutoReverse(true);

        TranslateTransition translateTransition = new TranslateTransition(Duration.millis(10), player);
        translateTransition.setByX((Math.random() - 0.5) * 2.5);
        translateTransition.setByY((Math.random() - 0.5) * 2.5);
        translateTransition.setAutoReverse(true);

        ParallelTransition parallelTransition = new ParallelTransition(scaleTransition, translateTransition);
        parallelTransition.setInterpolator(Interpolator.EASE_OUT);
        parallelTransition.play();
    }
}
