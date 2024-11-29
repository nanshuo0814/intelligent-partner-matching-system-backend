package icu.ydg.model.dto.user;

import lombok.Data;

@Data
public class UserGrowthRequest {
    private String date; // 如 "2024-01"
    private int userCount; // 该月份注册的用户数
}
