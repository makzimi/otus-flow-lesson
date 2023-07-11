package ru.otus.flow.domain

import ru.otus.flow.data.RAMRetrofitService

class CharactersRepository(private val api: RAMRetrofitService) {
    suspend fun getAllCharacters(): Result<List<RaMCharacter>> = runCatching {
        api.getCharacters().results
            .map { dto ->
                RaMCharacter(
                    id = dto.id,
                    name = dto.name,
                    image = dto.image,
                )
            }
    }
}
