package unsw.graphics.world;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

import unsw.graphics.Vector3;
import unsw.graphics.geometry.Point2D;
import unsw.graphics.geometry.Point3D;
import unsw.graphics.geometry.TriangleMesh;
import unsw.graphics.*;

import com.jogamp.opengl.GL;
import com.jogamp.opengl.GL3;




/**
 * Terrain for the world
 *
 * @author Yingzhi Zhou
 */
public class Terrain {
    private int width;
    private int depth;
    private float[][] altitudes;
    private List<Tree> trees;
    private List<Road> roads;
    private Vector3 sunlight;
    private TriangleMesh terrainMesh;
    private Texture texture;
    private Texture treeTexture;
    private Texture roadTexture;


    /**
     * Create a new terrain
     *
     * @param width The number of vertices in the x-direction
     * @param depth The number of vertices in the z-direction
     */
    public Terrain(int width, int depth, Vector3 sunlight) {
        this.width = width;
        this.depth = depth;
        altitudes = new float[width][depth];
        trees = new ArrayList<Tree>();
        roads = new ArrayList<Road>();
        this.sunlight = sunlight;
    }

    public List<Tree> trees() {
        return trees;
    }

    public List<Road> roads() {
        return roads;
    }

    public Vector3 getSunlight() {
        return sunlight;
    }

    /**
     * Set the sunlight direction.
     *
     * Note: the sun should be treated as a directional light, without a position
     *
     * @param dx
     * @param dy
     * @param dz
     */
    public void setSunlightDir(float dx, float dy, float dz) {
        sunlight = new Vector3(dx, dy, dz);
    }

    /**
     * Get the altitude at a grid point
     *
     * @param x
     * @param z
     * @return
     */
    public double getGridAltitude(int x, int z) {
        return altitudes[x][z];
    }

    /**
     * Set the altitude at a grid point
     *
     * @param x
     * @param z
     * @return
     */
    public void setGridAltitude(int x, int z, float h) {
        altitudes[x][z] = h;
    }

    /**
     * Get the altitude at an arbitrary point.
     * Non-integer points should be interpolated from neighbouring grid points
     *
     * @param x
     * @param z
     * @return
     */
    public float altitude(float x, float z) {
        float altitude = 0;

        // test if given x and z value out of bound, if it is, return 0
        if (x < 0 || x > width - 1 || z < 0 || z > depth - 1) {
            return altitude;
        }

        int leftX = (int)Math.floor(x);
        int rightX = (int)Math.ceil(x);
        int bottomZ = (int)Math.floor(z);
        int topZ = (int)Math.ceil(z);

        // test point is above or below a line, or in the edges of square
        // we save our triangle mesh we use square in the left
        // when we doing bilinear calculation, use square on the right
        // p0(zFloor)p1       p2(z ceil)p3
        // -----------        -----------
        // |        /|        |\        |
        // |   I1  / |        | \       |
        // |    . /  |        |  \      |
        // |     /   | -----> |   \     |
        // |    /    |        |    \    |
        // |   /  .  |        |     \   |
        // |  /  I2  |        |      \  |
        // | /       |        |       \ |
        // |---------|        |---------|
        // p2(zCeil) p3       p0(zFloor)p1

        if((int)x == x && (int)z == z) {
            // if x and z are integer, directly return altitude
            altitude =  (float)getGridAltitude((int)x, (int)z);

        } else if ((int)x != x && (int)z == z) {
            // point is in bottom or top edges
            altitude =  linearInterPolateX(x, leftX, rightX, z, z);

        } else if ((int)x == x && (int)z != z) {
            // point is in left side or right side edges
            altitude = linearInterPolateZ(z, bottomZ, topZ, x, x);

        } else {
            // point is inside the 1 * 1 square
            float hypotenuse = topZ - z + leftX;

            if(x < hypotenuse) {
                // in left triangle, above hypotenuse
                float lipz1 = linearInterPolateZ(z, bottomZ, topZ, leftX, leftX);
                float lipz2 = linearInterPolateZ(z, bottomZ, topZ, rightX, leftX);
                altitude = bilinearInterpolate(x, (float)leftX, hypotenuse, lipz1, lipz2);

            } else if (x > hypotenuse){
                // in the right triangle, below hypotenuse
                float lipz1 = linearInterPolateZ(z, bottomZ, topZ, rightX, leftX);
                float lipz2 = linearInterPolateZ(z, bottomZ, topZ, rightX, rightX);
                altitude = bilinearInterpolate(x, hypotenuse, (float)rightX, lipz1, lipz2);

            } else {
                // lie in the hypotenuse
                altitude = linearInterPolateZ(z, bottomZ, topZ, rightX, leftX);
            }
        }

        return altitude;
    }

    /**
     * Use bilinear Interpolation to calculate the depth of the point has x coordinate x
     * @return
     */
    private float bilinearInterpolate(float x, float x1, float x2, float lipz1, float lipz2) {
    	return ((x - x1)/(x2 - x1)) * lipz2 + ((x2 - x)/(x2 - x1)) * lipz1;
    }

    /**
     * Calculate the altitude of the intersection point which has z-coordinate value z in line
     * which point1 (x1, z1) and point2 (x2, z2) lie on it.
     * point1 is below point2, z1 less than z2
     * @return altitude
     */
    private float linearInterPolateZ(float z, float z1, float z2, float x1, float x2) {
    	return (float) (((z - z1) /(z2 - z1)) * getGridAltitude((int)x2, (int)z2) +
    			((z2 - z)/(z2 - z1)) * getGridAltitude((int)x1, (int)z1));
    }

    /**
     * Calculate the altitude of the intersection point which has x-coordinate value x in line
     * which point1 (x1, z1) and point2 (x2, z2) lie on it.
     * point1 is in the left point2, x1 less than x2
     * @return altitude
     */
    private float linearInterPolateX(float x, float x1, float x2, float z1, float z2) {
    	return (float) (((x - x1) / (x2 - x1)) * getGridAltitude((int)x2, (int)z2)
    			+ ((x2 - x) / (x2 - x1)) * getGridAltitude ((int)x1, (int)z1));
    }
    
    /**
     * Add a tree at the specified (x,z) point.
     * The tree's y coordinate is calculated from the altitude of the terrain at that point.
     *
     * @param x
     * @param z
     */
    public void addTree(float x, float z) {
        float y = altitude(x, z);
        Tree tree = new Tree(x, y, z);
        trees.add(tree);
    }


    /**
     * initialise triangle mesh for all the tree in tree list
     * @param gl
     */
    public void initTree(GL3 gl) {
        for (Tree tree : trees) {
            tree.initTree(gl, this.treeTexture);
        }
    }

    /**
     * draw the triangle mesh of each tree
     * @param gl
     * @param frame
     */
    public void drawTree(GL3 gl, CoordFrame3D frame) {
        initTree(gl);
        Shader.setInt(gl, "tex", 0);
        gl.glActiveTexture(GL.GL_TEXTURE0);
        gl.glBindTexture(GL.GL_TEXTURE_2D, this.treeTexture.getId());
        Shader.setPenColor(gl, Color.WHITE);
        for (Tree tree : trees) {
            tree.display(gl, frame);
        }
    }
    /**
     * Add roads to terrain
     */
    public void addRoad(float width, List<Point2D> spine) {
        Road road = new Road(width, spine);
        roads.add(road);
    }


    /**
     *  draw the triangle mesh of each road
     * @param gl
     * @param frame
     */
    public void drawRoad(GL3 gl, CoordFrame3D frame) {
        Shader.setInt(gl, "tex", 0);
        gl.glActiveTexture(GL.GL_TEXTURE0);
        gl.glBindTexture(GL.GL_TEXTURE_2D, this.roadTexture.getId());
        for (Road road : roads) {
            road.draw(gl, frame);
        }
        Shader.setPenColor(gl, Color.WHITE);
    }

    /**
     * initialise the triangle mesh for terrain according to the width and depth
     * @param gl
     */
    public void initTerrain(GL3 gl) {
        ArrayList<Point3D> allVertices = new ArrayList<>();
        ArrayList<Point2D> textureCoords = new ArrayList<>();

        // topLeft     topRight
        //    -----------
        //    |        /|
        //    |   I1  / |
        //    |    . /  |
        //    |     /   |
        //    |    /    |
        //    |   /  .  |
        //    |  /  I2  |
        //    | /       |
        //    |---------|
        // bottomLeft   bottomRight

        for (int x = 0; x < width - 1; x++) {
            for (int z = 0; z < depth - 1; z++) {
                // compute all vertices for each square
                Point3D topLeft = new Point3D((float)x, (float)getGridAltitude(x, z), (float)z);
                Point3D bottomLeft = new Point3D((float)x, (float)getGridAltitude(x, z + 1), (float)z + 1);
                Point3D topRight = new Point3D((float)x + 1, (float)getGridAltitude(x + 1, z), (float)z);
                Point3D bottomRight = new Point3D((float)x + 1, (float)getGridAltitude(x + 1, z + 1), (float)z + 1);

                // add left triangle
                allVertices.add(topLeft);
                allVertices.add(bottomLeft);
                allVertices.add(topRight);

                // add current vertex to texture array
                textureCoords.add(new Point2D((float)x, (float)z));
                textureCoords.add(new Point2D((float)x, (float)z + 1));
                textureCoords.add(new Point2D((float)x + 1, (float)z));

                // add right triangle
                allVertices.add(topRight);
                allVertices.add(bottomLeft);
                allVertices.add(bottomRight);

                // add current vertex to texture array
                textureCoords.add(new Point2D((float)x + 1, (float)z));
                textureCoords.add(new Point2D((float)x, (float)z + 1));
                textureCoords.add(new Point2D((float)x + 1, (float)z + 1));
            }
        }

        // create new triangleMesh for terrain
        terrainMesh = new TriangleMesh(allVertices, true, textureCoords);
        terrainMesh.init(gl);
    }

    /**
     * load all the textures we need in this terrain
     * @param gl
     */
    public void loadTexture(GL3 gl) {
        this.texture = new Texture(gl, "res/textures/grass.png", "png", true);
        this.treeTexture = new Texture(gl, "res/textures/tree.png", "png", true);
        this.roadTexture = new Texture(gl, "res/textures/rock.bmp", "bmp", true);
        for (Road r: this.roads) {
        	r.init(gl, this);
        }
    }

    /**
     * draw the triangle mesh of terrain
     * @param gl
     * @param frame
     */
    public void drawTerrain(GL3 gl, CoordFrame3D frame) {
        Shader.setInt(gl, "tex", 0);
        gl.glActiveTexture(GL.GL_TEXTURE0);
        gl.glBindTexture(GL.GL_TEXTURE_2D, texture.getId());
        Shader.setPenColor(gl, Color.WHITE);
        terrainMesh.draw(gl, frame);

    }


    public void init(GL3 gl) {
        initTerrain(gl);
        // load texture of terrain
        loadTexture(gl);
    }

    public void draw(GL3 gl, CoordFrame3D frame) {
        drawTerrain(gl, frame);
        drawTree(gl, frame);
        drawRoad(gl, frame);
    }

    public void destroy(GL3 gl) {
        texture.destroy(gl);
        treeTexture.destroy(gl);
        roadTexture.destroy(gl);
        terrainMesh.destroy(gl);
    }

}
