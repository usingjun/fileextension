package com.fileextension.main.repository;

import com.fileextension.main.dto.ExtensionListResponse;
import com.fileextension.main.entity.ExtensionType;
import com.fileextension.main.entity.Extension;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface FileExtensionRepository extends JpaRepository<Extension, Long> {

    @Query("""
            select new com.fileextension.main.dto.ExtensionListResponse(f.extensionId, f.extension)
            from Extension f
            where f.userId=:userId and f.type= :type
            """)
    List<ExtensionListResponse> findExtensionsByUserIdAndType(@Param("userId") String userId, @Param("type") ExtensionType type);

    Long countByUserIdAndType(@Param("userId") String userId, @Param("type") ExtensionType type);

    boolean existsByExtensionAndUserIdAndType(String extension, String userGuid, ExtensionType extensionType);

    void deleteByExtensionAndUserIdAndType(String extension, String userId, ExtensionType extensionType);

    void deleteByExtensionIdAndUserId(Long extensionId, String userId);
}
