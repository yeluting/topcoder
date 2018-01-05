package com.buaa.act.sdp.service.api;

import com.buaa.act.sdp.dao.ChallengeSubmissionDao;
import com.buaa.act.sdp.dao.UserDao;
import com.buaa.act.sdp.model.user.User;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.sql.*;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

/**
 * Created by YLT on 2017/6/12.
 */
@Component
public class DataHandle {

    @Autowired
    UserDao userDao;

    @Autowired
    ChallengeSubmissionDao challengeSubmissionDao;

    public void abilityInsert() {
        HashMap<String, String> ability = new HashMap<String, String>();
        ArrayList<User> allUser = (ArrayList<User>) userDao.getAllUsers();
        for (User u : allUser
                ) {
            if (u.getSkillDegree() != null) {
                JSONObject skillDegree = new JSONObject(u.getSkillDegree());
                JSONArray communicationJsonArray = (JSONArray) skillDegree.get("communication");
                JSONObject CompleteJson = (JSONObject) communicationJsonArray.get(0);
                int winNum = 0, loseNum = 0, collaborNum = 0;
                Iterator iter1 = CompleteJson.keys();
                while (iter1.hasNext()) {
                    String[] vs = CompleteJson.get(iter1.next().toString()).toString().split(":");
                    winNum = winNum + Integer.parseInt(vs[0]);
                    loseNum = loseNum + Integer.parseInt(vs[1]);
                }
                JSONObject CollaborJson = (JSONObject) communicationJsonArray.get(1);
                Iterator iter2 = CollaborJson.keys();
                while (iter2.hasNext()) {
                    collaborNum = collaborNum + Integer.parseInt(CollaborJson.get(iter2.next().toString()).toString());
                }
                skillDegree.remove("communication");
                JSONObject collaboration = new JSONObject();
                collaboration.put("winNum", winNum);
                collaboration.put("loseNum", loseNum);
                collaboration.put("collaborNum", collaborNum);
                skillDegree.put("collaborations", collaboration);
                ability.put(u.getHandle(), skillDegree.toString());
                System.out.println(u.getId());
            }
        }
        userDao.insertabilityBatch(ability);
    }

    public void skillDegreeToOne() {
        ArrayList<Integer> userIds = (ArrayList<Integer>) userDao.getAllUsersId();
        HashMap<String, Double> skillToOneMax = new HashMap<>();
        HashMap<String, Double> skillToOneMin = new HashMap<>();
        for (int id : userIds
                ) {
            System.out.println(id);
            User user = userDao.getUserById(id);
            String abilityStr = user.getAbility();
            if (abilityStr != null) {
                JSONObject abilityJson = new JSONObject(abilityStr);
                JSONObject skillJson = abilityJson.getJSONObject("skill");
                Iterator iterator = skillJson.keys();
                while (iterator.hasNext()) {
                    String key = (String) iterator.next();
                    if ((skillToOneMax.containsKey(key) && skillToOneMax.get(key) < skillJson.getDouble(key)) || !skillToOneMax.containsKey(key)) {
                        skillToOneMax.put(key, skillJson.getDouble(key));
                    }
                    if ((skillToOneMin.containsKey(key) && skillToOneMin.get(key) > skillJson.getDouble(key) || !skillToOneMin.containsKey(key))) {
                        skillToOneMin.put(key, skillJson.getDouble(key));
                    }
                }
            }
        }

        for (int id : userIds) {
            System.out.println(id);
            User user = userDao.getUserById(id);
            String abilityStr = user.getAbility();
            if (abilityStr != null) {
                JSONObject abilityJson = new JSONObject(abilityStr);
                JSONObject skillJson = abilityJson.getJSONObject("skill");
                JSONObject skillToOneJson = new JSONObject();
                Iterator iterator = skillJson.keys();
                while (iterator.hasNext()) {
                    String key = (String) iterator.next();
                    double max = skillToOneMax.get(key);
                    double min = skillToOneMin.get(key);
                    double valueToOne;
                    if(max > min){
                         valueToOne = ((skillJson.getDouble(key) - min) / ( max- min)) * 100;
                    }else {
                         valueToOne = skillJson.getDouble(key);
                    }
                    DecimalFormat df = new DecimalFormat("#.####");
                    skillToOneJson.put(key, Double.parseDouble(df.format(valueToOne)));
                }
                abilityJson.put("skillToOne", skillToOneJson);
                userDao.insertSkillDegreeToOne(abilityJson.toString(), id);
            }else{
                userDao.insertSkillDegreeToOne("{\"contribution\":{\"SubNum\":0,\"WinNum\":0,\"RegNum\":0},\"collaborations\":{\"winNum\":0,\"collaborNum\":0,\"loseNum\":0},\"skill\":{},\"skillToOne\":{}}",id);
            }
        }
    }

    public void relationshipTotal() {
        List<String[]> allCompleteRelation = new ArrayList<String[]>();
        List<String[]> allCollaborateRelation = new ArrayList<String[]>();
        String login = "neo4j";
        String password = "123456";
        Connection con = null;
        int num = 0;
        try {
            con = DriverManager.getConnection("jdbc:neo4j:http://192.168.7.109:7474/", login, password);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        String query = "MATCH (u:User) RETURN u.skillDegree,u.handle";
        PreparedStatement stmt = null;
        try {
            stmt = con.prepareStatement(query);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                JSONObject jsonObject = new JSONObject(rs.getString("u.skillDegree"));
                String handle = rs.getString("u.handle");
                JSONArray communication = jsonObject.getJSONArray("communication");
                JSONObject completeJson = communication.getJSONObject(0);
                JSONObject collaborateJson = communication.getJSONObject(1);
                Iterator it = completeJson.keys();
                while (it.hasNext()) {
                    String key = (String) it.next();
                    String value = completeJson.getString(key);
                    String[] vs = value.split(":");
                    String[] relation = {handle, key, value, String.valueOf(Integer.parseInt(vs[0]) + Integer.parseInt(vs[1]))};
                    allCompleteRelation.add(relation);
                    System.out.println(relation[2]);
                }
                Iterator it1 = collaborateJson.keys();
                while (it1.hasNext()) {
                    String key = (String) it1.next();
                    int value = collaborateJson.getInt(key);
                    String[] relation = {handle, key, String.valueOf(value)};
                    allCollaborateRelation.add(relation);
                }
                if (allCompleteRelation.size() > 500000) {
                    CSVWrite.writeToCSV(allCompleteRelation, "C:\\Users\\YLT\\Desktop\\relation\\CompleteRelation" + num + ".csv");
                    num++;
                    allCompleteRelation.clear();
                }
                if (allCollaborateRelation.size() > 500000) {
                    CSVWrite.writeToCSV(allCollaborateRelation, "C:\\Users\\YLT\\Desktop\\relation\\CollaborateRelation" + num + ".csv");
                    num++;
                    allCollaborateRelation.clear();
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        CSVWrite.writeToCSV(allCompleteRelation, "C:\\Users\\YLT\\Desktop\\relation\\CompleteRelation" + num + ".csv");
        num++;
        CSVWrite.writeToCSV(allCollaborateRelation, "C:\\Users\\YLT\\Desktop\\relation\\CollaborateRelation" + num + ".csv");
    }

    public void dealTag() {

    }
}
