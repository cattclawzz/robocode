package robotFiles;
import robocode.util.Utils;
import robocode.*;
import java.awt.geom.Point2D;
import java.awt.Color;

public class KevinBarnes extends AdvancedRobot{
	private byte moveDirection = 1;
	private double oldEnemyHeading = 0;
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
    	else {radarTurn += extraTurn;}
		
		setTurnRadarRightRadians(radarTurn);

		//-- colision detection --
		setTurnRight(Utils.normalRelativeAngleDegrees(e.getBearing() + 90 - (15 * moveDirection)));
		
		//-- Shooting --
		double bulletPower = Math.min(3.0,getEnergy());
		double myX = getX();
		double myY = getY();
		double absoluteBearing = getHeadingRadians() + e.getBearingRadians();
		double enemyX = getX() + e.getDistance() * Math.sin(absoluteBearing);
		double enemyY = getY() + e.getDistance() * Math.cos(absoluteBearing);
		double enemyHeading = e.getHeadingRadians();
		double enemyHeadingChange = enemyHeading - oldEnemyHeading;
		double enemyVelocity = e.getVelocity();
		oldEnemyHeading = enemyHeading;
	
		double deltaTime = 0;
		double battleFieldHeight = getBattleFieldHeight(), 
	    battleFieldWidth = getBattleFieldWidth();
		double predictedX = enemyX, predictedY = enemyY;
		while((++deltaTime) * (20.0 - 3.0 * bulletPower) < 
	    Point2D.Double.distance(myX, myY, predictedX, predictedY)){		
		predictedX += Math.sin(enemyHeading) * enemyVelocity;
		predictedY += Math.cos(enemyHeading) * enemyVelocity;
		enemyHeading += enemyHeadingChange;
		if(	predictedX < 18.0 
			|| predictedY < 18.0
			|| predictedX > battleFieldWidth - 18.0
			|| predictedY > battleFieldHeight - 18.0){
				predictedX = Math.min(Math.max(18.0, predictedX), 
				battleFieldWidth - 18.0);	
				predictedY = Math.min(Math.max(18.0, predictedY), 
				battleFieldHeight - 18.0);
				break;
		}
}
	double theta = Utils.normalAbsoluteAngle(Math.atan2(
    	predictedX - getX(), predictedY - getY()));
		setTurnRadarRightRadians(Utils.normalRelativeAngle(	
		absoluteBearing - getRadarHeadingRadians()));
		setTurnGunRightRadians(Utils.normalRelativeAngle(
		theta - getGunHeadingRadians()));
		fire(3);
	}
	
	//-- collision detection --
	public void onHitWall(HitWallEvent e) {
	    moveDirection *= -1;
	}
	public void onHitRobot(HitRobotEvent e) {
	    moveDirection *= -1;
	}
}
