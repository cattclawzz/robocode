package DB2EB;
import robocode.util.Utils;
import robocode.*;
import java.awt.geom.Point2D;
import java.awt.Color;

public class DavidBarnes2ElectricBoogaloo extends AdvancedRobot{
	private byte moveDirection = 1;
	private double oldEnemyHeading = 0;

	//-- Target lock -- 
	private String targetName = null;
	private long targetLastSeen = 0;
	private static final long TARGET_TIMEOUT = 30; // ticks before we give up on a lost target (

	public void run() {
		//-- Make him PINK--
		setBodyColor(Color.GREEN);
		setGunColor(Color.GREEN);
		setRadarColor(Color.GREEN);
		setScanColor(Color.GREEN);
		setBulletColor(Color.GREEN);
		
		
		
		//-- Perfect Lock --
		// There were built in things to do auto compensation for the gun moving aipdjaskldjk
		setAdjustGunForRobotTurn(true);
		setAdjustRadarForRobotTurn(true);
		setAdjustRadarForGunTurn(true);

		while(true){
		
			//-- Custom RGB Fade (#07FFFF to #FF66CC) --
			// Change 0.05 to make it fade faster (higher) or slower (lower)
			double fadeFactor = (Math.sin(getTime() * 0.1) + 1.0) / 2.0;
			
			int r = (int)(7 + (255 - 7) * fadeFactor);
			int g = (int)(255 + (102 - 255) * fadeFactor);
			int b = (int)(255 + (204 - 255) * fadeFactor);
			Color fadeColor = new Color(r, g, b);
			
			setBodyColor(fadeColor);
			setGunColor(fadeColor);
			setRadarColor(fadeColor);
			setScanColor(fadeColor);
			setBulletColor(fadeColor);
			
			
			if (getRadarTurnRemaining() == 0.0){
            	setTurnRadarRightRadians(Double.POSITIVE_INFINITY);
			}
			//-- collision detection + dodge--
			//-- occasional random reversal--
			if (Math.random() < 0.04) {
				moveDirection *= -1;
			}
			//-- vary step distance for dodge --
			setAhead(moveDirection * (60 + Math.random() * 60));

			setFire(1.9); //shoot non-blocking
	        execute(); //flushes all queued set...() commands for this tick and advances one turn (Idk google ai said this is what it does)
		}
	}

	public void onScannedRobot(ScannedRobotEvent e){
		//-- pick a target if we don't have one --
		if (targetName == null || (!e.getName().equals(targetName) && getTime() - targetLastSeen > TARGET_TIMEOUT)) {
			targetName = e.getName();
		}
		//-- Picks a target instead of everyone --
		if (!e.getName().equals(targetName)) {
			return;
		}
		targetLastSeen = getTime();

		//-- Perfect Lock --
		setTurnRadarRightRadians(Utils.normalRelativeAngle(
			(getHeadingRadians() + e.getBearingRadians()) - getRadarHeadingRadians()
		) * 2);

		//-- collision detection / orbit movement --
		//-- small random wobble added for dodge --
		setTurnRight(Utils.normalRelativeAngleDegrees(
			e.getBearing() + 90 - (15 * moveDirection) + (Math.random() * 20 - 10)));

		//-- Shooting --
		//   scale power down at range; floor at ~1.9,
		//   cap at 3.0
		double bulletPower = Math.min(3.0, Math.max(1.9, Math.min(getEnergy(), 400.0 / e.getDistance())));

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
		setTurnGunRightRadians(Utils.normalRelativeAngle(
		theta - getGunHeadingRadians()));

		//-- only fire once the gun is aimed accurately enough --
		if (Math.abs(getGunTurnRemaining()) < 10.0) {
			setFire(bulletPower);
		}
	}

	//-- collision detection --
	public void onHitWall(HitWallEvent e) {
	    moveDirection *= -1;
	}
	public void onHitRobot(HitRobotEvent e) {
	    moveDirection *= -1;
	}

	//-- target lock upkeep: if our locked target dies, drop the lock --
	public void onRobotDeath(RobotDeathEvent e) {
	    if (e.getName().equals(targetName)) {
	        targetName = null;
	    }
	}
}
