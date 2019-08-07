package unsw.graphics.world;

import com.jogamp.newt.event.KeyEvent;
import com.jogamp.newt.event.KeyListener;

import unsw.graphics.CoordFrame3D;
import unsw.graphics.geometry.Point3D;


public class Camera implements KeyListener {
    // position of the camera
    private Point3D position;

    // the speed of the camera run
    private static final float runSpeed = 0.1f;
    private static final float turnSpeed = 1.5f;
    private float rotate = -75;

    private float currentSpeend = 0;
    private float currentTurnSpeed = 0;

    // pass into the terrain we need to load
    private Terrain myTerrain;

    //coordinates of the camera
    private float rotateX = 0;
    private float rotateY = 0;
    private float rotateZ = 0;

    public Camera(Point3D position, Terrain terrain) {
        this.position = position;
        this.myTerrain = terrain;
    }

    public Point3D getPosition() {
        return position;
    }


    @Override
    public void keyPressed(KeyEvent e) {
        int keyCode = e.getKeyCode();
        //should of checked to see if we were in terrain but ran out of time
        //will implement in final milestone
        if(keyCode == KeyEvent.VK_UP) {
        	//moves forwards
            //change in x and z

                rotateX -= runSpeed * Math.sin(Math.toRadians(rotate));
                rotateZ -= runSpeed * Math.cos(Math.toRadians(rotate));
//            System.out.println("oldY is "+ oldY + "! newY is " + newY);
            System.out.println("I press up");
        } else if(keyCode == KeyEvent.VK_DOWN) {
            //this.currentSpeend = runSpeed;
        	//moves backwards
        	rotateX += runSpeed * Math.sin(Math.toRadians(rotate));
        	rotateZ += runSpeed * Math.cos(Math.toRadians(rotate));
            System.out.println("I press down");
        }

        else if(keyCode == KeyEvent.VK_LEFT) {
        	//moves left
        	rotate += turnSpeed;
            System.out.println("I press left");
        } else if(keyCode == KeyEvent.VK_RIGHT) {
        	//moves right
        	rotate -= turnSpeed;
            System.out.println("I press right");
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {

    }

    public void move() {
        // for testing, cannot do continuous
        this.rotateY = this.rotateY + this.currentTurnSpeed * 1;
        float distance = this.currentSpeend * 1;
        float changeInX = distance * (float)Math.sin(Math.toRadians(this.rotateY));
        float changeInZ = distance * (float)-Math.cos(Math.toRadians(this.rotateY));

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
//        CoordFrame3D frame = CoordFrame3D.identity().translate(0, 0, -2.4f).scale(0.3f, 0.3f, 0.3f);
//        frame = frame.rotateY(-this.rotateY);
//        frame = frame.translate(-1 * this.position.getX(), -1 * this.position.getY(), -1 * this.getPosition().getZ());
    	//calculate where Y axis of camera should be
    	rotateY = myTerrain.altitude(rotateX, rotateZ) + 0.5f;
    	System.out.println("Rotate  Y: " + rotateY + "! Rotate Z: " + rotateZ + "! rotateX: " + rotateX);
    	//place camera
    	//CoordFrame3D frame = CoordFrame3D.identity().translate(0,0,-3.5f).rotateY(-rotate).translate(-rotateX, -rotateY, -rotateZ);
        CoordFrame3D frame = CoordFrame3D.identity().translate(0,0,-1.1f).rotateY(-rotate).translate(-rotateX, -rotateY, -rotateZ);
    	
    	return frame;
    }

}
