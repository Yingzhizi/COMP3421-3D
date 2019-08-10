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
    private Point3D position;
    float rotateX, rotateY, rotateZ;
    float scale;
    private Texture texture;
    private TriangleMesh mesh;
    private static final float RUN_SPEED = 0.1f;
    private static final float TURN_SPEED = 1.5f;

    private float currentSpeed = 0;
    private float currentTurnSpeed = 0;

    public Avatar(Point3D position, float rX, float rY, float rZ) {
        this.position = position;
        this.rotateX = rX;
        this.rotateY = rY;
        this.rotateZ = rZ;

    }

    /**
     * change position of avatar
     * @param newPosition
     */
    public void setPosition(Point3D newPosition) {
        this.position = newPosition;

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
    public void init(GL3 gl) {
        try {
            mesh = new TriangleMesh("res/models/bunny_res2.ply", true, true);
            mesh.init(gl);
            this.texture = new Texture(gl, "res/textures/fur.png", "png", true);;
        } catch (Exception e) {
            System.out.println("Something is wrong");
        }
    }

    public void display(GL3 gl, CoordFrame3D frame) {
        // move the frame to the right position
        frame = frame.translate(getPosition().getX(), getPosition().getY()-0.05f, getPosition().getZ()).rotateY(-85);
        // bind texture
        Shader.setInt(gl, "tex", 0);
        gl.glActiveTexture(GL.GL_TEXTURE0);
        gl.glBindTexture(GL.GL_TEXTURE_2D, this.texture.getId());
        Shader.setPenColor(gl, Color.WHITE);
        mesh.draw(gl, frame);
    }
}
