import com.tschuchort.compiletesting.KotlinCompilation
import com.tschuchort.compiletesting.SourceFile
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

class TypeTests {

    @Test
    fun `IR plugin success in MutableLiveData`() {
        val result = compile(
            sourceFile = SourceFile.kotlin(
                "test.kt", """
 import androidx.lifecycle.SavedStateHandle
 import androidx.lifecycle.MutableLiveData
import io.github.toxa2033.saved.state.core.SaveState
open class Soma(
private val savedStateHandle: SavedStateHandle
) {
    
    @SaveState
    val phone: MutableLiveData<String>
    get() = getPhoneLiveData()

    fun testLiveData(): MutableLiveData<String> {
      return getPhoneLiveData("default")
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
        Assertions.assertEquals(KotlinCompilation.ExitCode.OK, result.exitCode)
    }

    @Test
    fun `IR plugin success in LiveData`() {
        val result = compile(
            sourceFile = SourceFile.kotlin(
                "test.kt", """
 import androidx.lifecycle.SavedStateHandle
 import androidx.lifecycle.LiveData
 import androidx.lifecycle.MutableLiveData
import io.github.toxa2033.saved.state.core.SaveState
class Soma(
private val savedStateHandle: SavedStateHandle
) {
    
    @SaveState
    val phone: LiveData<String>
    get() = getPhoneLiveData()

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
        Assertions.assertEquals(KotlinCompilation.ExitCode.OK, result.exitCode)
    }

    @Test
    fun `IR plugin success in simple supported type`() {
        val result = compile(
            sourceFile = SourceFile.kotlin(
                "test.kt", """
 import androidx.lifecycle.SavedStateHandle
 import androidx.lifecycle.MutableLiveData
 import io.github.toxa2033.saved.state.core.SaveState
class Soma(
private val savedStateHandle: SavedStateHandle
) {
    
    @SaveState
    var phone: String = String()

    fun testLiveData(): MutableLiveData<String> {
      return getPhoneLiveData(null)
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
        Assertions.assertEquals(KotlinCompilation.ExitCode.OK, result.exitCode)
    }

    @Test
    fun `IR plugin success in supported parcelable type`() {
        val result = compile(
            sourceFile = SourceFile.kotlin(
                "test.kt", """
 import androidx.lifecycle.*
import io.github.toxa2033.saved.state.core.SaveState
import types.SupportParcelableType
class Soma(
private val savedStateHandle: SavedStateHandle
) {
    
    @SaveState
    val phone: SupportParcelableType = SupportParcelableType()

    fun testLiveData(): MutableLiveData<SupportParcelableType> {
      return getPhoneLiveData()
    }

    fun testGetString(): SupportParcelableType? {
        return getPhoneValue()
    }
    
    fun testSetValue() {
        setPhoneValue(SupportParcelableType())
    }

    fun testConst() {
        getIdentifierPhone()
    }
    
}
"""
            )
        )
        Assertions.assertEquals(KotlinCompilation.ExitCode.OK, result.exitCode)
    }

    @Test
    fun `IR plugin`() {
        val result = compile(
            sourceFile = SourceFile.kotlin(
                "test.kt", """
 import androidx.lifecycle.*
import io.github.toxa2033.saved.state.core.SaveState
import types.SupportParcelableType
class Soma(
private val savedStateHandle: SavedStateHandle
) {
    
    @SaveState
    val phone: List<SupportParcelableType> = listOf()

    fun testLiveData(value: String? = null): MutableLiveData<List<SupportParcelableType>> {
      return getPhoneLiveData()
    }

    fun testGetString(): List<SupportParcelableType>? {
        return getPhoneValue()
    }
    
    fun testSetValue() {
        setPhoneValue(emptyList())
    }

    fun testConst() {
        getIdentifierPhone()
    }
    
}
"""
            )
        )
        Assertions.assertEquals(KotlinCompilation.ExitCode.OK, result.exitCode)
    }
}