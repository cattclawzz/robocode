package robotFiles;
import robocode.*;

public class JohnnyMarr extends Robot{
	public void run() {
		while(true){
			turnRadarRight(50);
		}
	}

	public void onScannedRobot(ScannedRobotEvent e) {
		//System.out.println(e.getDistance());
		//System.out.println(getRadarHeading());
		System.out.println(getX()-(e.getDistance() * Math.cos(getRadarHeading())));
	}
}

//checking robot pos --- not working currently :(