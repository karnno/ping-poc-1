package org.ping.services;

/**
 * Helper class for services
 * 
 * @author karnno
 *
 */
public class ServicesUtils {

	private static String OS = System.getProperty("os.name").toLowerCase();

	public static String getOsName() {
		return OS;
	}
	/**
	 * Return traceRoute command according to the os name
	 * @return String the traceroute command or empty string
	 */
	public static String getTraceRouteCommand() {
		String toReturn = "traceroute";
		if (isWindows()) {
			toReturn = "tracert" ;
		} else if (isMac()) {
			toReturn = "traceroute";
		} else if (isUnix()) {
			toReturn = "traceroute -l";
		} else {
			toReturn="";
		}
		
		return toReturn;
	}
	public static boolean isWindows() {

		return (OS.indexOf("win") >= 0);

	}

	public static boolean isMac() {

		return (OS.indexOf("mac") >= 0);

	}

	public static boolean isUnix() {

		return (OS.indexOf("nix") >= 0 || OS.indexOf("nux") >= 0 || OS.indexOf("aix") > 0 );

	}

	public static boolean isSolaris() {

		return (OS.indexOf("sunos") >= 0);

	}
	
}
