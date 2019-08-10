package unsw.graphics.world;

import java.awt.Color;
import com.jogamp.newt.event.KeyAdapter;
import com.jogamp.newt.event.KeyEvent;
import com.jogamp.newt.event.KeyListener;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import com.jogamp.newt.event.KeyListener;
import com.jogamp.opengl.GL2;
import com.jogamp.opengl.GL3;

import unsw.graphics.Application3D;
import unsw.graphics.CoordFrame3D;
import unsw.graphics.Matrix4;
import unsw.graphics.Shader;
import unsw.graphics.Texture;
import unsw.graphics.geometry.Point2D;
import unsw.graphics.geometry.Point3D;

/**
 * COMMENT: Comment Game
 *
 * @author malcolmr
 */
public class World extends Application3D implements KeyListener{

    private Terrain terrain;
    private Camera camera;
    private Avatar avatar;
    
    //night time mode and shifting time
    private boolean night = false;
    private boolean movingSun = false;
    private long starttime = System.currentTimeMillis();

	private Point2D myMousePoint = null;
	private static final int ROTATION_SCALE = 1;
	private float rotateX = 0;
	private float rotateY = 0;
	private Point3D sunPos;
	private Point3D initSunPos;
	private float sunAngle;
	
	
	//Lighting property that works for day time and night time
	private Color ambientIntensity = new Color(0.4f, 0.4f, 0.4f);
	private Color diffuseCoeff = new Color(0.7f, 0.7f, 0.7f);
	private Color specularCoeff = new Color(0.3f, 0.3f, 0.3f);

    public World(Terrain terrain) {
    	super("Assignment 2", 800, 800);
        this.terrain = terrain;
		this.avatar = new Avatar(new Point3D(1, (float)terrain.getGridAltitude(1, 1), 1), 0, 0, 0);
        this.camera = new Camera(new Point3D(0f, 0f, 0f), terrain);
        this.night = false;
    }

    /**
     * Load a level file and display it.
     *
     * @param args - The first argument is a level file in JSON format
     * @throws FileNotFoundException
     */
    public static void main(String[] args) throws FileNotFoundException {
        Terrain terrain = LevelIO.load(new File(args[0]));//"/Users/yingzhizhou/Desktop/COMP3421-3D/UNSWgraph-0.10/res/worlds/test1.json"));//"/Users/yingzhizhou/Desktop/COMP3421-3D/UNSWgraph-0.10/res/worlds/test4.json"));
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
		//Shader.setViewMatrix(gl, frame.getMatrix());
		// reset the position of avatar, also the rotation
		avatar.setPosition(camera.getNewPos());
		avatar.increaseRotation(0, camera.getRotate(),0);
		//System.out.println("new x:" + avatar.getPosition().getX() + ";" + "new y: " + avatar.getPosition().getY() + "; new z" + avatar.getPosition().getZ());
//		Shader.setViewMatrix(gl, camera.getMatrix());

		//set the lighting property
		Shader.setPoint3D(gl, "lightPos", sunPos);
		Shader.setColor(gl, "ambientIntensity", this.ambientIntensity);
		Shader.setColor(gl, "lightIntensity", Color.WHITE);

		
		//material property
		Shader.setColor(gl, "ambientCoeff", Color.WHITE);
		Shader.setColor(gl, "diffuseCoeff", this.diffuseCoeff);
		Shader.setColor(gl, "specularCoeff", this.specularCoeff);
		Shader.setFloat(gl, "phongExp", 16f);

		if(movingSun) {
			long elapsedTime = System.currentTimeMillis() - this.starttime;
			float timeInDay = ((float)elapsedTime % 15000f)/15000f;
			this.sunAngle = (float) Math.toRadians(360 * timeInDay);
			float updateX = ((float) Math.cos((double) this.sunAngle)*2f);
			float updateY = ((float) Math.sin((double) this.sunAngle)*2f);
			
			this.sunPos = new Point3D(updateX, updateY, sunPos.getZ());
 		} else if(night) {
			Shader.setInt(gl, "torch", 1);
			Shader.setPoint3D(gl, "torchDirection", new Point3D(0,0, -1));
			Shader.setColor(gl, "torchDiffuseCoeff", new Color(0.8f, 0.8f, 0.8f));
			Shader.setColor(gl, "torchSpecularCoeff", new Color(0.3f, 0.3f, 0.3f));
			Shader.setPoint3D(gl, "cameraPos", camera.getPosition());
			Shader.setFloat(gl, "cutoff", 10f);
			Shader.setFloat(gl, "attentExp", 128f);
			System.out.println("MAGIC CASTLE");
		} else {
			Shader.setInt(gl, "torch", 0);
		}

		
		terrain.draw(gl, frame);
		avatar.display(gl, avatarFame);
	}

	@Override
	public void destroy(GL3 gl) {
		super.destroy(gl);
	}

	@Override
	public void init(GL3 gl) {
		super.init(gl);
		getWindow().addKeyListener(this.camera);
		getWindow().addKeyListener(this);
		
		Shader shader = new Shader(gl, "shaders/vertex_tex_phong.glsl",
				"shaders/fragment_tex_phong.glsl");
		shader.use(gl);
		
		// Set the lighting properties
		//Shader.setPoint3D(gl, "lightPos", sunPos);
		//Shader.setColor(gl, "sunlightIntensity", Color.WHITE);
		//get the sun initial position
		this.initSunPos = this.terrain.getSunlight().asPoint3D();
		this.sunPos = this.terrain.getSunlight().asPoint3D();

		// Set the material properties
		

		terrain.init(gl);
		avatar.init(gl);
		gl.glEnable(GL2.GL_POLYGON_OFFSET_FILL);
		gl.glEnable(GL2.GL_POLYGON_OFFSET_LINE);
		gl.glEnable(GL2.GL_POLYGON_OFFSET_POINT);

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
	@Override
	public void keyPressed(KeyEvent e) {
		int keyCode = e.getKeyCode();
		if(keyCode == KeyEvent.VK_N) {
			//System.out.println("NIGHT TIME LOSER");
			//make night time boolean the opposite
			this.night = this.night == true ? false : true;
			if(night) {
				this.specularCoeff = new Color(0.1f, 0.1f, 0.1f);
				this.diffuseCoeff = new Color(0.1f, 0.1f, 0.1f);
				this.ambientIntensity = new Color(0.2f, 0.2f, 0.2f);
				System.out.println("WE ARE IN NIGHT MODE");
			} else {
				this.specularCoeff = new Color(0.3f, 0.3f, 0.3f);
				this.diffuseCoeff = new Color(0.6f, 0.6f, 0.6f);
				this.ambientIntensity = new Color(0.4f, 0.4f, 0.4f);
				System.out.println("WE ARE IN DAY MODE");
			}
		} else if (keyCode == KeyEvent.VK_D) {
			//praise the sun - ds3
			this.movingSun = this.movingSun == true ? false : true;
			this.sunPos = this.initSunPos;
			this.starttime = System.currentTimeMillis();
		}
		
	}

	@Override
	public void keyReleased(KeyEvent e) {
		// TODO Auto-generated method stub
		
	}

}
