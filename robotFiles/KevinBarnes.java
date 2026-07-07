package robotFiles;
import robocode.util.Utils;
import robocode.*;

public class KevinBarnes extends AdvancedRobot{
	private byte moveDirection = 1;
	public void run() {
		//-- Perfect Lock --
		while(true){
			if (getRadarTurnRemaining() == 0.0){
            	setTurnRadarRightRadians( Double.POSITIVE_INFINITY );
			}
			//-- collision detection --
			setAhead(moveDirection * 100);
	        execute();
		}

	}
	
	public void onScannedRobot(ScannedRobotEvent e){
		//-- Perfect Lock --
		double radarTurn = Utils.normalRelativeAngle((getHeadingRadians() + e.getBearingRadians()) - getRadarHeadingRadians());
		double extraTurn = Math.min(Math.atan(36.0 / e.getDistance() ), Rules.RADAR_TURN_RATE_RADIANS);
		
		if (radarTurn < 0) {radarTurn -= extraTurn;}
    	else{radarTurn += extraTurn;}
		
		setTurnRadarRightRadians(radarTurn);
	}
	
	//-- collision detection --
	public void onHitWall(HitWallEvent e) {
	    moveDirection *= -1;
	}
	public void onHitRobot(HitRobotEvent e) {
	    moveDirection *= -1;
	}

}
