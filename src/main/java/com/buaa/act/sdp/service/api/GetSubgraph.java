package com.buaa.act.sdp.service.api;

import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.stereotype.Component;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

/**
 * Created by luckcul on 17-6-21.
 * 已有的变量名不要修改
 */
@Component
public class GetSubgraph {
    List<Category> categories;
    List<Node> nodes;
    List<Link> links;

    public void getSubgraph1(int ID, int type, int limit) {
        categories = new ArrayList<>();
        nodes = new ArrayList<>();
        links = new ArrayList<>();
        HashMap<String, Integer> nameHandle = new HashMap<>();
        int limit1 = limit / 2;
        int limit2 = limit - limit1;
        Connection con = null;
        try {
            con = DriverManager.getConnection("jdbc:neo4j:http://192.168.7.109:7474/", "neo4j", "123456");
        } catch (SQLException e) {
            e.printStackTrace();
        }

        if (type == 0) {
            categories.add(new Category("Developer", "diamond"));
            categories.add(new Category("Competitor", "circle"));
            categories.add(new Category("Co-worker", "triangle"));
            categories.add(new Category("Both-relation", "rectangle"));

            String query = "match(n:User{userId:{1}})-[c:CompleteTotal]->(m:User)  return n.handle,m.handle,c.vsTotal,c.vsScore ORDER BY c.vsTotal DESC limit {2}";
            PreparedStatement stmt = null;
            try {
                stmt = con.prepareStatement(query);
                stmt.setInt(1, ID);
                stmt.setInt(2, limit1);
                ResultSet rs = stmt.executeQuery();
                while (rs.next()) {
                    String developer = rs.getString("n.handle");
                    if (!nameHandle.containsKey(developer)) {
                        nodes.add(new Node(0, developer, 10));
                        nameHandle.put(developer, 0);
                    }
                    String competitor = rs.getString("m.handle");
                    nodes.add(new Node(1, competitor, 8));
                    nameHandle.put(competitor, nodes.size() - 1);
                    links.add(new Link(0, nameHandle.get(competitor), (int) (Math.random() * 10), "比分" + rs.getString("c.vsScore"), null, 0));
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
            String query1 = "match(n:User{userId:{1}})<-[c:CollaborateWith]-(m:User) return count(c) AS countNum,m.handle ORDER BY countNum DESC limit {2}";
            PreparedStatement stmt1 = null;
            try {
                stmt1 = con.prepareStatement(query1);
                stmt1.setInt(1, ID);
                stmt1.setInt(2, limit2);
                ResultSet rs1 = stmt1.executeQuery();
                while (rs1.next()) {
                    String collaborator = rs1.getString("m.handle");
                    if (nameHandle.containsKey(collaborator)) {
                        for (Node n : nodes) {
                            if (n.getName().equals(collaborator)) {
                                n.setCategory(3);
                                break;
                            }
                        }
                    } else {
                        nodes.add(new Node(2, collaborator, 8));
                        nameHandle.put(collaborator, nodes.size() - 1);
                    }
                    links.add(new Link(0, nameHandle.get(collaborator), (int) (Math.random() * 10), "合作" + rs1.getInt("countNum") + "次", null, 1));
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        } else if (type == 1) {
            categories.add(new Category("Developer", "circle"));
            String query = "MATCH (u:User)-[s:SubmitTo]->(c:Challenge_item{challengeId:{1}}) RETURN u.userId,u.handle ORDER BY s.finalScore DESC LIMIT {2} ";
            PreparedStatement stmt = null;
            try {
                stmt = con.prepareStatement(query);
                stmt.setInt(1, ID);
                stmt.setInt(2, limit);
                ResultSet rs = stmt.executeQuery();
                ArrayList<Integer> allSubers = new ArrayList<Integer>();
                while (rs.next()) {
                    allSubers.add(rs.getInt("u.userId"));
                    String developer = rs.getString("u.handle");
                    nodes.add(new Node(0, developer, 8));
                    nameHandle.put(developer, nodes.size() - 1);
                }
                for (int s : allSubers) {
                    for (int t : allSubers) {
                        if (s != t) {
                            String query1 = "match(n:User{userId:{1}})-[c:CompleteTotal]->(m:User{userId:{2}})  return n.handle,m.handle,c.vsScore";
                            PreparedStatement stmt1 = null;
                            stmt1 = con.prepareStatement(query1);
                            stmt1.setInt(1, s);
                            stmt1.setInt(2, t);
                            ResultSet rs1 = stmt1.executeQuery();
                            while (rs1.next()) {
                                String develop1 = rs1.getString("n.handle");
                                String develop2 = rs1.getString("m.handle");
                                links.add(new Link(nameHandle.get(develop1), nameHandle.get(develop2), (int) (Math.random() * 10), "比分" + rs1.getString("c.vsScore"), null, 0));
                            }
                            String query2 = "match(n:User{userId:{1}})-[c:CollaborateWith]->(m:User{userId:{2}})  return n.handle,m.handle,count(c) as countNum";
                            PreparedStatement stmt2 = null;
                            stmt2 = con.prepareStatement(query2);
                            stmt2.setInt(1, s);
                            stmt2.setInt(2, t);
                            ResultSet rs2 = stmt2.executeQuery();
                            while (rs2.next()) {
                                String develop1 = rs2.getString("n.handle");
                                String develop2 = rs2.getString("m.handle");
                                links.add(new Link(nameHandle.get(develop1), nameHandle.get(develop2), (int) (Math.random() * 10), "合作" + rs2.getInt("countNum") + "次", null, 1));
                            }
                        }
                    }
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        for (Node n : nodes
                ) {
            System.out.println(n.getName());
        }
        for (Link l : links
                ) {
            System.out.println(l.getTarget() + "to" + l.getSource() + l.getName());
        }
    }

    /**
     * 这儿是是主要完成的代码,把图的信息存入categories,nodes,links
     */
    /*
    * type = 0时，得出开发者关系图
    * type = 1时，得出challenge中参与者之间的关系图
    * */
    public void getSubgraph(int ID, int type, int limit) { //自己需要的参数，比如userId，需要指定的点的数量上限
        categories = new ArrayList<>();
        nodes = new ArrayList<>();
        links = new ArrayList<>();
        Connection con = null;
        try {
            con = DriverManager.getConnection("jdbc:neo4j:http://192.168.7.109:7474/", "neo4j", "123456");
        } catch (SQLException e) {
            e.printStackTrace();
        }

        if (type == 0) {
            categories.add(new Category("Developer", "diamond"));
            categories.add(new Category("Competitor", "circle"));
            categories.add(new Category("Co-worker", "triangle"));
            String query = "MATCH (u:User) WHERE u.userId = {1} RETURN u.skillDegree,u.handle ";
            PreparedStatement stmt = null;
            try {
                stmt = con.prepareStatement(query);
                stmt.setInt(1, ID);
                ResultSet rs = stmt.executeQuery();

                while (rs.next()) {
                    nodes.add(new Node(0, rs.getString("u.handle"), 10));
                    JSONObject jsonObject = new JSONObject(rs.getString("u.skillDegree"));
                    JSONArray communication = jsonObject.getJSONArray("communication");
                    JSONObject completeJson = communication.getJSONObject(0);
                    JSONObject collaborateJson = communication.getJSONObject(1);
                    Iterator it = completeJson.keys();
                    while (it.hasNext() && nodes.size() < limit) {
                        String key = (String) it.next();
                        String value = completeJson.getString(key);
                        nodes.add(new Node(1, key, 5));
                        links.add(new Link(0, nodes.size() - 1, (int) (Math.random() * 10), "比分" + value, null, 0));
                    }
                    Iterator it1 = collaborateJson.keys();
                    while (it1.hasNext() && nodes.size() < limit) {
                        String key = (String) it1.next();
                        int value = collaborateJson.getInt(key);
                        nodes.add(new Node(2, key, 8));
                        links.add(new Link(0, nodes.size() - 1, (int) (Math.random() * 10), "合作" + value + "次", null, 1));
                    }
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        } else if (type == 1) {
            HashMap<String, Integer> handles = new HashMap<>();
            categories.add(new Category("Developer", "diamond"));
            String query = "MATCH (u:User)-[s:SubmitTo]->(c:Challenge_item) WHERE c.challengeId = {1} RETURN u.handle order by s.finalScore desc LIMIT {2}";
            PreparedStatement stmt = null;
            try {
                stmt = con.prepareStatement(query);
                stmt.setInt(1, ID);
                stmt.setInt(2, limit);
                ResultSet rs = stmt.executeQuery();
                while (rs.next()) {
                    nodes.add(new Node(0, rs.getString("u.handle"), 8));
                    handles.put(rs.getString("u.handle"), nodes.size() - 1);
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
            for (int i = 0; i < nodes.size(); i++) {
                String query1 = "MATCH (u:User) WHERE u.handle = {1} RETURN u.skillDegree";
                PreparedStatement stmt1 = null;
                try {
                    stmt1 = con.prepareStatement(query1);
                    stmt1.setString(1, nodes.get(i).getName());
                    ResultSet rs = stmt1.executeQuery();
                    while (rs.next()) {
                        JSONObject json = new JSONObject(rs.getString("u.skillDegree"));
                        JSONArray communication = json.getJSONArray("communication");
                        JSONObject completeJson = communication.getJSONObject(0);
                        JSONObject collaborateJson = communication.getJSONObject(1);
                        Iterator it = completeJson.keys();
                        while (it.hasNext()) {
                            String key = (String) it.next();
                            if (handles.containsKey(key)) {
                                String value = completeJson.getString(key);
                                links.add(new Link(i, handles.get(key), (int) (Math.random() * 10), "比分" + value, null, 0));//竞争关系
                            }
                        }
                        Iterator it1 = collaborateJson.keys();
                        while (it1.hasNext()) {
                            String key = (String) it1.next();
                            if (handles.containsKey(key)) {
                                int value = collaborateJson.getInt(key);
                                links.add(new Link(i, handles.get(key), (int) (Math.random() * 10), "合作" + value + "次", null, 1));//协作关系
                            }
                        }
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }

        for (Node n : nodes
                ) {
            System.out.println(n.getName());
        }
        for (Link l : links
                ) {
            System.out.println(l.getSource() + "to" + l.getTarget() + l.getName());
        }
    }


    public List<Category> getCategories() {
        return categories;
    }

    public List<Node> getNodes() {
        return nodes;
    }

    public List<Link> getLinks() {
        return links;
    }
}

class Category {
    String name;
    String symbol;

    public Category() {
    }

    public Category(String name, String symbol) {
        this.name = name;
        this.symbol = symbol;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSymbol() {
        return symbol;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }
}

class Node {
    int category;//代表属于GetSubgraph中List<Category> categories；第几类
    String name;
    int value;

    public Node() {
    }

    public Node(int category, String name, int value) {
        this.category = category;
        this.name = name;
        this.value = value;
    }

    public int getCategory() {
        return category;
    }

    public void setCategory(int category) {
        this.category = category;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getValue() {
        return value;
    }

    public void setValue(int value) {
        this.value = value;
    }
}

class History {
    String name;
    String url;

    public History() {
    }

    public History(String name, String url) {
        this.name = name;
        this.url = url;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}

class Link {
    int source; // 表示GetSubgraph中 List<Node> nodes;第source个
    int target; // 表示GetSubgraph中 List<Node> nodes;第target个
    int weight;
    String name;
    List<History> history;
    int type;

    public Link() {
    }

    public Link(int source, int target, int weight, String name, List<History> history, int type) {
        this.source = source;
        this.target = target;
        this.weight = weight;
        this.name = name;
        this.history = history;
        this.type = type;
    }

    public int getSource() {
        return source;
    }

    public void setSource(int source) {
        this.source = source;
    }

    public int getTarget() {
        return target;
    }

    public void setTarget(int target) {
        this.target = target;
    }

    public int getWeight() {
        return weight;
    }

    public void setWeight(int weight) {
        this.weight = weight;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<History> getHistory() {
        return history;
    }

    public void setHistory(List<History> history) {
        this.history = history;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }
}
