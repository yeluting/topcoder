package com.buaa.act.sdp.dao;

import com.buaa.act.sdp.model.user.User;
import org.apache.ibatis.annotations.Param;

import java.util.HashMap;
import java.util.List;

/**
 * Created by yang on 2016/10/15.
 */
public interface UserDao {
    void insert(User user);

    User getUserByName(@Param("name") String name);

    User getUserById(@Param("id") int userId);

    List<String> getUsers();

    List<User> getAllUsers();

    List<Integer> getAllUsersId();

    void updateUsers(User user);

    void insertSkillDegree(@Param("handle") String handle, @Param("skillDegree") String skillDegree);

    void insertSkillDegreeToOne(@Param("skillDegreeToOne") String skillDegreeToOne, @Param("id") int id);

    void insertSkillDegreeBatch(@Param("relationMap") HashMap<String, String> map);

    void insertabilityBatch(@Param("relationMap") HashMap<String, String> map);
}
