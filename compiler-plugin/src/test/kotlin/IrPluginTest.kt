import com.tschuchort.compiletesting.KotlinCompilation
import com.tschuchort.compiletesting.SourceFile
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class IrPluginTest {

    @Test
    fun `IR plugin success of class without SaveState`() {
        val result = compile(
            sourceFile = SourceFile.kotlin(
                "test.kt", """
class Soma() {
    
}
"""
            )
        )
        assertEquals(KotlinCompilation.ExitCode.OK, result.exitCode)
    }

    @Test
    fun `IR plugin success work with other properties`() {
        val result = compile(
            sourceFile = SourceFile.kotlin(
                "test.kt", """
import androidx.lifecycle.SavedStateHandle
 import androidx.lifecycle.MutableLiveData
 import io.github.toxa2033.saved.state.core.SaveState
class Test(
private val savedStateHandle: SavedStateHandle
) {
    
    var variableWithoutAnnotation: String = String()

    @SaveState
    var phone: String = String()

    fun testLiveData(): MutableLiveData<String> {
      return getPhoneLiveData()
    }

    fun testGetString(): String? {
        return getPhoneValue()
    }
    
    fun testSetValue() {
        setPhoneValue("test")
    }

    fun testConst() {
        getIdentifierPhone()
    }
    
}
"""
            )
        )
        assertEquals(KotlinCompilation.ExitCode.OK, result.exitCode)
    }
}
