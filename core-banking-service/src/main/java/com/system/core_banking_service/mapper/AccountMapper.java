package com.system.core_banking_service.mapper;

import com.system.common_library.dto.request.account.CreateAccountCoreDTO;
import com.system.common_library.dto.request.account.UpdateAccountCoreDTO;
import com.system.common_library.dto.response.account.AccountCoreDTO;
import com.system.common_library.dto.response.account.AccountExtraCoreDTO;
import com.system.common_library.enums.AccountType;
import com.system.core_banking_service.entity.Account;
import com.system.core_banking_service.util.AccountNumberGenerator;
import de.huxhorn.sulky.ulid.ULID;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Named;

@Mapper(componentModel = "spring")
public interface AccountMapper {

    @Mapping(target = "account", source = "accountNumber")
    @Mapping(target = "accountType", source = "type")
    @Mapping(target = "customerId", source = "customer.id")
    @Mapping(target = "customerCifCode", source = "customer.cifCode")
    @Mapping(target = "customerName", source = "customer.fullName")
    @Mapping(target = "customerEmail", source = "customer.email")
    @Mapping(target = "customerPhone", source = "customer.phone")
    AccountCoreDTO entityToDTO(Account entity);

    @Mapping(target = "accountType", source = "type")
    @Mapping(target = "accountTypeName", source = "type", qualifiedByName = "mapTypeDescription")
    @Mapping(target = "customerId", source = "customer.id")
    @Mapping(target = "customerCifCode", source = "customer.cifCode")
    @Mapping(target = "customerName", source = "customer.fullName")
    @Mapping(target = "customerEmail", source = "customer.email")
    @Mapping(target = "customerPhone", source = "customer.phone")
    AccountExtraCoreDTO entityToExtraDTO(Account entity);

    @Mapping(target = "id", expression = "java(mapId())")
    @Mapping(target = "accountNumber", expression = "java(mapAccountNumber(create.getType(), create.getCifCode()))")
    @Mapping(target = "balance", expression = "java(java.math.BigDecimal.ZERO)")
    @Mapping(target = "availableBalance", expression = "java(java.math.BigDecimal.ZERO)")
    @Mapping(target = "totalIncome", expression = "java(java.math.BigDecimal.ZERO)")
    @Mapping(target = "totalExpenditure", expression = "java(java.math.BigDecimal.ZERO)")
    @Mapping(target = "state", expression = "java(true)")
    @Mapping(target = "status", expression = "java(true)")
    Account createToEntity(CreateAccountCoreDTO create);

    Account updateToEntity(UpdateAccountCoreDTO update, @MappingTarget Account entity);

    @Named("mapId")
    default String mapId() {

        return new ULID().nextULID();
    }

    @Named("mapTypeDescription")
    default String mapTypeDescription(AccountType type) {

        return type.getDescription();
    }

    @Named("mapAccountNumber")
    default String mapAccountNumber(AccountType type, String cifCode) {

        return AccountNumberGenerator.generateAccountNumber(type, cifCode);
    }
}
