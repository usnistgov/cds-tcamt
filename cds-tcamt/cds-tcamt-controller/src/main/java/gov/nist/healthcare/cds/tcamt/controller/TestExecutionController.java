package gov.nist.healthcare.cds.tcamt.controller;

import java.security.Principal;
import java.util.Calendar;
import java.util.List;

import javax.servlet.http.HttpSession;

import gov.nist.healthcare.cds.domain.SoftwareConfig;
import gov.nist.healthcare.cds.domain.TestCase;
import gov.nist.healthcare.cds.domain.TestExecution;
import gov.nist.healthcare.cds.domain.exception.UnresolvableDate;
import gov.nist.healthcare.cds.domain.wrapper.AggregateReport;
import gov.nist.healthcare.cds.domain.wrapper.Counts;
import gov.nist.healthcare.cds.domain.wrapper.Report;
import gov.nist.healthcare.cds.repositories.SoftwareConfigRepository;
import gov.nist.healthcare.cds.repositories.TestCaseRepository;
import gov.nist.healthcare.cds.repositories.TestPlanRepository;
import gov.nist.healthcare.cds.service.AggregateReportService;
import gov.nist.healthcare.cds.service.TestCaseExecutionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TestExecutionController {
	
	@Autowired
	private SoftwareConfigRepository softwareConfigRepository;

	@Autowired
	private TestCaseExecutionService execService;
	
	@Autowired
	private TestCaseRepository testCaseRepository;
	
	@Autowired
	private TestPlanRepository testPlanRepository;
	
	@Autowired
	private AggregateReportService aggregateService;

	@RequestMapping(value = "/exec/configs", method = RequestMethod.GET)
	@ResponseBody
	public List<SoftwareConfig> configs(Principal p){
		return softwareConfigRepository.findByUser(p.getName());
	}
	
	@RequestMapping(value = "/exec/configs/save", method = RequestMethod.POST)
	@ResponseBody
	public SoftwareConfig save(@RequestBody SoftwareConfig sc,Principal user) {
		sc.setUser(user.getName());
		return softwareConfigRepository.save(sc);
	}
	
	@RequestMapping(value = "/exec/start/{id}", method = RequestMethod.GET)
	public boolean start(HttpSession session, @PathVariable String id,Principal user) {
		SoftwareConfig config = softwareConfigRepository.findOne(id);
		if(config.getUser().equals(user.getName())){
			TestExecution exec = new TestExecution();
			exec.setSoftware(config);
			exec.setExecutionDate(Calendar.getInstance().getTime());
			session.setAttribute("exec", exec);
			session.setAttribute("set", true);
			return true;
		}
		else{
			return false;
		}
	}
	
	@RequestMapping(value = "/exec/tc/{id}", method = RequestMethod.GET)
	public Counts add(HttpSession session, @PathVariable String id,Principal user) throws UnresolvableDate {
		TestExecution exec = (TestExecution) session.getAttribute("exec");
		if(exec == null || !testPlanRepository.tcUser(id).getUser().equals(user.getName())){
			return null;
		}
		else {
			TestCase tc = testCaseRepository.findOne(id);
			Report report = execService.execute(exec.getSoftware(), tc, exec.getExecutionDate());
			exec.getReports().add(report);
			return new Counts(report.getEvents(),report.getForecasts());
		}
	}
	
	@RequestMapping(value = "/exec/collect", method = RequestMethod.GET)
	public TestExecution collect(HttpSession session, Principal user) {
		TestExecution exec = (TestExecution) session.getAttribute("exec");
		return exec;
	}
	
	@RequestMapping(value = "/exec/agg", method = RequestMethod.GET)
	public AggregateReport aggregate(HttpSession session, Principal user) {
		TestExecution exec = (TestExecution) session.getAttribute("exec");
		return aggregateService.aggregate(exec.getReports());
	}
	
	@RequestMapping(value = "/exec/clean", method = RequestMethod.GET)
	public boolean set(HttpSession session, Principal user) {
		session.setAttribute("exec", null);
		session.setAttribute("start", false);
		return true;
	}
	
}
