package com.example.agario.models.utils;

import java.io.Serializable;

public class Dimension implements Serializable {
    private static final long serialVersionUID = -8862885764214783136L;
    private double xMin;
    private double yMin;
    private double xMax;
    private double yMax;

    /**
     * constructor for Dimension
     * @param xMin minimum x
     * @param yMin minimum y
     * @param xMax maximum x
     * @param yMax maximum y
     */
    public Dimension(double xMin, double yMin, double xMax, double yMax) {
        this.xMin = xMin;
        this.yMin = yMin;
        this.xMax = xMax;
        this.yMax = yMax;
    }

    /**
     * minimum x getter
     * @return double minimum x
     */
    public double getxMin() {
        return xMin;
    }

    /**
     * minimum y getter
     * @return double minimum y
     */
    public double getyMin() {
        return yMin;
    }

    /**
     * maximum x getter
     * @return double maximum x
     */
    public double getxMax() {
        return xMax;
    }

    /**
     * maximum y getter
     * @return double maximum y
     */
    public double getyMax() {
        return yMax;
    }

    /**
     * minimum x setter
     * @param xMin double minimum x
     */
    public void setxMin(double xMin) {
        this.xMin = xMin;
    }

    /**
     * minimum y setter
     * @param yMin double minimum y
     */
    public void setyMin(double yMin) {
        this.yMin = yMin;
    }

    /**
     * maximum x setter
     * @param xMax double maximum x
     */
    public void setxMax(double xMax) {
        this.xMax = xMax;
    }

    /**
     * maximum y setter
     * @param yMax double maximum y
     */
    public void setyMax(double yMax) {
        this.yMax = yMax;
    }

    /**
     * determine if a position is part of the dimension
     * @param x double x position
     * @param y double y position
     * @return boolean indicating if the position is part of the dimension
     */
    public boolean inRange(double x, double y) {
        return (x >= this.getxMin() && x <= this.getxMax() && y >= this.getyMin() && y <= this.getyMax());
    }
}
