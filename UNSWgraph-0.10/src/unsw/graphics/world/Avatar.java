package unsw.graphics.world;

import java.awt.*;
import unsw.graphics.geometry.Point3D;
import unsw.graphics.geometry.TriangleMesh;
import unsw.graphics.*;

import com.jogamp.opengl.GL;
import com.jogamp.opengl.GL3;

/**
 * The character can move around in the world
 */
public class Avatar {
    private Point3D position;
    float rotateX, rotateY, rotateZ;
    private Texture texture;
    private TriangleMesh mesh;

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


    /**
     * get the position of avatar
     * @return
     */
    public Point3D getPosition() {
        return position;
    }

    /**
     * create the triangle mesh for avatar, also add texture to it
     * @param gl
     */
    public void init(GL3 gl) {
        try {
            mesh = new TriangleMesh("res/models/bunny_res2.ply", true, true);
            mesh.init(gl);
            this.texture = new Texture(gl, "res/textures/fur.png", "png", true);;
        } catch (Exception e) {
            System.out.println("Something is wrong");
        }
    }

    /**
     * draw the triangle mesh of avatar
     * @param gl
     * @param frame
     */
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
