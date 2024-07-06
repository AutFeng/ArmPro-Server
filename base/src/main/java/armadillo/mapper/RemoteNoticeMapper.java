package armadillo.mapper;

import armadillo.model.RemoteNotice;
import org.apache.ibatis.annotations.*;
import org.apache.ibatis.type.JdbcType;

import java.util.List;

@CacheNamespace(blocking = true)
public interface RemoteNoticeMapper {
    @Delete({
            "delete from remote_notice",
            "where cid = #{cid,jdbcType=INTEGER}"
    })
    int deleteByPrimaryKey(Integer cid);

    @Insert({
            "insert into remote_notice (cid, title, ",
            "msg, title_color, ",
            "msg_color, confirm_text, ",
            "confirm_text_color, confirm_action, ",
            "confirm_body, cancel_text, ",
            "cancel_text_color, extra_text, ",
            "extra_text_color, extra_action, ",
            "extra_body, dialog_style, ",
            "background_url, smart_pop, ",
            "cancelable, soft_id)",
            "values (#{cid,jdbcType=INTEGER}, #{title,jdbcType=VARCHAR}, ",
            "#{msg,jdbcType=VARCHAR}, #{titleColor,jdbcType=INTEGER}, ",
            "#{msgColor,jdbcType=INTEGER}, #{confirmText,jdbcType=VARCHAR}, ",
            "#{confirmTextColor,jdbcType=INTEGER}, #{confirmAction,jdbcType=INTEGER}, ",
            "#{confirmBody,jdbcType=VARCHAR}, #{cancelText,jdbcType=VARCHAR}, ",
            "#{cancelTextColor,jdbcType=INTEGER}, #{extraText,jdbcType=VARCHAR}, ",
            "#{extraTextColor,jdbcType=INTEGER}, #{extraAction,jdbcType=INTEGER}, ",
            "#{extraBody,jdbcType=VARCHAR}, #{dialogStyle,jdbcType=INTEGER}, ",
            "#{backgroundUrl,jdbcType=VARCHAR}, #{smartPop,jdbcType=BIT}, ",
            "#{cancelable,jdbcType=BIT}, #{softId,jdbcType=INTEGER})"
    })
    int insert(RemoteNotice record);

    @Select({
            "select",
            "cid, title, msg, title_color, msg_color, confirm_text, confirm_text_color, confirm_action, ",
            "confirm_body, cancel_text, cancel_text_color, extra_text, extra_text_color, ",
            "extra_action, extra_body, dialog_style, background_url, smart_pop, cancelable, ",
            "soft_id",
            "from remote_notice",
            "where cid = #{cid,jdbcType=INTEGER}"
    })
    @Results({
            @Result(column="cid", property="cid", jdbcType=JdbcType.INTEGER, id=true),
            @Result(column="title", property="title", jdbcType=JdbcType.VARCHAR),
            @Result(column="msg", property="msg", jdbcType=JdbcType.VARCHAR),
            @Result(column="title_color", property="titleColor", jdbcType=JdbcType.INTEGER),
            @Result(column="msg_color", property="msgColor", jdbcType=JdbcType.INTEGER),
            @Result(column="confirm_text", property="confirmText", jdbcType=JdbcType.VARCHAR),
            @Result(column="confirm_text_color", property="confirmTextColor", jdbcType=JdbcType.INTEGER),
            @Result(column="confirm_action", property="confirmAction", jdbcType=JdbcType.INTEGER),
            @Result(column="confirm_body", property="confirmBody", jdbcType=JdbcType.VARCHAR),
            @Result(column="cancel_text", property="cancelText", jdbcType=JdbcType.VARCHAR),
            @Result(column="cancel_text_color", property="cancelTextColor", jdbcType=JdbcType.INTEGER),
            @Result(column="extra_text", property="extraText", jdbcType=JdbcType.VARCHAR),
            @Result(column="extra_text_color", property="extraTextColor", jdbcType=JdbcType.INTEGER),
            @Result(column="extra_action", property="extraAction", jdbcType=JdbcType.INTEGER),
            @Result(column="extra_body", property="extraBody", jdbcType=JdbcType.VARCHAR),
            @Result(column="dialog_style", property="dialogStyle", jdbcType=JdbcType.INTEGER),
            @Result(column="background_url", property="backgroundUrl", jdbcType=JdbcType.VARCHAR),
            @Result(column="smart_pop", property="smartPop", jdbcType=JdbcType.BIT),
            @Result(column="cancelable", property="cancelable", jdbcType=JdbcType.BIT),
            @Result(column="soft_id", property="softId", jdbcType=JdbcType.INTEGER)
    })
    RemoteNotice selectByPrimaryKey(Integer cid);

    @Select({
            "select",
            "cid, title, msg, title_color, msg_color, confirm_text, confirm_text_color, confirm_action, ",
            "confirm_body, cancel_text, cancel_text_color, extra_text, extra_text_color, ",
            "extra_action, extra_body, dialog_style, background_url, smart_pop, cancelable, ",
            "soft_id",
            "from remote_notice"
    })
    @Results({
            @Result(column="cid", property="cid", jdbcType=JdbcType.INTEGER, id=true),
            @Result(column="title", property="title", jdbcType=JdbcType.VARCHAR),
            @Result(column="msg", property="msg", jdbcType=JdbcType.VARCHAR),
            @Result(column="title_color", property="titleColor", jdbcType=JdbcType.INTEGER),
            @Result(column="msg_color", property="msgColor", jdbcType=JdbcType.INTEGER),
            @Result(column="confirm_text", property="confirmText", jdbcType=JdbcType.VARCHAR),
            @Result(column="confirm_text_color", property="confirmTextColor", jdbcType=JdbcType.INTEGER),
            @Result(column="confirm_action", property="confirmAction", jdbcType=JdbcType.INTEGER),
            @Result(column="confirm_body", property="confirmBody", jdbcType=JdbcType.VARCHAR),
            @Result(column="cancel_text", property="cancelText", jdbcType=JdbcType.VARCHAR),
            @Result(column="cancel_text_color", property="cancelTextColor", jdbcType=JdbcType.INTEGER),
            @Result(column="extra_text", property="extraText", jdbcType=JdbcType.VARCHAR),
            @Result(column="extra_text_color", property="extraTextColor", jdbcType=JdbcType.INTEGER),
            @Result(column="extra_action", property="extraAction", jdbcType=JdbcType.INTEGER),
            @Result(column="extra_body", property="extraBody", jdbcType=JdbcType.VARCHAR),
            @Result(column="dialog_style", property="dialogStyle", jdbcType=JdbcType.INTEGER),
            @Result(column="background_url", property="backgroundUrl", jdbcType=JdbcType.VARCHAR),
            @Result(column="smart_pop", property="smartPop", jdbcType=JdbcType.BIT),
            @Result(column="cancelable", property="cancelable", jdbcType=JdbcType.BIT),
            @Result(column="soft_id", property="softId", jdbcType=JdbcType.INTEGER)
    })
    List<RemoteNotice> selectAll();

    @Update({
            "update remote_notice",
            "set title = #{title,jdbcType=VARCHAR},",
            "msg = #{msg,jdbcType=VARCHAR},",
            "title_color = #{titleColor,jdbcType=INTEGER},",
            "msg_color = #{msgColor,jdbcType=INTEGER},",
            "confirm_text = #{confirmText,jdbcType=VARCHAR},",
            "confirm_text_color = #{confirmTextColor,jdbcType=INTEGER},",
            "confirm_action = #{confirmAction,jdbcType=INTEGER},",
            "confirm_body = #{confirmBody,jdbcType=VARCHAR},",
            "cancel_text = #{cancelText,jdbcType=VARCHAR},",
            "cancel_text_color = #{cancelTextColor,jdbcType=INTEGER},",
            "extra_text = #{extraText,jdbcType=VARCHAR},",
            "extra_text_color = #{extraTextColor,jdbcType=INTEGER},",
            "extra_action = #{extraAction,jdbcType=INTEGER},",
            "extra_body = #{extraBody,jdbcType=VARCHAR},",
            "dialog_style = #{dialogStyle,jdbcType=INTEGER},",
            "background_url = #{backgroundUrl,jdbcType=VARCHAR},",
            "smart_pop = #{smartPop,jdbcType=BIT},",
            "cancelable = #{cancelable,jdbcType=BIT},",
            "soft_id = #{softId,jdbcType=INTEGER}",
            "where cid = #{cid,jdbcType=INTEGER}"
    })
    int updateByPrimaryKey(RemoteNotice record);

    @Select({
            "select",
            "cid, title, msg, title_color, msg_color, confirm_text, confirm_text_color, confirm_action, ",
            "confirm_body, cancel_text, cancel_text_color, extra_text, extra_text_color, ",
            "extra_action, extra_body, dialog_style, background_url, smart_pop, cancelable, ",
            "soft_id",
            "from remote_notice",
            "where soft_id = #{soft_id,jdbcType=INTEGER} limit 1"
    })
    @Results({
            @Result(column="cid", property="cid", jdbcType=JdbcType.INTEGER, id=true),
            @Result(column="title", property="title", jdbcType=JdbcType.VARCHAR),
            @Result(column="msg", property="msg", jdbcType=JdbcType.VARCHAR),
            @Result(column="title_color", property="titleColor", jdbcType=JdbcType.INTEGER),
            @Result(column="msg_color", property="msgColor", jdbcType=JdbcType.INTEGER),
            @Result(column="confirm_text", property="confirmText", jdbcType=JdbcType.VARCHAR),
            @Result(column="confirm_text_color", property="confirmTextColor", jdbcType=JdbcType.INTEGER),
            @Result(column="confirm_action", property="confirmAction", jdbcType=JdbcType.INTEGER),
            @Result(column="confirm_body", property="confirmBody", jdbcType=JdbcType.VARCHAR),
            @Result(column="cancel_text", property="cancelText", jdbcType=JdbcType.VARCHAR),
            @Result(column="cancel_text_color", property="cancelTextColor", jdbcType=JdbcType.INTEGER),
            @Result(column="extra_text", property="extraText", jdbcType=JdbcType.VARCHAR),
            @Result(column="extra_text_color", property="extraTextColor", jdbcType=JdbcType.INTEGER),
            @Result(column="extra_action", property="extraAction", jdbcType=JdbcType.INTEGER),
            @Result(column="extra_body", property="extraBody", jdbcType=JdbcType.VARCHAR),
            @Result(column="dialog_style", property="dialogStyle", jdbcType=JdbcType.INTEGER),
            @Result(column="background_url", property="backgroundUrl", jdbcType=JdbcType.VARCHAR),
            @Result(column="smart_pop", property="smartPop", jdbcType=JdbcType.BIT),
            @Result(column="cancelable", property="cancelable", jdbcType=JdbcType.BIT),
            @Result(column="soft_id", property="softId", jdbcType=JdbcType.INTEGER)
    })
    RemoteNotice selectBySoftId(Integer soft_id);

    @Delete({
            "delete from remote_notice",
            "where soft_id = #{soft_id,jdbcType=INTEGER}"
    })
    int deleteBySoftId(Integer soft_id);
}