package armadillo.mapper;

import armadillo.model.SysCard;
import org.apache.ibatis.annotations.*;
import org.apache.ibatis.type.JdbcType;

import java.util.List;

@CacheNamespace(blocking = true)
public interface SysCardMapper {
    @Delete({
            "delete from sys_card",
            "where id = #{id,jdbcType=INTEGER}"
    })
    int deleteByPrimaryKey(Integer id);

    @Insert({
            "insert into sys_card (id, card, ",
            "count, type, user_id, ",
            "usr_time, usable)",
            "values (#{id,jdbcType=INTEGER}, #{card,jdbcType=VARCHAR}, ",
            "#{count,jdbcType=INTEGER}, #{type,jdbcType=INTEGER}, #{userId,jdbcType=INTEGER}, ",
            "#{usrTime,jdbcType=TIMESTAMP}, #{usable,jdbcType=BIT})"
    })
    int insert(SysCard record);

    @Select({
            "select",
            "id, card, count, type, user_id, usr_time, usable",
            "from sys_card",
            "where id = #{id,jdbcType=INTEGER}"
    })
    @Results({
            @Result(column = "id", property = "id", jdbcType = JdbcType.INTEGER, id = true),
            @Result(column = "card", property = "card", jdbcType = JdbcType.VARCHAR),
            @Result(column = "count", property = "count", jdbcType = JdbcType.INTEGER),
            @Result(column = "type", property = "type", jdbcType = JdbcType.INTEGER),
            @Result(column = "user_id", property = "userId", jdbcType = JdbcType.INTEGER),
            @Result(column = "usr_time", property = "usrTime", jdbcType = JdbcType.TIMESTAMP),
            @Result(column = "usable", property = "usable", jdbcType = JdbcType.BIT)
    })
    SysCard selectByPrimaryKey(Integer id);

    @Select({
            "select",
            "id, card, count, type, user_id, usr_time, usable",
            "from sys_card"
    })
    @Results({
            @Result(column = "id", property = "id", jdbcType = JdbcType.INTEGER, id = true),
            @Result(column = "card", property = "card", jdbcType = JdbcType.VARCHAR),
            @Result(column = "count", property = "count", jdbcType = JdbcType.INTEGER),
            @Result(column = "type", property = "type", jdbcType = JdbcType.INTEGER),
            @Result(column = "user_id", property = "userId", jdbcType = JdbcType.INTEGER),
            @Result(column = "usr_time", property = "usrTime", jdbcType = JdbcType.TIMESTAMP),
            @Result(column = "usable", property = "usable", jdbcType = JdbcType.BIT)
    })
    List<SysCard> selectAll();

    @Update({
            "update sys_card",
            "set card = #{card,jdbcType=VARCHAR},",
            "count = #{count,jdbcType=INTEGER},",
            "type = #{type,jdbcType=INTEGER},",
            "user_id = #{userId,jdbcType=INTEGER},",
            "usr_time = #{usrTime,jdbcType=TIMESTAMP},",
            "usable = #{usable,jdbcType=BIT}",
            "where id = #{id,jdbcType=INTEGER}"
    })
    int updateByPrimaryKey(SysCard record);

    @Select({"select * from sys_card where card like #{card,jdbcType=VARCHAR} limit 1"})
    @Results({
            @Result(column = "id", property = "id", jdbcType = JdbcType.INTEGER, id = true),
            @Result(column = "card", property = "card", jdbcType = JdbcType.VARCHAR),
            @Result(column = "count", property = "count", jdbcType = JdbcType.INTEGER),
            @Result(column = "type", property = "type", jdbcType = JdbcType.INTEGER),
            @Result(column = "user_id", property = "userId", jdbcType = JdbcType.INTEGER),
            @Result(column = "usr_time", property = "usrTime", jdbcType = JdbcType.TIMESTAMP),
            @Result(column = "usable", property = "usable", jdbcType = JdbcType.BIT)
    })
    SysCard findCard(String card);

    @Insert({
            "<script>",
            "insert into sys_card (card, count, type, usable) values ",
            "<foreach collection='Lists' item='item' index='index' separator=','>",
            "(#{item.card,jdbcType=VARCHAR}, #{item.count,jdbcType=INTEGER}, #{item.type,jdbcType=INTEGER}, #{item.usable,jdbcType=BIT})",
            "</foreach>",
            "</script>"
    })
    int insertAll(@Param(value = "Lists") List<SysCard> record);
}