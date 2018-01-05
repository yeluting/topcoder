package com.buaa.act.sdp.service.statistics;

import com.buaa.act.sdp.dao.ChallengeItemDao;
import com.buaa.act.sdp.dao.ChallengeSubmissionDao;
import com.buaa.act.sdp.model.challenge.ChallengeItem;
import com.buaa.act.sdp.model.challenge.ChallengeSubmission;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * Created by yang on 2017/5/31.
 */
@Component
public class TaskMsg {

    @Autowired
    private ChallengeSubmissionDao challengeSubmissionDao;
    @Autowired
    private ChallengeItemDao challengeItemDao;
    @Autowired
    private MsgFilter msgFilter;
    @Autowired
    private TaskScores taskScores;

    //不同类型的challenges
    private List<ChallengeItem> codeItems;
    private List<ChallengeItem> assemblyItems;
    private List<ChallengeItem> f2fItems;

    //不同类型challenge 对应的winner
    private List<String> codeWinners;
    private List<String> assemblyWinners;
    private List<String> f2fWinners;

    // 选取任务的worker得分情况
    private List<Map<String, Double>> codeScore;
    private List<Map<String, Double>> assemblyScore;
    private List<Map<String, Double>> f2fScore;

    public TaskMsg() {
        codeItems = new ArrayList<>();
        assemblyItems = new ArrayList<>();
        f2fItems = new ArrayList<>();
        codeWinners = new ArrayList<>();
        assemblyWinners = new ArrayList<>();
        f2fWinners = new ArrayList<>();
        codeScore = new ArrayList<>();
        assemblyScore = new ArrayList<>();
        f2fScore = new ArrayList<>();
    }

    public void initCode() {
        if (codeItems.isEmpty()) {
            synchronized (codeItems) {
                if (codeItems.isEmpty()) {
                    getWinnersAndScores("Code", codeItems, codeWinners, codeScore);
                }
            }
        }
    }

    public void initF2f() {
        if (f2fItems.isEmpty()) {
            synchronized (f2fItems) {
                if (f2fItems.isEmpty()) {
                    getWinnersAndScores("First2Finish", f2fItems, f2fWinners, f2fScore);
                }
            }
        }
    }

    public void initAssembly() {
        if (assemblyItems.isEmpty()) {
            synchronized (assemblyItems) {
                if (assemblyItems.isEmpty()) {
                    getWinnersAndScores("Assembly Competition", assemblyItems, assemblyWinners, assemblyScore);
                }
            }
        }
    }

    public List<ChallengeItem> getChallenges(Set<Integer> set) {
        List<ChallengeItem> items = new ArrayList<>(set.size());
        if (codeItems.isEmpty()) {
            initCode();
        }
        for (ChallengeItem item : codeItems) {
            if (set.contains(item.getChallengeId())) {
                items.add(item);
            }
        }
        if (f2fItems.isEmpty()) {
            initF2f();
        }
        for (ChallengeItem item : f2fItems) {
            if (set.contains(item.getChallengeId())) {
                items.add(item);
            }
        }
        if (assemblyItems.isEmpty()) {
            initAssembly();
        }
        for (ChallengeItem item : assemblyItems) {
            if (set.contains(item.getChallengeId())) {
                items.add(item);
            }
        }
        return items;
    }

    public List<ChallengeItem> getItems(String type) {
        if (type.equals("Code")) {
            if (codeItems.isEmpty()) {
                initCode();
            }
            return codeItems;
        } else if (type.equals("First2Finish")) {
            if (f2fItems.isEmpty()) {
                initF2f();
            }
            return f2fItems;
        } else {
            if (assemblyItems.isEmpty()) {
                initAssembly();
            }
            return assemblyItems;
        }
    }

    public List<String> getWinners(String type) {
        if (type.equals("First2Finish")) {
            if (f2fWinners.isEmpty()) {
                initF2f();
            }
            return f2fWinners;
        } else if (type.equals("Code")) {
            if (codeWinners.isEmpty()) {
                initCode();
            }
            return codeWinners;
        } else {
            if (assemblyWinners.isEmpty()) {
                initAssembly();
            }
            return assemblyWinners;
        }
    }

    public List<Map<String, Double>> getUserScore(String type) {
        if (type.equals("Assembly Competition")) {
            if (assemblyScore.isEmpty()) {
                initAssembly();
            }
            return assemblyScore;
        } else if (type.equals("Code")) {
            if (codeScore.isEmpty()) {
                initCode();
            }
            return codeScore;
        } else {
            if (f2fScore.isEmpty()) {
                initF2f();
            }
            return f2fScore;
        }
    }

    // 从所有的任务中进行筛选，过滤出一部分任务，计算winner、tasks，以及开发者所得分数
    public void getWinnersAndScores(String challengeType, List<ChallengeItem> items, List<String> winners, List<Map<String, Double>> userScore) {
        List<ChallengeSubmission> list = challengeSubmissionDao.getChallengeWinner();
        Map<String, Integer> map = new HashMap<>();
        Set<Integer> challengeSet = new HashSet<>();
        Map<Integer, String> user = new HashMap<>();
        Set<Integer> set = new HashSet<>();
        ChallengeItem challengeItem;
        List<ChallengeItem> challengeItems = new ArrayList<>();
        for (ChallengeSubmission challengeSubmission : list) {
            if (set.contains(challengeSubmission.getChallengeID())) {
                continue;
            }
            if (challengeSet.contains(challengeSubmission.getChallengeID())) {
                if (challengeSubmission.getPlacement() != null && challengeSubmission.getPlacement().equals("1") && Double.parseDouble(challengeSubmission.getFinalScore()) >= 80) {
                    user.put(challengeSubmission.getChallengeID(), challengeSubmission.getHandle());
                }
            } else {
                challengeItem = challengeItemDao.getChallengeItemById(challengeSubmission.getChallengeID());
                if (msgFilter.filterChallenge(challengeItem, challengeType)) {
                    challengeSet.add(challengeItem.getChallengeId());
                    challengeItems.add(challengeItem);
                    if (challengeSubmission.getPlacement() != null && challengeSubmission.getPlacement().equals("1") && Double.parseDouble(challengeSubmission.getFinalScore()) >= 80) {
                        user.put(challengeSubmission.getChallengeID(), challengeSubmission.getHandle());
                    }
                } else {
                    set.add(challengeSubmission.getChallengeID());
                }
            }
        }
        for (Map.Entry<Integer, String> entry : user.entrySet()) {
            if (map.containsKey(entry.getValue())) {
                map.put(entry.getValue(), map.get(entry.getValue()) + 1);
            } else {
                map.put(entry.getValue(), 1);
            }
        }
        Map<Integer, Map<String, Double>> scores = taskScores.getAllWorkerScores();
        for (int i = 0; i < challengeItems.size(); i++) {
            String win = user.get(challengeItems.get(i).getChallengeId());
            if (map.containsKey(win) && map.get(win) >= 5) {
                items.add(challengeItems.get(i));
                winners.add(win);
                userScore.add(scores.get(challengeItems.get(i).getChallengeId()));
            }
        }
        Set<String> sets = new HashSet<>(winners);
        System.out.println(winners.size() + "\t" + sets.size());
    }

}
