package com.example.agario.models.utils;

import com.example.agario.models.Entity;
import com.example.agario.models.factory.PelletFactory;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class QuadTree implements Serializable {
    private static final long serialVersionUID = -8862885764214783136L;
    private static int MAX_DEPTH = 6;
    private int depth = 0;
    private List<Entity> entities;
    public QuadTree northWest = null;
    public QuadTree northEast = null;
    public QuadTree southWest = null;
    public QuadTree southEast = null;
    public Dimension dimension;


    /**
     * getter for all pellets
     * @return List<Entity> of every pellets
     */
    public List<Entity> getAllPellets() {
        List<Entity> pellets = new ArrayList<>();
        DFSChunk(this,this.getDimension(),pellets);
        return pellets;
    }

    /**
     * constructor for QuadTree
     * @param depth depth of the QuaaTree
     * @param dimension dimensions of the QuaaTree
     */
    public QuadTree(int depth, Dimension dimension) {
        this.depth = depth;
        entities = new ArrayList<Entity>();
        this.dimension = dimension;
    }

    /**
     * constructor for QuadTree
     * @param level level of the QuadTree
     * @param dimension dimensions of the QuaaTree
     * @param MAX_DEPTH max depth
     */
    public QuadTree(int level, Dimension dimension, int MAX_DEPTH) {
        this(level, dimension);
        QuadTree.MAX_DEPTH = MAX_DEPTH;
    }

    /**
     * dimension getter
     * @return Dimension
     */
    public Dimension getDimension(){
        return dimension;
    }

    /**
     * traveling the graph using Depth First Search
     * @param tree the tree to travel
     */
    public static void DepthFirstSearch(QuadTree tree) {
        if (tree == null)
            return;

        System.out.printf("\nDepth = %d [XMin = %f YMin = %f] \t[XMax = %f YMax = %f] ",
                tree.depth, tree.dimension.getxMin(), tree.dimension.getyMin(),
                tree.dimension.getxMax(), tree.dimension.getyMax());

        for (Entity entity : tree.entities) {
            System.out.printf("\n\t  x = %f   y = %f", entity.getPosX(), entity.getPosY());
        }

        if (tree.entities.size() == 0) {
            System.out.print(" \n\t  Leaf Node.");
        }

        DepthFirstSearch(tree.northWest);
        DepthFirstSearch(tree.northEast);
        DepthFirstSearch(tree.southWest);
        DepthFirstSearch(tree.southEast);
    }

    /**
     * split the QuadTree
     */
    public void splitQuadTree() {
        double xOffset = this.dimension.getxMin() + (this.dimension.getxMax() - this.dimension.getxMin()) / 2;

        double yOffset = this.dimension.getyMin() + (this.dimension.getyMax() - this.dimension.getyMin()) / 2;

        northWest = new QuadTree(this.depth + 1, new Dimension(this.dimension.getxMin(), this.dimension.getyMin(), xOffset, yOffset));

        northEast = new QuadTree(this.depth + 1, new Dimension(xOffset, this.dimension.getyMin(), this.dimension.getxMax(), yOffset));

        southWest = new QuadTree(this.depth + 1, new Dimension(this.dimension.getxMin(), yOffset, xOffset, this.dimension.getyMax()));

        southEast = new QuadTree(this.depth + 1, new Dimension(xOffset, yOffset, this.dimension.getxMax(), this.dimension.getyMax()));
    }

    /**
     * generate pellet according to the camera
     * @param cameraView player's camera
     * @param minPellets minimum of pellets
     */
    public void generatePelletsIfNeeded(Dimension cameraView, int minPellets) {
        List<Entity> visiblePellets = new ArrayList<>();
        DFSChunk(this, cameraView, visiblePellets);

        if (visiblePellets.size() < minPellets) {
            Random rand = new Random();
            int pelletsToAdd = minPellets - visiblePellets.size();
            for (int i = 0; i < pelletsToAdd; i++) {
                double x = cameraView.getxMin() + rand.nextDouble() * (cameraView.getxMax() - cameraView.getxMin());
                double y = cameraView.getyMin() + rand.nextDouble() * (cameraView.getyMax() - cameraView.getyMin());

                PelletFactory pelletFactory = new PelletFactory(x, y);
                Entity newPellet = pelletFactory.launchFactory();
                insertNode(newPellet);
            }
        }
    }

    /**
     * insert node in QuadTree
     * @param entity entity to insert
     */
    public void insertNode(Entity entity) {
        double x = entity.getPosX();
        double y = entity.getPosY();

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
            System.out.printf("ERROR : Unhandled partition x : %f   y : %f", x, y);
    }

    /**
     * remove a node from the QuadTree
     * @param foe entity to remove
     * @param tree QuadTree
     */
    public synchronized void removeNode(Entity foe, QuadTree tree) {
        if (tree == null) return;

        // Remove the entity safely using removeIf
        tree.entities.removeIf(entity ->
                entity.getPosX() == foe.getPosX() && entity.getPosY() == foe.getPosY());

        // Recursively check and remove from child nodes
        removeNode(foe, tree.northWest);
        removeNode(foe, tree.northEast);
        removeNode(foe, tree.southWest);
        removeNode(foe, tree.southEast);
    }

    /**
     * add to the list the entities in the dimension
     * @param tree QuadTree
     * @param dimension dimension concerned
     * @param resultat list of entities
     */
    public static void DFSChunk(QuadTree tree, Dimension dimension, List<Entity> resultat) {
        if (tree == null)
            return;

        /*System.out.printf("\nDepth = %d [XMin = %f YMin = %f] \t[XMax = %f YMax = %f] ",
                tree.depth, tree.dimension.getxMin(), tree.dimension.getyMin(),
                tree.dimension.getxMax(), tree.dimension.getyMax());*/

        for (Entity entity : tree.entities) {
            if(dimension.inRange(entity.getPosX(), entity.getPosY())){
                //System.out.printf("\n\t  x = %f   y = %f", entity.getPosX(), entity.getPosY());
                resultat.add(entity);
            }
        }

        if (tree.entities.size() == 0) {
            //System.out.printf(" \n\t  Leaf Node.");
        }

        DFSChunk(tree.northWest, dimension, resultat);
        DFSChunk(tree.northEast, dimension, resultat);
        DFSChunk(tree.southWest, dimension, resultat);
        DFSChunk(tree.southEast, dimension, resultat);
    }

    /**
     * clear the QuadTree
     */
    public void clear() {
        this.northWest = null;
        this.northEast = null;
        this.southWest = null;
        this.southEast = null;
        entities.clear();
    }

    /*public static void main(String args[]) {
        QuadTree anySpace = new QuadTree(0, new Dimension(0, 0, 1000, 1000));
        anySpace.insertNode(new PelletFactory(0, 0).launchFactory());
        anySpace.insertNode(new PelletFactory(1, 0).launchFactory());
        anySpace.insertNode(new PelletFactory(10, 5).launchFactory());
        anySpace.insertNode(new PelletFactory(0, 10).launchFactory());

        //Traveling the graph
        List<Entity> liste = new ArrayList<>();
        QuadTree.DepthFirstSearch(anySpace);
        System.out.println();
        QuadTree.DFSChunk(anySpace, camera,liste);
        System.out.println();
        for (Entity ent : liste){
            System.out.println(ent.getPosX() + ", " + ent.getPosY());
        }
    }*/
}
