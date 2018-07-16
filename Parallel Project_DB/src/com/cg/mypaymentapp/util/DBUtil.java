package com.cg.mypaymentapp.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBUtil 
{
	public static Connection getConnection() throws ClassNotFoundException, SQLException
	{
		Connection con=null;
		Class.forName("oracle.jdbc.driver.OracleDriver");
		con=DriverManager.getConnection("jdbc:oracle:thin:@10.51.103.201:1521:orcl11g","lab1ctrg1","lab1coracle");
	return con;
	}
	}
