package androidx.lifecycle

class SavedStateHandle {
    fun <T> getLiveData(key: String): MutableLiveData<T> {
        return MutableLiveData()
    }

    fun <T> getLiveData(key: String, initialValue: T? = null): MutableLiveData<T> {
        return MutableLiveData()
    }

    operator fun <T> get(key: String): T? {
        return null
    }


    operator fun <T> set(key: String, value: T?) {

    }

}