package ru.otus.flow.data.dto

import com.google.gson.annotations.SerializedName

data class RAMResponseDto(
    @SerializedName("info") val info: ResponseDto,
    @SerializedName("results") val results: List<CharacterDto>,
    @SerializedName("error") val error: String?
)
