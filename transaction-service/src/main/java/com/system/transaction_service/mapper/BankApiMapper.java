package com.system.transaction_service.mapper;

import com.system.transaction_service.dto.response.BankApiDTO;
import com.system.transaction_service.entity.ExternalBank;
import de.huxhorn.sulky.ulid.ULID;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

@Mapper(componentModel = "spring")
public interface BankApiMapper {

    @Mapping(target = "id", expression = "java(mapId())")
    @Mapping(target = "swiftCode", source = "swift_code")
    @Mapping(target = "napasCode", source = "bin")
    @Mapping(target = "isAvailable", source = "isTransfer")
    @Mapping(target = "creatorId", expression = "java(\"Unknown\")")
    @Mapping(target = "state", expression = "java(true)")
    @Mapping(target = "status", expression = "java(true)")
    ExternalBank responseToEntity(BankApiDTO.BankResponse response);

    @Named("mapId")
    default String mapId() {

        return new ULID().nextULID();
    }
}
