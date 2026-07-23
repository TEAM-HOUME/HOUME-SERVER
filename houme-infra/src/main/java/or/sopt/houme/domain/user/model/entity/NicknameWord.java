package or.sopt.houme.domain.user.model.entity;

import jakarta.persistence.*;
import lombok.*;
import or.sopt.houme.global.entity.BaseEntity;

@Entity
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Table(
        name = "nickname_words",
        indexes = {
                @Index(name = "idx_nickname_word_type_active", columnList = "word_type, is_active")
        },
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_nickname_word_type_word", columnNames = {"word_type", "word"})
        }
)
public class NicknameWord extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "word_type", nullable = false)
    private NicknameWordType type;

    @Column(name = "word", nullable = false)
    private String word;

    @Builder.Default
    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    public static NicknameWord of(NicknameWordType type, String word) {
        return NicknameWord.builder()
                .type(type)
                .word(word)
                .isActive(true)
                .build();
    }
}
