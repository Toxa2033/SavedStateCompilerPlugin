import com.tschuchort.compiletesting.KotlinCompilation
import com.tschuchort.compiletesting.SourceFile
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import ru.petproject.saved.state.compiler.utils.capitalize

class CapacityTest {

    @Test
    fun `IR plugin success work with other properties and 100 files`() {
        val result = compile(sourceFiles = createListFile())
        Assertions.assertEquals(KotlinCompilation.ExitCode.OK, result.exitCode)
    }
}


private fun createListFile(): List<SourceFile> {
    val initName = "test"
    return IntRange(0, 100).map { i->
        val iterateName = "$initName$i"
        val fileName = "$iterateName.kt"
        val classString = createClass(iterateName)

        SourceFile.kotlin(fileName, classString)
    }
}

private fun createClass(className: String): String {
    val lowerName = className.lowercase()
    val capName = className.capitalize()
    return """
    import androidx.lifecycle.SavedStateHandle
    import androidx.lifecycle.MutableLiveData
    import ru.petproject.saved.state.core.SaveState
                     
    class $capName(
    private val savedStateHandle: SavedStateHandle
    ) {
        
        var variableWithoutAnnotation: String = String()

        @SaveState
        var $lowerName: String = String()

        fun testLiveData(): MutableLiveData<String> {
          return get${capName}LiveData()
        }

        fun testGetString(): String? {
            return get${capName}Value()
        }
        
        fun testSetValue() {
            set${capName}Value("test")
        }

        fun testConst() {
            getIdentifier${capName.capitalize()}()
        }
        
    }
""".trimIndent()
}