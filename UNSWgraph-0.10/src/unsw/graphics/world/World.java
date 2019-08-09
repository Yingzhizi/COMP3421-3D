package unsw.graphics.world;

import java.awt.*;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.io.*;

import com.jogamp.newt.event.KeyEvent;
import com.jogamp.newt.event.KeyListener;

import unsw.graphics.geometry.Point2D;
import unsw.graphics.geometry.Point3D;
import unsw.graphics.geometry.TriangleMesh;
import unsw.graphics.*;

import com.jogamp.opengl.GL;
import com.jogamp.opengl.GL3;

import java.util.concurrent.TimeUnit;

/**
 * COMMENT: Comment Game
 *
 * @author malcolmr
 */
public class World extends Application3D implements KeyListener{

    private Terrain terrain;
    private Camera camera;
    private Avatar avatar;
	private Pond pond;
	private TriangleMesh ground;
	private Texture groundTexture;

    //day time and night time mode
    private boolean night;

	private Point2D myMousePoint = null;
	private static final int ROTATION_SCALE = 1;
	private float rotateX = 0;
	private float rotateY = 0;
	private Point3D sunPos;
	static float totaltimepassed = 0;
	
	//Lighting property that works for day time and night time
	private Color lightIntensity = Color.WHITE;
	private Color sunlightIntensity = Color.WHITE;
	private Color ambientIntensity = new Color(0.3f, 0.3f, 0.3f);
	

    public World(Terrain terrain) {
    	super("Assignment 2", 800, 800);
        this.terrain = terrain;
		this.avatar = new Avatar(new Point3D(1, (float)terrain.getGridAltitude(1, 1), 1), 0, 0, 0);
        this.camera = new Camera(new Point3D(0f, 0f, 0f), terrain);
		pond = new Pond(1, 0, 1, 2, 2);
        this.night = false;
    }

    /**
     * Load a level file and display it.
     *
     * @param args - The first argument is a level file in JSON format
     * @throws FileNotFoundException
     */
    public static void main(String[] args) throws FileNotFoundException {
        Terrain terrain = LevelIO.load(new File("/Users/yingzhizhou/Desktop/COMP3421-3D/UNSWgraph-0.10/res/worlds/test1.json"));//"/Users/yingzhizhou/Desktop/COMP3421-3D/UNSWgraph-0.10/res/worlds/test4.json"));
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
		CoordFrame3D avatarFame = camera.resetAvatarFrame();
		// reset the position of avatar, also the rotation
		avatar.setPosition(camera.getNewPos());
		avatar.increaseRotation(0, camera.getRotate(),0);
		System.out.println("new x:" + avatar.getPosition().getX() + ";" + "new y: " + avatar.getPosition().getY() + "; new z" + avatar.getPosition().getZ());
		if(night) {
			Shader.setColor(gl, "lightIntensity", this.lightIntensity);
			Shader.setColor(gl, "sunlightIntensity", this.sunlightIntensity);
			Shader.setColor(gl, "ambientIntensity", this.ambientIntensity);
			Shader.setColor(gl, "skyColor", Color.GRAY);
		} else {
			Shader.setColor(gl, "lightIntensity", this.lightIntensity);
			Shader.setColor(gl, "sunlightIntensity", this.sunlightIntensity);
			Shader.setColor(gl, "ambientIntensity", this.ambientIntensity);
			Shader.setColor(gl, "skyColor", Color.WHITE);
		}
		// when drawing terrain, avatar, set tex to 0;
		Shader.setInt(gl, "tex", 0);
		terrain.draw(gl, frame);
		avatar.display(gl, avatarFame);
		totaltimepassed += 0.02;
		if (totaltimepassed < 0.1) {
			Shader.setInt(gl, "tex", 0);
		} else if (totaltimepassed < 0.2) {
			Shader.setInt(gl, "tex", 1);
		} else if (totaltimepassed < 0.3) {
			Shader.setInt(gl, "tex", 2);
		} else if (totaltimepassed < 0.4) {
			Shader.setInt(gl, "tex", 3);
		} else if (totaltimepassed < 0.5) {
			Shader.setInt(gl, "tex", 4);
		} else if (totaltimepassed < 0.6) {
			Shader.setInt(gl, "tex", 5);
		} else {
			totaltimepassed = 0;
		}
		pond.draw(gl, frame.translate(0, -0.09f, 0));
		// somehow start switching

	}

	@Override
	public void destroy(GL3 gl) {
    	super.destroy(gl);
    	terrain.destroy(gl);
    	groundTexture.destroy(gl);
		ground.destroy(gl);

	}

	@Override
	public void init(GL3 gl) {
		super.init(gl);
		getWindow().addKeyListener(this.camera);
		getWindow().addKeyListener(this);
		sunPos = this.terrain.getSunlight().asPoint3D();
		Shader shader = new Shader(gl, "shaders/vertex_tex_phong.glsl",
				"shaders/fragment_tex_phong.glsl");
		shader.use(gl);
		
		// Set the lighting properties
		Shader.setPoint3D(gl, "lightPos", sunPos);

		// Set the material properties
		Shader.setColor(gl, "ambientCoeff", Color.WHITE);
		Shader.setColor(gl, "diffuseCoeff", new Color(0.6f, 0.6f, 0.6f));
		Shader.setColor(gl, "specularCoeff", new Color(0.3f, 0.3f, 0.3f));
		Shader.setFloat(gl, "phongExp", 16f);
		Shader.setInt(gl, "tex", 0);
		Shader.setPoint3D(gl, "viewPosition", camera.getPosition());

		terrain.init(gl);
		avatar.init(gl);
		pond.init(gl);
		initGround(gl);

	}

	@Override
	public void reshape(GL3 gl, int width, int height) {
        super.reshape(gl, width, height);
        Shader.setProjMatrix(gl, Matrix4.perspective(60, width/(float)height, 1, 100));
	}

//	private void switchView(KeyEvent e) {
//		int keyCode = e.getKeyCode();
//		if (keyCode == KeyEvent.VK_C) {
//			if (this.thirdPlayer == false) {
//				this.thirdPlayer = true;
//			} else {
//				this.thirdPlayer = false;
//			}
//		}
//	}
//
//	@Override
//	public void keyPressed(KeyEvent e) {
//		switchView(e);
//	}
//
//	@Override
//	public void keyReleased(KeyEvent keyEvent) {
//
//	}
    public void initGround(GL3 gl) {
        //Build the meshes
        List<Point3D> grassVerts = new ArrayList<>();
        grassVerts.add(new Point3D(-50, 0, 50));
        grassVerts.add(new Point3D(50, 0, 50));
        grassVerts.add(new Point3D(50, 0, -50));
        grassVerts.add(new Point3D(-50, 0, -50));

        List<Point2D> grassTexCoords = new ArrayList<>();
        grassTexCoords.add(new Point2D(0, 0));
        grassTexCoords.add(new Point2D(100, 0));
        grassTexCoords.add(new Point2D(100, 100));
        grassTexCoords.add(new Point2D(0, 100));

        List<Integer> grassIndices = Arrays.asList(0,1,2, 0,2,3);

		ground = new TriangleMesh(grassVerts, grassIndices, false, grassTexCoords);
		ground.init(gl);
		this.groundTexture = new Texture(gl, "res/textures/grass.png", "png", true);
    }

    public void drawGround(GL3 gl, CoordFrame3D frame) {
        Shader.setInt(gl, "tex", 0);
        gl.glActiveTexture(GL.GL_TEXTURE0);
        gl.glBindTexture(GL.GL_TEXTURE_2D, groundTexture.getId());
        Shader.setPenColor(gl, Color.WHITE);
		ground.draw(gl, frame);
    }

	@Override
	public void keyPressed(KeyEvent e) {
		int keyCode = e.getKeyCode();
		if(keyCode == KeyEvent.VK_N) {
			//System.out.println("NIGHT TIME LOSER");
			//make night time boolean the opposite
			this.night = this.night == true ? false : true;
			if(night) {
				this.lightIntensity = new Color(0.3f, 0.3f, 0.3f);
				this.sunlightIntensity = new Color(0.3f, 0.3f, 0.3f);
				this.ambientIntensity = new Color(0.2f, 0.2f, 0.2f);
				System.out.println("WE ARE IN NIGHT MODE");
			} else {
				this.lightIntensity = Color.WHITE;
				this.sunlightIntensity = Color.WHITE;
				this.ambientIntensity = new Color(0.3f, 0.3f, 0.3f);
				System.out.println("WE ARE IN DAY MODE");
			}
		}
		
	}

	@Override
	public void keyReleased(KeyEvent e) {
		// TODO Auto-generated method stub
		
	}

}
