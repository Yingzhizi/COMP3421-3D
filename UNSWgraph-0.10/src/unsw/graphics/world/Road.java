package unsw.graphics.world;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.jogamp.opengl.GL3;

import unsw.graphics.CoordFrame3D;
import unsw.graphics.Matrix4;
import unsw.graphics.Texture;
import unsw.graphics.Vector3;
import unsw.graphics.geometry.Point2D;
import unsw.graphics.geometry.Point3D;
import unsw.graphics.geometry.TriangleMesh;

/**
 * COMMENT: Comment Road 
 *
 * @author malcolmr
 */
public class Road {

	//road needs to be drawn on a terrain
	private Terrain terrain;
	private TriangleMesh roadMesh;
    private List<Point2D> points;
    private float width;
    private Texture texture;
    
    /**
     * Create a new road with the specified spine 
     *
     * @param width
     * @param spine
     */
    public Road(float width, List<Point2D> spine, Terrain terrain) {
        this.width = width;
        this.points = spine;
        this.terrain = terrain;
    }

    /**
     * The width of the road.
     * 
     * @return
     */
    public double width() {
        return width;
    }
    
    /**
     * Get the number of segments in the curve
     * 
     * @return
     */
    public int size() {
        return points.size() / 3;
    }

    /**
     * Get the specified control point.
     * 
     * @param i
     * @return
     */
    public Point2D controlPoint(int i) {
        return points.get(i);
    }
    
    /**
     * Get a point on the spine. The parameter t may vary from 0 to size().
     * Points on the kth segment take have parameters in the range (k, k+1).
     * 
     * @param t
     * @return
     */
    public Point2D point(float t) {
        int i = (int)Math.floor(t);
        t = t - i;
        
        i *= 3;
        
        Point2D p0 = points.get(i++);
        Point2D p1 = points.get(i++);
        Point2D p2 = points.get(i++);
        Point2D p3 = points.get(i++);
        

        float x = b(0, t) * p0.getX() + b(1, t) * p1.getX() + b(2, t) * p2.getX() + b(3, t) * p3.getX();
        float y = b(0, t) * p0.getY() + b(1, t) * p1.getY() + b(2, t) * p2.getY() + b(3, t) * p3.getY();        
        
        return new Point2D(x, y);
    }
    
    /**
     * Calculate the Bezier coefficients
     * 
     * @param i
     * @param t
     * @return
     */
    private float b(int i, float t) {
        
        switch(i) {
        
        case 0:
            return (1-t) * (1-t) * (1-t);

        case 1:
            return 3 * (1-t) * (1-t) * t;
            
        case 2:
            return 3 * (1-t) * t * t;

        case 3:
            return t * t * t;
        }
        
        // this should never happen
        throw new IllegalArgumentException("" + i);
    }
    
    public void init(GL3 gl, Texture texture) {
    	List<Point2D> texCoords = new ArrayList<Point2D>();
    	List<Integer> indices = new ArrayList<>();
    	List<Point3D> vertices = new ArrayList<>();
    	List<Vector3> normals = new ArrayList<>();
    	
    	//necessary for calculating left and right side of the road later
    	Point3D l = new Point3D(-1 * this.width/2, 0, 0);
    	Point3D r = new Point3D(this.width/2, 0, 0);
    	
    	float roadAlt = getRoadAltitude();
    	
    	//create the vertices
    	for(float slice = 0f; slice < this.size(); slice++) {
    		//get where the point is on the road
    		//thanks rob for function to determine the point
    		Point2D firstPt = point(slice);
    		//get magic number for the second point
    		Point2D secondPt = point(slice + 0.004f);
    		//calculate the normal at the secondPoint
    		Vector3 norm = new Vector3(secondPt.getX() - firstPt.getX(), 0, secondPt.getY() - firstPt.getY()).normalize();
    		Vector3 i = new Vector3(norm.getZ(), 0, -1 * norm.getX());
    		Vector3 j = norm.cross(i);
    		Vector3 phi = new Vector3(firstPt.getX(), roadAlt, firstPt.getY());
    		
    		Matrix4 frenetFrame = calcFrenetFrame(i, j , norm, phi);
    		
    		Point3D leftSide = frenetFrame.multiply(l.asHomogenous()).asPoint3D();
    		Point3D rightSide = frenetFrame.multiply(r.asHomogenous()).asPoint3D();
    		
    		//add to vetices
    		vertices.add(leftSide);
    		vertices.add(rightSide);
    		//add to texture coord
    		texCoords.add(new Point2D(leftSide.getX(), leftSide.getZ()));
    		texCoords.add(new Point2D(rightSide.getX(), rightSide.getZ()));
    		
    		if(slice != 0) {
    			int in1 = vertices.size() - 4;
    			int in2 = vertices.size() - 3;
    			int in3 = vertices.size() - 2;
    			int in4 = vertices.size() - 1;
    			
    			//add the index now
    			indices.addAll(Arrays.asList(in3, in4, in2));
    			indices.addAll(Arrays.asList(in1, in3, in2));
    			normals.addAll(Arrays.asList(new Vector3(0,1,0),new Vector3(0,1,0),new Vector3(0,1,0)));
    			normals.addAll(Arrays.asList(new Vector3(0,1,0), new Vector3(0,1,0), new Vector3(0,1,0)));
    		}
    	}
    	roadMesh = new TriangleMesh(vertices, normals, indices, texCoords);
    	roadMesh.init(gl);
    	this.texture = texture;
    }
    
    //use to calculate the values of the frenet frame to draw the bezier curve
     private Matrix4 calcFrenetFrame(Vector3 i, Vector3 j, Vector3 norm, Vector3 phi) {
    	 float[] frenetVal = new float[] {i.getX(), i.getY(), i.getZ(), 0, j.getX(), j.getY(), j.getZ()
    			 ,0 , norm.getX(), norm.getY(), norm.getZ(), 0, phi.getX(), phi.getY(), phi.getZ(), 1};
    	 return new Matrix4(frenetVal);
     }
    
    //used to calculate road altitude
    private float getRoadAltitude() {
    	float x = points.get(0).getX();
    	float y = points.get(0).getY();
    	return terrain.altitude(x, y);
    }

    
    public void draw(GL3 gl, CoordFrame3D frame) {
    	roadMesh.draw(gl, frame);
    }


}
