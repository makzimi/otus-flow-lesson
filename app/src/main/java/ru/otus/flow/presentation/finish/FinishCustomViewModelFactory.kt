package ru.otus.flow.presentation.finish

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import ru.otus.flow.domain.CharactersRepository

class FinishCustomViewModelFactory(private val repository: CharactersRepository) :
    ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(FinishCharactersViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return FinishCharactersViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
