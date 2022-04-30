import com.tschuchort.compiletesting.KotlinCompilation
import com.tschuchort.compiletesting.SourceFile
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import ru.petproject.saved.state.compiler.utils.NOT_FOUND_SAVED_STATE_HANDLER_PROPERTY_ERROR

class ErrorTests {
    @Test
    fun `IR plugin error - not found savedStateHandle`() {
        val result = compile(
            sourceFile = SourceFile.kotlin(
                "test.kt", """
 import androidx.lifecycle.SavedStateHandle
 import androidx.lifecycle.MutableLiveData
import ru.petproject.saved.state.core.SaveState
class Soma() {
    
    @SaveState
    val phone: MutableLiveData<String>
    get() = getPhoneLiveData()
    
}
"""
            )
        )
        Assertions.assertEquals(KotlinCompilation.ExitCode.COMPILATION_ERROR, result.exitCode)
        Assertions.assertEquals(true, result.messages.contains(NOT_FOUND_SAVED_STATE_HANDLER_PROPERTY_ERROR))
    }

    @Test
    fun `IR plugin error in unsupported type`() {
        val result = compile(
            sourceFile = SourceFile.kotlin(
                "test.kt", """
 import androidx.lifecycle.LiveData
import androidx.lifecycle.SavedStateHandle
 import androidx.lifecycle.MutableLiveData
import types.UnsupportedType
 import ru.petproject.saved.state.core.SaveState
class Soma(
private val savedStateHandle: SavedStateHandle
) {
    
    @SaveState
    val phone: UnsupportedType = UnsupportedType()
}
"""
            )
        )
        Assertions.assertEquals(KotlinCompilation.ExitCode.COMPILATION_ERROR, result.exitCode)
        Assertions.assertEquals(
            true,
            result.messages.contains("This type is not supported for save in SavedStateHandle")
        )
    }
}