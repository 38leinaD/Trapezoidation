
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.LinkedList;

import org.lwjgl.BufferUtils;
import org.lwjgl.LWJGLException;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.DisplayMode;
import org.lwjgl.opengl.GL11;
import org.lwjgl.util.glu.GLU;
import org.lwjgl.util.vector.Vector2f;
import org.lwjgl.util.vector.Vector3f;
 
public class Main {
    private FloatBuffer modelViewMatrixFB, projectionMatrixFB;
    private IntBuffer viewportFB;
    private FloatBuffer unprojectFB;
    
    public void start() {
        try {
            Display.setDisplayMode(new DisplayMode(800, 600));
            Display.setTitle("Trapezoidation");
            Display.create();
        } catch (LWJGLException e) {
            e.printStackTrace();
            System.exit(0);
        }
         
        // init OpenGL here
        init();
        while (!Display.isCloseRequested()) {
            update();
        	render();
             
            Display.update();
        }
         
        Display.destroy();
    }
    
    private void init() {
    	modelViewMatrixFB = BufferUtils.createFloatBuffer(16);
    	projectionMatrixFB = BufferUtils.createFloatBuffer(16);
    	viewportFB = BufferUtils.createIntBuffer(16);
    	unprojectFB = BufferUtils.createFloatBuffer(4);
    	
    	loadPolygon();
	}

    private LinkedList<Vector3f> contour;
    private boolean contourClosed = false;
    
    private void loadPolygon() {
    	contour = new LinkedList<>();
    	/*
    	contour.add(new Vector3f(-1.0f, -1.0f, 0.0f));
    	contour.add(new Vector3f(1.0f, -2.0f, 0.0f));
    	contour.add(new Vector3f(1.5f, 0.0f, 0.0f));
    	contour.add(new Vector3f(2.5f, -2.0f, 0.0f));
    	contour.add(new Vector3f(2.0f, 2.0f, 0.0f));
    	contour.add(new Vector3f(1.0f, 3.0f, 0.0f));
    	
    	contourClosed = true;
    */
    	
    	try {
			BufferedReader br = new BufferedReader(new FileReader(DATA_PATH + "contour.txt"));
			while (true) {
				String line = br.readLine();
				if (line == null) break;
				
				if (line.startsWith("contour start")) {
					contour = new LinkedList<>();
				}
				else if (line.startsWith("contour end")) {
					contourClosed = true;
				}
				else {
					String[] tokens = line.split(",");
					Vector3f p = new Vector3f(Float.parseFloat(tokens[0].trim()), Float.parseFloat(tokens[1].trim()), Float.parseFloat(tokens[2].trim()));
					contour.add(p);
				}
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
    }
    
    private static final String DATA_PATH = "./data/";
    
    private void savePolygon() {
    	try {
			PrintWriter pw = new PrintWriter(DATA_PATH + "contour.txt");
			pw.println("contour start");
			for (Vector3f p : contour) {
				pw.println(p.x + ", " + p.y + ", " + p.z);
			}
			pw.println("contour end");
			pw.flush();
    	} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
    }
    
	private Vector2f cursorScreenSpace = new Vector2f();
	private Vector3f cursorWorldSpace = new Vector3f();
    private float aspectRatio;
    
    private boolean snapToGrid = true;
    
    private void update() {
		
    	aspectRatio = (float)Display.getHeight()/Display.getWidth();
    	
    	cursorScreenSpace.x = Mouse.getX();
    	cursorScreenSpace.y = Mouse.getY();
    	
    	unprojectFB.rewind();
    	GLU.gluUnProject(cursorScreenSpace.x, cursorScreenSpace.y, 0.0f, modelViewMatrixFB, projectionMatrixFB, viewportFB, unprojectFB);
    	
    	cursorWorldSpace.x = unprojectFB.get();
    	cursorWorldSpace.y = unprojectFB.get();
    	cursorWorldSpace.z = unprojectFB.get();
    
    	if (snapToGrid) {
    		cursorWorldSpace.x = Math.round(cursorWorldSpace.x);
    		cursorWorldSpace.y = Math.round(cursorWorldSpace.y);
    		cursorWorldSpace.z = Math.round(cursorWorldSpace.z);
    	}
    	
		while (Keyboard.next()) {
			if (Keyboard.getEventKeyState()) {
				if (!Keyboard.isRepeatEvent()) {
					if (Keyboard.getEventKey() == Keyboard.KEY_S) {
						savePolygon();
					}
					else if (Keyboard.getEventKey() == Keyboard.KEY_RETURN) {
						closePolygon();
					}
				}
			}
		}
    	
		while (Mouse.next()) {
			if (Mouse.isButtonDown(0)) {
				contour.add(new Vector3f(cursorWorldSpace));
			}
		}
    }
    
    private void closePolygon() {
    	if (contourClosed) return;
    	contourClosed = true;
	}

	private void render() {
    	
    	GL11.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
    	GL11.glClear(GL11.GL_COLOR_BUFFER_BIT);
    	
    	GL11.glMatrixMode(GL11.GL_PROJECTION);
    	GL11.glLoadIdentity();
    	GL11.glOrtho(-20.0f, 20.0f, -20.0f*aspectRatio, 20.0f*aspectRatio, 0.0f, 1.0f);
    	
    	GL11.glMatrixMode(GL11.GL_MODELVIEW);
    	GL11.glLoadIdentity();
    	
    	projectionMatrixFB.rewind();
    	modelViewMatrixFB.rewind();
    	viewportFB.rewind();
    	GL11.glGetFloat(GL11.GL_PROJECTION_MATRIX, projectionMatrixFB);
    	GL11.glGetFloat(GL11.GL_MODELVIEW_MATRIX, modelViewMatrixFB);
    	GL11.glGetInteger(GL11.GL_VIEWPORT, viewportFB);

    	GL11.glBegin(GL11.GL_LINES);
    	GL11.glColor3f(0.2f, 0.2f, 0.2f);

    	for (int x=-20; x<=20; x++) {
    		GL11.glVertex3f(x, -20.0f, 0.0f);
        	GL11.glVertex3f(x, 20.0f, 0.0f);
    	}
    	
    	for (int y=-20; y<=20; y++) {
    		GL11.glVertex3f(-20.0f, y, 0.0f);
        	GL11.glVertex3f(20.0f, y, 0.0f);
    	}
    	
    	GL11.glColor3f(0.6f, 0.6f, 0.6f);
    	GL11.glVertex3f(-20.0f, 0.0f, 0.0f);
    	GL11.glVertex3f(20.0f, 0.0f, 0.0f);

    	GL11.glVertex3f(0.0f, -20.0f, 0.0f);
    	GL11.glVertex3f(0.0f, 20.0f, 0.0f);
    	GL11.glEnd();
    	
    	if (contour != null) {
    		if (contourClosed) {
		    	GL11.glBegin(GL11.GL_LINE_LOOP);
		    	for (Vector3f v : contour) {
		    		GL11.glVertex3f(v.x, v.y, v.z);
		    	}
		    	GL11.glEnd();
    		}
    		else {
		    	GL11.glBegin(GL11.GL_LINE_STRIP);
		    	for (Vector3f v : contour) {
		    		GL11.glVertex3f(v.x, v.y, v.z);
		    	}
		    	GL11.glVertex3f(cursorWorldSpace.x, cursorWorldSpace.y, cursorWorldSpace.z);
		    	GL11.glEnd();
    		}
    	}
    	GL11.glPointSize(5.0f);
    	GL11.glBegin(GL11.GL_POINTS);
    		GL11.glColor3f(1.0f, 1.0f, 1.0f);
    		GL11.glVertex3f(cursorWorldSpace.x, cursorWorldSpace.y, cursorWorldSpace.z);
    	GL11.glEnd();
    	
    	if (contourClosed) {
			Trapezoidation.convert(contour);
		}
	}

	public static void main(String[] argv) {
    	Main m = new Main();
        m.start();
    }
}