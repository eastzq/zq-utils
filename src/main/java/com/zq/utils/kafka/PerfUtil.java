package com.zq.utils.kafka;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;

import com.alibaba.fastjson.JSONObject;

public class PerfUtil {

	public static String splitRegex = "globalMsgSplitConfig.*\\[(?<value>.*)\\]";

	public static String keyRegex = "(?<key>readDbf|sendMsg|operateRule|operateBcp|operateRedis|consumeMsg|loadBcp)_globalId.*\\[(?<value>.*)\\]";

	public static String rawFilter = "(总计total|globalMsgSplitConfig|readDbf|sendMsg|operateRule|operateBcp|operateRedis|consumeMsg|loadBcp).*\\[.*\\]";

	public static String totalRegex = "总计total_.*\\[(?<value>.*)\\]";

	public static final int ADVICE_NUM = 8;

	private static ExecutorService cachedThreadPool = new ThreadPoolExecutor(ADVICE_NUM, ADVICE_NUM, 0,
			TimeUnit.MILLISECONDS, new LinkedBlockingQueue<Runnable>());

	public static void main(String[] args) throws IOException {
		// 日志路径
		File logDir = new File("D:/data/");
		String content = "";
		if (!logDir.exists()) {
			return;
		}
		if (logDir.isFile()) {
			content = FileUtils.readFileToString(logDir);
		} else {
			for (File file : logDir.listFiles()) {
				if (file.getName().endsWith(".log")) {
					content += FileUtils.readFileToString(file);
				}
			}
		}
		if (StringUtils.isBlank(content)) {
			return;
		}

		Pattern rawPattern = Pattern.compile(rawFilter);
		Matcher rawMatcher = rawPattern.matcher(content);
		StringBuilder builder = new StringBuilder();
		while (rawMatcher.find()) {
			builder.append(rawMatcher.group()).append("\n");
		}
		content = builder.toString();

		Pattern totalPattern = Pattern.compile(totalRegex);
		Matcher totalMacher = totalPattern.matcher(content);
		List<Long> timestamps = new ArrayList<>();
		while (totalMacher.find()) {
			Long time = Long.valueOf(totalMacher.group("value"));
			timestamps.add(time);
		}

		Long startTimestamp = 0L;
		Long endTimestamp = 0L;
		if (timestamps.size() > 0) {
			Collections.sort(timestamps);
			startTimestamp = timestamps.get(0);
			endTimestamp = timestamps.get(timestamps.size() - 1);
		}
		Long totalCost = endTimestamp - startTimestamp;

		List<String> titles = new ArrayList<String>();
//		titles.add("配置项：1机四核/4线程发送/4线程接收处理/分片大小1024/数据量大小400M/总条数4820000");
//		titles.add("配置项：1机四核/8线程发送/8线程接收处理/分片大小1024/数据量大小400M/总条数4820000");
//		titles.add("配置项：1机四核，1机四核/8线程发送/16线程接收处理/分片大小1024/数据量大小400M/总条数4820000");
		titles.add("配置项：1机四核，1机四核/8线程发送/8线程接收处理/4线程处理bcp文件/分片大小1024/数据量大小400M/总条数4820000");
		titles.add("开始时间：" + startTimestamp);
		titles.add("结束时间：" + endTimestamp);
		titles.add("总计耗时：" + totalCost);

		Pattern splitPattern = Pattern.compile(splitRegex);
		Matcher configMatcher = splitPattern.matcher(content);
		List<Map<String, Object>> data = new ArrayList<>();
		int count = 0;
		while (configMatcher.find()) {
			/*
			 * if (count > 30) { break; }
			 */
			String config = configMatcher.group("value");
			Map<String, Object> dataMap = JSONObject.parseObject(config);
			data.add(dataMap);
			count++;
		}
		List<List<Map<String, Object>>> splitDatas = splitConfigsByBatchNum(data, 5);
		List<Future<?>> futures = new ArrayList<>();
		for (List<Map<String, Object>> splitData : splitDatas) {
			Future<?> future = cachedThreadPool.submit(new LogScanRunner(content, splitData));
			futures.add(future);
		}

		for (Future<?> future : futures) {
			try {
				future.get();
			} catch (InterruptedException e) {
				e.printStackTrace();
			} catch (ExecutionException e) {
				e.printStackTrace();
			}
		}

		if (data.size() > 0) {
			List<String> columns = new ArrayList<String>();
			columns.add("globalId");
			columns.add("intfCode");
			columns.add("filepath");
			columns.add("columns");
			columns.add("batchId");
			columns.add("total");
			columns.add("startLine");
			columns.add("endLine");
			columns.add("sendMsg");
			columns.add("readDbf");
			columns.add("consumeMsg");
			columns.add("operateRule");
			columns.add("operateBcp");
			columns.add("operateRedis");
			columns.add("loadBcp");
			genExcel(titles, columns, data);
			System.out.println("已经生成excel");
		}
	}

	/**
	 * 按分组数目分组，少于分组数，按每组一个分。分组数等于个数
	 * @param <T>
	 * @param list
	 * @param adviceNum
	 * @return
	 */
	public static <T> List<List<T>> splitConfigsByAdviceNum(List<T> list, int adviceNum) {
		int total = list.size();
		int batchNum = total / adviceNum;
		List<List<T>> sl = null;
		if (batchNum == 0) {
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

	/**
	 * by 按批次数目分组
	 * @param <T>
	 * @param list
	 * @param batchNum
	 * @return
	 */
	public static <T> List<List<T>> splitConfigsByBatchNum(List<T> list, int batchNum) {
		int total = list.size();
		List<List<T>> sl = new ArrayList<>();
		int adviceNum = total / batchNum;
		int y = total % batchNum;
		if (y > 0) {
			adviceNum++;
		}
		for (int i = 0; i < adviceNum; i++) {
			List<T> splitList = new ArrayList<>();
			for (int j = i * batchNum; j < (i + 1) * batchNum && j < total; j++) {
				splitList.add(list.get(j));
			}
			sl.add(splitList);
		}
		return sl;
	}

	public static void genExcel(List<String> titles, List<String> columns, List<Map<String, Object>> data)
			throws IOException {
		ExcelGenerator gen = new ExcelGenerator();
		gen.createTitles(titles);
		gen.createColumns(columns);
		gen.appendDataSet(data);
		String path = "D:/data/pf.xlsx";
		File file = new File(path);
		if (file.exists()) {
			file.delete();
		}
		gen.write(path);
	}

	static class LogScanRunner implements Callable<List<Map<String, Object>>> {
		private String content;
		private List<Map<String, Object>> data;

		public LogScanRunner(String content, List<Map<String, Object>> data) {
			this.content = content;
			this.data = data;
		}

		@Override
		public List<Map<String, Object>> call() throws Exception {
			for (Map<String, Object> dataMap : data) {
				String globalId = (String) dataMap.get("globalId");
				System.out.println("开始处理：" + globalId);
				String tRegex = keyRegex.replace("globalId", globalId);
				Pattern totalPattern = Pattern.compile(tRegex);
				Matcher totalMatcher = totalPattern.matcher(content);
				while (totalMatcher.find()) {
					dataMap.put(totalMatcher.group("key"), Integer.valueOf(totalMatcher.group("value")));
				}
				System.out.println("处理完成：" + globalId);
			}
			return data;
		}

	}
}
