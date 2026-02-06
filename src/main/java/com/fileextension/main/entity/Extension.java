package com.fileextension.main.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(
        name = "extension",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_user_extension",
                        columnNames = {"userId", "extension"}
                )
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Extension {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long extensionId;

    @Column(nullable = false)
    String userId;

    @Column(nullable = false)
    String extension;

    @Enumerated(EnumType.STRING)
    ExtensionType type;

    public Extension(String userId, String extension, ExtensionType type) {
        this.userId = userId;
        this.extension = extension;
        this.type = type;
    }
}
