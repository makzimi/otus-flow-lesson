package ru.otus.flow.presentation

import ru.otus.flow.domain.RaMCharacter

data class CharactersState(
    val items: List<RaMCharacter>,
    val isLoading: Boolean,
    val isError: Boolean,
)
