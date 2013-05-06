package com.hedleyproctor.www;

import java.sql.Types;

import org.apache.camel.Header;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.support.TransactionSynchronizationManager;

public class CustomerDAO {

	private static final Logger log = LoggerFactory.getLogger(CustomerDAO.class);
	
	private JdbcTemplate jdbcTemplate;
	
	public CustomerDAO(JdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}
	
	public void initCustomerTable() {
		log.info("Creating customer table");
		jdbcTemplate.execute("create table if not exists customer(id varchar(8),first_name varchar(20), last_name varchar(20))");
	}
	
	public void storeCustomer(	@Header("id") Integer customerId, @Header("firstName") String firstName, @Header("lastName") String lastName) {
		log.info("Adding customer record to database. id: " + customerId + " first name: " + firstName + " last name: " + lastName);
		log.info("Are we inside a transaction? " + TransactionSynchronizationManager.isActualTransactionActive());
		jdbcTemplate.update("insert into customer (id,first_name,last_name) values (?,?,?)", 
				new Object[]{customerId,firstName,lastName},
				new int[]{Types.INTEGER,Types.VARCHAR,Types.VARCHAR});
	}
	
}
