package icu.ydg.model.enums.message;

import lombok.Getter;
import org.apache.commons.lang3.ObjectUtils;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 消息读取状态枚举
 *
 * @author 袁德光
 * @date 2024/12/02
 */
@Getter
public enum MessageReadStatusEnums {
 //1：伙伴关注，2：帖子评论，3：私聊，4：队伍群聊，5：官方公共群聊
 UNREAD("未读", 0),
 READER("已读", 1);

 private final String text;

 private final Integer value;

 MessageReadStatusEnums(String text, Integer value) {
  this.text = text;
  this.value = value;
 }

 /**
  * 获取值列表
  *
  * @return {@code List<String>}
  */
 public static List<Integer> getValues() {
  return Arrays.stream(values()).map(item -> item.value).collect(Collectors.toList());
 }

 /**
  * 按值获取枚举
  * 根据 value 获取枚举
  *
  * @param value 价值
  * @return {@code UserGenderEnums}
  */
 public static MessageReadStatusEnums getEnumByValue(Integer value) {
  if (ObjectUtils.isEmpty(value)) {
   return null;
  }
  for (MessageReadStatusEnums anEnum : MessageReadStatusEnums.values()) {
   if (anEnum.value.equals(value)) {
    return anEnum;
   }
  }
  return null;
 }

}
