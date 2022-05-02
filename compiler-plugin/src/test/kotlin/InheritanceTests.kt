import com.tschuchort.compiletesting.KotlinCompilation
import com.tschuchort.compiletesting.SourceFile
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

class InheritanceTests {

    @Test
    fun `success generate method of superclass SaveState`() {
        val result = compile(
            sourceFile = SourceFile.kotlin(
                "test.kt", """
 import androidx.lifecycle.SavedStateHandle
 import androidx.lifecycle.LiveData
 import androidx.lifecycle.MutableLiveData
import io.github.toxa2033.saved.state.core.SaveState

open class A (private val savedStateHandle: SavedStateHandle){
    @SaveState
    val test: String = ""
}

class Soma(
private val savedStateHandle: SavedStateHandle
): A(savedStateHandle) {

    @SaveState
    val phone: String = ""

fun testLiveData(): MutableLiveData<String> {
        getTestLiveData()
      return getPhoneLiveData()
    }

    fun testGetString(): String? {
        getTestValue()
        return getPhoneValue()
    }
    
    fun testSetValue() {
        setTestValue(String())
        setPhoneValue(String())
    }

    fun testConst() {
        getIdentifierPhone()
        getIdentifierTest()
    }
}
"""
            )
        )
        Assertions.assertEquals(KotlinCompilation.ExitCode.OK, result.exitCode)
    }

}