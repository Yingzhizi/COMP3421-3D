package unsw.graphics.world;

import java.awt.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import unsw.graphics.Vector3;
import unsw.graphics.geometry.Point2D;
import unsw.graphics.geometry.Point3D;
import unsw.graphics.geometry.TriangleMesh;
import unsw.graphics.*;

import com.jogamp.opengl.GL3;




/**
 * COMMENT: Comment HeightMap
 *
 * @author malcolmr
 */
public class Terrain {

    private int width;
    private int depth;
    private float[][] altitudes;
    private List<Tree> trees;
    private List<Road> roads;
    private Vector3 sunlight;
    private TriangleMesh terrainMesh;
    private Texture terr;


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

        // TODO: Implement this
        // out of bound
        if (x < 0 || x > width || z < 0 || z > depth) {
            return altitude;
        }

        int leftX = (int)Math.floor(x);
        int rightX;// = leftX + 1;
        if(leftX + 1 == width) {
        	rightX = leftX;
        } else {
        	rightX = leftX + 1;
        }
        int bottomZ = (int)Math.floor(z);
        int topZ;// = bottomZ + 1;
        if(bottomZ + 1 == depth) {
        	topZ = bottomZ;
        } else {
        	topZ = bottomZ + 1;
        }

//        int leftX = (int)Math.floor(x);
//        int rightX = leftX + 1;
//        int bottomZ = (int)Math.floor(z);
//        int topZ = bottomZ + 1;
        // test point is above or below a line
        // p1(leftX, topZ) p0(rightX, topZ)
        // -----------
        // |        /|
        // |   I1  / |
        // |    . /  |
        // |     /   |
        // |    /    |
        // |   /  .  |
        // |  /  I2  |
        // | /       |
        // |----------
        // p2(leftX, bottomZ) p3(rightX, bottomZ)
        // if x, and z are the whole number, get altitude

//        if ((int)x == x && (int)z == z) {
//            System.out.println("whole number: " + (float)getGridAltitude((int)x, (int)z));
//            return (float)getGridAltitude((int)x, (int)z);
//
//        } else if ((int)x == x && (int)z != z) {
//            altitude = linearInterpolation(z, (int)x, bottomZ, (int)x, topZ);
//            System.out.println("x is whole number: " + altitude);
//
//        } else if ((int)z == z && (int)x != x) {
//
//            altitude = linerInterpolationX(x, leftX, rightX, (float)getGridAltitude(leftX, (int)z), (float)getGridAltitude(rightX, (int)z));
//            System.out.println("z is whole number: " + altitude);
//
//        } else {
//                // first, check if the given point P(x, z) lies in on diagonal, or above/below
//                float val = (((x - (float)leftX) * ((float)topZ - (float)bottomZ)) / ((float)rightX - (float)leftX)) + (float)bottomZ;
//                // if point line in the line
//                if (val == z) {
//                    altitude = linearInterpolation(z, leftX, bottomZ, rightX, topZ);
//                    System.out.println("point lie in the diagonal: " + altitude);
//
//                } else if (z > val) {
//                    // above the line, intersect with p1-p2 and p0-p2
//                    float left = linearInterpolation(z, leftX, bottomZ, leftX, topZ);
//                    float right = linearInterpolation(z, leftX, bottomZ, rightX, topZ);
//                    float interSec = interSectionL(z, leftX, bottomZ, rightX, topZ);
//                    altitude = linerInterpolationX(x, (float)leftX, interSec, left, right);
//                    System.out.println("point lie above the diagonal: " + altitude);
//
//                } else {
//                    // below the line, intersect with p2-p0 and p3-p0
//                    float left = linearInterpolation(z, leftX, bottomZ, rightX, topZ);
//                    float right = linearInterpolation(z, rightX, bottomZ, rightX, topZ);
//                    float interSec = interSectionL(z, leftX, bottomZ, rightX, topZ);
//                    altitude = linerInterpolationX(x, interSec, (float)rightX, left, right);
//                    System.out.println("point lie below the diagonal: " + altitude);
//                }
//        }
        //+++++++++++++++++++++++MAGIC+++++++++++++++++++++++++++++
        float hypotenuse = leftX + topZ - z;
        if(leftX == rightX && topZ == bottomZ) {
        	return (float) getGridAltitude(leftX, bottomZ);
        }
        if(x < hypotenuse) {
        	float lipz1 = linearInterPolateZ(z, topZ, bottomZ, leftX, rightX);
        	float lipz2 = linearInterPolateZ(z, topZ, bottomZ, leftX, leftX);
        	altitude = bilinearInterpolate(x, (float)leftX, hypotenuse, lipz1, lipz2);
        } else if(leftX == x || rightX == x) {
        	altitude = linearInterPolateZ(z, bottomZ, topZ, x, x);
        } else if(topZ == z || bottomZ == z) {
        	altitude = linearInterPolateX(x, leftX, rightX, z, z);
        } else {
        	float lipz1 = linearInterPolateZ(z, topZ, bottomZ, leftX, rightX);
        	float lipz2 = linearInterPolateZ(z, topZ, bottomZ, leftX, leftX);
        	altitude = bilinearInterpolate(x, (float)leftX, hypotenuse, lipz1, lipz2);
        }
        return altitude;
    }

//    /**
//     * Use bilinear Interpolation to calculate the depth of the
//     * @return
//     */
//    public float linearInterpolation(float z, int x1, int z1, int x2, int z2) {
//        float altitude = ((z - (float)z1)/((float)z2 - (float)z1)) * (float)getGridAltitude(x2, z2) + (((float)z2 - z) / ((float)z2 - (float)z1)) * (float)getGridAltitude(x1, z1);
//        return altitude;
//    }
//
//    public float linerInterpolationX(float x, float x1, float x2, float y1, float y2) {
//        return ((x - x1)/(x2 - x1)) * y2 + ((x2 - x)/(x2 - x1)) * y1;
//    }
//
//    public float interSectionL(float y, float x0, float y0, float x1, float y1) {
//        return (((x1 - x0) * (y - y0)) / (y1 - y0)) + x0;
//    }
//
//    public float interSectionR(float y, float x0, float y0, float x1, float y1) {
//        return (x1 - x0) * (y1 - y)/(y1 - y0) + x1;
//    }
    
    //================================MINE===========
    private float bilinearInterpolate(float x, float x1, float hypotenuse, float lipz1, float lipz2) {
    	return ((x - x1)/(hypotenuse - x1)) * lipz1 + ((hypotenuse - x)/(hypotenuse - x1) * lipz2);
    }

    private float linearInterPolateZ(float z, float z1, float z2, float x1, float x2) {
    	return (float) (((z - z1) /(z2 - z1)) * getGridAltitude((int) x2, (int)z2) +
    			((z2 - z)/(z2 - z1)) * getGridAltitude((int)x1, (int)z1));
    }
    private float linearInterPolateX(float x, float x1, float x2, float z1, float z2) {
    	return (float) (((x - x1)/ (x2 - x1)) *getGridAltitude((int) x2, (int)z2)
    			+ ((x2 - x)/ (x2 - x1)) * getGridAltitude ((int) x1, (int) z1));
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


    public void initTree(GL3 gl) {
        for (Tree tree : trees) {
            tree.initTree(gl);
        }
    }

    public void drawTree(GL3 gl, CoordFrame3D frame) {
        initTree(gl);
        // set shader color
        Shader.setPenColor(gl, Color.ORANGE);
        for (Tree tree : trees) {
            tree.initTree(gl);
            tree.display(gl, frame);
        }
    }
    /**
     * Add a road.
     *
     */
    public void addRoad(float width, List<Point2D> spine) {
        Road road = new Road(width, spine);
        roads.add(road);
    }


    public void initTerrian(GL3 gl) {
        ArrayList<Point3D> allVertices = new ArrayList<>();
        ArrayList<Point2D> textureCoords = new ArrayList<>();
        ArrayList<Integer> triangleMashes = new ArrayList<>();

        // save all the point into allVertices
        for (int x = 0; x < width; x++) {
            for (int z = 0; z < width; z++) {
                allVertices.add(new Point3D((float)x, (float)getGridAltitude(x, z), (float)z));
                // add current vertex to texture array
                textureCoords.add(new Point2D((float)x, (float)z));
            }
        }

        // add the vertives of each triangle mesh to triangleMeshes list
        for (int x = 0; x < width - 1; x++) {
            for (int z = 0; z < depth - 1; z++) {
                // add triangles to the triangleMashes
                // curr
                // x----------
                // |        /|
                // | LEFT  / |
                // |      /  |
                // |     /   |
                // |    /    |
                // |   /     |
                // |  / RIGHT|
                // | /       |
                // |----------

                int topLeft = z + x * depth;
                int leftButton = 1 + z + x * depth;
                int topRight = z + ((x + 1) * depth);
                int rightButton = 1 + z + ((x + 1) * depth);

                // add the left triangle
                triangleMashes.add(topLeft);
                triangleMashes.add(leftButton);
                triangleMashes.add(topRight);

                // add the right triangle
                triangleMashes.add(topRight);
                triangleMashes.add(leftButton);
                triangleMashes.add(rightButton);
            }
        }

        // create new triangleMesh for terrian
        terrainMesh = new TriangleMesh(allVertices, triangleMashes, true);
        terrainMesh.init(gl);
    }

    public void drawTerrain(GL3 gl, CoordFrame3D frame) {
        // set color of terrian to green
        Shader.setPenColor(gl, Color.GREEN);
        terrainMesh.draw(gl, frame);

    }

    public void init(GL3 gl) {
        initTerrian(gl);
    }

    public void draw(GL3 gl, CoordFrame3D frame) {
        drawTerrain(gl, frame);
        drawTree(gl, frame);
    }

}
