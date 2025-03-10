package com.system.transaction_service.mapper;

import com.system.transaction_service.dto.bank.CreateExternalBankDTO;
import com.system.transaction_service.dto.bank.ExternalBankDTO;
import com.system.transaction_service.dto.bank.ExternalBankExtraDTO;
import com.system.transaction_service.dto.bank.UpdateExternalBankDTO;
import com.system.transaction_service.entity.ExternalBank;
import de.huxhorn.sulky.ulid.ULID;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Named;

@Mapper(componentModel = "spring")
public interface ExternalBankMapper {

    ExternalBankDTO entityToDTO(ExternalBank entity);

    ExternalBankExtraDTO entityToExtraDTO(ExternalBank entity);

    @Mapping(target = "id", expression = "java(mapId())")
    @Mapping(target = "logo", ignore = true)
    @Mapping(target = "state", expression = "java(true)")
    @Mapping(target = "status", expression = "java(true)")
    ExternalBank createToEntity(CreateExternalBankDTO create);

    @Mapping(target = "logo", ignore = true)
    @Mapping(target = "state", expression = "java(true)")
    @Mapping(target = "status", expression = "java(true)")
    ExternalBank updateToEntity(UpdateExternalBankDTO update, @MappingTarget ExternalBank entity);

    @Named("mapId")
    default String mapId() {

        return new ULID().nextULID();
    }
}
