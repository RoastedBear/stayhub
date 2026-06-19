package com.roastedbear.stayhub.global.s3;

import com.roastedbear.stayhub.global.exception.BusinessException;
import com.roastedbear.stayhub.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

/**
 * S3 파일 업로드/삭제 공통 서비스
 *
 * 허용 파일 형식: image/jpeg, image/png, image/gif, image/webp
 * 최대 파일 크기: 10MB
 * S3 키 형식: {folder}/{UUID}.{확장자}
 * 반환 URL 형식: {endpoint}/{bucket}/{folder}/{UUID}.{확장자}
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class S3Service {

    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024L;   // 10MB
    private static final List<String> ALLOWED_TYPES = List.of(
            "image/jpeg", "image/png", "image/gif", "image/webp"
    );

    private final S3Client s3Client;

    @Value("${cloud.aws.s3.bucket}")
    private String bucketName;

    @Value("${cloud.aws.s3.endpoint}")
    private String endpoint;

    /**
     * 파일을 S3에 업로드하고 접근 URL을 반환
     *
     * @param file   업로드할 멀티파트 파일
     * @param folder S3 경로 폴더 (예: "accommodations", "rooms", "reviews")
     * @return 업로드된 파일의 접근 URL
     */
    public String upload(MultipartFile file, String folder) {
        validateImageFile(file);

        String extension = extractExtension(file.getOriginalFilename());
        String key = folder + "/" + UUID.randomUUID() + extension;

        try {
            PutObjectRequest putRequest = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(key)
                    .contentType(file.getContentType())
                    .contentLength(file.getSize())
                    .build();

            s3Client.putObject(putRequest, RequestBody.fromInputStream(
                    file.getInputStream(), file.getSize()));

            String url = buildUrl(key);
            log.debug("S3 업로드 완료: {}", url);
            return url;

        } catch (IOException e) {
            log.error("S3 업로드 실패: {}", e.getMessage());
            throw new BusinessException(ErrorCode.S3_UPLOAD_FAILED);
        }
    }

    /**
     * S3에서 파일 삭제
     *
     * @param imageUrl 삭제할 파일의 S3 URL
     */
    public void delete(String imageUrl) {
        String key = extractKey(imageUrl);
        DeleteObjectRequest deleteRequest = DeleteObjectRequest.builder()
                .bucket(bucketName)
                .key(key)
                .build();

        s3Client.deleteObject(deleteRequest);
        log.debug("S3 삭제 완료: {}", key);
    }

    // === Private 유효성 검사 ===

    private void validateImageFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException(ErrorCode.INVALID_FILE);
        }

        if (file.getSize() > MAX_FILE_SIZE) {
            throw new BusinessException(ErrorCode.FILE_TOO_LARGE);
        }

        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_TYPES.contains(contentType)) {
            throw new BusinessException(ErrorCode.INVALID_FILE_TYPE);
        }
    }

    /** 원본 파일명에서 확장자 추출 (예: ".jpg") */
    private String extractExtension(String originalFilename) {
        if (originalFilename == null || !originalFilename.contains(".")) {
            return "";
        }
        return originalFilename.substring(originalFilename.lastIndexOf(".")).toLowerCase();
    }

    /** S3 URL에서 객체 키 추출 */
    private String extractKey(String imageUrl) {
        String prefix = endpoint + "/" + bucketName + "/";
        if (!imageUrl.startsWith(prefix)) {
            throw new BusinessException(ErrorCode.IMAGE_NOT_FOUND);
        }
        return imageUrl.substring(prefix.length());
    }

    /** S3 객체 키로 접근 URL 생성 */
    private String buildUrl(String key) {
        return endpoint + "/" + bucketName + "/" + key;
    }
}
