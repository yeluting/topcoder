package com.buaa.act.sdp.service.api;

import com.buaa.act.sdp.common.Constant;
import com.buaa.act.sdp.dao.ChallengeItemDao;
import com.buaa.act.sdp.dao.UserDao;
import com.mongodb.MongoClient;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.elasticsearch.action.bulk.BulkRequestBuilder;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.index.query.MultiMatchQueryBuilder;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.transport.client.PreBuiltTransportClient;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import static org.elasticsearch.index.query.QueryBuilders.multiMatchQuery;

/**
 * Created by YLT on 2017/6/25.
 */
@Component
public class ElasticSearch {
    //   * 创建客户端，所有的操作都由客户端开始，这个就好像是JDBC的Connection对象
    //  * 用完记得要关闭
    private static final TransportClient client = init();
    @Autowired
    private ChallengeItemDao challengeItemDao;

    @Autowired
    private UserDao userDao;


    public static TransportClient init() {
        TransportClient ret = null;
        try {
            ret = new PreBuiltTransportClient(Settings.builder()
                    //指定集群名称
                    .put("cluster.name", "sdpes")
                    .build())
                    //添加集群IP列表
                    .addTransportAddress(new InetSocketTransportAddress(InetAddress.getByName("192.168.7.101"), 9300))
                    .addTransportAddress(new InetSocketTransportAddress(InetAddress.getByName("192.168.7.102"), 9300))
                    .addTransportAddress(new InetSocketTransportAddress(InetAddress.getByName("192.168.7.103"), 9300))
                    .addTransportAddress(new InetSocketTransportAddress(InetAddress.getByName("192.168.7.104"), 9300));
        } catch (UnknownHostException e) {
            System.out.println("initialize ES connection error !!");
            e.printStackTrace();
        }
        return ret;
    }

    public static TransportClient getConnection() {
        return client;
    }

    public void insertToEs() {
        int Segment_Insert = 5000, i = 0;
        // 连接到 mongodb 服务
        MongoClient mongoClient = new MongoClient("192.168.7.113", 30000);
        // 连接到数据库
        MongoDatabase mongoDatabase = mongoClient.getDatabase("topcoder");
        MongoCollection collection = mongoDatabase.getCollection("userInfo");

        FindIterable<Document> findIterable = collection.find();
        MongoCursor<Document> mongoCursor = findIterable.iterator();
        BulkRequestBuilder bulkRequest = client.prepareBulk();

        while (mongoCursor.hasNext()) {
            i++;
            Document user = mongoCursor.next();
            Document end = new Document().append("userId", user.getInteger("userId")).append("profile", user.get("profile"));

            int userId = user.getInteger("userId");
            JSONObject abilityToOne = new JSONObject(userDao.getUserById(userId).getSkillDegreeToOne());
            JSONObject skillMysql = (JSONObject) abilityToOne.get("skill");

            Document skillsDocument = new Document();
            Document tagDocument = new Document();
            ArrayList<String> pl = new ArrayList<>();
            ArrayList<String> others = new ArrayList<>();
            Iterator iterator = skillMysql.keys();
            while (iterator.hasNext()) {
                String key = (String) iterator.next();
                if (Constant.PL.contains(key)) {
                    pl.add(key);
                } else {
                    others.add(key);
                }
            }
            tagDocument.append("pl", pl);
            tagDocument.append("others", others);
            skillsDocument.append("tag", tagDocument);
            Document temp = (Document) user.get("skills");
            skillsDocument.append("document", temp.get("document"));

            JSONObject SkillToOneMysql = (JSONObject) abilityToOne.get("skillToOne");
            Document d1 = new Document();
            Document d2 = new Document();
            iterator = skillMysql.keys();
            while (iterator.hasNext()) {
                String key = (String) iterator.next();
                d1.append(key.replace('.', '*'), skillMysql.getDouble(key));
            }
            iterator = SkillToOneMysql.keys();
            while (iterator.hasNext()) {
                String key = (String) iterator.next();
                d2.append(key.replace('.', '*'), SkillToOneMysql.getDouble(key));
            }
            skillsDocument.append("tagWithValue", d1);
            skillsDocument.append("tagWithValue_", d2);
            end.append("skills", skillsDocument);

            end.append("contributions", user.get("contributions"));
            end.append("collaborations", user.get("collaborations"));

            ObjectId id = (ObjectId) user.get("_id");
            user.remove("_id");
            bulkRequest.add(client.prepareIndex("topcoder", "user", id.toString()).setSource(end));
           // System.out.println(end.toString());
            if (i % Segment_Insert == 0) {
                BulkResponse bulkResponse = bulkRequest.execute().actionGet();
                //错误处理
                if (bulkResponse.hasFailures()) {
                    System.out.println(bulkResponse.buildFailureMessage());
                }
                System.out.println("文档插入Number：" + i);
                bulkRequest = client.prepareBulk();
            }
        }
        if (i % Segment_Insert != 0) {
            BulkResponse bulkResponse = bulkRequest.execute().actionGet();
            //错误处理
            if (bulkResponse.hasFailures()) {
                System.out.println(bulkResponse.buildFailureMessage());
            }
            System.out.println("文档插入Number：" + i);
        }
        client.close();
    }

    public void jsonToHashMap(JSONObject j, HashMap<String, Double> h) {
        Iterator iterator = j.keys();
        while (iterator.hasNext()) {
            String key = (String) iterator.next();
            h.put(key, j.getDouble(key));
        }
    }

    /*
    * 从mongodb中将带有能力值的用户导入到es中
    * */
    public void insert1() {
        int Segment_Insert = 5000, i = 0;
        // 连接到 mongodb 服务
        MongoClient mongoClient = new MongoClient("192.168.7.113", 30000);
        // 连接到数据库
        MongoDatabase mongoDatabase = mongoClient.getDatabase("topcoder");
        MongoCollection collection = mongoDatabase.getCollection("userInfo");

        FindIterable<Document> findIterable = collection.find();
        MongoCursor<Document> mongoCursor = findIterable.iterator();
        BulkRequestBuilder bulkRequest = client.prepareBulk();

        while (mongoCursor.hasNext()) {
            i++;
            Document user = mongoCursor.next();
            int userId = user.getInteger("userId");
            JSONObject originAll = new JSONObject(userDao.getUserById(userId).getSkillDegree());
            JSONObject originTag = (JSONObject) originAll.get("skill");
            Document temp = new Document();
            Iterator iterator = originTag.keys();
            while (iterator.hasNext()) {
                String key = (String) iterator.next();
                temp.append(key.replace('.', '*'), originTag.getDouble(key));
            }
            ((Document) (user.get("skills"))).append("tagWithValue", temp);
            ObjectId id = (ObjectId) user.get("_id");
            user.remove("_id");
            bulkRequest.add(client.prepareIndex("tc", "user", id.toString()).setSource(user));
            if (i % Segment_Insert == 0) {
                BulkResponse bulkResponse = bulkRequest.execute().actionGet();
                //错误处理
                if (bulkResponse.hasFailures()) {
                    System.out.println(bulkResponse.buildFailureMessage());
                }
                System.out.println("文档插入Number：" + i);
                bulkRequest = client.prepareBulk();
            }
        }
        if (i % Segment_Insert != 0) {
            BulkResponse bulkResponse = bulkRequest.execute().actionGet();
            //错误处理
            if (bulkResponse.hasFailures()) {
                System.out.println(bulkResponse.buildFailureMessage());
            }
            System.out.println("文档插入Number：" + i);
        }
        client.close();
    }

    public void getData() {
        String index = "topcoder";
        String type = "challenge";
        long number = client.prepareSearch(index).setTypes(type).get().getHits().getTotalHits();
        System.out.println(number);
    }

    public void search() {
        MultiMatchQueryBuilder qb = multiMatchQuery(
                "java elasticsearch",
                "skills.tag.pl", "skills.tag.others", "skills.document"
        );

        qb.type(MultiMatchQueryBuilder.Type.CROSS_FIELDS)
                .field("skills.tag.pl", 3.0f)
                .field("skills.tag.others", 2.0f)
                .field("skills.document", 1.0f);

        SearchResponse response = client.prepareSearch("topcoder")
                .setTypes("user")
//		        .setSearchType(SearchType.DFS_QUERY_THEN_FETCH)
                .setQuery(qb)                                                       // Query
//		        .setPostFilter(QueryBuilders.rangeQuery("age").from(12).to(18))     // Filter
                .setFrom(0).setSize(5)
//		        .setExplain(true)
                .get();

        for (SearchHit hit : response.getHits().getHits()) {
            System.out.println(hit.getSourceAsString());
        }
        client.close();
    }

    public void closeClient() {
        client.close();
    }
}
