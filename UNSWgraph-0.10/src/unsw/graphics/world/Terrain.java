package unsw.graphics.world;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

//import sun.security.x509.AVA;
import unsw.graphics.Vector3;
import unsw.graphics.geometry.Point2D;
import unsw.graphics.geometry.Point3D;
import unsw.graphics.geometry.TriangleMesh;
import unsw.graphics.*;

import com.jogamp.opengl.GL;
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
    private Texture texture;
    private Texture treeTexture;
    private Texture avatarTexture;
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

        // TODO: Implement this
        // out of bound
        if (x < 0 || x > width || z < 0 || z > depth) {
            return altitude;
        }

//        // test point is above or below a line
//        // p1(leftX, topZ) p0(rightX, topZ)
//        // -----------
//        // |        /|
//        // |   I1  / |
//        // |    . /  |
//        // |     /   |
//        // |    /    |
//        // |   /  .  |
//        // |  /  I2  |
//        // | /       |
//        // |----------
//        if ((int)x == x && (int)z == z) {
//            System.out.println("whole number: " + (float)getGridAltitude((int)x, (int)z));
//            return (float)getGridAltitude((int)x, (int)z);
//
//        } else if ((int)x == x && (int)z != z) {
//            return linearInterPolateZ(z, bottomZ, topZ, x, x);
//        } else if ((int)z == z && (int)x != x) {
//            return linearInterPolateX(x, leftX, rightX, z, z);
//        } else {
//            float hypotenuse = z - bottomZ + leftX;
//            if(x > hypotenuse) {
//                float lipz1 = linearInterPolateZ(z, bottomZ, topZ, leftX, rightX);
//                float lipz2 = linearInterPolateZ(z, bottomZ, topZ, rightX, rightX);
//                altitude = bilinearInterpolate(x, hypotenuse, (float)rightX, lipz1, lipz2);
//            } else if (x < hypotenuse){
//                float lipz1 = linearInterPolateZ(z, bottomZ, topZ, leftX, leftX);
//                float lipz2 = linearInterPolateZ(z, bottomZ, topZ, leftX, rightX);
//                altitude = bilinearInterpolate(x, (float)leftX, hypotenuse, lipz1, lipz2);
//            } else {
//                altitude = linearInterPolateZ(z, bottomZ, topZ, leftX, rightX);
//            }
//        }
//        return altitude;
        int leftX = (int)Math.floor(x);
        int rightX = (int)Math.ceil(x);
        int bottomZ = (int)Math.floor(z);
        int topZ = (int)Math.ceil(z);
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

        float hypotenuse = z - bottomZ + leftX;
        if((int)x == x && (int)z == z) {
            altitude =  (float)getGridAltitude((int)x, (int)z);
        } else if ((int)x != x && (int)z == z) {
            altitude =  linearInterPolateX(x, leftX, rightX, z, z);
        } else if ((int)x == x && (int)z != z) {
            altitude = linearInterPolateZ(z, bottomZ, topZ, x, x);
        } else {
            if(x < hypotenuse) {
                float lipz1 = linearInterPolateZ(z, bottomZ, topZ, leftX, leftX);
                float lipz2 = linearInterPolateZ(z, bottomZ, topZ, leftX, rightX);
                altitude = bilinearInterpolate(x, (float)leftX, hypotenuse, lipz1, lipz2);
                System.out.println("above: ");
            } else if (x > hypotenuse){
                float lipz1 = linearInterPolateZ(z, bottomZ, topZ, leftX, rightX);
                float lipz2 = linearInterPolateZ(z, bottomZ, topZ, rightX, rightX);
                altitude = bilinearInterpolate(x, hypotenuse, (float)rightX, lipz1, lipz2);
                System.out.println("below: ");
            } else {
                altitude = linearInterPolateZ(z, bottomZ, topZ, leftX, rightX);
                System.out.println("hypotenuse:");
            }
        }
        return altitude;
    }

    /**
     * Use bilinear Interpolation to calculate the depth of the
     * @return
     */
    private float bilinearInterpolate(float x, float x1, float hypotenuse, float lipz1, float lipz2) {
    	return ((x - x1)/(hypotenuse - x1)) * lipz2 + ((hypotenuse - x)/(hypotenuse - x1)) * lipz1;
    }

    private float linearInterPolateZ(float z, float z1, float z2, float x1, float x2) {
    	return (float) (((z - z1) /(z2 - z1)) * getGridAltitude((int)x2, (int)z2) +
    			((z2 - z)/(z2 - z1)) * getGridAltitude((int)x1, (int)z1));
    }
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


    public void initTree(GL3 gl) {
        for (Tree tree : trees) {
            tree.initTree(gl, this.treeTexture);
        }
    }

    public void drawTree(GL3 gl, CoordFrame3D frame) {
        initTree(gl);
        // set shader color
        Shader.setInt(gl, "tex", 0);
        gl.glActiveTexture(GL.GL_TEXTURE0);
        gl.glBindTexture(GL.GL_TEXTURE_2D, this.treeTexture.getId());
        Shader.setPenColor(gl, Color.WHITE);
        for (Tree tree : trees) {
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

//    public void initRoad(GL3 gl) {
//        for (Road road : roads) {
//        	System.out.println("FUCKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKK");
//            road.init(gl, this.roadTexture);
//        }
//    }
//    
    
    public void drawRoad(GL3 gl, CoordFrame3D frame) {
        //initRoad(gl);
        // set shader color
        Shader.setInt(gl, "tex", 0);
        gl.glActiveTexture(GL.GL_TEXTURE0);
        gl.glBindTexture(GL.GL_TEXTURE_2D, this.roadTexture.getId());
        for (Road road : roads) {
            road.draw(gl, frame);
        }
        Shader.setPenColor(gl, Color.WHITE);
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
                textureCoords.add(x, new Point2D((float)x, (float)z));
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
        terrainMesh = new TriangleMesh(allVertices, triangleMashes, true, textureCoords);
        terrainMesh.init(gl);
    }

    // add texture to terrain
    public void loadTexture(GL3 gl) {
        this.texture = new Texture(gl, "res/textures/grass.bmp", "bmp", true);
        this.treeTexture = new Texture(gl, "res/textures/tree.png", "png", true);
        this.roadTexture = new Texture(gl, "res/textures/rock.bmp", "bmp", true);
        for (Road r: this.roads) {
        	r.init(gl, this);
        }
    }

    public void drawTerrain(GL3 gl, CoordFrame3D frame) {
        Shader.setInt(gl, "tex", 0);
        gl.glActiveTexture(GL.GL_TEXTURE0);
        gl.glBindTexture(GL.GL_TEXTURE_2D, texture.getId());
        Shader.setPenColor(gl, Color.WHITE);

        terrainMesh.draw(gl, frame);

    }



    public void init(GL3 gl) {
        initTerrian(gl);
        // load texture of terrain
        loadTexture(gl);
    }

    public void draw(GL3 gl, CoordFrame3D frame) {
        drawTerrain(gl, frame);
        drawTree(gl, frame);
        drawRoad(gl, frame);
    }
}
