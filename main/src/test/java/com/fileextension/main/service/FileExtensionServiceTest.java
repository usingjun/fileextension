package com.fileextension.main.service;

import com.fileextension.main.entity.Extension;
import com.fileextension.main.entity.ExtensionType;
import com.fileextension.main.repository.FileExtensionRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("FileExtensionService 비즈니스 로직 테스트")
class FileExtensionServiceTest {

    @Mock
    private FileExtensionRepository fileExtensionRepository;

    @InjectMocks
    private FileExtensionService fileExtensionService;

    private final String userId = "test-user-guid";

    @Nested
    @DisplayName("커스텀 확장자 추가 테스트")
    class AddCustomExtension {

        @Test
        @DisplayName("성공: 유효한 확장자명이 입력되면 저장된다")
        void success_add_extension() {
            // given
            String ext = "png";
            given(fileExtensionRepository.existsByExtensionAndUserIdAndType("png", userId, ExtensionType.CUSTOM))
                    .willReturn(false);

            // when
            fileExtensionService.addCustomExtensionByUserGuid(ext, userId);

            // then
            verify(fileExtensionRepository, times(1)).save(any(Extension.class));
        }

        @Test
        @DisplayName("실패: 빈 값이 입력되면 예외가 발생한다")
        void fail_empty_input() {
            assertThatThrownBy(() -> fileExtensionService.addCustomExtensionByUserGuid("", userId))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessage("확장자를 입력해주세요.");
        }

        @Test
        @DisplayName("실패: 20자를 초과하면 예외가 발생한다")
        void fail_length_over() {
            String longExt = "a".repeat(21);
            assertThatThrownBy(() -> fileExtensionService.addCustomExtensionByUserGuid(longExt, userId))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessage("커스텀 확장자 길이는 20자리 이하여야 합니다.");
        }

        @Test
        @DisplayName("실패: 고정 확장자(exe 등)를 입력하면 예외가 발생한다")
        void fail_fixed_extension_input() {
            assertThatThrownBy(() -> fileExtensionService.addCustomExtensionByUserGuid("exe", userId))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessage("고정 확장자에 포함된 파일 확장자입니다.");
        }

        @Test
        @DisplayName("실패: 이미 등록된 확장자면 예외가 발생한다")
        void fail_duplicate_extension() {
            given(fileExtensionRepository.existsByExtensionAndUserIdAndType("jpg", userId, ExtensionType.CUSTOM))
                    .willReturn(true);

            assertThatThrownBy(() -> fileExtensionService.addCustomExtensionByUserGuid("jpg", userId))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessage("이미 등록된 파일 확장자입니다.");
        }

        @Test
        @DisplayName("실패: 등록 개수가 200개를 초과하면 예외가 발생한다")
        void fail_count_over() {
            given(fileExtensionRepository.countByUserIdAndType(userId, ExtensionType.CUSTOM))
                    .willReturn(201L);

            assertThatThrownBy(() -> fileExtensionService.addCustomExtensionByUserGuid("newext", userId))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessage("커스텀 확장자는 최대 200개까지만 등록 가능합니다.");
        }
    }

    @Nested
    @DisplayName("고정 확장자 토글 테스트")
    class ToggleFixedExtension {

        @Test
        @DisplayName("이미 존재하면 삭제(체크 해제) 로직이 실행된다")
        void toggle_off() {
            given(fileExtensionRepository.existsByExtensionAndUserIdAndType("bat", userId, ExtensionType.FIXED))
                    .willReturn(true);

            fileExtensionService.toggleFixedExtensionByUserGuid("bat", userId);

            verify(fileExtensionRepository, times(1))
                    .deleteByExtensionAndUserIdAndType("bat", userId, ExtensionType.FIXED);
        }

        @Test
        @DisplayName("존재하지 않으면 저장(체크 설정) 로직이 실행된다")
        void toggle_on() {
            given(fileExtensionRepository.existsByExtensionAndUserIdAndType("bat", userId, ExtensionType.FIXED))
                    .willReturn(false);

            fileExtensionService.toggleFixedExtensionByUserGuid("bat", userId);

            verify(fileExtensionRepository, times(1)).save(any(Extension.class));
        }
    }
}