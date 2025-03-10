package com.system.customer_service.config;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class S3Config {

    @Value("${aws.s3.access-key}")
    private String accessKey;

    @Value("${aws.s3.secret-key}")
    private String secretKey;

    @Value("${aws.s3.region}")
    private String region;

    /**
     * Phương thức này tạo và trả về một bean `AmazonS3` để sử dụng trong ứng dụng.
     *
     * Bean được cấu hình với thông tin xác thực AWS, bao gồm `accessKey` và `secretKey`,
     * cũng như khu vực nơi dịch vụ S3 được đặt.
     *
     * Phương thức sử dụng:
     * - `BasicAWSCredentials`: Một đối tượng chứa AWS Access Key và Secret Key
     *   để xác thực với Amazon S3.
     * - `AmazonS3Client.builder()`: Một builder giúp tạo ra một `AmazonS3Client`
     *   với các cấu hình cần thiết.
     * - `withRegion(region)`: Chỉ định khu vực AWS nơi bucket S3 được lưu trữ.
     * - `withCredentials(new AWSStaticCredentialsProvider(credentials))`: Cung cấp
     *   thông tin xác thực để kết nối.
     *
     * Sau khi tạo và cấu hình client, phương thức sẽ trả về một đối tượng `AmazonS3`,
     * có thể được sử dụng bởi các dịch vụ khác để thực hiện các thao tác trên Amazon S3, như
     * tải lên, tải xuống hoặc xóa tệp.
     *
     * @return đối tượng `AmazonS3` đã được cấu hình.
     */

    @Bean
    public AmazonS3 amazonS3() {
        BasicAWSCredentials credentials = new BasicAWSCredentials(accessKey, secretKey);
        return AmazonS3Client.builder()
                .withRegion(region)
                .withCredentials(new AWSStaticCredentialsProvider(credentials))
                .build();
    }

}
