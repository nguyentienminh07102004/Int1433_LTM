package com.ptit.b22cn539.int1433.Mapper;

import com.ptit.b22cn539.int1433.DTO.Music.AnswerResponse;
import com.ptit.b22cn539.int1433.DTO.Music.MusicResponse;
import com.ptit.b22cn539.int1433.Models.MusicEntity;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class MusicMapper {

    public MusicResponse toMusicResponse(MusicEntity music) {
        List<AnswerResponse> answerResponses = music.getAnswers().stream()
                .map(answerEntity -> AnswerResponse.builder()
                        .id(answerEntity.getId())
                        .description(answerEntity.getDescription())
                        .build())
                .toList();
        return MusicResponse.builder()
                .id(music.getId())
                .title(music.getTitle())
                .description(music.getDescription())
                .url(music.getUrl().replaceAll("localhost", "10.109.180.251"))
                .answers(answerResponses)
                .build();
    }
}
