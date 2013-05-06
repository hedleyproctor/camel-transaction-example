package com.hedleyproctor.www;

import static org.junit.Assert.assertEquals;

import java.net.ConnectException;
import java.sql.Types;

import org.apache.camel.CamelContext;
import org.apache.camel.Produce;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.RouteDefinition;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.AbstractJUnit4SpringContextTests;

@ContextConfiguration( locations = ("/META-INF/spring/spring-context-jdbc-transactions.xml") )
public class TransactedJDBCTest extends AbstractJUnit4SpringContextTests {

	private static final Logger log = LoggerFactory.getLogger(TransactedJDBCTest.class);
	
	@Produce( uri = "file:c:/camel-transactions")
	protected ProducerTemplate fileInput;
	
	@Autowired
	protected JdbcTemplate jdbcTemplate;
	
	@Autowired
	protected CamelContext camelContext;

	
	@Before
	public void setUp() {
		// empty the DB of previous test data
		jdbcTemplate.execute("delete from customer");
		jdbcTemplate.execute("delete from address");
	}
	
	@Test
	@DirtiesContext
	public void transactedJDBCTest_noError() throws InterruptedException {
		log.info("Starting transacted test with no DB problems");
		// send the test input to the file endpoint using the producer template
		fileInput.sendBody(TestConstants.DUMMY_FILE_CONTENTS);
		// give Camel a second to convert the data and put it in the DB
		Thread.sleep(2000);
		int customerCount = jdbcTemplate.queryForInt("select count(*) from customer where last_name = ?",
				new Object[]{"Smith"}, new int[]{Types.VARCHAR});
		assertEquals("Didn't get expected number of customer rows back",1,customerCount);
		int addressCount = jdbcTemplate.queryForInt("select count(*) from address where town = ?",
				new Object[]{"Springfield"}, new int[]{Types.VARCHAR});
		assertEquals("Didn't get expected number of address rows back",1,addressCount);

	}
	
	@Test
	@DirtiesContext
	public void transactedJDBCTest_withConnectionProblem() throws Exception {
		log.info("Starting transacted test with simulated connection problem");
		
		// simulate an error when we try to save the data, but only on the second save
		RouteBuilder routeBuilder = new RouteBuilder() {
			@Override
			public void configure() throws Exception {
				interceptSendToEndpoint("bean:addressDAO?method=storeAddress").throwException(new Exception("Problem storing data in DB"));
			}
		};
		
		RouteDefinition route = camelContext.getRouteDefinition("storeCustomerInDB");
		route.adviceWith(camelContext,routeBuilder);
		
		// send the test input to the file endpoint using the producer template
		fileInput.sendBody(TestConstants.DUMMY_FILE_CONTENTS);
		// give Camel a second to convert the data and put it in the DB
		Thread.sleep(2000);
		
		// the first insert should have been rolled back
		int resultCount = jdbcTemplate.queryForInt("select count(*) from customer where last_name = ?",
				new Object[]{"Smith"}, new int[]{Types.VARCHAR});
		assertEquals("Didn't get expected number of results back. Customer insert should have been rolled back",
				0,resultCount);
		
	}
	
}
