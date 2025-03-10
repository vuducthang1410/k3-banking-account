package com.system.core_banking_service.service;

import com.system.common_library.dto.request.customer.CreateCustomerCoreDTO;
import com.system.common_library.dto.request.customer.UpdateCustomerCoreDTO;
import com.system.common_library.dto.response.PagedDTO;
import com.system.common_library.dto.response.customer.CustomerCoreDTO;
import com.system.common_library.dto.response.customer.CustomerExtraCoreDTO;
import com.system.core_banking_service.entity.Customer;
import com.system.core_banking_service.mapper.CustomerMapper;
import com.system.core_banking_service.repository.CustomerRepository;
import com.system.core_banking_service.service.interfaces.CustomerService;
import com.system.core_banking_service.service.interfaces.PagingService;
import com.system.core_banking_service.util.CIFGenerator;
import com.system.core_banking_service.util.Constant;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.security.InvalidParameterException;
import java.util.Optional;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class CustomerServiceImpl implements CustomerService {

    private final MessageSource messageSource;

    private final CustomerMapper customerMapper;

    private final PagingService pagingService;

    private final CustomerRepository customerRepository;

    @Override
    public CustomerExtraCoreDTO findById(String id) {

        log.info("Entering findById with parameters: id = {}", id);
        Optional<Customer> customer = customerRepository.findByIdAndStatus(id, true);

        return customer.map(customerMapper::entityToExtraDTO).orElse(null);
    }

    @Override
    public CustomerExtraCoreDTO findByCifCode(String cifCode) {

        log.info("Entering findByCifCode with parameters: cifCode = {}", cifCode);
        Optional<Customer> customer = customerRepository.findByCifCodeAndStatus(cifCode, true);

        return customer.map(customerMapper::entityToExtraDTO).orElse(null);
    }

    @Override
    public PagedDTO<CustomerCoreDTO> findAllByCondition(Boolean isActive, String search, String sort, int page, int limit) {

        log.info("Entering findAllByCondition with parameters: isActive = {}, search = {}, sort = {}, page = {}, limit = {}",
                isActive, search, sort, page, limit);
        Pageable pageable = pagingService.getPageable(sort, page, limit, Customer.class);
        Page<Customer> pageResult = customerRepository.findAllByCondition(true, isActive, search, pageable);

        return new PagedDTO<>(pageResult.map(customerMapper::entityToDTO));
    }

    @Override
    public CustomerCoreDTO create(CreateCustomerCoreDTO create) {

        try {

            log.info("Entering create with parameters: create = {}", create.toString());
//            if (customerRepository.existsByPhone(create.getPhone())) {
//
//                log.error("Duplicate phone number: {}", create.getPhone());
//                throw new InvalidParameterException(
//                        messageSource.getMessage(
//                                Constant.DUPLICATE_PHONE_NUMBER, null, LocaleContextHolder.getLocale()));
//            }

            Customer customer = customerMapper.createToEntity(create);

            // Re-generate CIF code if duplicate
            while (customerRepository.existsByCifCode(customer.getCifCode())) {

                log.warn("Duplicate CIF code: {}", customer.getCifCode());
                customer.setCifCode(CIFGenerator.generateCIFCode
                        (create.getGender(), create.getBirthday(), create.getPhone()));
            }

            log.info("Created a new CIF code: {}", customer.getCifCode());

            return customerMapper.entityToDTO(customerRepository.save(customer));
        } catch (Exception e) {

            log.error(e.getMessage());
            throw new InvalidParameterException(e instanceof InvalidParameterException ? e.getMessage() :
                    messageSource.getMessage(Constant.CREATE_FAIL, null, LocaleContextHolder.getLocale()));
        }
    }

    @Override
    public CustomerCoreDTO update(UpdateCustomerCoreDTO update, String cifCode) {

        log.info("Entering update with parameters: cifCode = {}, update = {}", cifCode, update.toString());
        Optional<Customer> entity = customerRepository.findByCifCodeAndStatus(cifCode, true);
        if (entity.isPresent()) {

            log.info("Customer is exist");
            try {

                Customer customer = customerMapper.updateToEntity(update, entity.get());
                return customerMapper.entityToDTO(customerRepository.save(customer));
            } catch (Exception e) {

                log.error(e.getMessage());
                throw new InvalidParameterException(
                        messageSource.getMessage(
                                Constant.UPDATE_FAIL, null, LocaleContextHolder.getLocale()));
            }
        } else {

            log.info("Customer is not exist");
            throw new InvalidParameterException(
                    messageSource.getMessage(Constant.INVALID_CUSTOMER, null, LocaleContextHolder.getLocale()));
        }
    }

    @Override
    public void delete(String id) {

        log.info("Entering delete with parameters: id = {}", id);
        Optional<Customer> customer = customerRepository.findByIdAndStatus(id, true);
        if (customer.isPresent()) {

            log.info("Customer is exist");
            try {

                customer.get().setStatus(false);
                log.info("Set status to false");
                customerRepository.save(customer.get());
            } catch (Exception e) {

                log.error(e.getMessage());
                throw new InvalidParameterException(
                        messageSource.getMessage(Constant.DELETE_FAIL, null, LocaleContextHolder.getLocale()));
            }
        } else {

            log.info("Customer is not exist");
            throw new InvalidParameterException(
                    messageSource.getMessage(Constant.INVALID_CUSTOMER, null, LocaleContextHolder.getLocale()));
        }
    }
}
