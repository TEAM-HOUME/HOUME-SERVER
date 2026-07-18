package or.sopt.houme.domain.house.model.taste.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@AllArgsConstructor
@Builder
@Table(name = "tastes", indexes = {
        @Index(name = "idx_filename", columnList = "filename", unique = true),
})
public class Taste {


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "url", nullable = false, length = 2048)
    private String url;

    @Column(name = "filename", nullable = false)
    private String filename;

    @Column(name = "original_filename", nullable = false)
    private String originalFilename;

    @Column(name = "file_extension", nullable = false)
    private String fileExtension;



    public static Taste createByPreSignedURL(String url, String filename, String originalFilename, String fileExtension){
        return Taste.builder()
                .url(url)
                .filename(filename)
                .originalFilename(originalFilename)
                .fileExtension(fileExtension)
                .build();
    }
}
