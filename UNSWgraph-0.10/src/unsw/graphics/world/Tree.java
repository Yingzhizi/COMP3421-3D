package unsw.graphics.world;

import unsw.graphics.CoordFrame3D;
import unsw.graphics.geometry.Point3D;
import unsw.graphics.geometry.TriangleMesh;

import com.jogamp.opengl.GL3;

import java.io.IOException;

/**
 * COMMENT: Comment Tree 
 *
 * @author malcolmr
 */
public class Tree {

    private Point3D position;
    private TriangleMesh treeMesh;
    
    public Tree(float x, float y, float z) {
        position = new Point3D(x, y, z);
    }
    
    public Point3D getPosition() {
        return position;
    }

    //initiate tree, create triangleMesh from ply file
    public void initTree(GL3 gl) {
        try {
            treeMesh = new TriangleMesh("res/models/tree.ply", true, true);
            treeMesh.init(gl);
        } catch (Exception e) {
            System.out.println("Something is wrong");
        }
    }

    public void display(GL3 gl, CoordFrame3D frame) {
        // move the frame to the right position
        frame = frame.translate(getPosition().getX(), getPosition().getY()+0.6f, getPosition().getZ()).scale(0.12f, 0.12f, 0.12f);
        treeMesh.draw(gl, frame);
    }
}
