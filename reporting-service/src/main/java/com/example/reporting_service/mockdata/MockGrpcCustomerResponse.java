package com.example.reporting_service.mockdata;

import com.system.common_library.dto.user.CustomerDetailDTO;
import com.system.common_library.enums.ObjectStatus;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class MockGrpcCustomerResponse {
    private static final String[] FIRST_NAMES = {"Nguyễn", "Trần", "Lê", "Phạm", "Hoàng", "Vũ", "Đặng", "Bùi", "Đỗ", "Hồ"};
    private static final String[] LAST_NAMES = {"An", "Bình", "Châu", "Dũng", "Hạnh", "Khoa", "Linh", "Minh", "Nhân", "Quý"};
    private static final String[] GENDERS = {"Nam", "Nữ"};
    private static final String[] ADDRESSES = {
            "123 Đường ABC, Hà Nội",
            "456 Đường XYZ, TP HCM",
            "789 Đường DEF, Đà Nẵng",
            "321 Đường LMN, Hải Phòng"
    };

    public static CustomerDetailDTO getSingleMockCustomer() {
        return CustomerDetailDTO.builder()
                .customerId("CUST001")
                .cifCode("CIF123456")
                .phone("0987654321")
                .address("123 Đường ABC, Hà Nội")
                .dob(LocalDate.of(1990, 5, 15))
                .mail("customer1@example.com")
                .fullName("Nguyễn Văn A")
                .firstName("Nguyễn")
                .lastName("A")
                .identityCard("012345678901")
                .gender("Nam")
                .isActive(true)
                .status(ObjectStatus.ACTIVE)
                .customerNumber("CUST-0001")
                .build();
    }

    public static List<CustomerDetailDTO> generateMockCustomers(int count) {
        List<CustomerDetailDTO> customers = new ArrayList<>();
        Random random = new Random();

        for (int i = 1; i <= count; i++) {
            String firstName = FIRST_NAMES[random.nextInt(FIRST_NAMES.length)];
            String lastName = LAST_NAMES[random.nextInt(LAST_NAMES.length)];
            String fullName = firstName + " " + lastName;

            customers.add(CustomerDetailDTO.builder()
                    .customerId("CUST" + String.format("%03d", i))
                    .cifCode("CIF" + (100000 + i))
                    .phone("09" + (random.nextInt(90000000) + 10000000)) // Sinh số điện thoại ngẫu nhiên
                    .address(ADDRESSES[random.nextInt(ADDRESSES.length)])
                    .dob(LocalDate.of(1980 + random.nextInt(30), random.nextInt(12) + 1, random.nextInt(28) + 1))
                    .mail("customer" + i + "@example.com")
                    .fullName(fullName)
                    .firstName(firstName)
                    .lastName(lastName)
                    .identityCard("0" + (random.nextInt(900000000) + 100000000)) // CMND/CCCD ngẫu nhiên
                    .gender(GENDERS[random.nextInt(GENDERS.length)])
                    .isActive(i % 2 == 0)
                    .status(i % 3 == 0 ? ObjectStatus.SUSPENDED : ObjectStatus.ACTIVE)
                    .customerNumber("CUST-" + String.format("%04d", i))
                    .build());
        }
        return customers;
    }
}
