package unsw.graphics.world;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.jogamp.opengl.GL;
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

	private TriangleMesh roadMesh;
    private List<Point2D> points;
    private float width;
    private Texture texture;
    //number of segments we should have
    private int segs = 32;
    
    /**
     * Create a new road with the specified spine 
     *
     * @param width
     * @param spine
     */
    public Road(float width, List<Point2D> spine) {
        this.width = width;
        this.points = spine;
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
     * Calculate the tangent point on the spine
     * 
     * @param t
     * @return
     */
    public Point2D tangent(float t) {
    	int i = (int)Math.floor(t);
        t = t - i;
        
        i *= 3;
        
        Point2D p0 = points.get(i++);
        Point2D p1 = points.get(i++);
        Point2D p2 = points.get(i++);
        Point2D p3 = points.get(i++);
        
        float x = 3 * (b_deriv(0, t) * (p1.getX() - p0.getX()) + b_deriv(1, t) * (p2.getX() - p1.getX()) + b_deriv(2, t) * (p3.getX() - p2.getX()));
        float y = 3 * (b_deriv(0, t) * (p1.getY() - p0.getY()) + b_deriv(1, t) * (p2.getY() - p1.getY()) + b_deriv(2, t) * (p3.getY() - p2.getY()));

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
    
    /**
     * 
     * Bezier derivative coefficient
     * 
     * @param i
     * @param t
     * @return
     */
    private float b_deriv(int i, float t) {
        
        switch(i) {
        
        case 0:
            return (1-t) * (1-t);

        case 1:
            return 2 * (1-t) * t;
            
        case 2:
            return t * t;
        }
        
        // this should never happen
        throw new IllegalArgumentException("" + i);
    }
    

    public void drawRoad(GL3 gl, Terrain terr) {
    	//texture coordinates
    	List<Point2D> texCoords = new ArrayList<Point2D>();
    	List<Point3D> vertices = new ArrayList<>();
    	//creates the triangle in the road
    	List<Integer> indices = new ArrayList<>();
    	List<Vector3> normals = new ArrayList<>();
    	
    	//These are the left and right points of the road
    	Point3D l = new Point3D(-this.width/2, 0, 0);
    	Point3D r = new Point3D(this.width/2, 0, 0);
    	
    	//lets get the altitude of the road
    	float roadAlt = getRoadAltitude(terr);
    	//change in slice
    	float dt = (points.size()/12f)/this.segs;
    	//Sample points along the spine with slice
    	for(float slice = 0f; slice <= size(); slice += dt) {
    		//get where the point is on the road
    		Point2D firstPt = point(slice);
    		//get the tangent
    		Point2D secondPt = tangent(slice);
    		if(secondPt.getX() == 0 && secondPt.getY() == 0) continue;
    		//get the values of the frenet transformation matrix
    		Vector3 norm = new Vector3(secondPt.getX(), 0, secondPt.getY()).normalize();
    		Vector3 i = new Vector3(norm.getZ(), 0, -1 *norm.getX());
    		Vector3 j = norm.cross(i).normalize();
    		Vector3 phi = new Vector3(firstPt.getX(), roadAlt, firstPt.getY());
    		//Create a frenet transformation matrix
    		Matrix4 frenetFrame = calcFrenetFrame(i, j , norm, phi);
    		
    		//Calculate the left and right points of the road
    		Point3D leftSide = frenetFrame.multiply(l.asHomogenous()).asPoint3D();
    		Point3D rightSide = frenetFrame.multiply(r.asHomogenous()).asPoint3D();
    		
    		//add to vertices
    		vertices.add(leftSide);
    		vertices.add(rightSide);
    	
    		//add to texture coord
    		texCoords.add(new Point2D(leftSide.getX(), leftSide.getZ()));
    		texCoords.add(new Point2D(rightSide.getX(), rightSide.getZ()));
    		
    		//make sure we have 4 vertices to create our triangle's
    		//for road
    		if(slice == 0f) continue;
    		//draw the two triangles
    		createRoadTriangle(vertices.size(), indices, normals);
    	}
    	//generate the Triangle mesh to draw road
    	this.roadMesh = new TriangleMesh(vertices, normals, indices, texCoords);
    }
    
    private void createRoadTriangle(int vs, List<Integer> i, List<Vector3> n) {
    	//draw the two triangles with 4 vertices
    	i.addAll(Arrays.asList(vs - 2, vs - 1, vs - 3));
    	n.addAll(Arrays.asList(new Vector3(0, 1, 0), new Vector3(0, 1, 0), new Vector3(0, 1, 0)));
    	i.addAll(Arrays.asList(vs - 4, vs - 2, vs - 3));
    	n.addAll(Arrays.asList(new Vector3(0, 1, 0), new Vector3(0, 1, 0), new Vector3(0, 1, 0)));
    }
    
    //initialise the road
    public void init(GL3 gl, Terrain t) {
    	drawRoad(gl, t);
    	this.roadMesh.init(gl);
    }
    
    //use to calculate the values of the frenet frame to draw the bezier curve
     private Matrix4 calcFrenetFrame(Vector3 i, Vector3 j, Vector3 norm, Vector3 phi) {
    	 float[] frenetVal = new float[] {i.getX(), 0, i.getZ(), 0, j.getX(), j.getY(), j.getZ()
    			 ,0 , norm.getX(), 0, norm.getZ(), 0, phi.getX(), phi.getY(), phi.getZ(), 1f};
    	 return new Matrix4(frenetVal);
     }
    
    //used to calculate road altitude
    private float getRoadAltitude(Terrain t) {
    	float x = points.get(0).getX();
    	float y = points.get(0).getY();
    	return t.altitude(x, y);
    }

    //draw road
    public void draw(GL3 gl, CoordFrame3D frame) {
    	CoordFrame3D rf = frame;
    	gl.glEnable(GL.GL_POLYGON_OFFSET_FILL);
    	gl.glPolygonOffset(-1f, -1f);
        roadMesh.draw(gl, rf);
        gl.glDisable(GL.GL_POLYGON_OFFSET_FILL);
    }
    
}