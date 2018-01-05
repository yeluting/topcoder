package com.buaa.act.sdp.service.ability;

import com.buaa.act.sdp.common.Constant;
import com.buaa.act.sdp.dao.ChallengeItemDao;
import com.buaa.act.sdp.dao.ChallengeRegistrantDao;
import com.buaa.act.sdp.dao.UserDao;
import com.buaa.act.sdp.model.challenge.ChallengeItem;
import com.buaa.act.sdp.model.challenge.ChallengeRegistrant;
import com.buaa.act.sdp.model.user.User;
import com.mongodb.*;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.mongodb.gridfs.GridFS;
import com.mongodb.gridfs.GridFSDBFile;
import com.mongodb.gridfs.GridFSInputFile;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.bson.Document;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.*;
import java.util.*;

/**
 * Created by YLT on 2017/5/31.
 */
@Service
public class AbilityFileMongodb {

    @Autowired
    UserDao userDao;

    @Autowired
    ChallengeItemDao challengeItemDao;

    @Autowired
    ChallengeRegistrantDao challengeRegistrantDao;

    /*
    * 连接到mongodb数据库的两种方式
    * */
    public DB linkToMongodb() {
        /*// 连接到 mongodb 服务
        MongoClient mongoClient = new MongoClient("192.168.7.113", 30000);
        // 连接到数据库
        MongoDatabase mongoDatabase = mongoClient.getDatabase("topcoder");
        System.out.println("Connect to database successfully");
        return mongoDatabase;*/
        Mongo connection = new Mongo("192.168.7.113", 30000);
        DB db = connection.getDB("topcoder");
        return db;
    }

    /*
    * 将challengeItem数据以document形式导入到mongodb中
    * */
    public void genMongoDbChallenge() {
        // 连接到 mongodb 服务
        MongoClient mongoClient = new MongoClient("192.168.7.113", 30000);
        // 连接到数据库
        MongoDatabase mongoDatabase = mongoClient.getDatabase("topcoder");
        MongoCollection collection = mongoDatabase.getCollection("ChallengeItem1");
        List<Integer> challengeIds = challengeItemDao.getChallenges();
        for (int c : challengeIds
                ) {
            collection.insertOne(getEachChallenge(c));
            System.out.println(c);
        }
    }

    /*
    * 将user数据以document形式存入到mongodb中
    * */
    public void genMongodbUser() {
      /*  // 连接到 mongodb 服务
        MongoClient mongoClient = new MongoClient("192.168.7.113", 30000);
        // 连接到数据库
        MongoDatabase mongoDatabase = mongoClient.getDatabase("topcoder");
        MongoCollection collection = mongoDatabase.getCollection("userInfo");
        ArrayList<User> allUser = (ArrayList<User>) userDao.getAllUsers();
        for (User user:allUser
             ) {
            if(user.getId() == 21506) {
                collection.insertOne(getEachAbilityCopy2(user.getId()));
                System.out.println(user.getId());
            }
        }*/
       /* ArrayList<User> allUser = (ArrayList<User>) userDao.getAllUsers();
        for (User user : allUser
                ) {
            if (user.getSkillDegree() == null || user.getId() == 21506) {
                continue;
            }
            collection.insertOne(getEachAbilityCopy1(user.getId()));
            System.out.println(user.getId());
        }*/
        /*DB db = linkToMongodb();
        ArrayList<User> allUser = (ArrayList<User>) userDao.getAllUsers();
        for (User user : allUser
                ) {
            if (user.getSkillDegree() == null) {
                continue;
            }
            putGridFile(getEachAbility(user.getId()).toJson().toString(),db,Integer.toString(user.getId()));
            System.out.println(user.getId());
        }*/
        DB db = linkToMongodb();
        putGridFile(getEachAbilityCopy2(21506).toJson().toString(),db,Integer.toString(21506));

    }

    public void findMongodb() {
        MongoClient mongoClient = new MongoClient("192.168.7.113", 30000);
        MongoDatabase mongoDatabase = mongoClient.getDatabase("topcoder");
        MongoCollection<Document> collection = mongoDatabase.getCollection("UserInfo");
        HashMap<Integer, Integer> map = new HashMap<>();
        FindIterable<Document> findIterable = collection.find();
        MongoCursor<Document> mongoCursor = findIterable.iterator();
        while (mongoCursor.hasNext()) {
            int id = (int) mongoCursor.next().get("userID");
            if (map.containsKey(id)) {
                map.put(id, map.get(id) + 1);
            } else {
                map.put(id, 1);
            }
        }
        ArrayList<User> allUser = (ArrayList<User>) userDao.getAllUsers();
        for (User user : allUser
                ) {
            if (user.getSkillDegree() == null || user.getId() == 21506) {
                continue;
            } else {
                if (!map.containsKey(user.getId())) {
                    System.out.println(user.getId());
                }
            }
        }
    }

    /*
    * 当mongodb的一条数据过大时，只能以gridFile形式存入
    * */
    public void putGridFile(String str, DB db, String filename) {
        GridFS gridFS = new GridFS(db, "userInfoGridFS");
        GridFSInputFile gridFSInputFile = gridFS.createFile(str.getBytes());
        gridFSInputFile.setFilename(filename);
        gridFSInputFile.save();
    }

    public GridFSDBFile getByFileName(String fileName, GridFS fs) {
        DBObject query = new BasicDBObject("filename", fileName);
        GridFSDBFile gridFSDBFile = fs.findOne(query);
        return gridFSDBFile;
    }

    public void findGridFS() {
        DB db = linkToMongodb();
        GridFS gridFS = new GridFS(db, "UserInfoGridFS");
        GridFSDBFile gfsFile = getByFileName("21509", gridFS);
        File outFile = new File("C:\\Users\\YLT\\Desktop\\1.txt");
        try {
            gfsFile.writeTo(outFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.print(gfsFile.toString());
    }

    /*
    * 将每个challengeItem需要存入到mongodb的数据，存入到一个document
    * */
    public Document getEachChallenge(int challengeId) {
        ChallengeItem challengeItem = challengeItemDao.getChallengeItemById(challengeId);
        Document document = new Document().append("challengeId", challengeId);
        document.append("challengeName", challengeItem.getChallengeName());
        document.append("typeName", challengeItem.getTypeName());
        document.append("challengeType", challengeItem.getChallengeType());
        document.append("requirements", challengeItem.getRequirements());
        document.append("currentStatus", challengeItem.getCurrentStatus());
        document.append("postingDate", challengeItem.getPostingDate());
        document.append("finalFixEndDate", challengeItem.getFinalFixEndDate());
        document.append("forumLink", challengeItem.getForumLink());
        String str = Arrays.toString(challengeItem.getTechnology());
        document.append("technology", str.substring(1, str.length() - 1));
        str = Arrays.toString(challengeItem.getPlatforms());
        document.append("platforms", str.substring(1, str.length() - 1));
        str = Arrays.toString(challengeItem.getPrize());
        document.append("prize", str.substring(1, str.length() - 1));
        document.append("numSubmissions", challengeItem.getNumSubmissions());
        document.append("numRegistrants", challengeItem.getNumRegistrants());
        document.append("duration", challengeItem.getDuration());
        return document;
    }

    /*
   * 将每个user需要存入到mongodb的数据，存入到一个document，版本1
   * */
    public Document getEachAbility(int userId) {

        //userID添加
        User user = userDao.getUserById(userId);
        Document document = new Document().append("userId", userId);

        //profile添加
        Document profileJson = new Document();
        profileJson.append("name", user.getHandle());
        profileJson.append("mail", "");
        profileJson.append("country", user.getCountry());
        profileJson.append("address", "");
        profileJson.append("url", "https://www.topcoder.com/members/" + user.getHandle() + "/");
        String date = user.getMemberSince().substring(0, user.getMemberSince().indexOf('T'));
        profileJson.append("joinDate", date);
        profileJson.append("belongTo", 1);
        profileJson.append("aboutMe", user.getQuote());
        profileJson.append("age", "");
        profileJson.append("imageUrl", user.getPhotoLink());
        String[] skills = user.getSkills();
        if (skills != null && skills[0] != "") {
            profileJson.append("tagsAppend", new ArrayList<String>(Arrays.asList(skills)));
        } else {
            profileJson.append("tagsAppend", new ArrayList<String>());
        }
        document.append("profile", profileJson);

        //skill添加
        Document skillsJson = new Document();
        Document tagJson = new Document();
        JSONObject originAll = new JSONObject(user.getSkillDegree());
        JSONObject originTag = (JSONObject) originAll.get("skill");
        ArrayList<String> pl = new ArrayList<String>();
        ArrayList<String> others = new ArrayList<String>();
        //tag添加
        Iterator iterator = originTag.keys();
        while (iterator.hasNext()) {
            String key = (String) iterator.next();
            if (Constant.PL.contains(key)) {
                pl.add(key);
            } else {
                others.add(key);
            }
        }
        tagJson.append("pl", pl);
        tagJson.append("others", others);

        //document添加
        StringBuffer requirement = new StringBuffer();
        ChallengeRegistrant[] challengeRegsEveryUser = challengeRegistrantDao.getRegistrantByHandle(user.getHandle());
        for (ChallengeRegistrant eachReg : challengeRegsEveryUser
                ) {
            ChallengeItem item = challengeItemDao.getChallengeItemById(eachReg.getChallengeID());
            String req = item.getRequirements();
            if (req != null) {
                requirement.append(req);
            }
        }
        skillsJson.append("tag", tagJson);
        skillsJson.append("document", requirement.toString());
        document.append("skills", skillsJson);

        //contributions、collaboration添加
        document.append("contributions", new Document((Map<String, Object>) originAll.toMap().get("contribution")));
        JSONArray communicationJsonArray = (JSONArray) originAll.get("communication");
        JSONObject originCompleteJson = (JSONObject) communicationJsonArray.get(0);
        int winNum = 0, loseNum = 0, collaborNum = 0;
        Iterator iter1 = originCompleteJson.keys();
        while (iter1.hasNext()) {
            String[] vs = originCompleteJson.get(iter1.next().toString()).toString().split(":");
            winNum = winNum + Integer.parseInt(vs[0]);
            loseNum = loseNum + Integer.parseInt(vs[1]);
        }
        JSONObject originCollaborJson = (JSONObject) communicationJsonArray.get(1);
        Iterator iter2 = originCollaborJson.keys();
        while (iter2.hasNext()) {
            collaborNum = collaborNum + Integer.parseInt(originCollaborJson.get(iter2.next().toString()).toString());
        }
        Document collaborJson = new Document();
        collaborJson.append("winNum", winNum);
        collaborJson.append("loseNum", loseNum);
        collaborJson.append("collaborNum", collaborNum);
        document.append("collaborations", collaborJson);
        return document;
    }

    /*
    * mongodb存储结构有所变化，比如，将“.“变为*；将skill变成key-value形式，版本2
    * */
    public Document getEachAbilityCopy1(int userId) {

        //userID添加
        User user = userDao.getUserById(userId);
        Document document = new Document().append("userId", userId);

        //profile添加
        Document profileJson = new Document();
        profileJson.append("name", user.getHandle());
        profileJson.append("mail", "");
        profileJson.append("country", user.getCountry());
        profileJson.append("address", "");
        profileJson.append("url", "https://www.topcoder.com/members/" + user.getHandle() + "/");
        String date = user.getMemberSince().substring(0, user.getMemberSince().indexOf('T'));
        profileJson.append("joinDate", date);
        profileJson.append("belongTo", 1);
        profileJson.append("aboutMe", user.getQuote());
        profileJson.append("age", "");
        profileJson.append("imageUrl", user.getPhotoLink());
        String[] skills = user.getSkills();
        if (skills != null && skills[0] != "") {
            profileJson.append("tagsAppend", new ArrayList<String>(Arrays.asList(skills)));
        } else {
            profileJson.append("tagsAppend", new ArrayList<String>());
        }
        document.append("profile", profileJson);

        //skill添加
        Document skillsJson = new Document();
        Document tagJson = new Document();
        JSONObject originAll = new JSONObject(user.getSkillDegree());
        JSONObject originTag = (JSONObject) originAll.get("skill");
        HashMap<String,Double> pl = new HashMap<>();
        HashMap<String,Double> others = new HashMap<>();
        //tag添加
        Iterator iterator = originTag.keys();
        while (iterator.hasNext()) {
            String key = (String) iterator.next();
            if (Constant.PL.contains(key)) {
                pl.put(key.replace('.','*'),originTag.getDouble(key));
            } else {
                pl.put(key.replace('.','*'),originTag.getDouble(key));
            }
        }
        tagJson.append("pl", pl);
        tagJson.append("others", others);

        //document添加
        StringBuffer requirement = new StringBuffer();
        ChallengeRegistrant[] challengeRegsEveryUser = challengeRegistrantDao.getRegistrantByHandle(user.getHandle());
        for (ChallengeRegistrant eachReg : challengeRegsEveryUser
                ) {
            ChallengeItem item = challengeItemDao.getChallengeItemById(eachReg.getChallengeID());
            String req = item.getRequirements();
            if (req != null) {
                requirement.append(req);
            }
        }
        skillsJson.append("tag", tagJson);
        skillsJson.append("document", requirement.toString());
        document.append("skills", skillsJson);

        //contributions、collaboration添加
        document.append("contributions", new Document((Map<String, Object>) originAll.toMap().get("contribution")));
        JSONArray communicationJsonArray = (JSONArray) originAll.get("communication");
        JSONObject originCompleteJson = (JSONObject) communicationJsonArray.get(0);
        int winNum = 0, loseNum = 0, collaborNum = 0;
        Iterator iter1 = originCompleteJson.keys();
        while (iter1.hasNext()) {
            String[] vs = originCompleteJson.get(iter1.next().toString()).toString().split(":");
            winNum = winNum + Integer.parseInt(vs[0]);
            loseNum = loseNum + Integer.parseInt(vs[1]);
        }
        JSONObject originCollaborJson = (JSONObject) communicationJsonArray.get(1);
        Iterator iter2 = originCollaborJson.keys();
        while (iter2.hasNext()) {
            collaborNum = collaborNum + Integer.parseInt(originCollaborJson.get(iter2.next().toString()).toString());
        }
        Document collaborJson = new Document();
        collaborJson.append("winNum", winNum);
        collaborJson.append("loseNum", loseNum);
        collaborJson.append("collaborNum", collaborNum);
        document.append("collaborations", collaborJson);
        return document;
    }

    /*
    * mongodb存储结构有所变化，比如，将“.“变为*；将skill变成key-value形式，版本3，将用户技能分归一化
    * */
    public Document getEachAbilityCopy2(int userId) {

        //userID添加
        User user = userDao.getUserById(userId);
        Document document = new Document().append("userId", userId);

        //profile添加
        Document profileJson = new Document();
        profileJson.append("name", user.getHandle());
        profileJson.append("mail", "");
        profileJson.append("country", user.getCountry());
        profileJson.append("address", "");
        profileJson.append("url", "https://www.topcoder.com/members/" + user.getHandle() + "/");
        String date = user.getMemberSince().substring(0, user.getMemberSince().indexOf('T'));
        profileJson.append("joinDate", date);
        profileJson.append("belongTo", 1);
        profileJson.append("aboutMe", user.getQuote());
        profileJson.append("age", "");
        profileJson.append("imageUrl", user.getPhotoLink());
        String[] skills = user.getSkills();
        if (skills != null && skills[0] != "") {
            profileJson.append("tagsAppend", new ArrayList<String>(Arrays.asList(skills)));
        } else {
            profileJson.append("tagsAppend", new ArrayList<String>());
        }
        document.append("profile", profileJson);

        //skill添加
        Document skillsJson = new Document();
        Document tagJson = new Document();
        JSONObject originAll = new JSONObject(user.getSkillDegreeToOne());
        JSONObject originSkill = (JSONObject) originAll.get("skill");
        HashMap<String,Double> pl = new HashMap<>();
        HashMap<String,Double> others = new HashMap<>();
        //tag添加
        Iterator iterator = originSkill.keys();
        while (iterator.hasNext()) {
            String key = (String) iterator.next();
            if (Constant.PL.contains(key)) {
                pl.put(key.replace('.','*'),originSkill.getDouble(key));
            } else {
                others.put(key.replace('.','*'),originSkill.getDouble(key));
            }
        }
        tagJson.append("pl", pl);
        tagJson.append("others", others);
        JSONObject originSkillToOne = (JSONObject) originAll.get("skillToOne");
        HashMap<String,Double> pl_ = new HashMap<>();
        HashMap<String,Double> others_ = new HashMap<>();
        iterator = originSkillToOne.keys();
        while (iterator.hasNext()) {
            String key = (String) iterator.next();
            if (Constant.PL.contains(key)) {
                pl_.put(key.replace('.','*'),originSkillToOne.getDouble(key));
            } else {
                others_.put(key.replace('.','*'),originSkillToOne.getDouble(key));
            }
        }
        tagJson.append("pl_", pl_);
        tagJson.append("others_", others_);


        //document添加
        StringBuffer requirement = new StringBuffer();
        ChallengeRegistrant[] challengeRegsEveryUser = challengeRegistrantDao.getRegistrantByHandle(user.getHandle());
        for (ChallengeRegistrant eachReg : challengeRegsEveryUser
                ) {
            ChallengeItem item = challengeItemDao.getChallengeItemById(eachReg.getChallengeID());
            String req = item.getRequirements();
            if (req != null) {
                requirement.append(req);
            }
        }
        skillsJson.append("tag", tagJson);
        skillsJson.append("document", requirement.toString());
        document.append("skills", skillsJson);

        //contributions、collaboration添加
        document.append("contributions", new Document((Map<String, Object>) originAll.toMap().get("contribution")));
        document.append("collaborations", new Document((Map<String, Object>) originAll.toMap().get("collaborations")));
       /* JSONArray communicationJsonArray = (JSONArray) originAll.get("communication");
        JSONObject originCompleteJson = (JSONObject) communicationJsonArray.get(0);
        int winNum = 0, loseNum = 0, collaborNum = 0;
        Iterator iter1 = originCompleteJson.keys();
        while (iter1.hasNext()) {
            String[] vs = originCompleteJson.get(iter1.next().toString()).toString().split(":");
            winNum = winNum + Integer.parseInt(vs[0]);
            loseNum = loseNum + Integer.parseInt(vs[1]);
        }
        JSONObject originCollaborJson = (JSONObject) communicationJsonArray.get(1);
        Iterator iter2 = originCollaborJson.keys();
        while (iter2.hasNext()) {
            collaborNum = collaborNum + Integer.parseInt(originCollaborJson.get(iter2.next().toString()).toString());
        }
        Document collaborJson = new Document();
        collaborJson.append("winNum", winNum);
        collaborJson.append("loseNum", loseNum);
        collaborJson.append("collaborNum", collaborNum);
        document.append("collaborations", collaborJson);*/
        return document;
    }
    /*
    * 处理原始PL文档
    * */
    public void dealPLDoc(String filePath) {
        String encoding = "GBK";
        File file = new File(filePath);
        if (file.isFile() && file.exists()) { //判断文件是否存在
            InputStreamReader read = null;//考虑到编码格式
            try {
                read = new InputStreamReader(new FileInputStream(file), encoding);
                BufferedReader bufferedReader = new BufferedReader(read);
                String lineTxt = null;
                while ((lineTxt = bufferedReader.readLine()) != null && lineTxt != "") {
                    lineTxt = lineTxt.substring(lineTxt.indexOf('>') + 1);
                    System.out.println("PL.add(\"" + lineTxt.toLowerCase() + "\");");
                }
                read.close();
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /*
    * 调用StandardAnalyzer对项目需求进行简单分词
    */
    public ArrayList<String> textHandle(String requirements) {
        Analyzer analyzer = new StandardAnalyzer();
        TokenStream tokenStream = null;
        ArrayList<String> words = new ArrayList<String>();
        String test = challengeItemDao.getChallengeItemRequirementById(30054266);

        try {
            tokenStream = analyzer.tokenStream(null, test);
            tokenStream.reset();
            while (tokenStream.incrementToken()) {
                CharTermAttribute ch = tokenStream.addAttribute(CharTermAttribute.class);
                words.add(ch.toString());
            }
            tokenStream.end();
            tokenStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return words;
    }

    /*
    *删除项目requirements中的html标签
    * */
    public void HTMLTagDelete() {
      /*  ArrayList<ChallengeItem> allChallenges = (ArrayList<ChallengeItem>) challengeItemDao.getAllChallenges();
        for (ChallengeItem challenge : allChallenges
                ) {
            String str = challenge.getDetailedRequirements();
            if (str != null) {
                str = str.replaceAll("<[a-zA-Z]+[1-9]?[^><]*>", "")
                        .replaceAll("</[a-zA-Z]+[1-9]?>", "").replaceAll("&nbsp;", "").replaceAll("\\n", "").replaceAll("&lt;", "").replaceAll("&gt;", "").replaceAll("&quot;", "").replaceAll("&qpos;", "").replaceAll("&amp;", "");
                System.out.println(challenge.getChallengeId());
                challengeItemDao.setHandledRequirements(str, challenge.getChallengeId());
            }
        }*/
        String encoding = "GBK";
        File file = new File("C:\\Users\\YLT\\Desktop\\1.txt");
        if (file.isFile() && file.exists()) { //判断文件是否存在
            InputStreamReader read = null;//考虑到编码格式
            try {
                read = new InputStreamReader(new FileInputStream(file), encoding);
                BufferedReader bufferedReader = new BufferedReader(read);
                String lineTxt = null;
                while ((lineTxt = bufferedReader.readLine()) != null && lineTxt != "") {
                    lineTxt = lineTxt.replaceAll("<[a-zA-Z]+[1-9]?[^><]*>", "")
                            .replaceAll("</[a-zA-Z]+[1-9]?>", "").replaceAll("&nbsp;", "").replaceAll("\\\\t", "").replaceAll("\\\\n", "").replaceAll("&lt;", "").replaceAll("&gt;", "").replaceAll("&quot;", "").replaceAll("&qpos;", "").replaceAll("&amp;", "");
                    /*Pattern p = Pattern.compile("\\s*|\t|\r|\n");
                    Matcher m = p.matcher(lineTxt);
                    lineTxt = m.replaceAll("");*/
                    System.out.println(lineTxt);
                }
                read.close();
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


    public void updateMongodb(int id) {
        // 连接到 mongodb 服务
        MongoClient mongoClient = new MongoClient("192.168.7.113", 30000);
        // 连接到数据库
        MongoDatabase mongoDatabase = mongoClient.getDatabase("topcoder");
        MongoCollection collection = mongoDatabase.getCollection("UserInfo");

        Document doc = (Document) collection.find(Filters.eq("userID", 4021));
        collection.updateOne(Filters.eq("userID", 4021), new Document("$set", new Document("document", doc.get("document").toString().replaceAll("\\\\t", "").replaceAll("\\\\n", ""))));

    }
}
