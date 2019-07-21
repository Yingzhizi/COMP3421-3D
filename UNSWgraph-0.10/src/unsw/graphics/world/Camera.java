package unsw.graphics.world;

import com.jogamp.newt.event.KeyEvent;
import com.jogamp.newt.event.KeyListener;
import com.jogamp.opengl.GL3;

import unsw.graphics.CoordFrame3D;
import unsw.graphics.Shader;
import unsw.graphics.Vector3;
import unsw.graphics.geometry.Point3D;
import unsw.graphics.Matrix4;

import unsw.graphics.geometry.Point2D;


public class Camera implements KeyListener {
    // position of the camera
    private Point3D position;

    // the speed of the camera run
    private static final float runSpeed = 0.1f;
    private static final float turnSpeed = 0.5f;

    private float currentSpeend = 0;
    private float currentTurnSpeed = 0;

    // pass into the terrain we need to load
    private Terrain myTerrain;

    private float rotateY;


    public Camera(Point3D position, Terrain terrain, float rotateY) {
        this.position = position;
        this.myTerrain = terrain;
        this.rotateY = rotateY;
    }

    public Point3D getPosition() {
        return position;
    }


    @Override
    public void keyPressed(KeyEvent e) {
        int keyCode = e.getKeyCode();
        if(keyCode == KeyEvent.VK_UP) {
            this.currentSpeend = runSpeed;
            System.out.println("I press up");
        } else if(keyCode == KeyEvent.VK_DOWN) {
            this.currentSpeend = -runSpeed;
            System.out.println("I press down");
        } else {
            this.currentSpeend = 0;
        }

        if(keyCode == KeyEvent.VK_LEFT) {
            this.currentTurnSpeed = turnSpeed;
            System.out.println("I press left");
        } else if(keyCode == KeyEvent.VK_RIGHT) {
            this.currentTurnSpeed = -turnSpeed;
            System.out.println("I press right");
        } else {
            this.currentTurnSpeed = 0;
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {

    }

    // camera will move with key press
    public void move(KeyEvent e) {
        keyPressed(e);
        // for testing, cannot do continuous
        this.rotateY = this.rotateY + this.currentTurnSpeed * 1;
        float distance = this.currentSpeend * 1;
        float x = distance * (float)Math.sin(Math.toRadians(this.rotateY));
        float z = distance * (float)Math.cos(Math.toRadians(this.rotateY));

        // get y position
        float y = myTerrain.altitude(x, z);

        // calculate the increment/decrement in y axis
        y -= position.getY();

        // update the camera position
        this.position = this.position.translate(x, y, z);
    }

}
