package com.system.core_banking_service.mapper;

import com.system.common_library.dto.request.customer.CreateCustomerCoreDTO;
import com.system.common_library.dto.request.customer.UpdateCustomerCoreDTO;
import com.system.common_library.dto.response.customer.CustomerCoreDTO;
import com.system.common_library.dto.response.customer.CustomerExtraCoreDTO;
import com.system.common_library.enums.Gender;
import com.system.core_banking_service.entity.Customer;
import com.system.core_banking_service.util.CIFGenerator;
import de.huxhorn.sulky.ulid.ULID;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Named;

import java.time.LocalDate;

@Mapper(componentModel = "spring")
public interface CustomerMapper {

    CustomerCoreDTO entityToDTO(Customer entity);

    CustomerExtraCoreDTO entityToExtraDTO(Customer entity);

    @Mapping(target = "id", expression = "java(mapId())")
    @Mapping(target = "cifCode",
            expression = "java(mapCifCode(create.getGender(), create.getBirthday(), create.getPhone()))")
    @Mapping(target = "totalIncome", expression = "java(java.math.BigDecimal.ZERO)")
    @Mapping(target = "totalExpenditure", expression = "java(java.math.BigDecimal.ZERO)")
    @Mapping(target = "state", expression = "java(true)")
    @Mapping(target = "status", expression = "java(true)")
    Customer createToEntity(CreateCustomerCoreDTO create);

    Customer updateToEntity(UpdateCustomerCoreDTO update, @MappingTarget Customer entity);

    @Named("mapId")
    default String mapId() {

        return new ULID().nextULID();
    }

    @Named("mapCifCode")
    default String mapCifCode(Gender gender, LocalDate date, String phone) {

        return CIFGenerator.generateCIFCode(gender, date, phone);
    }
}
