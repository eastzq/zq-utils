/**
 *  类说明:libIntefaceClass.java.
 *
 * <pre>
 * 修改日期        修改人      修改原因
 * Jun 9, 2020	    Lings 		新建
 * </pre>
 */
package com.zq.utils.test.cli;

import com.sun.jna.Library;
import com.sun.jna.Native;
import com.sun.jna.Pointer;

/**
 *  类说明:libIntefaceClass.java.
 *
 * <pre>
 * 修改日期        修改人      修改原因
 * Jun 9, 2020	    Lings   		新建
 * </pre>
 */
public class libIntefaceClass {
	/**
	 * 
	 * 方法说明：调用dll.
	 * @return
	 * @throws Exception
	 *  <pre>
	 * 修改日期        修改人     修改原因
	 * 2016-3-11         潘超群       新建
	 * </pre>
	 */
	public interface FixApi extends Library {   
		
		FixApi instance = (FixApi) Native.loadLibrary("FixApi30", FixApi.class);
		public boolean Fix_Initialize();
		public boolean Fix_Uninitialize();
		public Long Fix_Connect(String pszAddr, String pszUser, String pszPwd, long timeOut);
		public boolean Fix_Close(Long handleConn);
		public Long Fix_AllocateSession(Long handleConn);
		public boolean Fix_ReleaseSession(Long handleConn);
		public boolean Fix_CreateReq(Long handleSession,Long funcCode);
		public boolean Fix_SetString(Long handleSession,Long paramId,String paramVal);
		public boolean Fix_SetLong(Long handleSession,Long paramId,Long paramVal);
		public boolean Fix_Run(Long handleSession);
		public Pointer[]Fix_GetErrMsg(Long handleSession, byte[] retVal, int retSize);
		public Long	Fix_GetCount(Long handleSession);
		public Pointer[]Fix_GetItem(Long handleSession, Long paramId, byte[] retVal, int retSize, long rowNum);
    }
	
	public boolean Initialize(){
		boolean reslut = FixApi.instance.Fix_Initialize();
		return reslut;
	}
}
