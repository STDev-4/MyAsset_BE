package io.api.myasset.global.s3.service;

import java.io.IOException;
import java.util.UUID;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import io.api.myasset.global.s3.S3Properties;
import io.api.myasset.global.exception.error.BusinessException;
import io.api.myasset.global.s3.exception.S3Error;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.NoSuchKeyException;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;

@Slf4j
@Service
@RequiredArgsConstructor
@ConditionalOnProperty(name = "aws.s3.enabled", havingValue = "true")
public class S3Service {

	private final S3Client s3Client;
	private final S3Properties props;

	/**
	 * 파일 업로드
	 * @param file 업로드할 파일
	 * @param dir  S3 저장 경로 (예: "characters", "profiles")
	 * @return key(S3 경로)와 CloudFront URL을 담은 UploadedFile
	 */
	public UploadedFile upload(MultipartFile file, String dir) {
		if (file == null || file.isEmpty()) {
			throw new BusinessException(S3Error.EMPTY_FILE);
		}

		String key = buildKey(dir, file.getOriginalFilename());
		String contentType = (file.getContentType() != null)
			? file.getContentType()
			: MediaType.APPLICATION_OCTET_STREAM_VALUE;

		try (var inputStream = file.getInputStream()) {
			PutObjectRequest putReq = PutObjectRequest.builder()
				.bucket(props.bucket())
				.key(key)
				.contentType(contentType)
				.contentLength(file.getSize())
				.build();

			s3Client.putObject(putReq, RequestBody.fromInputStream(inputStream, file.getSize()));

			String url = buildS3Url(key);
			log.info("[S3] Upload success. key={}", key);
			return new UploadedFile(key, url);

		} catch (IOException e) {
			log.warn("[S3] IO error while reading file. key={}", key, e);
			throw new BusinessException(S3Error.IO_ERROR);
		} catch (S3Exception e) {
			log.error("[S3] Upload failed. key={} status={} awsErrorCode={} message={}",
				key, e.statusCode(),
				e.awsErrorDetails() != null ? e.awsErrorDetails().errorCode() : "null",
				e.getMessage(), e);
			throw new BusinessException(S3Error.UPLOAD_FAILED);
		}
	}

	/**
	 * 파일 삭제
	 * @param key S3 오브젝트 키
	 */
	public void delete(String key) {
		if (key == null || key.isBlank()) {
			throw new BusinessException(S3Error.EMPTY_KEY);
		}

		try {
			s3Client.deleteObject(DeleteObjectRequest.builder()
				.bucket(props.bucket())
				.key(key)
				.build());
			log.info("[S3] Delete success. key={}", key);

		} catch (NoSuchKeyException e) {
			log.warn("[S3] Object not found on delete. key={}", key, e);
		} catch (S3Exception e) {
			log.error("[S3] Delete failed. key={} status={} awsErrorCode={} message={}",
				key, e.statusCode(),
				e.awsErrorDetails() != null ? e.awsErrorDetails().errorCode() : "null",
				e.getMessage(), e);
			throw new BusinessException(S3Error.DELETE_FAILED);
		}
	}

	private String buildS3Url(String key) {
		return "https://" + props.bucket() + ".s3." + props.region() + ".amazonaws.com/" + key;
	}

	private String buildKey(String dir, String originalFilename) {
		String safeDir = (dir == null || dir.isBlank()) ? "uploads" : dir.strip();
		String ext = "";
		if (originalFilename != null) {
			int dot = originalFilename.lastIndexOf('.');
			if (dot >= 0 && dot < originalFilename.length() - 1) {
				ext = originalFilename.substring(dot);
			}
		}
		return safeDir + "/" + UUID.randomUUID() + ext;
	}

	public record UploadedFile(String key, String url) {
	}
}
