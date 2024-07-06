package armadillo.mapper;

import armadillo.model.SoftCustom;
import org.apache.ibatis.annotations.*;
import org.apache.ibatis.type.JdbcType;

import java.util.List;

@CacheNamespace(blocking = true)
public interface SoftCustomMapper {
    @Delete({
        "delete from soft_custom",
        "where cid = #{cid,jdbcType=INTEGER}"
    })
    int deleteByPrimaryKey(Integer cid);

    @Insert({
        "insert into soft_custom (cid, custom_loader_mode, ",
        "custom_loader_path, custom_invoke_mode, ",
        "custom_invoke_rule, soft_id)",
        "values (#{cid,jdbcType=INTEGER}, #{customLoaderMode,jdbcType=INTEGER}, ",
        "#{customLoaderPath,jdbcType=VARCHAR}, #{customInvokeMode,jdbcType=INTEGER}, ",
        "#{customInvokeRule,jdbcType=VARCHAR}, #{softId,jdbcType=INTEGER})"
    })
    int insert(SoftCustom record);

    @Select({
        "select",
        "cid, custom_loader_mode, custom_loader_path, custom_invoke_mode, custom_invoke_rule, ",
        "soft_id",
        "from soft_custom",
        "where cid = #{cid,jdbcType=INTEGER}"
    })
    @Results({
        @Result(column="cid", property="cid", jdbcType=JdbcType.INTEGER, id=true),
        @Result(column="custom_loader_mode", property="customLoaderMode", jdbcType=JdbcType.INTEGER),
        @Result(column="custom_loader_path", property="customLoaderPath", jdbcType=JdbcType.VARCHAR),
        @Result(column="custom_invoke_mode", property="customInvokeMode", jdbcType=JdbcType.INTEGER),
        @Result(column="custom_invoke_rule", property="customInvokeRule", jdbcType=JdbcType.VARCHAR),
        @Result(column="soft_id", property="softId", jdbcType=JdbcType.INTEGER)
    })
    SoftCustom selectByPrimaryKey(Integer cid);

    @Select({
        "select",
        "cid, custom_loader_mode, custom_loader_path, custom_invoke_mode, custom_invoke_rule, ",
        "soft_id",
        "from soft_custom"
    })
    @Results({
        @Result(column="cid", property="cid", jdbcType=JdbcType.INTEGER, id=true),
        @Result(column="custom_loader_mode", property="customLoaderMode", jdbcType=JdbcType.INTEGER),
        @Result(column="custom_loader_path", property="customLoaderPath", jdbcType=JdbcType.VARCHAR),
        @Result(column="custom_invoke_mode", property="customInvokeMode", jdbcType=JdbcType.INTEGER),
        @Result(column="custom_invoke_rule", property="customInvokeRule", jdbcType=JdbcType.VARCHAR),
        @Result(column="soft_id", property="softId", jdbcType=JdbcType.INTEGER)
    })
    List<SoftCustom> selectAll();

    @Update({
        "update soft_custom",
        "set custom_loader_mode = #{customLoaderMode,jdbcType=INTEGER},",
          "custom_loader_path = #{customLoaderPath,jdbcType=VARCHAR},",
          "custom_invoke_mode = #{customInvokeMode,jdbcType=INTEGER},",
          "custom_invoke_rule = #{customInvokeRule,jdbcType=VARCHAR},",
          "soft_id = #{softId,jdbcType=INTEGER}",
        "where cid = #{cid,jdbcType=INTEGER}"
    })
    int updateByPrimaryKey(SoftCustom record);

    @Select({
            "select",
            "cid, custom_loader_mode, custom_loader_path, custom_invoke_mode, custom_invoke_rule, ",
            "soft_id",
            "from soft_custom",
            "where soft_id = #{soft_id,jdbcType=INTEGER} limit 1"
    })
    @Results({
            @Result(column="cid", property="cid", jdbcType=JdbcType.INTEGER, id=true),
            @Result(column="custom_loader_mode", property="customLoaderMode", jdbcType=JdbcType.INTEGER),
            @Result(column="custom_loader_path", property="customLoaderPath", jdbcType=JdbcType.VARCHAR),
            @Result(column="custom_invoke_mode", property="customInvokeMode", jdbcType=JdbcType.INTEGER),
            @Result(column="custom_invoke_rule", property="customInvokeRule", jdbcType=JdbcType.VARCHAR),
            @Result(column="soft_id", property="softId", jdbcType=JdbcType.INTEGER)
    })
    SoftCustom selectBySoftId(Integer soft_id);

    @Delete({
            "delete from soft_custom",
            "where soft_id = #{soft_id,jdbcType=INTEGER}"
    })
    int deleteBySoftId(Integer soft_id);
}