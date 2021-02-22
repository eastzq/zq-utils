/**  
 * @Title: MdWriter.java
 * @Package com.shine.tech.epdce.csveditor.multiexplorer
 * @Description 
 * @author zq
 * @date 2021年1月21日 上午11:24:39
 * @Copyright 
 */

package com.zq.utils.multiexplorer;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * @Description
 * @author zq
 * @date 2021年1月21日 上午11:24:39
 * @see
 * @since 2021年1月21日 上午11:24:39
 */
public class MdWriter {

    public MdWriter() {
    }

    public static void genMarkDown(List<Map<String, Object>> data, String filePath) throws IOException {
        File file = new File(filePath);
        if (file.exists()) {
            file.delete();
        }
        file.createNewFile();
        for (Map<String, Object> m : data) {
            String path = (String) m.get("filePath");
            path = "#### " + path + "";
            String checkResult = (String) m.get("checkResult") + "\n\n";
            if (StringUtils.isNotBlank(checkResult)) {
                FileUtils.write(file, path, true);
                FileUtils.write(file, checkResult, true);
            }
        }
    }
}
