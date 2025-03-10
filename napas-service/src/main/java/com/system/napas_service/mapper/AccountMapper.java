package com.system.napas_service.mapper;

import com.system.common_library.dto.response.account.AccountExtraNapasDTO;
import com.system.common_library.dto.response.account.AccountNapasDTO;
import com.system.napas_service.dto.account.AccountDTO;
import com.system.napas_service.dto.account.AccountExtraDTO;
import com.system.napas_service.dto.account.CreateAccountDTO;
import com.system.napas_service.dto.account.UpdateAccountDTO;
import com.system.napas_service.entity.Account;
import com.system.napas_service.entity.Bank;
import de.huxhorn.sulky.ulid.ULID;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Named;

@Mapper(componentModel = "spring")
public interface AccountMapper {

    @Mapping(target = "bankId", source = "bank.id")
    @Mapping(target = "bankName", source = "bank.name")
    AccountDTO entityToDTO(Account entity);

    @Mapping(target = "bankId", source = "bank.id")
    @Mapping(target = "bankName", source = "bank.name")
    @Mapping(target = "bankCode", source = "bank.code")
    @Mapping(target = "bankNapasCode", source = "bank.napasCode")
    @Mapping(target = "bankSwiftCode", source = "bank.swiftCode")
    @Mapping(target = "bankLogo", source = "bank.logo")
    AccountExtraDTO entityToExtraDTO(Account entity);

    @Mapping(target = "bankId", source = "bank.id")
    @Mapping(target = "bankName", source = "bank.name")
    AccountNapasDTO entityToNapasDTO(Account entity);

    @Mapping(target = "bankId", source = "bank.id")
    @Mapping(target = "bankName", source = "bank.name")
    @Mapping(target = "bankCode", source = "bank.code")
    @Mapping(target = "bankNapasCode", source = "bank.napasCode")
    @Mapping(target = "bankSwiftCode", source = "bank.swiftCode")
    @Mapping(target = "bankLogo", source = "bank.logo")
    AccountExtraNapasDTO entityToExtraNapasDTO(Account entity);

    @Mapping(target = "id", expression = "java(mapId())")
    @Mapping(target = "bank", source = "bankId", qualifiedByName = "mapBank")
    @Mapping(target = "totalIncome", source = "balance")
    @Mapping(target = "totalExpenditure", expression = "java(java.math.BigDecimal.ZERO)")
    @Mapping(target = "state", expression = "java(true)")
    @Mapping(target = "status", expression = "java(true)")
    Account createToEntity(CreateAccountDTO create);

    Account updateToEntity(UpdateAccountDTO update, @MappingTarget Account entity);

    @Named("mapBank")
    default Bank mapBank(String id) {

        Bank bank = new Bank();
        bank.setId(id);
        return bank;
    }

    @Named("mapId")
    default String mapId() {

        return new ULID().nextULID();
    }
}
