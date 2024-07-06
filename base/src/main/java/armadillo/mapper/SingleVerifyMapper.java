package armadillo.mapper;

import armadillo.model.SingleVerify;
import org.apache.ibatis.annotations.*;
import org.apache.ibatis.type.JdbcType;

import java.util.List;

@CacheNamespace(blocking = true)
public interface SingleVerifyMapper {
    @Delete({
            "delete from single_verify",
            "where cid = #{cid,jdbcType=INTEGER}"
    })
    int deleteByPrimaryKey(Integer cid);

    @Insert({
            "insert into single_verify (cid, title, ",
            "msg, weburl, background_url, ",
            "confirm_text, cancel_text, ",
            "extra_text, title_text_color, ",
            "msg_text_color, confirm_text_color, ",
            "cancel_text_color, extra_text_color, ",
            "extra_action, try_count, ",
            "try_minutes, dialog_style, ",
            "bind_mode, soft_id)",
            "values (#{cid,jdbcType=INTEGER}, #{title,jdbcType=VARCHAR}, ",
            "#{msg,jdbcType=VARCHAR}, #{weburl,jdbcType=VARCHAR}, #{backgroundUrl,jdbcType=VARCHAR}, ",
            "#{confirmText,jdbcType=VARCHAR}, #{cancelText,jdbcType=VARCHAR}, ",
            "#{extraText,jdbcType=VARCHAR}, #{titleTextColor,jdbcType=INTEGER}, ",
            "#{msgTextColor,jdbcType=INTEGER}, #{confirmTextColor,jdbcType=INTEGER}, ",
            "#{cancelTextColor,jdbcType=INTEGER}, #{extraTextColor,jdbcType=INTEGER}, ",
            "#{extraAction,jdbcType=INTEGER}, #{tryCount,jdbcType=INTEGER}, ",
            "#{tryMinutes,jdbcType=INTEGER}, #{dialogStyle,jdbcType=INTEGER}, ",
            "#{bindMode,jdbcType=INTEGER}, #{softId,jdbcType=INTEGER})"
    })
    int insert(SingleVerify record);

    @Select({
            "select",
            "cid, title, msg, weburl, background_url, confirm_text, cancel_text, extra_text, ",
            "title_text_color, msg_text_color, confirm_text_color, cancel_text_color, extra_text_color, ",
            "extra_action, try_count, try_minutes, dialog_style, bind_mode, soft_id",
            "from single_verify",
            "where cid = #{cid,jdbcType=INTEGER}"
    })
    @Results({
            @Result(column = "cid", property = "cid", jdbcType = JdbcType.INTEGER, id = true),
            @Result(column = "title", property = "title", jdbcType = JdbcType.VARCHAR),
            @Result(column = "msg", property = "msg", jdbcType = JdbcType.VARCHAR),
            @Result(column = "weburl", property = "weburl", jdbcType = JdbcType.VARCHAR),
            @Result(column = "background_url", property = "backgroundUrl", jdbcType = JdbcType.VARCHAR),
            @Result(column = "confirm_text", property = "confirmText", jdbcType = JdbcType.VARCHAR),
            @Result(column = "cancel_text", property = "cancelText", jdbcType = JdbcType.VARCHAR),
            @Result(column = "extra_text", property = "extraText", jdbcType = JdbcType.VARCHAR),
            @Result(column = "title_text_color", property = "titleTextColor", jdbcType = JdbcType.INTEGER),
            @Result(column = "msg_text_color", property = "msgTextColor", jdbcType = JdbcType.INTEGER),
            @Result(column = "confirm_text_color", property = "confirmTextColor", jdbcType = JdbcType.INTEGER),
            @Result(column = "cancel_text_color", property = "cancelTextColor", jdbcType = JdbcType.INTEGER),
            @Result(column = "extra_text_color", property = "extraTextColor", jdbcType = JdbcType.INTEGER),
            @Result(column = "extra_action", property = "extraAction", jdbcType = JdbcType.INTEGER),
            @Result(column = "try_count", property = "tryCount", jdbcType = JdbcType.INTEGER),
            @Result(column = "try_minutes", property = "tryMinutes", jdbcType = JdbcType.INTEGER),
            @Result(column = "dialog_style", property = "dialogStyle", jdbcType = JdbcType.INTEGER),
            @Result(column = "bind_mode", property = "bindMode", jdbcType = JdbcType.INTEGER),
            @Result(column = "soft_id", property = "softId", jdbcType = JdbcType.INTEGER)
    })
    SingleVerify selectByPrimaryKey(Integer cid);

    @Select({
            "select",
            "cid, title, msg, weburl, background_url, confirm_text, cancel_text, extra_text, ",
            "title_text_color, msg_text_color, confirm_text_color, cancel_text_color, extra_text_color, ",
            "extra_action, try_count, try_minutes, dialog_style, bind_mode, soft_id",
            "from single_verify"
    })
    @Results({
            @Result(column = "cid", property = "cid", jdbcType = JdbcType.INTEGER, id = true),
            @Result(column = "title", property = "title", jdbcType = JdbcType.VARCHAR),
            @Result(column = "msg", property = "msg", jdbcType = JdbcType.VARCHAR),
            @Result(column = "weburl", property = "weburl", jdbcType = JdbcType.VARCHAR),
            @Result(column = "background_url", property = "backgroundUrl", jdbcType = JdbcType.VARCHAR),
            @Result(column = "confirm_text", property = "confirmText", jdbcType = JdbcType.VARCHAR),
            @Result(column = "cancel_text", property = "cancelText", jdbcType = JdbcType.VARCHAR),
            @Result(column = "extra_text", property = "extraText", jdbcType = JdbcType.VARCHAR),
            @Result(column = "title_text_color", property = "titleTextColor", jdbcType = JdbcType.INTEGER),
            @Result(column = "msg_text_color", property = "msgTextColor", jdbcType = JdbcType.INTEGER),
            @Result(column = "confirm_text_color", property = "confirmTextColor", jdbcType = JdbcType.INTEGER),
            @Result(column = "cancel_text_color", property = "cancelTextColor", jdbcType = JdbcType.INTEGER),
            @Result(column = "extra_text_color", property = "extraTextColor", jdbcType = JdbcType.INTEGER),
            @Result(column = "extra_action", property = "extraAction", jdbcType = JdbcType.INTEGER),
            @Result(column = "try_count", property = "tryCount", jdbcType = JdbcType.INTEGER),
            @Result(column = "try_minutes", property = "tryMinutes", jdbcType = JdbcType.INTEGER),
            @Result(column = "dialog_style", property = "dialogStyle", jdbcType = JdbcType.INTEGER),
            @Result(column = "bind_mode", property = "bindMode", jdbcType = JdbcType.INTEGER),
            @Result(column = "soft_id", property = "softId", jdbcType = JdbcType.INTEGER)
    })
    List<SingleVerify> selectAll();

    @Update({
            "update single_verify",
            "set title = #{title,jdbcType=VARCHAR},",
            "msg = #{msg,jdbcType=VARCHAR},",
            "weburl = #{weburl,jdbcType=VARCHAR},",
            "background_url = #{backgroundUrl,jdbcType=VARCHAR},",
            "confirm_text = #{confirmText,jdbcType=VARCHAR},",
            "cancel_text = #{cancelText,jdbcType=VARCHAR},",
            "extra_text = #{extraText,jdbcType=VARCHAR},",
            "title_text_color = #{titleTextColor,jdbcType=INTEGER},",
            "msg_text_color = #{msgTextColor,jdbcType=INTEGER},",
            "confirm_text_color = #{confirmTextColor,jdbcType=INTEGER},",
            "cancel_text_color = #{cancelTextColor,jdbcType=INTEGER},",
            "extra_text_color = #{extraTextColor,jdbcType=INTEGER},",
            "extra_action = #{extraAction,jdbcType=INTEGER},",
            "try_count = #{tryCount,jdbcType=INTEGER},",
            "try_minutes = #{tryMinutes,jdbcType=INTEGER},",
            "dialog_style = #{dialogStyle,jdbcType=INTEGER},",
            "bind_mode = #{bindMode,jdbcType=INTEGER},",
            "soft_id = #{softId,jdbcType=INTEGER}",
            "where cid = #{cid,jdbcType=INTEGER}"
    })
    int updateByPrimaryKey(SingleVerify record);

    @Select({
            "select",
            "cid, title, msg, weburl, background_url, confirm_text, cancel_text, extra_text, ",
            "title_text_color, msg_text_color, confirm_text_color, cancel_text_color, extra_text_color, ",
            "extra_action, try_count, try_minutes, dialog_style, bind_mode, soft_id",
            "from single_verify",
            "where soft_id = #{soft_id,jdbcType=INTEGER} limit 1"
    })
    @Results({
            @Result(column = "cid", property = "cid", jdbcType = JdbcType.INTEGER, id = true),
            @Result(column = "title", property = "title", jdbcType = JdbcType.VARCHAR),
            @Result(column = "msg", property = "msg", jdbcType = JdbcType.VARCHAR),
            @Result(column = "weburl", property = "weburl", jdbcType = JdbcType.VARCHAR),
            @Result(column = "background_url", property = "backgroundUrl", jdbcType = JdbcType.VARCHAR),
            @Result(column = "confirm_text", property = "confirmText", jdbcType = JdbcType.VARCHAR),
            @Result(column = "cancel_text", property = "cancelText", jdbcType = JdbcType.VARCHAR),
            @Result(column = "extra_text", property = "extraText", jdbcType = JdbcType.VARCHAR),
            @Result(column = "title_text_color", property = "titleTextColor", jdbcType = JdbcType.INTEGER),
            @Result(column = "msg_text_color", property = "msgTextColor", jdbcType = JdbcType.INTEGER),
            @Result(column = "confirm_text_color", property = "confirmTextColor", jdbcType = JdbcType.INTEGER),
            @Result(column = "cancel_text_color", property = "cancelTextColor", jdbcType = JdbcType.INTEGER),
            @Result(column = "extra_text_color", property = "extraTextColor", jdbcType = JdbcType.INTEGER),
            @Result(column = "extra_action", property = "extraAction", jdbcType = JdbcType.INTEGER),
            @Result(column = "try_count", property = "tryCount", jdbcType = JdbcType.INTEGER),
            @Result(column = "try_minutes", property = "tryMinutes", jdbcType = JdbcType.INTEGER),
            @Result(column = "dialog_style", property = "dialogStyle", jdbcType = JdbcType.INTEGER),
            @Result(column = "bind_mode", property = "bindMode", jdbcType = JdbcType.INTEGER),
            @Result(column = "soft_id", property = "softId", jdbcType = JdbcType.INTEGER)
    })
    SingleVerify selectBySoftId(Integer soft_id);

    @Delete({
            "delete from single_verify",
            "where soft_id = #{soft_id,jdbcType=INTEGER}"
    })
    int deleteBySoftId(Integer soft_id);
}