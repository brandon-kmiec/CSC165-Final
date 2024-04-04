package tage.nodeControllers;

import tage.*;
import org.joml.*;

/**
* A StretchController is a node controller that, when enabled, causes any object
* it is attached to to stretch on the y-axis.
*/
public class StretchController extends NodeController
{	private float scaleRate = 0.0006f;
	private float cycleTime = 2000.0f;
	private float totalTime = 0.0f;
	private float direction = 1.0f;
	private Matrix4f curScale, newScale;
	private Engine engine;
	
	/** Creates a stretch controller with scale rate = 0.0006f. */
	public StretchController(Engine e, float ctime)
	{	super();
		cycleTime = ctime;
		engine = e;
		newScale = new Matrix4f();
	} // end StretchController constructor
	
	/** This is called automatically by the RenderSystem (via SceneGraph) once per frame
	*   during display().  It is for engine use and should not be called by the application.
	*/
	public void apply(GameObject go)
	{	float elapsedTime = super.getElapsedTime();
		totalTime += elapsedTime / 100.0f;
		
		if(totalTime > cycleTime)
		{	direction = -direction;
			totalTime = 0.0f;
		} // end if
		
		curScale = go.getLocalScale();
		float scaleAmt = 1.0f + direction * scaleRate * elapsedTime;
		newScale.scaling(curScale.m00(), curScale.m11() * scaleAmt, curScale.m22());
		go.setLocalScale(newScale);
	} // end apply
} // end StretchController class