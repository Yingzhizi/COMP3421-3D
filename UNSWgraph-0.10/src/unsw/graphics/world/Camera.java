package unsw.graphics.world;

import com.jogamp.newt.event.KeyEvent;
import com.jogamp.newt.event.KeyListener;

import unsw.graphics.CoordFrame3D;
import unsw.graphics.geometry.Point3D;


public class Camera implements KeyListener {

    // position of the camera
    private Point3D position;

    // the speed of the camera run forward and backward
    private static final float runSpeed = 0.1f;

    // the speed of the camera turning around
    private static final float turnSpeed = 2f;

    // set the initial direction it facing as rotate
    private float rotate = -150;
    private boolean thirdPerson = false;

    // pass into the terrain we need to load
    private Terrain myTerrain;

    //coordinates of the camera
    private float rotateX = 0;
    private float rotateY = 0;
    private float rotateZ = 0;

    /**
     * initialise the camera by given position and pass terrain into it
     * @param position
     * @param terrain
     */
    public Camera(Point3D position, Terrain terrain) {
        this.position = position;
        this.myTerrain = terrain;
    }

    /**
     * get the position of the camera
     * @return
     */
    public Point3D getPosition() {
        return position;
    }

    /**
     * movement of the camera
     * @param e the key we press
     */
    @Override
    public void keyPressed(KeyEvent e) {
        int keyCode = e.getKeyCode();
        if(keyCode == KeyEvent.VK_UP) {
        	// moves forwards
            // update the position of the camera
            rotateX -= runSpeed * Math.sin(Math.toRadians(rotate));
            rotateZ -= runSpeed * Math.cos(Math.toRadians(rotate));
            this.position = new Point3D(rotateX, myTerrain.altitude(rotateX, rotateZ), rotateZ);
        } else if(keyCode == KeyEvent.VK_DOWN) {
        	// moves backwards
            // update the position of the camera
        	rotateX += runSpeed * Math.sin(Math.toRadians(rotate));
        	rotateZ += runSpeed * Math.cos(Math.toRadians(rotate));
            this.position = new Point3D(rotateX, myTerrain.altitude(rotateX, rotateZ), rotateZ);
        }
        // when turn left or right, camera's position stay the same
        // but need to update the position camera facing
        else if(keyCode == KeyEvent.VK_LEFT) {
        	//turn left
        	rotate += turnSpeed;
        } else if(keyCode == KeyEvent.VK_RIGHT) {
        	//turn right
        	rotate -= turnSpeed;
        }

        switchView(e);
    }

    @Override
    public void keyReleased(KeyEvent e) {

    }

    /**
     * reset the frame for camera
     * @return updated frame
     */
    public CoordFrame3D resetFrame() {
    	// calculate where Y axis of camera should be
        CoordFrame3D frame = CoordFrame3D.identity();
        if (thirdPerson == false) {
            // place the camera position to avatar's position
            rotateY = myTerrain.altitude(rotateX, rotateZ + 0.5f) + 0.7f;
            frame = frame.translate(0f,-0.3f,-0.8f).rotateY(-rotate).translate(-rotateX, -rotateY, -rotateZ);
        } else {
            rotateY = myTerrain.altitude(rotateX, rotateZ) + 0.5f;
            frame = frame.translate(0f,-0.3f,-1.5f).rotateY(-rotate).translate(-rotateX, -rotateY, -rotateZ);
        }
    	return frame;
    }

    /**
     * reset the frame for avatar
     * @return
     */
    public CoordFrame3D resetAvatarFrame() {
        // calculate where Y axis of camera should be
        rotateY = myTerrain.altitude(rotateX, rotateZ) + 0.8f;
        // reset the position of avatar
        // because terrain has rotate, so avatar don't need to actually rotate
        CoordFrame3D frame = CoordFrame3D.identity();
        if (thirdPerson == false) {
            frame = frame.translate(0f,-0.3f,-0.1f).translate(-rotateX, -rotateY, -rotateZ);
        } else {
            frame = frame.translate(0f,-0.3f,-1.5f).translate(-rotateX, -rotateY, -rotateZ);
        }
        return frame;
    }


    /**
     * get the position of updated position of avatar
     * @return
     */
    public Point3D getNewPos() {
        return new Point3D(rotateX, rotateY-0.5f, rotateZ);
    }

    /**
     * get the direction of camera
     * @return
     */
    public float getRotate() {
        return this.rotate;
    }

    /**
     * switch between first player mode and third player mode
     * @param e
     */
    private void switchView(KeyEvent e) {
        int keyCode = e.getKeyCode();
        if (keyCode == KeyEvent.VK_C) {
            if (this.thirdPerson == false) {
                this.thirdPerson = true;
            } else {
                this.thirdPerson = false;
            }
        }
    }
}
