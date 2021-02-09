/**
 *  类说明:LoadDllTestMain.java.
 *
 * <pre>
 * 修改日期        修改人      修改原因
 * Jun 9, 2020	    Lings 		新建
 * </pre>
 */
package com.zq.utils.test.cli;

/**
 *  类说明:LoadDllTestMain.java.
 *
 * <pre>
 * 修改日期        修改人      修改原因
 * Jun 9, 2020	    Lings   		新建
 * </pre>
 */
public class LoadDllTestMain {

	public static void main(String[] args) {
		try {
		    System.setProperty("jna.debug_load", "true");
		    System.setProperty("jna.library.path","D:\\ZQ\\eastzq\\utils\\src\\main\\resources");
		    System.out.println( System.getProperty("jna.library.path"));
			libIntefaceClass libClass = new libIntefaceClass();
			libClass.Initialize();
		} catch (Throwable e) {
			e.printStackTrace();
		}
	}
}
