public class StateAndReward {

	
	/* State discretization function for the angle controller */
	public static String getStateAngle(double angle, double vx, double vy) {

		String direction = (angle < 0) ? "W" : "E";
	    double degrees = Math.toDegrees(Math.abs(angle));
	    
	    // Define the state values based on angle ranges
	    int[] angleRanges = {1, 2, 5, 15, 90};
	    String[] stateValues = {"1", "2", "5", "15", "90"};

	    // Find the appropriate state based on degrees
	    String state = "180";  // Default value for angles greater than 90
	    
	    for (int i = 0; i < angleRanges.length; i++) {
	        if (degrees < angleRanges[i]) {
	            state = stateValues[i];
	            break;
	        }
	    }

	    return state + "(" + direction + ")";
	}

	/* Reward function for the angle controller */
	public static double getRewardAngle(double angle, double vx, double vy) {

	    return 1000 * Math.exp(-Math.pow(10 * angle, 2));
	}

	/* State discretization function for the full hover controller */
	public static String getStateHover(double angle, double vx, double vy) {

		/* TODO: IMPLEMENT THIS FUNCTION */
		String yDir = (vy < 0) ? "N": "S";
		double yVel = Math.abs(vy);
		double xVel = Math.abs(vx);
		double vel = Math.sqrt(Math.pow(xVel, 2) + Math.pow(yVel, 2));
		
		String state = "";
				
    	if (yVel < 0.02 && vel < 0.05) {
    		state = "HOVER";
    	}
    	
    	else if (yVel < 0.02) {
    		state = "YHOVER";
    	}
    	
    	else if (yVel < 0.1) {
    		state = "YSLOW";
    	}
    	
    	else if (yVel < 1) {
    		state = "YMED";
    	}
    	
		else
			state = "YFAST";
    	
    	state += String.format("(%s)", yDir);

		return state;
	}

	/* Reward function for the full hover controller */
	public static double getRewardHover(double angle, double vx, double vy) {

		return 1000 / Math.pow(1000, Math.abs(vy));
	}
	
	public static String getTotalState(double angle, double vx, double vy){
		
		String rotationState = getStateAngle(angle, vx, vy);
		String velocityState = getStateHover(angle, vx, vy);
		
		return String.format("%s_%s", rotationState, velocityState);
	}
	
	public static double getTotalReward(double angle, double vx, double vy){
		
		double rotationReward = getRewardAngle(angle, vx, vy);
		double hoverReward = getRewardHover(angle, vx, vy);
		double reward = rotationReward * hoverReward;
		
		return reward;
	}
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	

	// ///////////////////////////////////////////////////////////
	// discretize() performs a uniform discretization of the
	// value parameter.
	// It returns an integer between 0 and nrValues-1.
	// The min and max parameters are used to specify the interval
	// for the discretization.
	// If the value is lower than min, 0 is returned
	// If the value is higher than min, nrValues-1 is returned
	// otherwise a value between 1 and nrValues-2 is returned.
	//
	// Use discretize2() if you want a discretization method that does
	// not handle values lower than min and higher than max.
	// ///////////////////////////////////////////////////////////
	public static int discretize(double value, int nrValues, double min, double max) {
		if (nrValues < 2) {
			return 0;
		}

		double diff = max - min;

		if (value < min) {
			return 0;
		}
		if (value > max) {
			return nrValues - 1;
		}

		double tempValue = value - min;
		double ratio = tempValue / diff;

		return (int) (ratio * (nrValues - 2)) + 1;
	}

	// ///////////////////////////////////////////////////////////
	// discretize2() performs a uniform discretization of the
	// value parameter.
	// It returns an integer between 0 and nrValues-1.
	// The min and max parameters are used to specify the interval
	// for the discretization.
	// If the value is lower than min, 0 is returned
	// If the value is higher than min, nrValues-1 is returned
	// otherwise a value between 0 and nrValues-1 is returned.
	// ///////////////////////////////////////////////////////////
	public static int discretize2(double value, int nrValues, double min,
			double max) {
		double diff = max - min;

		if (value < min) {
			return 0;
		}
		if (value > max) {
			return nrValues - 1;
		}

		double tempValue = value - min;
		double ratio = tempValue / diff;

		return (int) (ratio * nrValues);
	}

}
