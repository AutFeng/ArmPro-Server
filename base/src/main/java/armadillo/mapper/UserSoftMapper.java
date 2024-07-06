package armadillo.mapper;

import armadillo.model.UserSoft;
import armadillo.result.sys.Softs;
import org.apache.ibatis.annotations.*;
import org.apache.ibatis.type.JdbcType;

import java.util.List;

@CacheNamespace(blocking = true)
public interface UserSoftMapper {
    @Delete({
            "delete from user_soft",
            "where id = #{id,jdbcType=INTEGER}"
    })
    int deleteByPrimaryKey(Integer id);

    @Insert({
            "insert into user_soft (id, appkey, ",
            "name, package_name, ",
            "version, user_id, ",
            "handle)",
            "values (#{id,jdbcType=INTEGER}, #{appkey,jdbcType=VARCHAR}, ",
            "#{name,jdbcType=VARCHAR}, #{packageName,jdbcType=VARCHAR}, ",
            "#{version,jdbcType=INTEGER}, #{userId,jdbcType=INTEGER}, ",
            "#{handle,jdbcType=INTEGER})"
    })
    int insert(UserSoft record);

    @Select({
            "select",
            "id, appkey, name, package_name, version, user_id, handle",
            "from user_soft",
            "where id = #{id,jdbcType=INTEGER}"
    })
    @Results({
            @Result(column = "id", property = "id", jdbcType = JdbcType.INTEGER, id = true),
            @Result(column = "appkey", property = "appkey", jdbcType = JdbcType.VARCHAR),
            @Result(column = "name", property = "name", jdbcType = JdbcType.VARCHAR),
            @Result(column = "package_name", property = "packageName", jdbcType = JdbcType.VARCHAR),
            @Result(column = "version", property = "version", jdbcType = JdbcType.INTEGER),
            @Result(column = "user_id", property = "userId", jdbcType = JdbcType.INTEGER),
            @Result(column = "handle", property = "handle", jdbcType = JdbcType.INTEGER)
    })
    UserSoft selectByPrimaryKey(Integer id);

    @Select({
            "select",
            "id, appkey, name, package_name, version, user_id, handle",
            "from user_soft"
    })
    @Results({
            @Result(column = "id", property = "id", jdbcType = JdbcType.INTEGER, id = true),
            @Result(column = "appkey", property = "appkey", jdbcType = JdbcType.VARCHAR),
            @Result(column = "name", property = "name", jdbcType = JdbcType.VARCHAR),
            @Result(column = "package_name", property = "packageName", jdbcType = JdbcType.VARCHAR),
            @Result(column = "version", property = "version", jdbcType = JdbcType.INTEGER),
            @Result(column = "user_id", property = "userId", jdbcType = JdbcType.INTEGER),
            @Result(column = "handle", property = "handle", jdbcType = JdbcType.INTEGER)
    })
    List<UserSoft> selectAll();

    @Update({
            "update user_soft",
            "set appkey = #{appkey,jdbcType=VARCHAR},",
            "name = #{name,jdbcType=VARCHAR},",
            "package_name = #{packageName,jdbcType=VARCHAR},",
            "version = #{version,jdbcType=INTEGER},",
            "user_id = #{userId,jdbcType=INTEGER},",
            "handle = #{handle,jdbcType=INTEGER}",
            "where id = #{id,jdbcType=INTEGER}"
    })
    int updateByPrimaryKey(UserSoft record);

    @Select("select * from user_soft WHERE user_id = #{user_id,jdbcType=INTEGER}")
    @Results({
            @Result(column = "id", property = "id", jdbcType = JdbcType.INTEGER, id = true)
    })
    List<UserSoft> findAllSoft(int user_id);

    @Select({"select * from user_soft a JOIN ( SELECT id FROM user_soft WHERE user_id = #{user_id,jdbcType=INTEGER} ORDER BY id DESC LIMIT #{offset,jdbcType=INTEGER}, #{limit,jdbcType=INTEGER} ) b ON a.id = b.id"})
    @Results({
            @Result(column = "id", property = "id", jdbcType = JdbcType.INTEGER, id = true),
            @Result(column = "appkey", property = "appkey", jdbcType = JdbcType.VARCHAR),
            @Result(column = "name", property = "name", jdbcType = JdbcType.VARCHAR),
            @Result(column = "package_name", property = "packageName", jdbcType = JdbcType.VARCHAR),
            @Result(column = "version", property = "version", jdbcType = JdbcType.INTEGER),
            @Result(column = "user_id", property = "userId", jdbcType = JdbcType.INTEGER),
            @Result(column = "handle", property = "handle", jdbcType = JdbcType.INTEGER)
    })
    List<UserSoft> findOffSetSoft(@Param("user_id") int user_id, @Param("offset") int offset, @Param("limit") int limit);

    @Select({"select * from user_soft a JOIN ( SELECT id FROM user_soft WHERE user_id = #{user_id,jdbcType=INTEGER} ORDER BY id DESC LIMIT #{offset,jdbcType=INTEGER}, #{limit,jdbcType=INTEGER} ) b ON a.id = b.id"})
    @Results({
            @Result(column = "id", property = "id", jdbcType = JdbcType.INTEGER, id = true),
            @Result(column = "appkey", property = "appkey", jdbcType = JdbcType.VARCHAR),
            @Result(column = "name", property = "name", jdbcType = JdbcType.VARCHAR),
            @Result(column = "package_name", property = "packageName", jdbcType = JdbcType.VARCHAR),
            @Result(column = "version", property = "version", jdbcType = JdbcType.INTEGER),
            @Result(column = "user_id", property = "userId", jdbcType = JdbcType.INTEGER),
            @Result(column = "handle", property = "handle", jdbcType = JdbcType.INTEGER)
    })
    List<Softs> findOffSetSofts(@Param("user_id") int user_id, @Param("offset") int offset, @Param("limit") int limit);

    @Select("select * from user_soft WHERE appkey = #{appkey,jdbcType=VARCHAR} limit 1")
    @Results({
            @Result(column = "id", property = "id", jdbcType = JdbcType.INTEGER, id = true),
            @Result(column = "appkey", property = "appkey", jdbcType = JdbcType.VARCHAR),
            @Result(column = "name", property = "name", jdbcType = JdbcType.VARCHAR),
            @Result(column = "package_name", property = "packageName", jdbcType = JdbcType.VARCHAR),
            @Result(column = "version", property = "version", jdbcType = JdbcType.INTEGER),
            @Result(column = "user_id", property = "userId", jdbcType = JdbcType.INTEGER),
            @Result(column = "handle", property = "handle", jdbcType = JdbcType.INTEGER)
    })
    UserSoft findSoftKey(String appkey);

    @Select("select * from user_soft WHERE user_id = #{user_id,jdbcType=INTEGER} and package_name = #{package_name,jdbcType=VARCHAR} limit 1")
    @Results({
            @Result(column = "id", property = "id", jdbcType = JdbcType.INTEGER, id = true),
            @Result(column = "appkey", property = "appkey", jdbcType = JdbcType.VARCHAR),
            @Result(column = "name", property = "name", jdbcType = JdbcType.VARCHAR),
            @Result(column = "package_name", property = "packageName", jdbcType = JdbcType.VARCHAR),
            @Result(column = "version", property = "version", jdbcType = JdbcType.INTEGER),
            @Result(column = "user_id", property = "userId", jdbcType = JdbcType.INTEGER),
            @Result(column = "handle", property = "handle", jdbcType = JdbcType.INTEGER)
    })
    UserSoft findUserIdAndPack(@Param("user_id") int user_id, @Param("package_name") String pack);
}