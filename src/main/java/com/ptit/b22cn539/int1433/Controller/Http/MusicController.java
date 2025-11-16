package com.ptit.b22cn539.int1433.Controller.Http;

import com.ptit.b22cn539.int1433.DTO.Music.MusicCreateRequest;
import com.ptit.b22cn539.int1433.DTO.Music.MusicResponse;
import com.ptit.b22cn539.int1433.Service.Music.IMusicService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/musics")
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class MusicController {
    IMusicService musicService;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public MusicResponse createMusic(@ModelAttribute MusicCreateRequest musicCreateRequest) {
        return this.musicService.createMusic(musicCreateRequest);
    }
}
