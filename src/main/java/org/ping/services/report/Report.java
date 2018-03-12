package org.ping.services.report;

import java.util.Observable;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class Report extends Observable {

	private String host="";
    private String icmp_ping="";
    private String tcp_ping="";
    private String trace="";

    ReadWriteLock rwLock = new ReentrantReadWriteLock();
   
    //Telescoping Constructor as need no to scale this later
    public Report(String host) {
    		this(host, "");
    }
    
    public Report(String host, String icmp_ping) {
        this(host, icmp_ping, "");
    }
    
    public Report(String host, String icmp_ping, String tcp_ping){
        this(host, icmp_ping, tcp_ping, "");
    }
    
    public Report(String host, String icmp_ping, String tcp_ping, String trace) {
        this.host = host; 
    		this.icmp_ping=icmp_ping;
        this.tcp_ping=tcp_ping;
        this.trace=trace;
    }

    public String getJson() {
    	
    		StringBuilder sb = new StringBuilder();
    		
    		sb.append("{")
    			.append("\"host\":")
    			.append("\"").append(this.host).append("\",")
    			.append("\"icmp_ping\":")
    			.append("\"").append(this.icmp_ping).append("\",")
    			.append("\"tcp_ping\":")
    			.append("\"").append(this.tcp_ping).append("\",")
    			.append("\"trace\":")
    			.append("\"").append(this.trace).append("\"")
    		.append("}")
    		;
    		
    		return sb.toString();
    }

	public void setHost(String host) {
		this.host=host;
	}
    
	public void setIcmp(String icmpReport) {
		this.icmp_ping = icmpReport;
	}
    
	public String getIcmp() {
		return this.icmp_ping;
	}
	
    public void setTcp(String tcpReport) {
    		this.tcp_ping = tcpReport;
    }

    public String getTcp() {
    		return this.tcp_ping;
    }
    
	public void setTracer(String toReport) {
		this.trace = toReport;
	}

	public String getTracer() {
		return this.trace;
	}
	
	public void wakeObservers() {
		setChanged();
		notifyObservers(this.getJson());
	}

	public void update(String host2, ReportPart part, String toReport) {
		//
		Lock writeLock = rwLock.writeLock();
		writeLock.lock();
		try {
//			System.out.println(part.toString() + " : update : lock aquired");
			this.setHost(host);
			switch(part) {
			
			case ICMP:
					this.setIcmp(toReport);
					break;
			case TCP:
					this.setTcp(toReport);
					break;
			case TRACER:
					this.setTracer(toReport);
					break;
				default:;
			}
		} catch(Exception lockExc){
			lockExc.printStackTrace();
		} finally {
			writeLock.unlock();
			System.out.println(part.toString() +" = " + this.getJson());
//			System.out.println(part.toString() + " : update : lock released");
		}
	}
}