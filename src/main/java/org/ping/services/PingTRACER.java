package org.ping.services;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import org.ping.services.report.Report;
import org.ping.services.report.ReportPart;

public class PingTRACER extends AbstractPing implements Callable<String>{

	/**
	 * If multiple instances of PingTRACER, forbid the call to the host
	 * by checking the queue.
	 * If empty queue, the current instance inits it and 
	 * runs the call, otherwise, it skips the call.
	 */
	static BlockingQueue<String> concurrentQueueForHost  = new LinkedBlockingQueue<String>();
	
	public PingTRACER(Report report, String host, int delay) {
		super(report, host, delay);
		
	}

	@Override
	public String call() throws Exception {
		
		String traceRouteCommand = ServicesUtils.getTraceRouteCommand();
		if (traceRouteCommand.isEmpty()) {
			String issueValue = 	" Unknown traceroute command for your operating system : " + ServicesUtils.getOsName();
			reportIssue(issueValue);
			throw new Exception(issueValue);
		}
		
		List<String> results = new ArrayList<String>();
//        ProcessBuilder processBuilder = new ProcessBuilder("cmd.exe", "tracert", host);
        StringBuilder result = new StringBuilder();
	        
		while (true) {
			try {
				// use the concurrentQueue to be sure no other PingTracer instance tries to 
				// ping the current host
				//
				if (concurrentQueueForHost.contains(host)) {
					continue; 
				}else {
					concurrentQueueForHost.add(host);
				}
				
				//at this point , we are sure to be the only PingTracer instance
				// to ping the current host
				
					//
					String line="";
		            result.setLength(0);
		            
	            		Process process = Runtime.getRuntime().exec(traceRouteCommand + " " + host);
	                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(process.getInputStream()));
	                innerWhile : while ((line = bufferedReader.readLine()) != null) {
	                		if (line.indexOf("*") == -1) {
	                			result.append(line).append("\t");
	                		}else {
	                			break innerWhile;
	                		}
	                }
	        			
		            results.add(result.toString());
		            //
		            if (result.length() != 0) {
						//update the report continuously even if everything is OK 
						updateReport(host, ReportPart.TRACER, result.toString());
						TimeUnit.SECONDS.sleep(delay);
		            }else {
		            		// command returned nothing ?
		            		reportIssue("TRACER failed with empty result");
		            		break;
		            }
					
			}catch(IOException ex) {
				System.out.println("\n---- ping tracer issue !"  + ex.getLocalizedMessage());
				reportIssue("TRACER failed " + ex.getLocalizedMessage());
				break;
			}finally {
				concurrentQueueForHost.remove(host);
			}
		}// while
		return "ok traceroute"; 
				
	}
	
	private void reportIssue(String what) {
		super.updateReportWithIssue(host, ReportPart.TRACER, what);
	}

}
