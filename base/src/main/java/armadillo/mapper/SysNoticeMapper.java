package armadillo.mapper;

import armadillo.model.SysNotice;
import org.apache.ibatis.annotations.*;
import org.apache.ibatis.type.JdbcType;

import java.util.Date;
import java.util.List;

@CacheNamespace(blocking = true)
public interface SysNoticeMapper {
    @Delete({
            "delete from sys_notice",
            "where id = #{id,jdbcType=INTEGER}"
    })
    int deleteByPrimaryKey(Integer id);

    @Insert({
            "insert into sys_notice (id, title, ",
            "time, msg)",
            "values (#{id,jdbcType=INTEGER}, #{title,jdbcType=VARCHAR}, ",
            "#{time,jdbcType=TIMESTAMP}, #{msg,jdbcType=LONGVARCHAR})"
    })
    int insert(SysNotice record);

    @Select({
            "select",
            "id, title, time, msg",
            "from sys_notice",
            "where id = #{id,jdbcType=INTEGER}"
    })
    @Results({
            @Result(column = "id", property = "id", jdbcType = JdbcType.INTEGER, id = true),
            @Result(column = "title", property = "title", jdbcType = JdbcType.VARCHAR),
            @Result(column = "time", property = "time", jdbcType = JdbcType.TIMESTAMP),
            @Result(column = "msg", property = "msg", jdbcType = JdbcType.LONGVARCHAR)
    })
    SysNotice selectByPrimaryKey(Integer id);

    @Select({
            "select",
            "id, title, time, msg",
            "from sys_notice ORDER BY id DESC"
    })
    @Results({
            @Result(column = "id", property = "id", jdbcType = JdbcType.INTEGER, id = true),
            @Result(column = "title", property = "title", jdbcType = JdbcType.VARCHAR),
            @Result(column = "time", property = "time", jdbcType = JdbcType.TIMESTAMP),
            @Result(column = "msg", property = "msg", jdbcType = JdbcType.LONGVARCHAR)
    })
    //@Options(flushCache = Options.FlushCachePolicy.TRUE)
    List<SysNotice> selectAll();

    @Update({
            "update sys_notice",
            "set title = #{title,jdbcType=VARCHAR},",
            "time = #{time,jdbcType=TIMESTAMP},",
            "msg = #{msg,jdbcType=LONGVARCHAR}",
            "where id = #{id,jdbcType=INTEGER}"
    })
    int updateByPrimaryKey(SysNotice record);

    @Select({"select * from sys_notice where time > #{time,jdbcType=TIMESTAMP} order by id desc limit 1"})
    @Results({
            @Result(column = "id", property = "id", jdbcType = JdbcType.INTEGER, id = true),
            @Result(column = "title", property = "title", jdbcType = JdbcType.VARCHAR),
            @Result(column = "time", property = "time", jdbcType = JdbcType.TIMESTAMP),
            @Result(column = "msg", property = "msg", jdbcType = JdbcType.LONGVARCHAR)
    })
    List<SysNotice> LatNotices(Date time);
}
