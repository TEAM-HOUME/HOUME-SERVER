package or.sopt.houme.domain.admin.controller.dto.moodboard;

public record AdminMoodBoardGetResponseDTO(

        String filename,
        String originalFilename,
        String url

) {

    public static AdminMoodBoardGetResponseDTO of(String filename, String originalFilename, String url) {
        return new AdminMoodBoardGetResponseDTO(filename, originalFilename, url);
    }
}
