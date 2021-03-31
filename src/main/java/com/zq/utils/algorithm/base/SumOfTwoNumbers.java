package com.zq.utils.algorithm.base;

import java.util.HashMap;
import java.util.Map;

/**
 * 两数之和 leetCode算
 给定一个整数数组 nums 和一个整数目标值 target，请你在该数组中找出 和为目标值 的那 两个 整数，并返回它们的数组下标。

 你可以假设每种输入只会对应一个答案。但是，数组中同一个元素在答案里不能重复出现。

 你可以按任意顺序返回答案。



 示例 1：

 输入：nums = [2,7,11,15], target = 9
 输出：[0,1]
 解释：因为 nums[0] + nums[1] == 9 ，返回 [0, 1] 。
 示例 2：

 输入：nums = [3,2,4], target = 6
 输出：[1,2]
 示例 3：

 输入：nums = [3,3], target = 6
 输出：[0,1]

 */
public class SumOfTwoNumbers {

    public static void main(String[] args) {
        int[] arr = new int[]{1,12,3,44};
        int a = 15;
        Map<Integer,Integer> indexMap = new HashMap<>();
        for(int i=0;i<arr.length;i++){
            if(indexMap.containsKey(a-arr[i])){
                System.out.println(i+","+indexMap.get(a-arr[i]));
                break;
            }
            //相当于从后往前遍历，a和a的补数计算是相互的只需一次即可。
            indexMap.put(arr[i],i);
        }
    }
}
