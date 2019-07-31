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


    public Camera(Point3D position, Terrain terrain) {
        this.position = position;
        this.myTerrain = terrain;
        this.rotateY = 0;
    }

    public Point3D getPosition() {
        return position;
    }


    @Override
    public void keyPressed(KeyEvent e) {
        int keyCode = e.getKeyCode();
        if(keyCode == KeyEvent.VK_UP) {
            this.currentSpeend = -runSpeed;
            this.currentTurnSpeed = 0;
            //move();
            System.out.println("I press up");
        }
        if(keyCode == KeyEvent.VK_DOWN) {
            this.currentSpeend = runSpeed;
            this.currentTurnSpeed = 0;
            //move();
            System.out.println("I press down");
        }

        if(keyCode == KeyEvent.VK_LEFT) {
            this.currentTurnSpeed = turnSpeed;
            this.currentSpeend = 0;
            //move();
            System.out.println("I press left");
        }
        if(keyCode == KeyEvent.VK_RIGHT) {
            this.currentTurnSpeed = -turnSpeed;
            this.currentSpeend = 0;
            //move();
            System.out.println("I press right");
        }

        move();
    }

    @Override
    public void keyReleased(KeyEvent e) {

    }
//    public void moveForward() {
//        this.currentSpeend = -runSpeed;
//        float distance = this.currentSpeend * 1;
//        float x = -1 * distance * (float)Math.sin(-1 * Math.toRadians(this.rotateY));
//        float z = distance * (float)Math.cos(-1 * Math.toRadians(this.rotateY));
//        // get y position
//        float y = myTerrain.altitude(x, z);
//
//        // calculate the increment/decrement in y axis
//        y = y - position.getY() + 0.5f;
//
//        // update the camera position
//        this.position = this.position.translate(x, y, z);
//
//        // test
//        System.out.println("x: " + this.position.getX() + "y: " + this.position.getY() + "z: " + this.position.getZ());
//    }
//
//    public void moveBackward() {
//        this.currentSpeend = runSpeed;
//    }
    // camera will move with key press
    public void move() {
        // for testing, cannot do continuous
        this.rotateY = this.rotateY + this.currentTurnSpeed * 1;
        float distance = this.currentSpeend * 1;
        float changeInX = distance * (float)Math.sin(Math.toRadians(this.rotateY));
        float changeInZ = distance * (float)Math.cos(Math.toRadians(this.rotateY));

        // get y position
        float oldY = myTerrain.altitude(this.position.getX(), this.position.getZ());
        float newY = myTerrain.altitude(this.position.getX() + changeInX, this.position.getZ()+changeInZ);

        // calculate the increment/decrement in y axis
        float changeInY = newY - oldY;

        // update the camera position
        this.position = this.position.translate(changeInX, changeInY, changeInZ);

        // test
        System.out.println("x: " + this.position.getX() + "y: " + this.position.getY() + "z: " + this.position.getZ());


    }

    public CoordFrame3D resetFrame() {
        // update new cameraPosition
        CoordFrame3D frame = CoordFrame3D.identity().translate(0, 0, -2.4f).scale(0.3f, 0.3f, 0.3f);
        frame = frame.rotateY(-this.rotateY);
        frame = frame.translate(-1 * this.position.getX(), -1 * this.position.getY(), -1 * this.getPosition().getZ());

        return frame;
    }

}
