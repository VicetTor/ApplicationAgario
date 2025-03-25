package com.example.agario.utils;

public class Dimension {
    private double xMin;
    private double yMin;
    private double xMax;
    private double yMax;

    public Dimension(double xMin, double yMin, double xMax, double yMax) {
        this.xMin = xMin;
        this.yMin = yMin;
        this.xMax = xMax;
        this.yMax = yMax;
    }

    public double getxMin() {
        return xMin;
    }

    public double getyMin() {
        return yMin;
    }

    public double getxMax() {
        return xMax;
    }

    public double getyMax() {
        return yMax;
    }

    public boolean inRange(double x, double y) {
        return (x >= this.getxMin() && x <= this.getxMax() && y >= this.getyMin() && y <= this.getyMax());
    }
}
