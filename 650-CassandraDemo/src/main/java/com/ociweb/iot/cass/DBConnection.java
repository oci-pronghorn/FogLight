package com.ociweb.iot.cass;

import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Session;
import com.ociweb.iot.maker.StartupListener;

public class DBConnection implements StartupListener {

	
	private Cluster cluster;
	private Session session;

	public DBConnection(){
		
	}

	@Override
	public void startup() {
		
		cluster = Cluster.builder().addContactPoint("127.0.0.1").build();
		session = cluster.connect("demo");
		
		
		session.execute("INSERT INTO users (lastname, age, city, email, firstname) VALUES ('Jones', 35, 'Austin', 'bob@example.com', 'Bob')");
		
		
		ResultSet results = session.execute("SELECT * FROM users WHERE lastname='Jones'");
		for (Row row : results) {
		System.out.format("%s %d\n", row.getString("firstname"), row.getInt("age"));
		}
		
		
		session.execute("update users set age = 36 where lastname = 'Jones'");
	}
	
	
	
}
