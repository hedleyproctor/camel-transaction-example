package com.hedleyproctor.www;

import java.sql.Types;

import org.apache.camel.Header;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.support.TransactionSynchronizationManager;

public class AddressDAO {
	private static final Logger log = LoggerFactory.getLogger(CustomerDAO.class);
	
	private JdbcTemplate jdbcTemplate;
	
	public AddressDAO(JdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}
	
	public void initAddressTable() {
		log.info("Creating address table");
		jdbcTemplate.execute("create table if not exists address(street varchar(30), town varchar(20), postcode varchar(8))");
	}
	
	public void storeAddress(@Header("street") String street, @Header("town") String town, @Header("postcode") String postcode) {
		log.info("Adding address record to database. street: " + street + " town: " + town + " postcode: " + postcode);
		log.info("Are we inside a transaction? " + TransactionSynchronizationManager.isActualTransactionActive());
		jdbcTemplate.update("insert into address (street,town,postcode) values (?,?,?)", 
				new Object[]{street,town,postcode},
				new int[]{Types.VARCHAR,Types.VARCHAR,Types.VARCHAR});

	}

}
