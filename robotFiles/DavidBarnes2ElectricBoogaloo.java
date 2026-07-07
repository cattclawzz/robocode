package robotFiles;
import robocode.util.Utils;
import robocode.*;
import java.awt.geom.Point2D;
import java.awt.Color;

public class DavidBarnes extends AdvancedRobot{
	private byte moveDirection = 1;
	private double oldEnemyHeading = 0;

	//-- Target lock (for melee / 1v1v1+) --
	private String targetName = null;
	private long targetLastSeen = 0;
	private static final long TARGET_TIMEOUT = 30; // ticks before we give up on a lost target

	public void run() {
		//-- Make him pink--
		setBodyColor(Color.GREEN);
		setGunColor(Color.GREEN);
		setRadarColor(Color.GREEN);
		setScanColor(Color.GREEN);
		setBulletColor(Color.GREEN);
		//-- Perfect Lock --
		while(true){
			if (getRadarTurnRemaining() == 0.0){
            	setTurnRadarRightRadians(Double.POSITIVE_INFINITY);
			}
			//-- collision detection + dodge movement --
			//-- occasional random reversal so movement isn't a smooth, predictable orbit --
			if (Math.random() < 0.06) {
				moveDirection *= -1;
			}
			//-- vary step distance a bit too, same reason --
			setAhead(moveDirection * (60 + Math.random() * 60));

			setFire(1.9); //shoot (non-blocking, queued with everything else)
	        execute(); //flushes all queued set...() commands for this tick and advances one turn
		}
	}

	public void onScannedRobot(ScannedRobotEvent e){
		//-- Target lock: pick a target if we don't have one, or if ours has gone stale --
		if (targetName == null || (!e.getName().equals(targetName) && getTime() - targetLastSeen > TARGET_TIMEOUT)) {
			targetName = e.getName();
		}
		//-- Only act on scans of our current target, ignore everyone else --
		if (!e.getName().equals(targetName)) {
			return;
		}
		targetLastSeen = getTime();

		//-- Perfect Lock --
		double radarTurn = Utils.normalRelativeAngle((getHeadingRadians() + e.getBearingRadians()) - getRadarHeadingRadians());
		double extraTurn = Math.min(Math.atan(36.0 / e.getDistance() ), Rules.RADAR_TURN_RATE_RADIANS);

		if (radarTurn < 0) {radarTurn -= extraTurn;}
    	else {radarTurn += extraTurn;}

		setTurnRadarRightRadians(radarTurn);

		//-- colision detection / orbit movement --
		//-- small random wobble added to the orbit angle so the radius isn't perfectly constant --
		setTurnRight(Utils.normalRelativeAngleDegrees(
			e.getBearing() + 90 - (15 * moveDirection) + (Math.random() * 20 - 10)));

		//-- Shooting --
		//-- scale power down at range for faster, harder-to-dodge bullets; floor at ~1.9,
		//   the empirically-favored low end from robowiki discussion; cap at 3.0 and never
		//   exceed current energy --
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
		setTurnRadarRightRadians(Utils.normalRelativeAngle(
		absoluteBearing - getRadarHeadingRadians()));
		setTurnGunRightRadians(Utils.normalRelativeAngle(
		theta - getGunHeadingRadians()));

		//-- only fire once the gun is close enough to on-target, so shots aren't
		//   wasted while the gun is still mid-swing --
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

	//-- target lock upkeep: if our locked target dies, drop the lock so we
	//   pick a fresh one on the next scan instead of waiting out the timeout --
	public void onRobotDeath(RobotDeathEvent e) {
	    if (e.getName().equals(targetName)) {
	        targetName = null;
	    }
	}
}
