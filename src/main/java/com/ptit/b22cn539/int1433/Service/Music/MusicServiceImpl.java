package com.ptit.b22cn539.int1433.Service.Music;

import com.ptit.b22cn539.int1433.DTO.Music.MusicCreateRequest;
import com.ptit.b22cn539.int1433.DTO.Music.MusicResponse;
import com.ptit.b22cn539.int1433.Mapper.MusicMapper;
import com.ptit.b22cn539.int1433.Models.AnswerEntity;
import com.ptit.b22cn539.int1433.Models.MusicEntity;
import com.ptit.b22cn539.int1433.Repository.IMusicRepository;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class MusicServiceImpl implements IMusicService {
    IMusicRepository musicRepository;
    MinioClient minioClient;
    @Value(value = "${minio.bucket}")
    @NonFinal
    String bucket;
    @Value(value = "${minio.host}")
    @NonFinal
    String host;
    MusicMapper musicMapper;


    @Override
    @Transactional
    public MusicResponse createMusic(MusicCreateRequest createRequest) {
        try {
            MusicEntity music = MusicEntity.builder()
                    .title(createRequest.getTitle())
                    .description(createRequest.getDescription())
                    .build();
            if (!createRequest.getFile().isEmpty()) {
                minioClient.putObject(PutObjectArgs.builder()
                        .bucket(this.bucket)
                        .stream(createRequest.getFile().getInputStream(), createRequest.getFile().getSize(), -1)
                        .object(createRequest.getFile().getOriginalFilename())
                        .contentType(createRequest.getFile().getContentType())
                        .build());
                String fileUrl = this.host + "/" + this.bucket + "/" + createRequest.getFile().getOriginalFilename();
                music.setUrl(fileUrl);
            }
            List<AnswerEntity> answers = new ArrayList<>();
            for (String answerDesc : createRequest.getAnswers()) {
                String[] answerParts = answerDesc.split("-");
                AnswerEntity answer = AnswerEntity.builder()
                        .description(answerParts[0].strip())
                        .music(music)
                        .isCorrect(Boolean.parseBoolean(answerParts[1].strip()))
                        .build();
                answers.add(answer);
            }
            music.setAnswers(answers);
            this.musicRepository.save(music);
            return this.musicMapper.toMusicResponse(music);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
