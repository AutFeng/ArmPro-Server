package armadillo.mapper;

import armadillo.model.SoftAdmob;
import org.apache.ibatis.annotations.*;
import org.apache.ibatis.type.JdbcType;

import java.util.List;

@CacheNamespace(blocking = true)
public interface SoftAdmobMapper {
    @Delete({
            "delete from soft_admob",
            "where cid = #{cid,jdbcType=INTEGER}"
    })
    int deleteByPrimaryKey(Integer cid);

    @Insert({
            "insert into soft_admob (cid, handle, ",
            "banner_ids, interstitial_ids, ",
            "rewarded_ids, open_ids, ",
            "rules, soft_id)",
            "values (#{cid,jdbcType=INTEGER}, #{handle,jdbcType=INTEGER}, ",
            "#{bannerIds,jdbcType=VARCHAR}, #{interstitialIds,jdbcType=VARCHAR}, ",
            "#{rewardedIds,jdbcType=VARCHAR}, #{openIds,jdbcType=VARCHAR}, ",
            "#{rules,jdbcType=VARCHAR}, #{softId,jdbcType=INTEGER})"
    })
    int insert(SoftAdmob record);

    @Select({
            "select",
            "cid, handle, banner_ids, interstitial_ids, rewarded_ids, open_ids, rules, soft_id",
            "from soft_admob",
            "where cid = #{cid,jdbcType=INTEGER}"
    })
    @Results({
            @Result(column="cid", property="cid", jdbcType=JdbcType.INTEGER, id=true),
            @Result(column="handle", property="handle", jdbcType=JdbcType.INTEGER),
            @Result(column="banner_ids", property="bannerIds", jdbcType=JdbcType.VARCHAR),
            @Result(column="interstitial_ids", property="interstitialIds", jdbcType=JdbcType.VARCHAR),
            @Result(column="rewarded_ids", property="rewardedIds", jdbcType=JdbcType.VARCHAR),
            @Result(column="open_ids", property="openIds", jdbcType=JdbcType.VARCHAR),
            @Result(column="rules", property="rules", jdbcType=JdbcType.VARCHAR),
            @Result(column="soft_id", property="softId", jdbcType=JdbcType.INTEGER)
    })
    SoftAdmob selectByPrimaryKey(Integer cid);

    @Select({
            "select",
            "cid, handle, banner_ids, interstitial_ids, rewarded_ids, open_ids, rules, soft_id",
            "from soft_admob"
    })
    @Results({
            @Result(column="cid", property="cid", jdbcType=JdbcType.INTEGER, id=true),
            @Result(column="handle", property="handle", jdbcType=JdbcType.INTEGER),
            @Result(column="banner_ids", property="bannerIds", jdbcType=JdbcType.VARCHAR),
            @Result(column="interstitial_ids", property="interstitialIds", jdbcType=JdbcType.VARCHAR),
            @Result(column="rewarded_ids", property="rewardedIds", jdbcType=JdbcType.VARCHAR),
            @Result(column="open_ids", property="openIds", jdbcType=JdbcType.VARCHAR),
            @Result(column="rules", property="rules", jdbcType=JdbcType.VARCHAR),
            @Result(column="soft_id", property="softId", jdbcType=JdbcType.INTEGER)
    })
    List<SoftAdmob> selectAll();

    @Update({
            "update soft_admob",
            "set handle = #{handle,jdbcType=INTEGER},",
            "banner_ids = #{bannerIds,jdbcType=VARCHAR},",
            "interstitial_ids = #{interstitialIds,jdbcType=VARCHAR},",
            "rewarded_ids = #{rewardedIds,jdbcType=VARCHAR},",
            "open_ids = #{openIds,jdbcType=VARCHAR},",
            "rules = #{rules,jdbcType=VARCHAR},",
            "soft_id = #{softId,jdbcType=INTEGER}",
            "where cid = #{cid,jdbcType=INTEGER}"
    })
    int updateByPrimaryKey(SoftAdmob record);

    @Select({
            "select",
            "cid, handle, banner_ids, interstitial_ids, rewarded_ids, open_ids, rules, soft_id",
            "from soft_admob",
            "where soft_id = #{soft_id,jdbcType=INTEGER} limit 1"
    })
    @Results({
            @Result(column="cid", property="cid", jdbcType=JdbcType.INTEGER, id=true),
            @Result(column="handle", property="handle", jdbcType=JdbcType.INTEGER),
            @Result(column="banner_ids", property="bannerIds", jdbcType=JdbcType.VARCHAR),
            @Result(column="interstitial_ids", property="interstitialIds", jdbcType=JdbcType.VARCHAR),
            @Result(column="rewarded_ids", property="rewardedIds", jdbcType=JdbcType.VARCHAR),
            @Result(column="open_ids", property="openIds", jdbcType=JdbcType.VARCHAR),
            @Result(column="rules", property="rules", jdbcType=JdbcType.VARCHAR),
            @Result(column="soft_id", property="softId", jdbcType=JdbcType.INTEGER)
    })
    SoftAdmob selectBySoftId(Integer soft_id);

    @Delete({
            "delete from soft_admob",
            "where soft_id = #{soft_id,jdbcType=INTEGER}"
    })
    int deleteBySoftId(Integer soft_id);
}