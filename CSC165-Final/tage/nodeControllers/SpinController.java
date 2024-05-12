package tage.nodeControllers;

import tage.*;
import org.joml.*;

/**
* A SpinController is a node controller that, when enabled, causes any object
* it is attached to to rotate.
*/
public class SpinController extends NodeController
{	private float spinRateX = 0.0003f;
	private float spinRateY = 0.0003f;
	private float spinRateZ = 0.0003f;
	private float cycleTime = 2000.0f;
	private float totalTime = 0.0f;
	private Matrix4f curRotate, newRotate;
	private Engine engine;
	
	/** Creates a spin controller with spin rates as specified. */
	public SpinController(Engine e, float ctime, float xRate, float yRate, float zRate)
	{	super();
		cycleTime = ctime;
		engine = e;
		newRotate = new Matrix4f();
		
		spinRateX = xRate;
		spinRateY = yRate;
		spinRateZ = zRate;
	} // end SpinController constructor
	
	/** This is called automatically by the RenderSystem (via SceneGraph) once per frame
	*   during display().  It is for engine use and should not be called by the application.
	*/
	public void apply(GameObject go)
	{	float elapsedTime = super.getElapsedTime();
		totalTime += elapsedTime / 1000.0f;
		
		if(totalTime > cycleTime)
		{	
			totalTime = 0.0f;
		} // end if
		
		curRotate = go.getLocalRotation();
		float rotateAmtX = spinRateX * elapsedTime;
		float rotateAmtY = spinRateY * elapsedTime;
		float rotateAmtZ = spinRateZ * elapsedTime;
		
		newRotate.mul(curRotate.rotation(rotateAmtX, 1, 0, 0));
		newRotate.mul(curRotate.rotation(rotateAmtY, 0, 1, 0));
		newRotate.mul(curRotate.rotation(rotateAmtZ, 0, 0, 1));

		go.setLocalRotation(newRotate);
	} // end apply
	
	/** sets the spin rate on the x axis when the controller is enabled */
	public void setSpinRateX(float rateX) {
		spinRateX = rateX;
	} // end setSpinRateX
	
	/** sets the spin rate on the y axis when the controller is enabled */
	public void setSpinRateY(float rateY) {
		spinRateY = rateY;
	} // end setSpinRateY
	
	/** sets the spin rate on the z axis when the controller is enabled */
	public void setSpinRateZ(float rateZ) {
		spinRateZ = rateZ;
	} // end setSpinRateZ
} // end SpinController class