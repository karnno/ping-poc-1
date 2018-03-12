package org.ping.services;

import org.ping.services.report.Report;
import org.ping.services.report.ReportPart;

public abstract class AbstractPing {

	String host;
	int delay;
	
	Report report;
	
	public AbstractPing(Report report, String host, int delay) {
		this.report = report;
		this.host = host;
		this.delay = delay;
	}
	
	protected void updateReport(String host, ReportPart part, String toReport) {
		report.update(host, part, toReport);
	}
	
	
	protected void updateReportWithIssue(String host, ReportPart part, String toReport) {
		report.update(host, part, toReport);
		report.wakeObservers();
	}
	
}
