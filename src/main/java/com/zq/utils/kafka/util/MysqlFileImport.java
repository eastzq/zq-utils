package com.zq.utils.kafka.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.zq.utils.cli.ExecuteResult;
import com.zq.utils.cli.ProcessBuilderCommandExecutor;
import com.zq.utils.cli.intf.CommandExecutor;

public class MysqlFileImport {

	public static final String PATH = "/data/clear/bcpFile";
	public static final String TEMP_SQL_PATH = "/data/clear/bcpFile/sqlFile";

	public static int adviceNum = 4;
	public static boolean isBcpLoadByJdbc = false;


	public static final String SUCCESS_DIR = "sucessDir";
	public static final String FAIL_DIR = "failDir";
	public static final String SQL_DIR = "sqlDir";

	private static final Logger logger = LoggerFactory.getLogger(MysqlFileImport.class);

	public static ExecutorService bcpLoadThreadPool = (ExecutorService) Executors.newFixedThreadPool(adviceNum);

	static {
		try {
			Class.forName("com.mysql.jdbc.Driver");
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
	}


	public static void loadByJdbc(String filePath) throws Exception {
		if (filePath == null || filePath.equals("")) {
			return;
		}
		Connection conn = null;
		Statement statement = null;
		try {
			conn = getConnection();
			String t = "LOAD DATA local INFILE '" + filePath.replaceAll("\\\\", "/")
					+ "' IGNORE INTO TABLE mysql_load_test FIELDS TERMINATED BY ',' ENCLOSED BY '' LINES TERMINATED BY '\\n' IGNORE 1 LINES (SEAT_CODE,TRAN_ACCOUNT,PROC_STATUS,MKT_GROUP,ZJZH,SETTLE_DEPT_CODE,MEDIA_TYPE,MKT_CODE,SETTLE_ENTITY_ID,INV_ACC,TRADE_ACCOUNT)";
			// 执行语句
			logger.debug("执行语句：{}", t);
			statement = conn.createStatement();
			statement.executeUpdate(t);
			move2Success(new File(filePath));
		} catch (Exception e) {
			move2Fail(new File(filePath));
			throw e;
		} finally {
			if (statement != null) {
				try {
					statement.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
			if (conn != null) {
				try {
					conn.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
		}
	}

	public static void main(String[] args) throws Exception {
		loadByCli("/data/clear/bcpFile/sqlFile/ffff227f2dc745439415991de8f2a6a2.bcp.sql");
	}
	
	public static void loadByCli(String filePath) throws Exception {
//		String commandPrefix = "mysqlimport -u root -proot -P 3307 -L -h 10.168.0.196 test ";
//		String t = filePath.replaceAll("\\\\", "/")+ " --fields-enclosed-by=\"\" --fields-terminated-by=, --lines-terminated-by=\\n --ignore-lines=1 --columns=SEAT_CODE,TRAN_ACCOUNT,PROC_STATUS,MKT_GROUP,ZJZH,SETTLE_DEPT_CODE,MEDIA_TYPE,MKT_CODE,SETTLE_ENTITY_ID,INV_ACC,TRADE_ACCOUNT";
//		String command = commandPrefix + t;

		String t = "LOAD DATA local INFILE '" + filePath.replaceAll("\\\\", "/")
				+ "' IGNORE INTO TABLE mysql_load_test FIELDS TERMINATED BY ',' ENCLOSED BY '' LINES TERMINATED BY '\\n' IGNORE 1 LINES (SEAT_CODE,TRAN_ACCOUNT,PROC_STATUS,MKT_GROUP,ZJZH,SETTLE_DEPT_CODE,MEDIA_TYPE,MKT_CODE,SETTLE_ENTITY_ID,INV_ACC,TRADE_ACCOUNT);";
		String fileName = new File(filePath).getName();
		File sqlFile = new File(TEMP_SQL_PATH + File.separator + fileName + ".sql");
		FileUtils.writeStringToFile(sqlFile, t, "UTF-8");

		String command = "mysql -h 10.168.0.196 -P 3307 -uroot -D test -proot <" + sqlFile.getPath();
		CommandExecutor ce = new ProcessBuilderCommandExecutor("GBK");
		logger.debug("准备调用命令：{}", command);

		ExecuteResult er = ce.executeCommand(command);
		if (er.getExitCode() == 0) {
			logger.debug(er.getExecuteOut());
//			move2Success(new File(filePath));
		} else {
//			move2Fail(new File(filePath));
			logger.error(er.getExecuteOut());
			throw new Exception("执行导入命令异常！原因：" + er.getExecuteOut());
		}
	}

	public static void genSqlFile(List<File> files) {

	}

	public static <T> List<List<T>> splitConfigs(List<T> list, int adviceNum) {
		int total = list.size();
		int st = total / adviceNum;
		List<List<T>> sl = null;
		if (st == 0) {
			sl = new ArrayList<>(Collections.nCopies(total, null));
		} else {
			sl = new ArrayList<>(Collections.nCopies(adviceNum, null));
		}
		for (int i = 0; i < list.size(); i++) {
			int t = i % adviceNum;
			if (sl.get(t) == null) {
				List<T> tl = new ArrayList<>();
				sl.set(t, tl);
			}
			sl.get(t).add(list.get(i));
		}
		return sl;
	}

	private static void move2Fail(File file) {
		File failPath = new File(file.getParent() + File.separator + FAIL_DIR + File.separator + file.getName());
		File failDir = failPath.getParentFile();
		if (!failDir.exists()) {
			failDir.mkdirs();
		}
		file.renameTo(failPath);
	}

	private static void move2Success(File file) {
		File successPath = new File(file.getParent() + File.separator + SUCCESS_DIR + File.separator + file.getName());
		File successDir = successPath.getParentFile();
		if (!successDir.exists()) {
			successDir.mkdirs();
		}
		file.renameTo(successPath);
	}

	private static List<File> listIntfSourceFile(String dirPath) throws FileNotFoundException {
		File dir = new File(dirPath);
		if (!dir.exists()) {
			throw new FileNotFoundException("文件不存在:" + dirPath);
		}
		if (!dir.isDirectory()) {
			throw new FileNotFoundException("当前文件并非目录，请指定一个目录！");
		}
		File[] files = dir.listFiles();
		List<File> fileList = new ArrayList<File>();
		for (int i = 0; i < files.length; i++) {
			File file = files[i];
			if (!file.isDirectory()) {
				fileList.add(file);
			}
		}
		return fileList;
	}

	public static Connection getConnection() throws Exception {
//		Class.forName("com.mysql.jdbc.Driver");
		String url = "jdbc:mysql://10.168.0.196:3307/test";
		String user = "root";
		String password = "root";
		Connection conn = DriverManager.getConnection(url, user, password);
		return conn;
	}

	public static String readFirstLine(File file) {
		if (file == null) {
			return "";
		}
		String text = "";
		BufferedReader br = null;
		try {
			br = new BufferedReader(new FileReader(file));
			text = br.readLine();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (br != null) {
				try {
					br.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return text;
	}

	public static void handle() throws Exception {
		while (true) {
			List<File> allFiles = listIntfSourceFile(PATH);
			if (allFiles == null || allFiles.size() == 0) {
				logger.debug("未找到任何文件，3s后重新检查...");
				Thread.sleep(3000);
				continue;
			}
			logger.info("总计total_bcp开始导入开始时间：[{}]", System.currentTimeMillis());
			List<List<File>> filesList = splitConfigs(allFiles, adviceNum);
			List<Future<Boolean>> futures = new ArrayList<>();
			for (List<File> files : filesList) {
				futures.add(bcpLoadThreadPool.submit(new BcpLoadRunner(files)));
			}
			for (Future<Boolean> future : futures) {
				future.get();
			}
			logger.info("总计total_bcp开始导入结束时间：[{}]", System.currentTimeMillis());
			Thread.sleep(500);
		}
	}

	public static class BcpLoadRunner implements Callable<Boolean> {

		private List<File> files;

		public BcpLoadRunner(List<File> files) {
			this.files = files;
		}

		@Override
		public Boolean call() throws Exception {
			if (files == null || files.size() == 0) {
				return true;
			}
			for (File file : files) {
				String fileName = file.getName();
				String msgId = fileName.substring(0, fileName.lastIndexOf("."));
				logger.debug("准备导入{}", file.getPath());
				String loadBcpKey = String.format("loadBcp_%s", msgId);
				try {
					TimeTotalUtil.addStartTimestamp(loadBcpKey);
					if (isBcpLoadByJdbc) {
						loadByJdbc(file.getPath());
					} else {
						loadByCli(file.getPath());
					}
					TimeTotalUtil.showIntevalTime(loadBcpKey, "导入bcp文件到mysql消耗时间");
					logger.debug("导入成功！");
				} finally {
					TimeTotalUtil.clearKey(loadBcpKey);
				}
			}
			return true;
		}
	}

}
