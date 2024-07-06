package armadillo.mapper;

import armadillo.model.SysVer;
import org.apache.ibatis.annotations.*;
import org.apache.ibatis.type.JdbcType;

import java.util.List;

@CacheNamespace(blocking = true)
public interface SysVerMapper {
    @Delete({
            "delete from sys_ver",
            "where id = #{id,jdbcType=INTEGER}"
    })
    int deleteByPrimaryKey(Integer id);

    @Insert({
            "insert into sys_ver (id, version, ",
            "version_name, version_mode, ",
            "time, version_msg)",
            "values (#{id,jdbcType=INTEGER}, #{version,jdbcType=INTEGER}, ",
            "#{versionName,jdbcType=VARCHAR}, #{versionMode,jdbcType=BIT}, ",
            "#{time,jdbcType=TIMESTAMP}, #{versionMsg,jdbcType=LONGVARCHAR})"
    })
    int insert(SysVer record);

    @Select({
            "select",
            "id, version, version_name, version_mode, time, version_msg",
            "from sys_ver",
            "where id = #{id,jdbcType=INTEGER}"
    })
    @Results({
            @Result(column="id", property="id", jdbcType=JdbcType.INTEGER, id=true),
            @Result(column="version", property="version", jdbcType=JdbcType.INTEGER),
            @Result(column="version_name", property="versionName", jdbcType=JdbcType.VARCHAR),
            @Result(column="version_mode", property="versionMode", jdbcType=JdbcType.BIT),
            @Result(column="time", property="time", jdbcType=JdbcType.TIMESTAMP),
            @Result(column="version_msg", property="versionMsg", jdbcType=JdbcType.LONGVARCHAR)
    })
    SysVer selectByPrimaryKey(Integer id);

    @Select({
            "select",
            "id, version, version_name, version_mode, time, version_msg",
            "from sys_ver"
    })
    @Results({
            @Result(column="id", property="id", jdbcType=JdbcType.INTEGER, id=true),
            @Result(column="version", property="version", jdbcType=JdbcType.INTEGER),
            @Result(column="version_name", property="versionName", jdbcType=JdbcType.VARCHAR),
            @Result(column="version_mode", property="versionMode", jdbcType=JdbcType.BIT),
            @Result(column="time", property="time", jdbcType=JdbcType.TIMESTAMP),
            @Result(column="version_msg", property="versionMsg", jdbcType=JdbcType.LONGVARCHAR)
    })
    List<SysVer> selectAll();

    @Update({
            "update sys_ver",
            "set version = #{version,jdbcType=INTEGER},",
            "version_name = #{versionName,jdbcType=VARCHAR},",
            "version_mode = #{versionMode,jdbcType=BIT},",
            "time = #{time,jdbcType=TIMESTAMP},",
            "version_msg = #{versionMsg,jdbcType=LONGVARCHAR}",
            "where id = #{id,jdbcType=INTEGER}"
    })
    int updateByPrimaryKey(SysVer record);

    @Select("SELECT * FROM sys_ver WHERE version > #{ver,jdbcType=INTEGER} order by version desc limit 1")
    @Results({
            @Result(column="id", property="id", jdbcType=JdbcType.INTEGER, id=true),
            @Result(column="version", property="version", jdbcType=JdbcType.INTEGER),
            @Result(column="version_name", property="versionName", jdbcType=JdbcType.VARCHAR),
            @Result(column="version_mode", property="versionMode", jdbcType=JdbcType.BIT),
            @Result(column="time", property="time", jdbcType=JdbcType.TIMESTAMP),
            @Result(column="version_msg", property="versionMsg", jdbcType=JdbcType.LONGVARCHAR)
    })
    List<SysVer> findNewVer(Integer ver);
}