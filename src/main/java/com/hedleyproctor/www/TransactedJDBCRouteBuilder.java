package com.hedleyproctor.www;

import org.apache.camel.builder.RouteBuilder;
import org.milyn.smooks.camel.dataformat.SmooksDataFormat;

public class TransactedJDBCRouteBuilder extends RouteBuilder {

	private CustomerDAO customerDAO;
	private AddressDAO addressDAO;
	
	public TransactedJDBCRouteBuilder(CustomerDAO customerDAO, AddressDAO addressDAO) {
		this.customerDAO = customerDAO;
		this.addressDAO = addressDAO;
	}
	
	@Override
	public void configure() throws Exception {
		
		customerDAO.initCustomerTable();
		addressDAO.initAddressTable();
		
		SmooksDataFormat sdf = new SmooksDataFormat("smooks-config.xml");
		
		
		// read in customer data from a file, one customer per line
		from("file:c:/camel-transactions")
		.log("Picked up input file")
		// split each line of the file and convert it to an xml message using smooks
		.split().tokenize("\n")
			.log("Processing file line ${property.CamelSplitIndex} with content: ${body}")
			.unmarshal(sdf)
			.log("After unmarshal, message is ${body}")
			.to("activemq:customers.xml")
		.end()
		.log("Done reading input file");
		
		from("activemq:customers.xml")
		.id("storeCustomerInDB")
		.transacted()
		.log("Storing customer info in DB")
		.setHeader("id",xpath("/customerList/customer/id/text()"))
		.setHeader("firstName",xpath("/customerList/customer/firstName/text()"))
		.setHeader("lastName",xpath("/customerList/customer/lastName/text()"))
		.to("bean:customerDAO?method=storeCustomer")
		.setHeader("street",xpath("/customerList/customer/street/text()"))
		.setHeader("town",xpath("/customerList/customer/town/text()"))
		.setHeader("postcode",xpath("/customerList/customer/postcode/text()"))
		.to("bean:addressDAO?method=storeAddress");


	}

}
