package tage;

import java.lang.Math;
import net.java.games.input.Event;
import org.joml.*;
import tage.input.action.AbstractInputAction;
import tage.input.InputManager;

/**
* 3rd person orbit controller.
* orbit the camera wihtout adjusting the avatar's heading.
* adjust camera elevation angle.
* move and turn the avatar while maintaining the camera's relative position to the avatar. 
*/
public class CameraOrbitController {
	private Engine engine;
	private Camera camera;			// the camera being controlled
	private GameObject avatar;		// the target avatar the camera looks at
	private float cameraAzimuth;	// rotation around target Y axis
	private float cameraElevation;	// elevation of camera above target
	private float cameraRadius;		// distance between camera and target
	
	/**
	*	Create an orbit controller with camera, avatar, gamepad name, and engine as specified.
	*/
	public CameraOrbitController(Camera cam, GameObject av, String gpName, Engine e) {
		engine = e;
		camera = cam;
		avatar = av;
		cameraAzimuth = 0.0f;		// start BEHIND and ABOVE the target
		cameraElevation = 20.0f;	// elevation is in degrees
		cameraRadius = 4.0f;		// distance from camera to avatar
		setupInputs(gpName);
		updateCameraPosition();
	} // end CameraOrbitController Constructor
	
	/**	Associate keyboard and controller inputs with the inputmanager */
	private void setupInputs(String gp) {
		OrbitAzimuthAction azmAction = new OrbitAzimuthAction();
		OrbitRadiusAction radAction = new OrbitRadiusAction();
		OrbitElevationAction elvAction = new OrbitElevationAction();
		
		InputManager im = engine.getInputManager();
				
		im.associateAction(gp, net.java.games.input.Component.Identifier.Axis.RX, azmAction,
						   InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
		im.associateAction(gp, net.java.games.input.Component.Identifier.Axis.Z, radAction,
						   InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
		im.associateAction(gp, net.java.games.input.Component.Identifier.Axis.RY, elvAction,
						   InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
				   
		im.associateActionWithAllKeyboards(net.java.games.input.Component.Identifier.Key.I, elvAction,
						   InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
		im.associateActionWithAllKeyboards(net.java.games.input.Component.Identifier.Key.K, elvAction,
						   InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
		im.associateActionWithAllKeyboards(net.java.games.input.Component.Identifier.Key.J, azmAction,
						   InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);				   
		im.associateActionWithAllKeyboards(net.java.games.input.Component.Identifier.Key.L, azmAction,
						   InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
		im.associateActionWithAllKeyboards(net.java.games.input.Component.Identifier.Key.LBRACKET, radAction,
						   InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);				   
		im.associateActionWithAllKeyboards(net.java.games.input.Component.Identifier.Key.RBRACKET, radAction,
						   InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);				   
	} // end setupInputs
	
	/**
	*	Compute the camera's azimuth, elevation, and distance relative to
	*	the target in spherical coordinates, then convert to world Cartesian
	*	coordinated and set the camera position from that.
	*/
	public void updateCameraPosition() {
		Vector3f avatarRot = avatar.getWorldForwardVector();
		double avatarAngle = Math.toDegrees((double)avatarRot.angleSigned(new Vector3f(0, 0, -1), 
											new Vector3f(0, 1, 0)));
		float totalAz = cameraAzimuth - (float)avatarAngle;
		double theta = Math.toRadians(totalAz);
		double phi = Math.toRadians(cameraElevation);
		float x = cameraRadius * (float)(Math.cos(phi) * Math.sin(theta));
		float y = cameraRadius * (float)(Math.sin(phi));
		float z = cameraRadius * (float)(Math.cos(phi) * Math.cos(theta));
				
		if(y < -1.0f)
			y = -0.5f;
		camera.setLocation(new Vector3f(x, y, z).add(avatar.getWorldLocation()));
		camera.lookAt(avatar);
	} // end updateCameraPosition
	
	
	/**	update camera azimuth */
	private class OrbitAzimuthAction extends AbstractInputAction {
		/** Update camera position based on the value of an event */
		public void performAction(float time, Event event) {
			float rotAmount;
		
			if(event.getValue() < -0.2 || event.getComponent().getIdentifier() == net.java.games.input.Component.Identifier.Key.J)
				rotAmount = -0.2f;
			else
				if(event.getValue() > 0.2 || event.getComponent().getIdentifier() == net.java.games.input.Component.Identifier.Key.L)
					rotAmount = 0.2f;
				else
					rotAmount = 0.0f;
			
			cameraAzimuth += rotAmount;
			cameraAzimuth = cameraAzimuth % 360;
			updateCameraPosition();
		} // end performAction
	} // end OrbitAzimuthAction Class

	
	/** update camera radius */
	private class OrbitRadiusAction extends AbstractInputAction {
		/** Update camera position based on the value of an event */
		public void performAction(float time, Event event) {
			float rotAmount;
			
			if(event.getValue() < -0.2 || event.getComponent().getIdentifier() == net.java.games.input.Component.Identifier.Key.LBRACKET)
				rotAmount = -0.2f;
			else
				if((event.getValue() > 0.2 || event.getComponent().getIdentifier() == net.java.games.input.Component.Identifier.Key.RBRACKET) && 
				   (cameraElevation > -14.0f && camera.getLocation().y() > -0.5f))
					rotAmount = 0.2f;
				else
					rotAmount = 0.0f;
			
			cameraRadius += rotAmount;
			cameraRadius = cameraRadius % 360;
			
			if(cameraRadius < 4.0f)
				cameraRadius = 4.0f;
			
			updateCameraPosition();
		} // end performAction
	} // end OrbitRadiusAction Class
	
	
	/**	update camera elevation */
	private class OrbitElevationAction extends AbstractInputAction {
		/** Update camera position based on the value of an event */
		public void performAction(float time, Event event) {
			float rotAmount;
			
			/*
			if(event.getComponent().getIdentifier() == net.java.games.input.Component.Identifier.
			   Button._4)
				rotAmount = -0.2f;
			else if(event.getComponent().getIdentifier() == net.java.games.input.Component.
					Identifier.Button._5)
				rotAmount = 0.2f;
			else
				rotAmount = 0.0f;
			*/
			
			if(event.getValue() < -0.2 || event.getComponent().getIdentifier() == net.java.games.input.Component.Identifier.Key.I)
				rotAmount = 0.2f;
			else
				if(event.getValue() > 0.2 || event.getComponent().getIdentifier() == net.java.games.input.Component.Identifier.Key.K)
					rotAmount = -0.2f;
				else
					rotAmount = 0.0f;
			
			cameraElevation += rotAmount;
			cameraElevation = cameraElevation % 360;

			if(cameraElevation < -14.0f)
				cameraElevation = -14.0f;
			
			if(cameraElevation > 89.9f)
				cameraElevation = 89.9f;
			
			updateCameraPosition();
		} // end performAction
	} // end OrbitElevationAction Class
	
} // end CameraOrbitController Class