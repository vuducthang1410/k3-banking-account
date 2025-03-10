package com.system.transaction_service.mapper;

import com.system.common_library.dto.user.UserDetailDTO;
import com.system.transaction_service.dto.user.UserDetailCustom;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface UserDetailMapper {

    UserDetailCustom detailToDetailCustom(UserDetailDTO user);
}
