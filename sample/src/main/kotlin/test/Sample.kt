package test

import androidx.lifecycle.LiveData
import androidx.lifecycle.SavedStateHandle
import ru.petproject.saved.state.core.SaveState

class Sample(
    private val savedStateHandle: SavedStateHandle
) {

    @SaveState
    val test: LiveData<String>
    get() = getTestLiveData()

    @SaveState
    val unsupportedType: List<String> = emptyList()

    fun setTest(text: String) {

    }
}