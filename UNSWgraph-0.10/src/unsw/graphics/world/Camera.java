package unsw.graphics.world;

import com.jogamp.newt.event.KeyEvent;
import com.jogamp.newt.event.KeyListener;
import com.jogamp.opengl.GL3;

import unsw.graphics.CoordFrame3D;
import unsw.graphics.geometry.Point3D;

import java.awt.*;


public class Camera implements KeyListener {
    // position of the camera
    private Point3D position;

    // the speed of the camera run
    private static final float runSpeed = 0.1f;
    private static final float turnSpeed = 1.5f;
    private float pitch;
    private float yaw;
    private float roll;
    private float rotate = -150;
    private float distanceFromAvatar = 30;
    private float angleAroundAvatar = 0;
    private boolean thirdPerson = false;
    private Avatar avatar;

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

    public float getPitch() {
        return this.pitch;
    }

    public float getYaw() {
        return this.yaw;
    }

    public float getRoll() {
        return this.roll;
    }

    @Override
    public void keyPressed(KeyEvent e) {
        int keyCode = e.getKeyCode();
        //should of checked to see if we were in terrain but ran out of time
        //will implement in final milestone
        if(keyCode == KeyEvent.VK_UP) {
        	//moves forwards
            rotateX -= runSpeed * Math.sin(Math.toRadians(rotate));
            rotateZ -= runSpeed * Math.cos(Math.toRadians(rotate));
            this.position = new Point3D(rotateX, myTerrain.altitude(rotateX, rotateZ), rotateZ);
            //System.out.println("I press up");
        } else if(keyCode == KeyEvent.VK_DOWN) {
        	//moves backwards
        	rotateX += runSpeed * Math.sin(Math.toRadians(rotate));
        	rotateZ += runSpeed * Math.cos(Math.toRadians(rotate));
            this.position = new Point3D(rotateX, myTerrain.altitude(rotateX, rotateZ), rotateZ);
            //System.out.println("I press down");
        }

        // when turn left or right, camera's position stay the same
        else if(keyCode == KeyEvent.VK_LEFT) {
        	//moves left
        	rotate += turnSpeed;
//            System.out.println("I press left");
        } else if(keyCode == KeyEvent.VK_RIGHT) {
        	//moves right
        	rotate -= turnSpeed;
//            System.out.println("I press right");
        }

        switchView(e);
    }

    @Override
    public void keyReleased(KeyEvent e) {

    }


    public CoordFrame3D resetFrame() {
        // update new cameraPosition
    	// calculate where Y axis of camera should be
    	// reset the position of avatar, also the rotation
    	// place camera
        CoordFrame3D frame = CoordFrame3D.identity();
        if (thirdPerson == false) {
            // place the camera position to avatar's position
            rotateY = myTerrain.altitude(rotateX, rotateZ + 0.5f) + 0.7f;
            frame = frame.translate(0f,-0.3f,-0.8f).rotateY(-rotate).translate(-rotateX, -rotateY, -rotateZ);
        } else {
            rotateY = myTerrain.altitude(rotateX, rotateZ) + 0.5f;
            frame = frame.translate(0f,-0.3f,-1.5f).rotateY(-rotate).translate(-rotateX, -rotateY, -rotateZ);
        }
    	//System.out.println("Rotate  Y: " + rotateY + "! Rotate Z: " + rotateZ + "! rotateX: " + rotateX);
    	return frame;
    }

    public CoordFrame3D resetAvatarFrame() {
        // update new cameraPosition
        //calculate where Y axis of camera should be
        rotateY = myTerrain.altitude(rotateX, rotateZ) + 0.8f;

        // reset the position of avatar, also the rotation
        CoordFrame3D frame = CoordFrame3D.identity();
        if (thirdPerson == false) {
            frame = frame.translate(0f,-0.3f,-0.1f).translate(-rotateX, -rotateY, -rotateZ);
        } else {
            frame = frame.translate(0f,-0.3f,-1.5f).translate(-rotateX, -rotateY, -rotateZ);
        }
        return frame;
    }


    public Point3D getNewPos() {
        return new Point3D(rotateX, rotateY-0.5f, rotateZ);
    }

    public float getRotate() {
        return this.rotate;
    }

    private void calculateZoom(KeyEvent e) {
        int keyCode = e.getKeyCode();
        if (keyCode == KeyEvent.VK_Z) {
            float zoomLevel = 0.5f;
            distanceFromAvatar -= zoomLevel;
        } else if (keyCode == KeyEvent.VK_X) {
            float zoomLevel = 0.5f;
            distanceFromAvatar += zoomLevel;
        }

    }

    private void calculatePitch(KeyEvent e) {
        int keyCode = e.getKeyCode();
        float pitchChange = 0;
        if (keyCode == KeyEvent.VK_S) {
            pitchChange = -0.5f;
        } else if (keyCode == KeyEvent.VK_W) {
            pitchChange = 0.5f;
        }
        pitch += pitchChange;

    }

    private void calculateAngleAround(KeyEvent e) {
        int keyCode = e.getKeyCode();
        float angle = 0;
        if (keyCode == KeyEvent.VK_A) {
            angle = 0.7f;
        } else if (keyCode == KeyEvent.VK_D) {
            angle = -0.7f;
        }
        angleAroundAvatar += angle;
    }

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

    private float calculatingHorizontalDistance() {
        return distanceFromAvatar * (float)Math.cos(Math.toRadians(pitch));
    }

    private float calculateVerticalDistance() {
        return distanceFromAvatar * (float)Math.sin(Math.toRadians(pitch));
    }

//    private void calculateCameraPosition(float horiDistance, float verticeDistance) {
//        float theta = avatar.getRotateY() + angleAroundAvatar;
//        float offsetX = horiDistance * (float)Math.sin(Math.toRadians(theta));
//        float offsetZ = horiDistance * (float)Math.cos(Math.toRadians(theta));
//        float newX = avatar.getPosition().getX() - offsetX;
//        float newY = avatar.getPosition().getY() + verticeDistance;
//        float newZ = avatar.getPosition().getZ() - offsetZ;
//
//    }
}
