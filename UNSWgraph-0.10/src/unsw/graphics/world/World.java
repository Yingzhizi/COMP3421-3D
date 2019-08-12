package unsw.graphics.world;

import java.awt.*;
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


/**
 * COMMENT: Comment Game
 *
 * @author Yingzhi Zhou / Francis Dong
 */
public class World extends Application3D implements KeyListener{

    private Terrain terrain;
    private Camera camera;
    private Avatar avatar;
    
    //night time mode and shifting time
    private boolean night = false;
    private boolean movingSun = false;
    private long starttime = System.currentTimeMillis();
	private Pond pond;
	private TriangleMesh ground;
	private Texture groundTexture;

	private Point3D sunPos;
	private Point3D initSunPos;
	private float sunAngle;
	static float totaltimepassed = 0;
	
	
	//Lighting property that works for day time and night time
	private Color ambientIntensity = new Color(0.4f, 0.4f, 0.4f);
	private Color diffuseCoeff = new Color(0.7f, 0.7f, 0.7f);
	private Color specularCoeff = new Color(0.3f, 0.3f, 0.3f);

    public World(Terrain terrain) {
    	super("Assignment 2", 700, 700);
        this.terrain = terrain;
		this.avatar = new Avatar(new Point3D(1, (float)terrain.getGridAltitude(1, 1), 1), 0, 0, 0);
        this.camera = new Camera(new Point3D(0f, 0f, 0f), terrain);
		pond = new Pond(0, 0, 0, 2, 2);
        this.night = false;
    }

    /**
     * Load a level file and display it.
     * @param args - The first argument is a level file in JSON format
     * @throws FileNotFoundException
     */
    public static void main(String[] args) throws FileNotFoundException {
        Terrain terrain = LevelIO.load(new File(args[0]));//"/Users/yingzhizhou/Desktop/COMP3421-3D/UNSWgraph-0.10/res/worlds/test1.json"));
        World world = new World(terrain);
        world.start();
    }

	/**
	 * display my world
	 * @param gl
	 */
	@Override
	public void display(GL3 gl) {
		super.display(gl);

		//reset the frame, change with camera, avatar has different frame as terrain
		CoordFrame3D frame = camera.resetFrame();
		CoordFrame3D avatarFame = camera.resetAvatarFrame();

		// reset the position of avatar, also the rotation
		avatar.setPosition(camera.getNewPos());
		avatar.increaseRotation(0, camera.getRotate(),0);

		//set the lighting property
		Shader.setPoint3D(gl, "lightPos", sunPos);
		Shader.setColor(gl, "ambientIntensity", this.ambientIntensity);
		Shader.setColor(gl, "lightIntensity", Color.WHITE);
		//material property
		Shader.setColor(gl, "ambientCoeff", Color.WHITE);
		Shader.setColor(gl, "diffuseCoeff", this.diffuseCoeff);
		Shader.setColor(gl, "specularCoeff", this.specularCoeff);
		Shader.setFloat(gl, "phongExp", 16f);


		//turn the torch at night
		if(night) {
 			//set the torch property and set sky to gray
 			Vector4 td = new Vector4(0,0,-1,0);
			Shader.setInt(gl, "torch", 1);
			Shader.setPoint3D(gl, "torchDirection", td.asPoint3D());
			Shader.setColor(gl, "torchDiffuseCoeff", new Color(0.8f, 0.8f, 0.8f));
			Shader.setColor(gl, "torchSpecularCoeff", new Color(0.3f, 0.3f, 0.3f));
			Shader.setPoint3D(gl, "cameraPos", new Point3D(0, 0, -1).translate(0, -0.5f, 0));
			Shader.setFloat(gl, "cutoff", 10f);
			Shader.setFloat(gl, "attentExp", 128f);
			Shader.setColor(gl, "skyColor", Color.GRAY);
			this.setBackground(Color.GRAY);
		//just day time
		} else {
			Shader.setInt(gl, "torch", 0);
			Shader.setColor(gl, "skyColor", Color.WHITE);
			this.setBackground(Color.WHITE);
		}
		
		//move the sun
		if(movingSun) {
			long elapsedTime = System.currentTimeMillis() - this.starttime;
			float timeInDay = ((float)elapsedTime % 15000f)/10000f;
			this.sunAngle = (float) Math.toRadians(360 * timeInDay);
			float updateX = ((float) Math.cos((double) this.sunAngle)*1.2f);
			float updateY = ((float) Math.sin((double) this.sunAngle)*1.2f);
			this.sunPos = new Point3D(updateX, updateY, sunPos.getZ());
			//update the color
			float setSunColor = (float)Math.abs(Math.cos((Math.toRadians(90)+ sunAngle)/2));
			System.out.println(setSunColor);
			updateSkyCol(setSunColor);
 		}

		// draw terrain and avatar
		terrain.draw(gl, frame);
		avatar.display(gl, avatarFame);

		// switch textures for pond, different frame using different texture
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

		// draw pond for the world
		pond.draw(gl, frame.translate(0, -0.095f, 0));

		// draw ground for my world
		drawGround(gl, frame);

	}
	
	//update the color of the sky
	private void updateSkyCol(float sunAngle) {
		this.setBackground(new Color(
			//get the red
			(int)(126 - (sunAngle * 90)),
			//get the green
			(int)(192 - (sunAngle * 100)),
			//get the blue
			(int)(238 - (sunAngle * 120))
		));
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
		Shader shader = new Shader(gl, "shaders/vertex_tex_phong.glsl",
				"shaders/fragment_tex_phong.glsl");
		shader.use(gl);
		
		// Set the lighting properties
		// set the sun initial position
		this.initSunPos = this.terrain.getSunlight().asPoint3D();
		this.sunPos = this.terrain.getSunlight().asPoint3D();

		// start initiating of terrain, avatar, pond and ground
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

	/**
	 * function to draw triangle mesh for ground and initialise it
	 * also add texture to it
	 * @param gl
	 */
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

	/**
	 * draw ground for my world
	 * @param gl
	 * @param frame
	 */
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
			// make night time boolean the opposite
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
