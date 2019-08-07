package unsw.graphics.world;

import java.awt.Color;
import com.jogamp.newt.event.KeyAdapter;
import com.jogamp.newt.event.KeyEvent;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import com.jogamp.opengl.GL3;

import unsw.graphics.Application3D;
import unsw.graphics.CoordFrame3D;
import unsw.graphics.Matrix4;
import unsw.graphics.Shader;
import unsw.graphics.geometry.Point2D;
import unsw.graphics.geometry.Point3D;

/**
 * COMMENT: Comment Game
 *
 * @author malcolmr
 */
public class World extends Application3D{

    private Terrain terrain;
    private Camera camera;

	private Point2D myMousePoint = null;
	private static final int ROTATION_SCALE = 1;
	private float rotateX = 0;
	private float rotateY = 0;

    public World(Terrain terrain) {
    	super("Assignment 2", 1000, 1000);
        this.terrain = terrain;
        this.camera = new Camera(new Point3D(0f, 0.5f, -1.1f), this.terrain);

    }

    /**
     * Load a level file and display it.
     *
     * @param args - The first argument is a level file in JSON format
     * @throws FileNotFoundException
     */
    public static void main(String[] args) throws FileNotFoundException {
        Terrain terrain = LevelIO.load(new File("/Users/yingzhizhou/Desktop/COMP3421-3D/UNSWgraph-0.10/res/worlds/test1.json"));
        World world = new World(terrain);
        world.start();
    }

	@Override
	//test
	public void display(GL3 gl) {
		super.display(gl);
		//Shader.setPenColor(gl, Color.GREEN);
		//CoordFrame3D frame = CoordFrame3D.identity().translate(0, 0, -2.4f).scale(0.3f, 0.3f, 0.3f).rotateX(rotateX).rotateY(rotateY);

		// change with camera
		CoordFrame3D frame = camera.resetFrame();
		//Shader.setViewMatrix(gl, frame.getMatrix());

		terrain.draw(gl, frame);

	}

	@Override
	public void destroy(GL3 gl) {
		super.destroy(gl);
	}

	@Override
	public void init(GL3 gl) {
		super.init(gl);
		Shader shader = new Shader(gl, "shaders/vertex_tex_phong.glsl",
				"shaders/fragment_tex_phong.glsl");
		shader.use(gl);

		// Set the lighting properties
		Shader.setPoint3D(gl, "lightPos", new Point3D(0, 0, 5));
		Shader.setColor(gl, "lightIntensity", Color.WHITE);
		Shader.setColor(gl, "sunlightIntensity", Color.WHITE);
		Shader.setColor(gl, "ambientIntensity", new Color(0.5f, 0.5f, 0.5f));

		// Set the material properties
		Shader.setColor(gl, "ambientCoeff", Color.WHITE);
		Shader.setColor(gl, "diffuseCoeff", new Color(0.6f, 0.6f, 0.6f));
		Shader.setColor(gl, "specularCoeff", new Color(0.3f, 0.3f, 0.3f));
		Shader.setFloat(gl, "phongExp", 16f);
		Shader.setInt(gl, "tex", 0);
		Shader.setPoint3D(gl, "viewPosition", camera.getPosition());

		terrain.init(gl);
		getWindow().addKeyListener(this.camera);
	}

	@Override
	public void reshape(GL3 gl, int width, int height) {
        super.reshape(gl, width, height);
        Shader.setProjMatrix(gl, Matrix4.perspective(60, width/(float)height, 1, 100));
	}

}
