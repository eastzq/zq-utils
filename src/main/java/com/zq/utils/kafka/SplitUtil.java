package com.zq.utils.kafka;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 分片工具类
 * @author zq
 *
 */
public class SplitUtil {
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
}
