package armadillo.mapper;

import armadillo.model.SingleTrial;
import org.apache.ibatis.annotations.*;
import org.apache.ibatis.type.JdbcType;

import java.util.List;

@CacheNamespace(blocking = true)
public interface SingleTrialMapper {
    @Delete({
            "delete from single_trial",
            "where id = #{id,jdbcType=INTEGER}"
    })
    int deleteByPrimaryKey(Integer id);

    @Insert({
            "insert into single_trial (id, soft_id, ",
            "count, last_time, ",
            "mac, token)",
            "values (#{id,jdbcType=INTEGER}, #{softId,jdbcType=INTEGER}, ",
            "#{count,jdbcType=INTEGER}, #{lastTime,jdbcType=TIMESTAMP}, ",
            "#{mac,jdbcType=VARCHAR}, #{token,jdbcType=VARCHAR})"
    })
    int insert(SingleTrial record);

    @Select({
            "select",
            "id, soft_id, count, last_time, mac, token",
            "from single_trial",
            "where id = #{id,jdbcType=INTEGER}"
    })
    @Results({
            @Result(column = "id", property = "id", jdbcType = JdbcType.INTEGER, id = true),
            @Result(column = "soft_id", property = "softId", jdbcType = JdbcType.INTEGER),
            @Result(column = "count", property = "count", jdbcType = JdbcType.INTEGER),
            @Result(column = "last_time", property = "lastTime", jdbcType = JdbcType.TIMESTAMP),
            @Result(column = "mac", property = "mac", jdbcType = JdbcType.VARCHAR),
            @Result(column = "token", property = "token", jdbcType = JdbcType.VARCHAR)
    })
    SingleTrial selectByPrimaryKey(Integer id);

    @Select({
            "select",
            "id, soft_id, count, last_time, mac, token",
            "from single_trial"
    })
    @Results({
            @Result(column = "id", property = "id", jdbcType = JdbcType.INTEGER, id = true),
            @Result(column = "soft_id", property = "softId", jdbcType = JdbcType.INTEGER),
            @Result(column = "count", property = "count", jdbcType = JdbcType.INTEGER),
            @Result(column = "last_time", property = "lastTime", jdbcType = JdbcType.TIMESTAMP),
            @Result(column = "mac", property = "mac", jdbcType = JdbcType.VARCHAR),
            @Result(column = "token", property = "token", jdbcType = JdbcType.VARCHAR)
    })
    List<SingleTrial> selectAll();

    @Update({
            "update single_trial",
            "set soft_id = #{softId,jdbcType=INTEGER},",
            "count = #{count,jdbcType=INTEGER},",
            "last_time = #{lastTime,jdbcType=TIMESTAMP},",
            "mac = #{mac,jdbcType=VARCHAR},",
            "token = #{token,jdbcType=VARCHAR}",
            "where id = #{id,jdbcType=INTEGER}"
    })
    int updateByPrimaryKey(SingleTrial record);

    @Select({
            "select",
            "id, soft_id, count, last_time, mac, token",
            "from single_trial",
            "where soft_id = #{soft_id,jdbcType=INTEGER} and mac = #{mac,jdbcType=VARCHAR}"
    })
    @Results({
            @Result(column = "id", property = "id", jdbcType = JdbcType.INTEGER, id = true),
            @Result(column = "soft_id", property = "softId", jdbcType = JdbcType.INTEGER),
            @Result(column = "count", property = "count", jdbcType = JdbcType.INTEGER),
            @Result(column = "last_time", property = "lastTime", jdbcType = JdbcType.TIMESTAMP),
            @Result(column = "mac", property = "mac", jdbcType = JdbcType.VARCHAR),
            @Result(column = "token", property = "token", jdbcType = JdbcType.VARCHAR)
    })
    SingleTrial findSoftIdAndMac(@Param("soft_id") Integer id, @Param("mac") String mac);

    @Select({
            "select",
            "id, soft_id, count, last_time, mac, token",
            "from single_trial",
            "where soft_id = #{soft_id,jdbcType=INTEGER} and token = #{token,jdbcType=VARCHAR}"
    })
    @Results({
            @Result(column = "id", property = "id", jdbcType = JdbcType.INTEGER, id = true),
            @Result(column = "soft_id", property = "softId", jdbcType = JdbcType.INTEGER),
            @Result(column = "count", property = "count", jdbcType = JdbcType.INTEGER),
            @Result(column = "last_time", property = "lastTime", jdbcType = JdbcType.TIMESTAMP),
            @Result(column = "mac", property = "mac", jdbcType = JdbcType.VARCHAR),
            @Result(column = "token", property = "token", jdbcType = JdbcType.VARCHAR)
    })
    SingleTrial findSoftIdAndToken(@Param("soft_id") Integer id, @Param("token") String token);

    @Select({"select * from single_trial a JOIN ( SELECT id FROM single_trial WHERE soft_id = #{soft_id,jdbcType=INTEGER} ORDER BY id DESC LIMIT #{offset,jdbcType=INTEGER}, #{limit,jdbcType=INTEGER} ) b ON a.id = b.id"})
    @Results({
            @Result(column = "id", property = "id", jdbcType = JdbcType.INTEGER, id = true),
            @Result(column = "soft_id", property = "softId", jdbcType = JdbcType.INTEGER),
            @Result(column = "count", property = "count", jdbcType = JdbcType.INTEGER),
            @Result(column = "last_time", property = "lastTime", jdbcType = JdbcType.TIMESTAMP),
            @Result(column = "mac", property = "mac", jdbcType = JdbcType.VARCHAR),
            @Result(column = "token", property = "token", jdbcType = JdbcType.VARCHAR)
    })
    List<SingleTrial> findOffSetTrials(@Param("soft_id") int user_id, @Param("offset") int offset, @Param("limit") int limit);

    @Delete({
            "delete from single_trial",
            "where mac = #{mac,jdbcType=VARCHAR}"
    })
    int deleteByMac(String mac);

    @Delete({
            "delete from single_trial",
            "where mac = #{mac,jdbcType=VARCHAR} and soft_id = #{soft_id,jdbcType=INTEGER}"
    })
    int deleteByMacAndSoftId(@Param("mac") String mac, @Param("soft_id") int soft_id);

    @Select({
            "select",
            "id, soft_id, count, last_time, mac, token",
            "from single_trial",
            "where soft_id = #{soft_id,jdbcType=INTEGER} and mac = #{mac,jdbcType=VARCHAR}"
    })
    @Results({
            @Result(column = "id", property = "id", jdbcType = JdbcType.INTEGER, id = true),
            @Result(column = "soft_id", property = "softId", jdbcType = JdbcType.INTEGER),
            @Result(column = "count", property = "count", jdbcType = JdbcType.INTEGER),
            @Result(column = "last_time", property = "lastTime", jdbcType = JdbcType.TIMESTAMP),
            @Result(column = "mac", property = "mac", jdbcType = JdbcType.VARCHAR),
            @Result(column = "token", property = "token", jdbcType = JdbcType.VARCHAR)
    })
    SingleTrial findSoftAndMac(@Param(value = "soft_id") Integer soft_id, @Param(value = "mac") String mac);

    @Delete({
            "delete from single_trial",
            "where soft_id = #{soft_id,jdbcType=INTEGER}"
    })
    int deleteBySoftId(Integer soft_id);
}