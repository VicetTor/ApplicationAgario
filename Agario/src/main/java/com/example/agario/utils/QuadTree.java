package com.example.agario.utils;

import java.util.ArrayList;
import java.util.List;

public class QuadTree {
    private static int MAX_DEPTH = 6;
    private int depth = 0;
    private List<Entity> entities;
    QuadTree northWest = null;
    QuadTree northEast = null;
    QuadTree southWest = null;
    QuadTree southEast = null;
    Dimension dimension;

    public QuadTree(int depth, Dimension dimension) {
        this.depth = depth;
        entities = new ArrayList<Entity>();
        this.dimension = dimension;
    }

    public QuadTree(int level, Dimension dimension, int MAX_DEPTH) {
        this(level, dimension);
        this.MAX_DEPTH = MAX_DEPTH;
    }

    /* Traveling the Graph using Depth First Search*/
    static void DepthFirstSearch(QuadTree tree) {
        if (tree == null)
            return;

        System.out.printf("\nDepth = %d [XMin = %d YMin = %d] \t[XMax = %d YMax = %d] ",
                tree.depth, tree.dimension.getxMin(), tree.dimension.getyMin(),
                tree.dimension.getxMax(), tree.dimension.getyMax());

        for (Entity entity : tree.entities) {
            System.out.printf("\n\t  x = %d   y = %d", entity.getX(), entity.getY());
        }

        if (tree.entities.size() == 0) {
            System.out.printf(" \n\t  Leaf Node.");
        }

        DepthFirstSearch(tree.northWest);
        DepthFirstSearch(tree.northEast);
        DepthFirstSearch(tree.southWest);
        DepthFirstSearch(tree.southEast);

    }

    void splitQuadTree() {
        int xOffset = this.dimension.getxMin() + (this.dimension.getxMax() - this.dimension.getxMin()) / 2;

        int yOffset = this.dimension.getyMin() + (this.dimension.getyMax() - this.dimension.getyMin()) / 2;

        northWest = new QuadTree(this.depth + 1, new Dimension(this.dimension.getxMin(), this.dimension.getyMin(), xOffset, yOffset));

        northEast = new QuadTree(this.depth + 1, new Dimension(xOffset, this.dimension.getyMin(), xOffset, yOffset));

        southWest = new QuadTree(this.depth + 1, new Dimension(this.dimension.getxMin(), xOffset, xOffset, this.dimension.getyMax()));

        southEast = new QuadTree(this.depth + 1, new Dimension(xOffset, yOffset, this.dimension.getxMax(), this.dimension.getyMax()));
    }

    void insertNode(Entity entity) {
        int x = entity.getPosX();
        int y = entity.getPosY();
        if (entity == null)
            return;

        if (!this.dimension.inRange(x, y)) {
            return;
        }

        if (this.depth == MAX_DEPTH) {
            entities.add(entity);
            return;
        }

        // Exceeded the capacity so split it in FOUR
        if (northWest == null) {
            splitQuadTree();
        }

        // Check coordinates belongs to which partition
        if (this.northWest.dimension.inRange(x, y))
            this.northWest.insertNode(entity);

        else if (this.northEast.dimension.inRange(x, y))
            this.northEast.insertNode(entity);

        else if (this.southWest.dimension.inRange(x, y))
            this.southWest.insertNode(entity);

        else if (this.southEast.dimension.inRange(x, y))
            this.southEast.insertNode(entity);

        else
            System.out.printf("ERROR : Unhandled partition x : %d   y : %d", x, y);
    }
}
