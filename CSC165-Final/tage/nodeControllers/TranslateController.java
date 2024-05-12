package tage.nodeControllers;

import tage.*;
import org.joml.*;

/**
* A TranslateController is a node controller that, when enabled, causes any object
* it is attached to to translate.
* @author Brandon Kmiec
*/
public class TranslateController extends NodeController
{	private float translateSpeed = 0.0003f;
	private float translateRange = 1.0f;
	private float cycleTime = 2000.0f;
	private float totalTime = 0.0f;
	private float direction = 1.0f;
	private int translateAxis = 0;
	private Vector3f startLoc, curLoc;
	private Matrix4f curTranslate, newTranslate;
	private Engine engine;
	
	/** Creates a translate controller with aixs (0=x, 1=y, 2=z), speed, range, and initial location as specified. */
	public TranslateController(Engine e, float ctime, int axis, float speed, float range, Vector3f initLoc)
	{	super();
		cycleTime = ctime;
		engine = e;
		newTranslate = new Matrix4f();
		
		translateAxis = axis;
		translateSpeed = speed;
		translateRange = range;
		startLoc = initLoc;
	} // end TranslateController constructor
	
	/** This is called automatically by the RenderSystem (via SceneGraph) once per frame
	*   during display().  It is for engine use and should not be called by the application.
	*/
	public void apply(GameObject go)
	{	float elapsedTime = super.getElapsedTime();
		totalTime += elapsedTime / 1000.0f;
		
		if(go.getWorldLocation().distance(startLoc) > translateRange) {
			direction = -direction;
			totalTime = 0.0f;
		} // end if
		
		curLoc = go.getWorldLocation();
		float translateAmt = direction * translateSpeed * elapsedTime;
		
		switch(translateAxis) {
			case 0:	// x axis
				curLoc.add(new Vector3f(translateAmt, 0, 0));				
				break;
			case 1: // y axis
				curLoc.add(new Vector3f(0, translateAmt, 0));
				break;
			case 2:	// z axis
				curLoc.add(new Vector3f(0, 0, translateAmt));
				break;
			default:	// default to x axis
				curLoc.add(new Vector3f(translateAmt, 0, 0));
				break;
		} // end switch
		
		go.setLocalLocation(curLoc);
	} // end apply
} // end TranslateController class