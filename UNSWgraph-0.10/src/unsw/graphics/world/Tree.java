package unsw.graphics.world;

import unsw.graphics.CoordFrame3D;
import unsw.graphics.geometry.Point3D;
import unsw.graphics.geometry.TriangleMesh;

import com.jogamp.opengl.GL3;
import unsw.graphics.*;


/**
 * Class for tree
 *
 * @author Yingzhi Zhou
 */
public class Tree {

    private Point3D position;
    private TriangleMesh treeMesh;
    private Texture treeTexture;

    /**
     * initialise tree
     * @param x x coordinate of tree position
     * @param y y coordinate of tree position
     * @param z z coordinate of tree position
     */
    public Tree(float x, float y, float z) {
        position = new Point3D(x, y, z);
    }

    /**
     * get the position of this tree
     * @return
     */
    public Point3D getPosition() {
        return position;
    }

    /**
     * create triangleMesh for a tree from ply file
     * set texture for this tree
     * @param gl
     * @param tex the texture of this tree
     */
    public void initTree(GL3 gl, Texture tex) {
        try {
            treeMesh = new TriangleMesh("res/models/tree.ply", true, true);
            treeMesh.init(gl);
            this.treeTexture = tex;
        } catch (Exception e) {
            System.out.println("Something is wrong");
        }
    }

    /**
     * draw the triangle mesh of this tree on the world by the given frame
     * set the frame to the proper position according to the position of this tree
     * @param gl
     * @param frame
     */
    public void display(GL3 gl, CoordFrame3D frame) {
        // move the frame to the right position, adjust the altitude a bit higher and scale it down
        frame = frame.translate(getPosition().getX(), getPosition().getY()+0.6f, getPosition().getZ()).scale(0.12f, 0.12f, 0.12f);
        treeMesh.draw(gl, frame);
    }
}
