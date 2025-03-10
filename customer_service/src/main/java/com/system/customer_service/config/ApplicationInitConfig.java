package com.system.customer_service.config;

import com.system.customer_service.entity.Province;
import com.system.customer_service.repository.ProvinceRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class ApplicationInitConfig {

    @Bean
    @ConditionalOnProperty(
            prefix = "spring",
            value = "datasource.driverClassName",
            havingValue = "com.mysql.cj.jdbc.Driver")
    ApplicationRunner applicationRunner(ProvinceRepository provinceRepository) {
        log.info("Initializing application.....");
        return args -> {
            // Khởi tạo danh sách 63 tỉnh thành nếu chưa có
            if (provinceRepository.count() == 0) {
                List<Province> provinces = List.of(
                        new Province("001", "Hà Nội"),
                        new Province("002", "Hà Giang"),
                        new Province("004", "Cao Bằng"),
                        new Province("006", "Bắc Kạn"),
                        new Province("008", "Tuyên Quang"),
                        new Province("010", "Lào Cai"),
                        new Province("011", "Điện Biên"),
                        new Province("012", "Lai Châu"),
                        new Province("014", "Sơn La"),
                        new Province("015", "Yên Bái"),
                        new Province("017", "Hoà Bình"),
                        new Province("019", "Thái Nguyên"),
                        new Province("020", "Lạng Sơn"),
                        new Province("022", "Quảng Ninh"),
                        new Province("024", "Bắc Giang"),
                        new Province("025", "Phú Thọ"),
                        new Province("026", "Vĩnh Phúc"),
                        new Province("027", "Bắc Ninh"),
                        new Province("030", "Hải Dương"),
                        new Province("031", "Hải Phòng"),
                        new Province("033", "Hưng Yên"),
                        new Province("034", "Thái Bình"),
                        new Province("035", "Hà Nam"),
                        new Province("036", "Nam Định"),
                        new Province("037", "Ninh Bình"),
                        new Province("038", "Thanh Hóa"),
                        new Province("040", "Nghệ An"),
                        new Province("042", "Hà Tĩnh"),
                        new Province("044", "Quảng Bình"),
                        new Province("045", "Quảng Trị"),
                        new Province("046", "Thừa Thiên Huế"),
                        new Province("048", "Đà Nẵng"),
                        new Province("049", "Quảng Nam"),
                        new Province("051", "Quảng Ngãi"),
                        new Province("052", "Bình Định"),
                        new Province("054", "Phú Yên"),
                        new Province("056", "Khánh Hòa"),
                        new Province("058", "Ninh Thuận"),
                        new Province("060", "Bình Thuận"),
                        new Province("062", "Kon Tum"),
                        new Province("064", "Gia Lai"),
                        new Province("066", "Đắk Lắk"),
                        new Province("067", "Đắk Nông"),
                        new Province("068", "Lâm Đồng"),
                        new Province("070", "Bình Phước"),
                        new Province("072", "Tây Ninh"),
                        new Province("074", "Bình Dương"),
                        new Province("075", "Đồng Nai"),
                        new Province("077", "Bà Rịa - Vũng Tàu"),
                        new Province("079", "Hồ Chí Minh"),
                        new Province("080", "Long An"),
                        new Province("082", "Tiền Giang"),
                        new Province("083", "Bến Tre"),
                        new Province("084", "Trà Vinh"),
                        new Province("086", "Vĩnh Long"),
                        new Province("087", "Đồng Tháp"),
                        new Province("089", "An Giang"),
                        new Province("091", "Kiên Giang"),
                        new Province("092", "Cần Thơ"),
                        new Province("093", "Hậu Giang"),
                        new Province("094", "Sóc Trăng"),
                        new Province("095", "Bạc Liêu"),
                        new Province("096", "Cà Mau")

                );

                provinceRepository.saveAll(provinces);
                log.info("Initialized 63 provinces.");
            }

            log.info("Application initialization completed .....");
        };
    }
}
