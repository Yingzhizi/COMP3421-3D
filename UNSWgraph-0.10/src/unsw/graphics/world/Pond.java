package unsw.graphics.world;

import com.jogamp.opengl.GL;
import com.jogamp.opengl.GL3;
import unsw.graphics.CoordFrame3D;
import unsw.graphics.Shader;
import unsw.graphics.Texture;
import unsw.graphics.geometry.Point2D;
import unsw.graphics.geometry.Point3D;
import unsw.graphics.geometry.TriangleMesh;

import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Pond can been added to world
 */
public class Pond {
    private Point3D position;
    private float width;
    private float length;
    private TriangleMesh pondMesh;
    private Texture pondTexture1;
    private Texture pondTexture2;
    private Texture pondTexture3;
    private Texture pondTexture4;
    private Texture pondTexture5;
    private Texture pondTexture6;

    /**
     * generate a pond by entering it's position, width and length
     */
    public Pond(float x, float y, float z, float width, float length) {
        this.position = new Point3D(x, y, z);
        this.width  = width;
        this.length = length;
    }

    /**
     * get position of the pond
     * @return
     */
    public Point3D getPosition() {
        return position;
    }

    /**
     * get width of the pond
     * @return
     */
    public float getWidth() {
        return width;
    }

    /**
     * get lenght of the pond
     * @return
     */
    public float getLength() {
        return length;
    }

    /**
     * generate triangle mesh for pond and add textures to it to make it animates
     * @param gl
     */
    public void init(GL3 gl) {
        List<Point3D> pondsVertices = new ArrayList<>();
        Point3D topLeft = new Point3D(this.position.getX() - length/2, 0, this.position.getZ() + width/2);
        Point3D bottomLeft = new Point3D(this.position.getX() - length/2, 0, this.position.getZ() - width/2);
        Point3D topRight = new Point3D(this.position.getX() + length/2, 0, this.position.getZ() + width/2);
        Point3D bottomRight = new Point3D(this.position.getX() + length/2, 0, this.position.getZ() - width/2);

        pondsVertices.add(topLeft);
        pondsVertices.add(topRight);
        pondsVertices.add(bottomRight);
        pondsVertices.add(bottomLeft);

        List<Integer> pondIndices = Arrays.asList(0,1,2, 0,2,3);

        List<Point2D> pondTextCoords = new ArrayList<>();
        pondTextCoords.add(new Point2D(0, 0));
        pondTextCoords.add(new Point2D(1, 0));
        pondTextCoords.add(new Point2D(1, 1));
        pondTextCoords.add(new Point2D(0, 1));

        // add textCoord
        pondMesh = new TriangleMesh(pondsVertices, pondIndices,true, pondTextCoords);
        pondMesh.init(gl);
        this.pondTexture1 = new Texture(gl, "res/textures/water1.png", "png", true);
        this.pondTexture2 = new Texture(gl, "res/textures/water2.png", "png", true);
        this.pondTexture3 = new Texture(gl, "res/textures/water3.png", "png", true);
        this.pondTexture4 = new Texture(gl, "res/textures/water4.png", "png", true);
        this.pondTexture5 = new Texture(gl, "res/textures/water5.png", "png", true);
        this.pondTexture6 = new Texture(gl, "res/textures/water6.png", "png", true);
    }

    /**
     * draw the triangle mesh of pond and create textures array
     * @param gl
     * @param frame
     */
    public void draw(GL3 gl, CoordFrame3D frame) {
        frame = frame.translate(0, 0.1f, 0);

        gl.glActiveTexture(GL.GL_TEXTURE0);
        gl.glBindTexture(GL.GL_TEXTURE_2D, this.pondTexture1.getId());

        gl.glActiveTexture(GL.GL_TEXTURE1);
        gl.glBindTexture(GL.GL_TEXTURE_2D, this.pondTexture2.getId());

        gl.glActiveTexture(GL.GL_TEXTURE2);
        gl.glBindTexture(GL.GL_TEXTURE_2D, this.pondTexture3.getId());

        gl.glActiveTexture(GL.GL_TEXTURE3);
        gl.glBindTexture(GL.GL_TEXTURE_2D, this.pondTexture4.getId());

        gl.glActiveTexture(GL.GL_TEXTURE4);
        gl.glBindTexture(GL.GL_TEXTURE_2D, this.pondTexture5.getId());

        gl.glActiveTexture(GL.GL_TEXTURE5);
        gl.glBindTexture(GL.GL_TEXTURE_2D, this.pondTexture6.getId());

        Shader.setPenColor(gl, Color.WHITE);

        pondMesh.draw(gl, frame);
    }
}

