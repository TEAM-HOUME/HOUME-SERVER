package or.sopt.houme.domain.openai.service;

import lombok.RequiredArgsConstructor;
import or.sopt.houme.domain.openai.client.FastApiImageClient;
import or.sopt.houme.domain.prompt.dto.PromptRequestDTO;
import or.sopt.houme.global.dto.ImageUploadResponseDTO;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class FastApiServiceImpl implements FastApiService {

    private final FastApiImageClient fastApiImageClient;

    @Override
    public ImageUploadResponseDTO getImageByFastApi(PromptRequestDTO request){

        return fastApiImageClient.generateImage(request);
    }


}
