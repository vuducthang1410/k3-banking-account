package com.system.transaction_service.service;

import com.system.transaction_service.dto.bank.CreateExternalBankDTO;
import com.system.transaction_service.dto.bank.ExternalBankDTO;
import com.system.transaction_service.dto.bank.ExternalBankExtraDTO;
import com.system.transaction_service.dto.bank.UpdateExternalBankDTO;
import com.system.transaction_service.dto.response.PagedDTO;
import com.system.transaction_service.entity.ExternalBank;
import com.system.transaction_service.mapper.ExternalBankMapper;
import com.system.transaction_service.repository.ExternalBankRepository;
import com.system.transaction_service.service.interfaces.ExternalBankService;
import com.system.transaction_service.service.interfaces.FileService;
import com.system.transaction_service.service.interfaces.PagingService;
import com.system.transaction_service.util.Constant;
import de.huxhorn.sulky.ulid.ULID;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
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
public class ExternalBankServiceImpl implements ExternalBankService {

    private final MessageSource messageSource;

    private final ExternalBankMapper externalBankMapper;

    private final PagingService pagingService;

    private final FileService fileService;

    private final ExternalBankRepository externalBankRepository;

    private static final String FOLDER_NAME = "Bank";

    @Override
    @Cacheable(cacheNames = "external_banks:detail", key = "#id")
    public ExternalBankExtraDTO findById(String id) {

        log.info("Entering findById with parameters: id = {}", id);
        Optional<ExternalBank> bank = externalBankRepository.findByIdAndStatus(id, true);

        return bank.map(externalBankMapper::entityToExtraDTO).orElse(null);
    }

    @Override
    @Cacheable(cacheNames = "external_banks:list")
    public PagedDTO<ExternalBankDTO> findAllByCondition(
            Boolean isAvailable, String search, String sort, int page, int limit) {

        log.info("Entering findAllByCondition with parameters: " +
                        "isAvailable = {}, search = {}, sort = {}, page = {}, limit = {}",
                isAvailable, search, sort, page, limit);
        Pageable pageable = pagingService.getPageable(sort, page, limit, ExternalBank.class);
        Page<ExternalBank> pageResult = externalBankRepository.findAllByCondition(true, isAvailable, search, pageable);

        return new PagedDTO<>(pageResult.map(externalBankMapper::entityToDTO));
    }

    @Override
    public void create(CreateExternalBankDTO create) {

        try {

            log.info("Entering create with parameters: create = {}", create.toString());
            ExternalBank bank = externalBankMapper.createToEntity(create);

            String fileName = FOLDER_NAME + "/" + new ULID().nextULID();
            String link = fileService.upload(create.getLogo(), fileName);
            if (!link.isBlank()) {

                bank.setLogo(link);
                bank.setLogoImageName(fileName);
            }

            log.info("Created a new external bank: {}", bank.toString());
            externalBankRepository.save(bank);
        } catch (Exception e) {

            log.error(e.getMessage());
            throw new InvalidParameterException(
                    messageSource.getMessage(Constant.CREATE_FAIL, null, LocaleContextHolder.getLocale()));
        }
    }

    @Override
    @CachePut(cacheNames = "external_banks:detail", key = "#id")
    public ExternalBankExtraDTO update(UpdateExternalBankDTO update, String id) {

        log.info("Entering update with parameters: id = {}, update = {}", id, update.toString());
        Optional<ExternalBank> bank = externalBankRepository.findByIdAndStatus(id, true);
        if (bank.isPresent()) {

            try {

                log.info("Bank is exist");
                if (!Optional.ofNullable(bank.get().getLogo()).orElse("").isBlank() &&
                        !Optional.ofNullable(bank.get().getLogoImageName()).orElse("").isBlank()) {

                    fileService.remove(bank.get().getLogoImageName());
                }

                String fileName = FOLDER_NAME + "/" + new ULID().nextULID();
                String link = fileService.upload(update.getLogo(), fileName);
                if (!link.isBlank()) {

                    bank.get().setLogo(link);
                    bank.get().setLogoImageName(fileName);
                }

                ExternalBank entity = externalBankRepository.save(externalBankMapper.updateToEntity(update, bank.get()));
                return externalBankMapper.entityToExtraDTO(entity);
            } catch (Exception e) {

                log.error(e.getMessage());
                throw new InvalidParameterException(
                        messageSource.getMessage(Constant.UPDATE_FAIL, null, LocaleContextHolder.getLocale()));
            }
        } else {

            log.info("Invalid external bank");
            throw new InvalidParameterException(
                    messageSource.getMessage(Constant.INVALID_EXTERNAL_BANK, null, LocaleContextHolder.getLocale()));
        }
    }

    @Override
    @CacheEvict(cacheNames = "external_banks:detail", key = "#id")
    public void delete(String id) {

        log.info("Entering delete with parameters: id = {}", id);
        Optional<ExternalBank> bank = externalBankRepository.findByIdAndStatus(id, true);
        if (bank.isPresent()) {

            log.info("Bank is exist");
            try {

                if (!bank.get().getLogo().isBlank() && !bank.get().getLogoImageName().isBlank()) {

                    fileService.remove(bank.get().getLogoImageName());
                }

                bank.get().setStatus(false);
                log.info("Set status to false");
                externalBankRepository.save(bank.get());
            } catch (Exception e) {

                log.error(e.getMessage());
                throw new InvalidParameterException(
                        messageSource.getMessage(Constant.DELETE_FAIL, null, LocaleContextHolder.getLocale()));
            }
        } else {

            log.info("Invalid external bank");
            throw new InvalidParameterException(
                    messageSource.getMessage(Constant.INVALID_EXTERNAL_BANK, null, LocaleContextHolder.getLocale()));
        }
    }
}
