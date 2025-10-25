package com.ptit.b22cn539.int1433.Service.Music;

import com.ptit.b22cn539.int1433.DTO.Music.MusicCreateRequest;
import com.ptit.b22cn539.int1433.DTO.Music.MusicResponse;

public interface IMusicService {
    MusicResponse createMusic(MusicCreateRequest createRequest);
}
