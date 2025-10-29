package com.ptit.b22cn539.int1433.Models;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class GameItemEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;
    String username1;
    String username2;
    Long answerCorrectId;
    Long answerUser1ChooseId;
    Long answerUser2ChooseId;
    @ManyToOne
    @JoinColumn(name = "musicId")
    MusicEntity music;
    @ManyToOne
    @JoinColumn(name = "gameId")
    GameEntity game;
}
