package com.system.napas_service.mapper;

import com.system.napas_service.dto.bank.BankDTO;
import com.system.napas_service.dto.bank.BankExtraDTO;
import com.system.napas_service.dto.bank.CreateBankDTO;
import com.system.napas_service.dto.bank.UpdateBankDTO;
import com.system.napas_service.entity.Bank;
import de.huxhorn.sulky.ulid.ULID;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Named;

@Mapper(componentModel = "spring")
public interface BankMapper {

    BankDTO entityToDTO(Bank entity);

    BankExtraDTO entityToExtraDTO(Bank entity);

    @Mapping(target = "id", expression = "java(mapId())")
    @Mapping(target = "logo", ignore = true)
    @Mapping(target = "state", expression = "java(true)")
    @Mapping(target = "status", expression = "java(true)")
    Bank createToEntity(CreateBankDTO create);

    @Mapping(target = "logo", ignore = true)
    Bank updateToEntity(UpdateBankDTO update, @MappingTarget Bank entity);

    @Named("mapId")
    default String mapId() {

        return new ULID().nextULID();
    }
}
