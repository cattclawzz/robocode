package robotFiles;
import robocode.util.Utils;
import robocode.*;
import java.awt.Color;

public class KevinBarnes extends AdvancedRobot{
	private byte moveDirection = 1;
	public void run() {
	
		//-- Make him pink--
		setBodyColor(Color.PINK);
		setGunColor(Color.PINK);
		setRadarColor(Color.PINK);
		setScanColor(Color.PINK);
		setBulletColor(Color.PINK);

		//-- Perfect Lock --
		while(true){
			if (getRadarTurnRemaining() == 0.0){
            	setTurnRadarRightRadians(Double.POSITIVE_INFINITY);
			}
			//-- collision detection --
			setAhead(moveDirection * 100);
			
			fire(1.9); //shoot

	        execute(); //I dont know what this does
		}
	}
	
	public void onScannedRobot(ScannedRobotEvent e){
		//-- Perfect Lock --
		double radarTurn = Utils.normalRelativeAngle((getHeadingRadians() + e.getBearingRadians()) - getRadarHeadingRadians());
		double extraTurn = Math.min(Math.atan(36.0 / e.getDistance() ), Rules.RADAR_TURN_RATE_RADIANS);
		
		if (radarTurn < 0) {radarTurn -= extraTurn;}
    	else{radarTurn += extraTurn;}
		
		setTurnRadarRightRadians(radarTurn);

		//-- colision detection --
		setTurnRight(Utils.normalRelativeAngleDegrees(e.getBearing() + 90 - (15 * moveDirection)));
		
		//-- Shooting --
		double absoluteBearing = getHeadingRadians() + e.getBearingRadians();
		setTurnGunRightRadians(robocode.util.Utils.normalRelativeAngle(absoluteBearing - getGunHeadingRadians()));
	}
	
	//-- collision detection --
	public void onHitWall(HitWallEvent e) {
	    moveDirection *= -1;
	}
	public void onHitRobot(HitRobotEvent e) {
	    moveDirection *= -1;
	}
}
