package com.app.superxlcr.mypaintboard.model;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

/**
 * 数据库，单件
 * @author superxlcr
 *
 */
public class Database {

	// 数据库端口
	private static final int port = 3306;
	// 数据库名称
	private static final String databaseName = "MyPaintBoardDB";
	// 数据库连接地址
	private static final String mysqlConnectStr = "jdbc:mysql://localhost:" + port + "/" + databaseName
			+ "?autoReconnect=true&useUnicode=true&characterEncoding=UTF-8";
	// 数据库用户名
	private static final String mysqlAccount = "root";
	// 数据库密码
	private static final String mysqlPassword = "root";

	private static Database instance = new Database();

	public static Database getInstance() {
		return instance;
	}

	// 数据库连接
	private Connection conn = null;

	private Database() {
		try {
			Class.forName("com.mysql.jdbc.Driver");
			conn = DriverManager.getConnection(mysqlConnectStr, mysqlAccount, mysqlPassword);
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(0);
		}
	}

	public boolean execute(String sql) {
		try {
			Statement statement = conn.createStatement();
			statement.execute(sql);
			return true;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}
	
	public ResultSet executeQuery(String sql) {
		try {
			Statement statement = conn.createStatement();
			return statement.executeQuery(sql);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
}
