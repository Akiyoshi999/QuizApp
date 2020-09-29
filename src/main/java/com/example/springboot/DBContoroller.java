package com.example.springboot;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

public class DBContoroller {
	private List<Quiz> quizzes = new ArrayList<>();
	private Logger logger = Logger.getLogger(DBContoroller.class);
	Map<Integer, Boolean> answerInfo = new HashMap<Integer, Boolean>();

	// 1.データベース・テーブルに接続する準備
	Connection con = null;
	Statement stmt = null;
	ResultSet result = null;

	// 2.接続文字列の設定
	String url = "jdbc:postgresql://localhost:5432/QUIZ_APP";
	String user = "postgres";
	String password = "oracle";

	/*
	 * DBを初期化する処理
	 */
	public void initialization(String initialTable) {
		tableUpdate("DELETE FROM " + initialTable);
		logger.info("DBの[" + initialTable + "]の初期化が完了しました");
	}

	// DBのテーブル確認メソッド
	public void tableShow(String proccess_sql) {
		try {
			logger.info("DBの確認処理[START]");
			con = DriverManager.getConnection(url, user, password);

			stmt = con.createStatement();
			String sql = proccess_sql;
			result = stmt.executeQuery(sql);
			logger.debug("SQL実行 :[" + sql + "]");

			while (result.next()) {
				int pid = result.getInt(1);
				String question = result.getString(2);
				boolean answer = result.getBoolean(3);
				System.out.println(pid + " " + question + " " + answer);
			}

		} catch (SQLException e) {
			e.printStackTrace();
			logger.error(e);
		} finally {

			// クローズ処理
			try {
				if (result != null) {
					result.close();
				}
				if (stmt != null) {
					stmt.close();
				}
				if (con != null) {
					con.close();
				}

			} catch (SQLException e) {
				e.printStackTrace();
				logger.error(e);
			}
		}
		logger.info("DBの確認処理 [END]");
	}

	/*
	 * String型のSQL分を引数にDBに対して更新を行う
	 */
	public void tableUpdate(String update_sql) {
		try {
			logger.info("DBの更新処理[START]");
			con = DriverManager.getConnection(url, user, password);

			stmt = con.createStatement();
			String sql = update_sql;
			stmt.executeUpdate(sql);
			logger.debug("DBの更新 :[" + sql + "]");

		} catch (SQLException e) {
			e.printStackTrace();
			logger.error(e);
		} finally {

			// クローズ処理
			try {
				if (result != null) {
					result.close();
				}
				if (stmt != null) {
					stmt.close();
				}
				if (con != null) {
					con.close();
				}
				logger.info("DBの更新処理 [END]");
			} catch (SQLException e) {
				e.printStackTrace();
				logger.error(e);
			}
		}
	}

	/*
	 * 複数のUpdateのSQL分を実行する
	 */
	public void multiUpdate(List<String> update_sql) {
		try {
			logger.info("DBの更新処理[START]");
			con = DriverManager.getConnection(url, user, password);

			stmt = con.createStatement();
			for (String sql : update_sql) {
				stmt.executeUpdate(sql);
			}
			logger.debug("実行したSQL文 :" + update_sql);
		} catch (SQLException e) {
			e.printStackTrace();
			logger.error(e);
		} finally {

			// クローズ処理
			try {
				if (result != null) {
					result.close();
				}
				if (stmt != null) {
					stmt.close();
				}
				if (con != null) {
					con.close();
				}
				logger.info("DBの更新処理 [END]");
			} catch (SQLException e) {
				e.printStackTrace();
				logger.error(e);
			}
		}
	}

	/*
	 * DBからデータを読み込む処理
	 */
	public List<Quiz> quizLoad() {
		try {
			logger.trace("DBからクイズデータの読込 [START]");
			con = DriverManager.getConnection(url, user, password);

			stmt = con.createStatement();
			String sql = "SELECT * from quiz_list";
			result = stmt.executeQuery(sql);

			while (result.next()) {
				Quiz quiz = new Quiz(result.getString(2), result.getBoolean(3));
				quizzes.add(quiz);
			}

		} catch (SQLException e) {
			e.printStackTrace();
			logger.error(e);
		} finally {

			// クローズ処理
			try {
				if (result != null) {
					result.close();
				}
				if (stmt != null) {
					stmt.close();
				}
				if (con != null) {
					con.close();
				}

			} catch (SQLException e) {
				e.printStackTrace();
				logger.error(e);
			}
		}
		logger.trace("quizzes = " + quizzes);
		logger.trace("DBからクイズデータの読込 [END]");
		return quizzes;
	}

	/*
	 * String型でうけたテーブルの
	 */
	public Map<Integer, Boolean> answerLoad(String table) {
		Map<Integer, Boolean> uploadMap = new HashMap<Integer, Boolean>();
		try {
			logger.trace("DBから回答済みデータの読込 [START]");
			con = DriverManager.getConnection(url, user, password);

			stmt = con.createStatement();
			String sql = "SELECT * from " + table;
			result = stmt.executeQuery(sql);

			while (result.next()) {
				uploadMap.put(result.getInt(1), result.getBoolean(3));
			}

		} catch (SQLException e) {
			e.printStackTrace();
			logger.error(e);
		} finally {

			// クローズ処理
			try {
				if (result != null) {
					result.close();
				}
				if (stmt != null) {
					stmt.close();
				}
				if (con != null) {
					con.close();
				}

			} catch (SQLException e) {
				e.printStackTrace();
				logger.error(e);
			}
		}
		logger.trace("uploadMap = " + uploadMap);
		logger.trace("DBから回答済みデータの読込 [END]");
		return uploadMap;
	}
}
