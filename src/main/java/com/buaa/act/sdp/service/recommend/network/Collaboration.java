package com.buaa.act.sdp.service.recommend.network;

import com.buaa.act.sdp.service.statistics.TaskScores;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * Created by yang on 2017/3/17.
 */
@Component
public class Collaboration {

    @Autowired
    private TaskScores taskScores;

    // 一个project内的协作统计
    public void countCollaboration(Map<String, Integer> index, int[][] colCount, int[] taskCount, double[][] colScores, List<Map<String, Double>> score) {
        Set<String> set = new HashSet<>();
        Map<String, Double> map;
        int m, n;
        for (int i = 0; i < score.size(); i++) {
            map = score.get(i);
            List<String> list = new ArrayList<>();
            if (map != null && !map.isEmpty()) {
                for (Map.Entry<String, Double> entry : map.entrySet()) {
                    if (index.containsKey(entry.getKey())) {
                        list.add(entry.getKey());
                        m = index.get(entry.getKey());
                        taskCount[m]++;
                        for (String user : set) {
                            n = index.get(user);
                            colCount[m][n]++;
                            colCount[n][m]++;
                            colScores[m][n] += entry.getValue();
                            colScores[n][m] += entry.getValue();
                        }
                    }
                }
                set.addAll(list);
            }
        }
    }

    // 计算worker之间协作强度
    public double[][] calCollaboration(int[][] colCount, int[] taskCount, double[][] colScores) {
        double[][] result = new double[colCount.length][colCount.length];
        int sum;
        for (int i = 0; i < colCount.length; i++) {
            for (int j = i; j < colCount.length; j++) {
                sum = taskCount[i] + taskCount[j];
                if (sum != 0) {
                    result[i][j] = 1.0 * colCount[i][j] / sum + colScores[i][j] / sum / 100;
                    result[j][i] = 1.0 * colCount[i][j] / sum + colScores[i][j] / sum / 100;
                } else {
                    result[i][j] = 0;
                    result[j][i] = 0;
                }
            }
        }
        return result;
    }

    public double[][] generateCollaboration(Map<String, Integer> index, List<List<Integer>> challenges) {
        Map<Integer, Map<String, Double>> scores = taskScores.getAllWorkerScores();
        int[][] colCount = new int[index.size()][index.size()];
        int[] taskCount = new int[index.size()];
        double[][] colScores = new double[index.size()][index.size()];
        for (List<Integer> list : challenges) {
            List<Map<String, Double>> score = new ArrayList<>(list.size());
            for (int taskId : list) {
                if (scores.containsKey(taskId)) {
                    score.add(scores.get(taskId));
                }
            }
            countCollaboration(index, colCount, taskCount, colScores, score);
        }
        return calCollaboration(colCount, taskCount, colScores);
    }
}
