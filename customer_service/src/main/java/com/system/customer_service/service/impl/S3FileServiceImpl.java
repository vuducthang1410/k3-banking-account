package com.system.customer_service.service.impl;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.system.customer_service.service.S3FileService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Component
@Slf4j
public class S3FileServiceImpl implements S3FileService {

    @Autowired
    private AmazonS3 amazonS3;

    @Value("${aws.s3.bucket-name}")
    private String bucketName;



    /**
     * Tải lên một tệp lên Amazon S3.
     *
     * @param file tệp cần được tải lên Amazon S3.
     * @return URL của tệp đã tải lên.
     * @throws Exception nếu có lỗi xảy ra trong quá trình tải lên.
     * @throws IOException nếu có vấn đề với các thao tác nhập/xuất tệp.
     */
    @Override
    public String uploadFile(MultipartFile file) throws Exception, IOException {
        // Tạo một khóa duy nhất cho tệp bằng cách kết hợp thời gian hiện tại với tên tệp gốc
        String key = System.currentTimeMillis() + "_" + file.getOriginalFilename();
        try {
            // Tạo siêu dữ liệu cho tệp
            ObjectMetadata metadata = new ObjectMetadata();

            // Đặt kích thước tệp vào siêu dữ liệu
            metadata.setContentLength(file.getSize());

            // Đặt loại nội dung của tệp (MIME type) vào siêu dữ liệu, giúp S3 hiểu loại tệp
            metadata.setContentType(file.getContentType()); // Ví dụ: image/jpeg, application/pdf, ...

            // Tạo PutObjectRequest bao gồm tên bucket, khóa, luồng đầu vào của tệp và siêu dữ liệu
            PutObjectRequest putObjectRequest = new PutObjectRequest(
                    bucketName, key, file.getInputStream(), metadata);

            // Thực hiện tải lên S3 với thông tin đã cấu hình
            amazonS3.putObject(putObjectRequest);

            // Ghi lại thông báo thành công cho việc tải lên với khóa tương ứng
            log.info("Tải lên amazon s3 thành công - key={}", key);

            // Trả về URL của tệp đã được tải lên S3
            return amazonS3.getUrl(bucketName, key).toString();
        } catch (Exception ex) {
            // Ghi lại thông báo lỗi với chi tiết nếu việc tải lên thất bại
            log.error("Tải lên amazon s3 thất bại - key={} - nguyên nhân = {}", key, ex.getMessage());

            // Ném ra IOException khi có lỗi xảy ra
            throw new IOException(ex);
        }
    }

    /**
     * Deletes a file from Amazon S3.
     *
     * @param url the URL of the file to be deleted from S3.
     * @return a message indicating the result of the deletion operation.
     * @throws Exception if there is an error during the deletion process.
     */

    @Override
    public void deleteFile(String url) throws Exception {
        try {
            // Extract the key from the URL by getting the substring after the last '/'
            String key = url.substring(url.lastIndexOf("/") + 1);

            // Delete the file from S3 using the extracted key
            amazonS3.deleteObject(bucketName, key);

            // Log a success message indicating the file has been deleted
            log.info("File deleted successfully from S3 - key={}", key);
        } catch (Exception ex) {
            // Log the error and throw an exception if an error occurs during deletion
            log.error("Failed to delete file from S3 - cause = {}", ex.getMessage());
            throw new Exception("Failed to delete file from S3", ex);
        }
    }


}
