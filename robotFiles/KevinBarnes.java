package robotFiles;
import robocode.util.Utils;
import robocode.*;

public class KevinBarnes extends AdvancedRobot{
	public void run() {
		while(true){
			if (getRadarTurnRemaining() == 0.0){
            	setTurnRadarRightRadians( Double.POSITIVE_INFINITY );
			}
	        execute();
		}
	}
	
	public void onScannedRobot(ScannedRobotEvent e){
		double radarTurn = Utils.normalRelativeAngle((getHeadingRadians() + e.getBearingRadians()) - getRadarHeadingRadians());
		double extraTurn = Math.min(Math.atan(36.0 / e.getDistance() ), Rules.RADAR_TURN_RATE_RADIANS);
		
		if (radarTurn < 0) {radarTurn -= extraTurn;}
    	else{radarTurn += extraTurn;}
		
		setTurnRadarRightRadians(radarTurn);
	}
}
