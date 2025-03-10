package com.system.customer_service.service.impl;

import com.system.customer_service.dto.request.KycRequest;
import com.system.customer_service.dto.response.KycResponse;
import com.system.customer_service.entity.Kyc;
import com.system.customer_service.enums.KycStatus;
import com.system.customer_service.exception.AppException;
import com.system.customer_service.exception.ErrorCode;
import com.system.customer_service.mapper.KycMapper;
import com.system.customer_service.repository.CustomerRepository;
import com.system.customer_service.repository.KycRepository;
import com.system.customer_service.service.KycService;
import com.system.customer_service.service.ProvinceService;
import com.system.customer_service.service.S3FileService;
import com.system.customer_service.util.CustomMultipartFile;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class KycServiceImpl implements KycService {
    KycRepository kycRepository;
    CustomerRepository customerRepository;
    ProvinceService provinceService;
    KycMapper kycMapper;
    S3FileService s3FileService;

    @Override
    public KycResponse getKycByCustomerId(String customerId) {
        // Trả về thông tin kyc của khách hàng hiện tại
        return kycRepository.findByCustomerId(customerId)
                .map(kycMapper::fromKyc).orElseThrow(() -> new AppException(ErrorCode.KYC_NOT_FOUND));
    }

    @Override
    public KycResponse updateKycStatus(String customerId, KycStatus kycStatus) {
        // Tìm Kyc khách hàng theo id
        Kyc kyc = kycRepository.findByCustomerId(customerId)
                .orElseThrow(() -> new AppException(ErrorCode.KYC_NOT_FOUND));

        // Cập nhật lại trạng thái kyc
        kyc.setKycStatus(kycStatus);
        this.kycRepository.save(kyc);

        return kycMapper.fromKyc(kyc);
    }
    public KycResponse updateKycStatus(Kyc kyc, KycStatus kycStatus) {
        // Cập nhật lại trạng thái kyc
        kyc.setKycStatus(kycStatus);
        this.kycRepository.save(kyc);

        return kycMapper.fromKyc(kyc);
    }

    @Transactional
    @Override
    public void createKyc(KycRequest kycRequest) throws Exception {
        String customerId = kycRequest.getCustomerId();
        // TÌm thông tin khách hàng
        CustomMultipartFile identityCardFrontFile = new CustomMultipartFile(kycRequest.getIdentityCardFront(),"cccd_front_" + customerId,"image/jpeg");
        CustomMultipartFile identityCardBackFile = new CustomMultipartFile(kycRequest.getIdentityCardBack(),"cccd_back_" + customerId,"image/jpeg");
        CustomMultipartFile avatarFile = new CustomMultipartFile(kycRequest.getAvatar(), "avatar_" + customerId,"image/jpeg");

        // UploadFile lên S3 và trả về url
        String identityCardFront = this.uploadImageToS3(identityCardFrontFile);
        String identityCardBack = this.uploadImageToS3(identityCardBackFile);
        String avatar = this.uploadImageToS3(avatarFile);
        log.info("Ảnh đã upload thành công");

        // TÌm khách hàng và lưu avatar
        var customer = this.customerRepository.findById(customerId)
                .orElseThrow(()-> new AppException(ErrorCode.USER_NOT_EXISTED));
        customer.setAvatar(avatar);
        this.customerRepository.save(customer);

        // Tạo kyc cho khách hàng nếu chưa có và cho trạng thái là PENDING
        Kyc kyc = Kyc.builder()
                .customer(customer)
                .phone(kycRequest.getPhone())
                .mail(kycRequest.getMail())
                .identityNumber(kycRequest.getIdentityNumber())
                .gender(kycRequest.getGender())
                .placeOrigin(kycRequest.getPlaceOrigin())
                .identityCardFront(identityCardFront)
                .identityCardBack(identityCardBack)
                .avatar(avatar)
                .kycStatus(KycStatus.PENDING)
                .build();

        // Lưu thông tin kyc
        this.kycRepository.save(kyc);
        log.info("KYC đã tạo thành công");
    }

    @Transactional
    @Override
    public void deleteByCustomerId(String customerId) {
        kycRepository.deleteByCustomerId(customerId);
    }

    private String uploadImageToS3(MultipartFile imageFile) throws Exception {
        if (imageFile != null && !imageFile.isEmpty()) {
            return s3FileService.uploadFile(imageFile);
        }
        return null;
    }

}
