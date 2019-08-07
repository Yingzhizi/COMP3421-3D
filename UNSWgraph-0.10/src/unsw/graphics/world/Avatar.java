package unsw.graphics.world;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

import com.jogamp.newt.event.KeyEvent;
import com.jogamp.newt.event.KeyListener;
import unsw.graphics.Vector3;
import unsw.graphics.geometry.Point2D;
import unsw.graphics.geometry.Point3D;
import unsw.graphics.geometry.TriangleMesh;
import unsw.graphics.*;

import com.jogamp.opengl.GL;
import com.jogamp.opengl.GL3;

public class Avatar {
    Texture texture;
    private Point3D position;
    float rotateX, rotateY, rotateZ;
    float scale;
    private TriangleMesh mesh;
    private static final float RUN_SPEED = 0.1f;
    private static final float TURN_SPEED = 1.5f;

    private float currentSpeed = 0;
    private float currentTurnSpeed = 0;

    public Avatar(Texture tex, Point3D position, float rX, float rY, float rZ) {
        this.texture = tex;
        this.position = position;
        this.rotateX = rX;
        this.rotateY = rY;
        this.rotateZ = rZ;

    }

    /**
     * change position of avatar
     * @param dx
     * @param dy
     * @param dz
     */
    public void increasePosition(float dx, float dy, float dz) {
        this.position.translate(dx, dy, dz);

    }

    /**
     * change rotation of avatar
     * @param dx
     * @param dy
     * @param dz
     */
    public void increaseRotation(float dx, float dy, float dz) {
        this.rotateX += dx;
        this.rotateY += dy;
        this.rotateZ += dz;
    }
    public Texture getTexture() {
        return texture;
    }

    public float getRotateX() {
        return rotateX;
    }

    public float getRotateY() {
        return rotateY;
    }

    public float getRotateZ() {
        return rotateZ;
    }

    public float getScale() {
        return scale;
    }

    public Point3D getPosition() {
        return position;
    }
    //initiate tree, create triangleMesh from ply file
    public void init(GL3 gl, Texture tex) {
        try {
            mesh = new TriangleMesh("res/models/bunny_res2.ply", true, true);
            mesh.init(gl);
            this.texture = tex;
        } catch (Exception e) {
            System.out.println("Something is wrong");
        }
    }

    public void display(GL3 gl, CoordFrame3D frame) {
        // move the frame to the right position
        frame = frame.translate(0, -0.04f, 0);
        mesh.draw(gl, frame);
    }
}
