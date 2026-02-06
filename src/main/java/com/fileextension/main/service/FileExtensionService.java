package com.fileextension.main.service;

import com.fileextension.main.dto.ExtensionListResponse;
import com.fileextension.main.entity.Extension;
import com.fileextension.main.entity.ExtensionType;
import com.fileextension.main.entity.FixedExtensions;
import com.fileextension.main.repository.FileExtensionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;

@Service
@RequiredArgsConstructor
public class FileExtensionService {
    private final FileExtensionRepository fileExtensionRepository;

    @Transactional(readOnly = true)
    public List<ExtensionListResponse> findExtensionsByUserGuidAndType(String userId, ExtensionType type) {
        return fileExtensionRepository.findExtensionsByUserIdAndType(userId, type);
    }

    @Transactional
    public void toggleFixedExtensionByUserGuid(String extension, String userId) {
        if (fileExtensionRepository.existsByExtensionAndUserIdAndType(extension, userId, ExtensionType.FIXED)) {
            fileExtensionRepository.deleteByExtensionAndUserIdAndType(extension, userId, ExtensionType.FIXED);
        } else {
            fileExtensionRepository.save(new Extension(userId, extension, ExtensionType.FIXED));
        }
    }

    @Transactional
    public void addCustomExtensionByUserGuid(String extension, String userId) {
        String normalized = normalizeExtensionName(extension);

        if (normalized == null || normalized.isEmpty()) {
            throw new RuntimeException("확장자를 입력해주세요.");
        }
        if (normalized.length() > 20) {
            throw new RuntimeException("커스텀 확장자 길이는 20자리 이하여야 합니다.");
        }
        if (!normalized.matches("^[a-z0-9-_]+$")) {
            throw new RuntimeException("확장자는 영문 소문자와 숫자만 입력 가능하며 한글이나 특수문자는 사용할 수 없습니다.");
        }
        if (isFixedExtension(normalized)) {
            throw new RuntimeException("고정 확장자에 포함된 파일 확장자입니다.");
        }
        if (fileExtensionRepository.existsByExtensionAndUserIdAndType(normalized, userId, ExtensionType.CUSTOM)) {
            throw new RuntimeException("이미 등록된 파일 확장자입니다.");
        }
        if (fileExtensionRepository.countByUserIdAndType(userId, ExtensionType.CUSTOM) > 200) {
            throw new RuntimeException("커스텀 확장자는 최대 200개까지만 등록 가능합니다.");
        }

        fileExtensionRepository.save(new Extension(userId, normalized, ExtensionType.CUSTOM));
    }

    private boolean isFixedExtension(String normalized) {
        return Arrays.stream(FixedExtensions.values())
                .anyMatch(e -> e.name().equalsIgnoreCase(normalized));
    }

    private String normalizeExtensionName(String extension) {
        if (extension == null || extension.isBlank()) {
            return null;
        }

        return extension.trim()           // 앞뒤 공백 제거
                .replace(" ", "") // 중간에 낀 공백 제거
                .replace(".", "") // .exe 입력 시 점 제거
                .toLowerCase();    // 소문자로 통일
    }

    @Transactional
    public void deleteCustomExtensionByUserGuid(Long extensionId, String userId) {
        fileExtensionRepository.deleteByExtensionIdAndUserId(extensionId, userId);
    }
}
