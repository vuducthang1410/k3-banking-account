package com.system.napas_service.service;

import com.system.napas_service.dto.bank.BankDTO;
import com.system.napas_service.dto.bank.BankExtraDTO;
import com.system.napas_service.dto.bank.CreateBankDTO;
import com.system.napas_service.dto.bank.UpdateBankDTO;
import com.system.napas_service.dto.response.PagedDTO;
import com.system.napas_service.entity.Bank;
import com.system.napas_service.mapper.BankMapper;
import com.system.napas_service.repository.BankRepository;
import com.system.napas_service.service.interfaces.BankService;
import com.system.napas_service.service.interfaces.FileService;
import com.system.napas_service.service.interfaces.PagingService;
import com.system.napas_service.util.Constant;
import de.huxhorn.sulky.ulid.ULID;
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
public class BankServiceImpl implements BankService {

    private final MessageSource messageSource;

    private final BankMapper bankMapper;

    private final PagingService pagingService;

    private final FileService fileService;

    private final BankRepository bankRepository;

    private static final String FOLDER_NAME = "Napas/Bank";

    @Override
    public BankExtraDTO findById(String id) {

        log.info("Entering findById with parameters: id = {}", id);
        Optional<Bank> bank = bankRepository.findByIdAndStatus(id, true);

        return bank.map(bankMapper::entityToExtraDTO).orElse(null);
    }

    @Override
    public PagedDTO<BankDTO> findAllByCondition(
            Boolean isAvailable, String search, String sort, int page, int limit) {

        log.info("Entering findAllByCondition with parameters: " +
                "isAvailable = {}, search = {}, sort = {}, page = {}, limit = {}", isAvailable, search, sort, page, limit);
        Pageable pageable = pagingService.getPageable(sort, page, limit, Bank.class);
        Page<Bank> pageResult = bankRepository.findAllByCondition(true, isAvailable, search, pageable);

        return new PagedDTO<>(pageResult.map(bankMapper::entityToDTO));
    }

    @Override
    public void create(CreateBankDTO create) {

        try {

            log.info("Entering create with parameters: create = {}", create.toString());
            Bank bank = bankMapper.createToEntity(create);

            String fileName = FOLDER_NAME + "/" + new ULID().nextULID();
            String link = fileService.upload(create.getLogo(), fileName);
            if (!link.isBlank()) {

                bank.setLogo(link);
                bank.setLogoImageName(fileName);
                log.info("File name: {}", fileName);
                log.info("Link image: {}", link);
            }

            bankRepository.save(bank);
        } catch (Exception e) {

            log.error(e.getMessage());
            throw new InvalidParameterException(
                    messageSource.getMessage(Constant.CREATE_FAIL, null, LocaleContextHolder.getLocale()));
        }
    }

    @Override
    public void update(UpdateBankDTO update, String id) {

        log.info("Entering update with parameters: id = {}, update = {}", id, update.toString());
        Optional<Bank> bank = bankRepository.findByIdAndStatus(id, true);
        if (bank.isPresent()) {

            log.info("Bank is exist");
            try {

                if (!Optional.ofNullable(bank.get().getLogo()).orElse("").isBlank() &&
                        !Optional.ofNullable(bank.get().getLogoImageName()).orElse("").isBlank()) {

                    fileService.remove(bank.get().getLogoImageName());
                }

                String fileName = FOLDER_NAME + "/" + new ULID().nextULID();
                String link = fileService.upload(update.getLogo(), fileName);
                if (!link.isBlank()) {

                    bank.get().setLogo(link);
                    bank.get().setLogoImageName(fileName);
                    log.info("File name: {}", fileName);
                    log.info("Link image: {}", link);
                }

                bankRepository.save(bankMapper.updateToEntity(update, bank.get()));
            } catch (Exception e) {

                log.error(e.getMessage());
                throw new InvalidParameterException(
                        messageSource.getMessage(Constant.UPDATE_FAIL, null, LocaleContextHolder.getLocale()));
            }
        } else {

            log.error("Bank is not exist");
            throw new InvalidParameterException(
                    messageSource.getMessage(Constant.INVALID_BANK, null, LocaleContextHolder.getLocale()));
        }
    }

    @Override
    public void delete(String id) {

        log.info("Entering delete with parameters: id = {}", id);
        Optional<Bank> bank = bankRepository.findByIdAndStatus(id, true);
        if (bank.isPresent()) {

            log.info("Bank is exist");
            try {

                if (!bank.get().getLogo().isBlank() && !bank.get().getLogoImageName().isBlank()) {

                    fileService.remove(bank.get().getLogoImageName());
                }

                bank.get().setStatus(false);
                log.info("Set status to false");
                bankRepository.save(bank.get());
            } catch (Exception e) {

                log.error(e.getMessage());
                throw new InvalidParameterException(
                        messageSource.getMessage(Constant.DELETE_FAIL, null, LocaleContextHolder.getLocale()));
            }
        } else {

            log.info("Bank is not exist");
            throw new InvalidParameterException(
                    messageSource.getMessage(Constant.INVALID_BANK, null, LocaleContextHolder.getLocale()));
        }
    }
}
