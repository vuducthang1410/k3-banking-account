package com.system.napas_service.mapper;

import com.system.napas_service.dto.response.BankApiDTO;
import com.system.napas_service.entity.Bank;
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
    @Mapping(target = "state", expression = "java(true)")
    @Mapping(target = "status", expression = "java(true)")
    Bank responseToEntity(BankApiDTO.BankResponse response);

    @Named("mapId")
    default String mapId() {

        return new ULID().nextULID();
    }
}
