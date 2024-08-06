package utils;

public class Battery {
	public static final int ERROR = -1;
	public static final int METHOD_NONE = -1;
	public static final int METHOD_BATTERYLEVEL = 0;
	public static final int METHOD_COM_NOKIA_MID_BATTERYLEVEL = 1;
	public static final int METHOD_SENSOR_API = 2;
	private static int method = METHOD_NONE;
	
	
	public static boolean checkAndInit() {
		if (method != METHOD_NONE) {
			return true;
		}
		
		if (System.getProperty("batterylevel") != null) {
			method = METHOD_BATTERYLEVEL;
			Logger.log("method 0 success");
			return true;
		} else {
			Logger.log("method 0 failed");
		}
		
		if (System.getProperty("com.nokia.mid.batterylevel") != null) {
			method = METHOD_COM_NOKIA_MID_BATTERYLEVEL;
			Logger.log("method 1 success");
			return true;
		} else {
			Logger.log("method 1 failed");
		}
		
		try {
			Class.forName("javax.microedition.sensor.SensorManager");
			//method = METHOD_SENSOR_API;
			Logger.log("method 2 success but is not supported yet");
			return false; // true;
		} catch (ClassNotFoundException e) {
			Logger.log("method 2 failed: " + e);
		}
		
		return false;
	}
	
	public static int getBatteryLevel() {
		try {
			switch (method) {
			case METHOD_BATTERYLEVEL:
				return Integer.parseInt(System.getProperty("batterylevel"));
			case METHOD_COM_NOKIA_MID_BATTERYLEVEL:
				return Integer.parseInt(System.getProperty("com.nokia.mid.batterylevel").replace('%', ' ').trim());
			/*case METHOD_SENSOR_API:
				return new BatteryViaSensors().getBatteryLevel();*/
			}
		} catch (Exception ex) {
			Logger.log("can't get battery level:");
			Logger.log(ex.toString());
		}
		return ERROR;
	}
	
	public static int getMethod() {
		return method;
	}
}
