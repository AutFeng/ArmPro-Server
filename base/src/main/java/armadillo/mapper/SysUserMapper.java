package armadillo.mapper;

import armadillo.model.SysUser;
import org.apache.ibatis.annotations.*;
import org.apache.ibatis.type.JdbcType;

import java.util.List;

@CacheNamespace(blocking = true)
public interface SysUserMapper {
    @Delete({
            "delete from sys_user",
            "where id = #{id,jdbcType=INTEGER}"
    })
    int deleteByPrimaryKey(Integer id);

    @Insert({
            "insert into sys_user (id, username, ",
            "password, email, ",
            "token, openid, login_count, ",
            "expire_time, reg_time, ",
            "value)",
            "values (#{id,jdbcType=INTEGER}, #{username,jdbcType=VARCHAR}, ",
            "#{password,jdbcType=VARCHAR}, #{email,jdbcType=VARCHAR}, ",
            "#{token,jdbcType=VARCHAR}, #{openid,jdbcType=VARCHAR}, #{loginCount,jdbcType=INTEGER}, ",
            "#{expireTime,jdbcType=TIMESTAMP}, #{regTime,jdbcType=TIMESTAMP}, ",
            "#{value,jdbcType=INTEGER})"
    })
    int insert(SysUser record);

    @Select({
            "select",
            "id, username, password, email, token, openid, login_count, expire_time, reg_time, ",
            "value",
            "from sys_user",
            "where id = #{id,jdbcType=INTEGER}"
    })
    @Results({
            @Result(column="id", property="id", jdbcType=JdbcType.INTEGER, id=true),
            @Result(column="username", property="username", jdbcType=JdbcType.VARCHAR),
            @Result(column="password", property="password", jdbcType=JdbcType.VARCHAR),
            @Result(column="email", property="email", jdbcType=JdbcType.VARCHAR),
            @Result(column="token", property="token", jdbcType=JdbcType.VARCHAR),
            @Result(column="openid", property="openid", jdbcType=JdbcType.VARCHAR),
            @Result(column="login_count", property="loginCount", jdbcType=JdbcType.INTEGER),
            @Result(column="expire_time", property="expireTime", jdbcType=JdbcType.TIMESTAMP),
            @Result(column="reg_time", property="regTime", jdbcType=JdbcType.TIMESTAMP),
            @Result(column="value", property="value", jdbcType=JdbcType.INTEGER)
    })
    SysUser selectByPrimaryKey(Integer id);

    @Select({
            "select",
            "id, username, password, email, token, openid, login_count, expire_time, reg_time, ",
            "value",
            "from sys_user"
    })
    @Results({
            @Result(column="id", property="id", jdbcType=JdbcType.INTEGER, id=true),
            @Result(column="username", property="username", jdbcType=JdbcType.VARCHAR),
            @Result(column="password", property="password", jdbcType=JdbcType.VARCHAR),
            @Result(column="email", property="email", jdbcType=JdbcType.VARCHAR),
            @Result(column="token", property="token", jdbcType=JdbcType.VARCHAR),
            @Result(column="openid", property="openid", jdbcType=JdbcType.VARCHAR),
            @Result(column="login_count", property="loginCount", jdbcType=JdbcType.INTEGER),
            @Result(column="expire_time", property="expireTime", jdbcType=JdbcType.TIMESTAMP),
            @Result(column="reg_time", property="regTime", jdbcType=JdbcType.TIMESTAMP),
            @Result(column="value", property="value", jdbcType=JdbcType.INTEGER)
    })
    List<SysUser> selectAll();

    @Update({
            "update sys_user",
            "set username = #{username,jdbcType=VARCHAR},",
            "password = #{password,jdbcType=VARCHAR},",
            "email = #{email,jdbcType=VARCHAR},",
            "token = #{token,jdbcType=VARCHAR},",
            "openid = #{openid,jdbcType=VARCHAR},",
            "login_count = #{loginCount,jdbcType=INTEGER},",
            "expire_time = #{expireTime,jdbcType=TIMESTAMP},",
            "reg_time = #{regTime,jdbcType=TIMESTAMP},",
            "value = #{value,jdbcType=INTEGER}",
            "where id = #{id,jdbcType=INTEGER}"
    })
    int updateByPrimaryKey(SysUser record);

    @Select("SELECT * FROM sys_user WHERE openid LIKE #{openid,jdbcType=VARCHAR}")
    @Results({
            @Result(column="id", property="id", jdbcType=JdbcType.INTEGER, id=true),
            @Result(column="username", property="username", jdbcType=JdbcType.VARCHAR),
            @Result(column="password", property="password", jdbcType=JdbcType.VARCHAR),
            @Result(column="email", property="email", jdbcType=JdbcType.VARCHAR),
            @Result(column="token", property="token", jdbcType=JdbcType.VARCHAR),
            @Result(column="openid", property="openid", jdbcType=JdbcType.VARCHAR),
            @Result(column="login_count", property="loginCount", jdbcType=JdbcType.INTEGER),
            @Result(column="expire_time", property="expireTime", jdbcType=JdbcType.TIMESTAMP),
            @Result(column="reg_time", property="regTime", jdbcType=JdbcType.TIMESTAMP),
            @Result(column="value", property="value", jdbcType=JdbcType.INTEGER)
    })
    SysUser findUser(String openid);

    @Select("SELECT * FROM sys_user WHERE username LIKE #{username,jdbcType=VARCHAR}")
    @Results({
            @Result(column="id", property="id", jdbcType=JdbcType.INTEGER, id=true),
            @Result(column="username", property="username", jdbcType=JdbcType.VARCHAR),
            @Result(column="password", property="password", jdbcType=JdbcType.VARCHAR),
            @Result(column="email", property="email", jdbcType=JdbcType.VARCHAR),
            @Result(column="token", property="token", jdbcType=JdbcType.VARCHAR),
            @Result(column="openid", property="openid", jdbcType=JdbcType.VARCHAR),
            @Result(column="login_count", property="loginCount", jdbcType=JdbcType.INTEGER),
            @Result(column="expire_time", property="expireTime", jdbcType=JdbcType.TIMESTAMP),
            @Result(column="reg_time", property="regTime", jdbcType=JdbcType.TIMESTAMP),
            @Result(column="value", property="value", jdbcType=JdbcType.INTEGER)
    })
    SysUser findUserName(String username);

    @Select("SELECT * FROM sys_user WHERE email LIKE #{email,jdbcType=VARCHAR}")
    @Results({
            @Result(column="id", property="id", jdbcType=JdbcType.INTEGER, id=true),
            @Result(column="username", property="username", jdbcType=JdbcType.VARCHAR),
            @Result(column="password", property="password", jdbcType=JdbcType.VARCHAR),
            @Result(column="email", property="email", jdbcType=JdbcType.VARCHAR),
            @Result(column="token", property="token", jdbcType=JdbcType.VARCHAR),
            @Result(column="openid", property="openid", jdbcType=JdbcType.VARCHAR),
            @Result(column="login_count", property="loginCount", jdbcType=JdbcType.INTEGER),
            @Result(column="expire_time", property="expireTime", jdbcType=JdbcType.TIMESTAMP),
            @Result(column="reg_time", property="regTime", jdbcType=JdbcType.TIMESTAMP),
            @Result(column="value", property="value", jdbcType=JdbcType.INTEGER)
    })
    SysUser findEmail(String email);

    @Select("SELECT * FROM sys_user WHERE token LIKE #{token,jdbcType=VARCHAR}")
    @Results({
            @Result(column="id", property="id", jdbcType=JdbcType.INTEGER, id=true),
            @Result(column="username", property="username", jdbcType=JdbcType.VARCHAR),
            @Result(column="password", property="password", jdbcType=JdbcType.VARCHAR),
            @Result(column="email", property="email", jdbcType=JdbcType.VARCHAR),
            @Result(column="token", property="token", jdbcType=JdbcType.VARCHAR),
            @Result(column="openid", property="openid", jdbcType=JdbcType.VARCHAR),
            @Result(column="login_count", property="loginCount", jdbcType=JdbcType.INTEGER),
            @Result(column="expire_time", property="expireTime", jdbcType=JdbcType.TIMESTAMP),
            @Result(column="reg_time", property="regTime", jdbcType=JdbcType.TIMESTAMP),
            @Result(column="value", property="value", jdbcType=JdbcType.INTEGER)
    })
    SysUser findTokenUser(String token);
}
