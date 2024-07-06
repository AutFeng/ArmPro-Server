package armadillo.mapper;

import armadillo.model.SingleCard;
import org.apache.ibatis.annotations.*;
import org.apache.ibatis.type.JdbcType;

import java.util.List;

@CacheNamespace(blocking = true)
public interface SingleCardMapper {
    @Delete({
            "delete from single_card",
            "where id = #{id,jdbcType=INTEGER}"
    })
    int deleteByPrimaryKey(Integer id);

    @Insert({
            "insert into single_card (id, card, ",
            "value, type, mac, ",
            "token, mark, usr_time, ",
            "usr_count, usable, soft_id)",
            "values (#{id,jdbcType=INTEGER}, #{card,jdbcType=VARCHAR}, ",
            "#{value,jdbcType=TINYINT}, #{type,jdbcType=TINYINT}, #{mac,jdbcType=VARCHAR}, ",
            "#{token,jdbcType=VARCHAR}, #{mark,jdbcType=VARCHAR}, #{usrTime,jdbcType=TIMESTAMP}, ",
            "#{usrCount,jdbcType=INTEGER}, #{usable,jdbcType=BIT}, #{softId,jdbcType=INTEGER})"
    })
    int insert(SingleCard record);

    @Select({
            "select",
            "id, card, value, type, mac, token, mark, usr_time, usr_count, usable, soft_id",
            "from single_card",
            "where id = #{id,jdbcType=INTEGER}"
    })
    @Results({
            @Result(column = "id", property = "id", jdbcType = JdbcType.INTEGER, id = true),
            @Result(column = "card", property = "card", jdbcType = JdbcType.VARCHAR),
            @Result(column = "value", property = "value", jdbcType = JdbcType.TINYINT),
            @Result(column = "type", property = "type", jdbcType = JdbcType.TINYINT),
            @Result(column = "mac", property = "mac", jdbcType = JdbcType.VARCHAR),
            @Result(column = "token", property = "token", jdbcType = JdbcType.VARCHAR),
            @Result(column = "mark", property = "mark", jdbcType = JdbcType.VARCHAR),
            @Result(column = "usr_time", property = "usrTime", jdbcType = JdbcType.TIMESTAMP),
            @Result(column = "usr_count", property = "usrCount", jdbcType = JdbcType.INTEGER),
            @Result(column = "usable", property = "usable", jdbcType = JdbcType.BIT),
            @Result(column = "soft_id", property = "softId", jdbcType = JdbcType.INTEGER)
    })
    SingleCard selectByPrimaryKey(Integer id);

    @Select({
            "select",
            "id, card, value, type, mac, token, mark, usr_time, usr_count, usable, soft_id",
            "from single_card"
    })
    @Results({
            @Result(column = "id", property = "id", jdbcType = JdbcType.INTEGER, id = true),
            @Result(column = "card", property = "card", jdbcType = JdbcType.VARCHAR),
            @Result(column = "value", property = "value", jdbcType = JdbcType.TINYINT),
            @Result(column = "type", property = "type", jdbcType = JdbcType.TINYINT),
            @Result(column = "mac", property = "mac", jdbcType = JdbcType.VARCHAR),
            @Result(column = "token", property = "token", jdbcType = JdbcType.VARCHAR),
            @Result(column = "mark", property = "mark", jdbcType = JdbcType.VARCHAR),
            @Result(column = "usr_time", property = "usrTime", jdbcType = JdbcType.TIMESTAMP),
            @Result(column = "usr_count", property = "usrCount", jdbcType = JdbcType.INTEGER),
            @Result(column = "usable", property = "usable", jdbcType = JdbcType.BIT),
            @Result(column = "soft_id", property = "softId", jdbcType = JdbcType.INTEGER)
    })
    List<SingleCard> selectAll();

    @Update({
            "update single_card",
            "set card = #{card,jdbcType=VARCHAR},",
            "value = #{value,jdbcType=TINYINT},",
            "type = #{type,jdbcType=TINYINT},",
            "mac = #{mac,jdbcType=VARCHAR},",
            "token = #{token,jdbcType=VARCHAR},",
            "mark = #{mark,jdbcType=VARCHAR},",
            "usr_time = #{usrTime,jdbcType=TIMESTAMP},",
            "usr_count = #{usrCount,jdbcType=INTEGER},",
            "usable = #{usable,jdbcType=BIT},",
            "soft_id = #{softId,jdbcType=INTEGER}",
            "where id = #{id,jdbcType=INTEGER}"
    })
    int updateByPrimaryKey(SingleCard record);

    @Delete({
            "delete from single_card",
            "where soft_id = #{soft_id,jdbcType=INTEGER}"
    })
    int deleteBySoftId(Integer soft_id);

    @Delete({
            "delete from single_card",
            "where card = #{card,jdbcType=VARCHAR} and soft_id = #{soft_id,jdbcType=INTEGER}"
    })
    int deleteByCard(@Param("card") String card, @Param("soft_id") int soft_id);

    @Select({"select * from single_card a JOIN ( SELECT id FROM single_card WHERE soft_id = #{soft_id,jdbcType=INTEGER} ORDER BY id DESC LIMIT #{offset,jdbcType=INTEGER}, #{limit,jdbcType=INTEGER} ) b ON a.id = b.id"})
    @Results({
            @Result(column = "id", property = "id", jdbcType = JdbcType.INTEGER, id = true),
            @Result(column = "card", property = "card", jdbcType = JdbcType.VARCHAR),
            @Result(column = "value", property = "value", jdbcType = JdbcType.TINYINT),
            @Result(column = "type", property = "type", jdbcType = JdbcType.TINYINT),
            @Result(column = "mac", property = "mac", jdbcType = JdbcType.VARCHAR),
            @Result(column = "token", property = "token", jdbcType = JdbcType.VARCHAR),
            @Result(column = "mark", property = "mark", jdbcType = JdbcType.VARCHAR),
            @Result(column = "usr_time", property = "usrTime", jdbcType = JdbcType.TIMESTAMP),
            @Result(column = "usr_count", property = "usrCount", jdbcType = JdbcType.INTEGER),
            @Result(column = "usable", property = "usable", jdbcType = JdbcType.BIT),
            @Result(column = "soft_id", property = "softId", jdbcType = JdbcType.INTEGER)
    })
    List<SingleCard> findOffSetCards(@Param("soft_id") int user_id, @Param("offset") int offset, @Param("limit") int limit);

    @Insert({
            "<script>",
            "insert into single_card (card, value, type, soft_id, mark) values ",
            "<foreach collection='Lists' item='item' index='index' separator=','>",
            "(#{item.card,jdbcType=VARCHAR}, #{item.value,jdbcType=INTEGER}, #{item.type,jdbcType=INTEGER}, #{item.softId,jdbcType=INTEGER}, #{item.mark,jdbcType=VARCHAR})",
            "</foreach>",
            "</script>"
    })
    void insertAll(@Param(value = "Lists") List<SingleCard> cards);

    @Select({
            "select",
            "id, card, value, type, mac, token, mark, usr_time, usr_count, usable, soft_id",
            "from single_card",
            "where soft_id = #{soft_id,jdbcType=INTEGER} and card = #{card,jdbcType=VARCHAR}"
    })
    @Results({
            @Result(column = "id", property = "id", jdbcType = JdbcType.INTEGER, id = true),
            @Result(column = "card", property = "card", jdbcType = JdbcType.VARCHAR),
            @Result(column = "value", property = "value", jdbcType = JdbcType.TINYINT),
            @Result(column = "type", property = "type", jdbcType = JdbcType.TINYINT),
            @Result(column = "mac", property = "mac", jdbcType = JdbcType.VARCHAR),
            @Result(column = "token", property = "token", jdbcType = JdbcType.VARCHAR),
            @Result(column = "mark", property = "mark", jdbcType = JdbcType.VARCHAR),
            @Result(column = "usr_time", property = "usrTime", jdbcType = JdbcType.TIMESTAMP),
            @Result(column = "usr_count", property = "usrCount", jdbcType = JdbcType.INTEGER),
            @Result(column = "usable", property = "usable", jdbcType = JdbcType.BIT),
            @Result(column = "soft_id", property = "softId", jdbcType = JdbcType.INTEGER)
    })
    SingleCard findSoftAndCard(@Param(value = "soft_id") Integer soft_id, @Param(value = "card") String card);


    @Select({
            "select",
            "id, card, value, type, mac, token, mark, usr_time, usr_count, usable, soft_id",
            "from single_card",
            "where soft_id = #{soft_id,jdbcType=INTEGER} and token = #{token,jdbcType=VARCHAR}"
    })
    @Results({
            @Result(column = "id", property = "id", jdbcType = JdbcType.INTEGER, id = true),
            @Result(column = "card", property = "card", jdbcType = JdbcType.VARCHAR),
            @Result(column = "value", property = "value", jdbcType = JdbcType.TINYINT),
            @Result(column = "type", property = "type", jdbcType = JdbcType.TINYINT),
            @Result(column = "mac", property = "mac", jdbcType = JdbcType.VARCHAR),
            @Result(column = "token", property = "token", jdbcType = JdbcType.VARCHAR),
            @Result(column = "mark", property = "mark", jdbcType = JdbcType.VARCHAR),
            @Result(column = "usr_time", property = "usrTime", jdbcType = JdbcType.TIMESTAMP),
            @Result(column = "usr_count", property = "usrCount", jdbcType = JdbcType.INTEGER),
            @Result(column = "usable", property = "usable", jdbcType = JdbcType.BIT),
            @Result(column = "soft_id", property = "softId", jdbcType = JdbcType.INTEGER)
    })
    SingleCard findSoftAndToken(@Param(value = "soft_id") Integer soft_id, @Param(value = "token") String token);

    @Select({
            "select",
            "id, card, value, type, mac, token, mark, usr_time, usr_count, usable, soft_id",
            "from single_card",
            "where soft_id = #{soft_id,jdbcType=INTEGER}"
    })
    @Results({
            @Result(column = "id", property = "id", jdbcType = JdbcType.INTEGER, id = true),
            @Result(column = "card", property = "card", jdbcType = JdbcType.VARCHAR),
            @Result(column = "value", property = "value", jdbcType = JdbcType.TINYINT),
            @Result(column = "type", property = "type", jdbcType = JdbcType.TINYINT),
            @Result(column = "mac", property = "mac", jdbcType = JdbcType.VARCHAR),
            @Result(column = "token", property = "token", jdbcType = JdbcType.VARCHAR),
            @Result(column = "mark", property = "mark", jdbcType = JdbcType.VARCHAR),
            @Result(column = "usr_time", property = "usrTime", jdbcType = JdbcType.TIMESTAMP),
            @Result(column = "usr_count", property = "usrCount", jdbcType = JdbcType.INTEGER),
            @Result(column = "usable", property = "usable", jdbcType = JdbcType.BIT),
            @Result(column = "soft_id", property = "softId", jdbcType = JdbcType.INTEGER)
    })
    List<SingleCard> findSoftId(Integer soft_id);
}