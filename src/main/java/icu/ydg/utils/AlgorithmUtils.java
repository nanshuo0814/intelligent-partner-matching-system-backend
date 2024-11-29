package icu.ydg.utils;

import java.util.List;

/**
 * 算法工具类
 *
 * @author dengxian
 */
public class AlgorithmUtils {

    /**
     * 计算两个字符串的编辑距离
     *
     * @param str1 str1
     * @param str2 str2
     * @return int
     */
    public static int getEditDistance(String str1, String str2) {
        int len1 = str1.length();
        int len2 = str2.length();

        // 创建二维数组 dp，其中 dp[i][j] 表示 str1[0..i-1] 和 str2[0..j-1] 之间的编辑距离
        int[][] dp = new int[len1 + 1][len2 + 1];

        // 初始化边界条件：将一个字符串转化为空字符串所需的操作次数
        for (int i = 0; i <= len1; i++) {
            dp[i][0] = i;  // 删除所有字符
        }
        for (int j = 0; j <= len2; j++) {
            dp[0][j] = j;  // 插入所有字符
        }

        // 填充 dp 数组，计算编辑距离
        for (int i = 1; i <= len1; i++) {
            for (int j = 1; j <= len2; j++) {
                if (str1.charAt(i - 1) == str2.charAt(j - 1)) {
                    dp[i][j] = dp[i - 1][j - 1];  // 如果字符相同，不需要操作
                } else {
                    dp[i][j] = Math.min(Math.min(dp[i - 1][j], dp[i][j - 1]), dp[i - 1][j - 1]) + 1;
                    // 插入、删除或替换字符，选择最小的操作
                }
            }
        }

        // 返回两个字符串之间的编辑距离
        return dp[len1][len2];
    }

    /**
     * 计算两个List之间的编辑距离
     *
     * @param list1 列表1
     * @param list2 列表2
     * @return int
     */
    public static int getListEditDistance(List<String> list1, List<String> list2) {
        int totalDistance = 0;

        // 遍历两个列表的元素，计算每对元素之间的编辑距离
        int len1 = list1.size();
        int len2 = list2.size();

        // 用两个指针分别指向两个列表
        for (int i = 0; i < len1; i++) {
            boolean foundMatch = false;
            int minDistance = Integer.MAX_VALUE;
            // 遍历第二个列表，找到最小编辑距离
            for (int j = 0; j < len2; j++) {
                int distance = getEditDistance(list1.get(i), list2.get(j));
                if (distance < minDistance) {
                    minDistance = distance;
                }
            }
            totalDistance += minDistance;
        }

        // 返回所有元素之间的总编辑距离
        return totalDistance;
    }

}

