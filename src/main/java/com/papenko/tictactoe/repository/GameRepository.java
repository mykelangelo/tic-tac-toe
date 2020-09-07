package com.papenko.tictactoe.repository;

import com.papenko.tictactoe.entity.GameData;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

public interface GameRepository extends ElasticsearchRepository<GameData, String> {
}
