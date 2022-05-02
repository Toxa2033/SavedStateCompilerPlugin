# SavedState Compiler Plugin

Kotlin compiler plugin generates support methods for use SaveStateHandle without constants and string variables. 

# Example 

If need to use SaveStateHandle, you must declare a variable, like that: 
```kotlin
    companion object {
        private const val TEXT_FIELD = "text"
    }

    val text: MutableLiveData<String> = savedStateHandle.getLiveData(TEXT_FIELD, "Hello World")
```


This plugin eliminates necessity declares a variable. Same example with plugin:

```kotlin
    @SaveState
    val text: MutableLiveData<String> = getTextLiveData("Hello World")
```

# How it use

1. Apply plugin to your module, sync it and you can use annotation `@SaveState`
2. Mark by this annotation fields witch you want to use with SavedStateHandle, and support methods generated in realtime.

**Importance:** 
* The SaveStateHandle variable mast be declarated in class. 
* As a key for SaveStateHandle plugin uses name of a annotated variable

## 4 generated methods

For example takes this variable 

```kotlin
    private val savedStateHandle: SavedStateHandle
    
    @SaveState
    val text: LiveData<String>
```


### 1. getTextValue() 
It method return value from SavedStateHandle. Inside it: 
```kotlin
  private fun getTextValue(): String? {
    return savedStateHandle.get<String>(key = "text")
  }
```

### 2. getTextLiveData() 
It method returns LiveData from SavedStateHandle. Inside it: 
```kotlin
  private fun getTextLiveData(default: String? = null): MutableLiveData<String> {
    return savedStateHandle.getLiveData<String>(key = "text", initialValue = default)
  }
```

### 3. getIdentifierText() 
It method returns the key witch uses for set and get value in SavedStateHandle. Inside it: 
```kotlin
  private fun getIdentifierText(): String {
    return "text"
  }
```

### 4. setTextValue() 
It method sets value in SavedStateHandle. Inside it: 
```kotlin
  private fun setTextValue(text: String?) {
    savedStateHandle.set<String?>(key = "text", value = text)
  }
```

## One more things
The plugin also allows: 

1.Type checking. 
Supports all types from the [documentation](https://developer.android.com/topic/libraries/architecture/viewmodel-savedstate#types) and indicates an error if, for example, POJO did not implement parcelable

<img width="618" alt="Снимок экрана 2022-05-01 в 15 21 35" src="https://user-images.githubusercontent.com/7330056/166145646-dfbf4767-f895-436d-9bf0-c512cda1b13f.png">


2. Validates presence of a variable SavedStateHandle in a class. If class contain an annotated variable and the variable SavedStateHandle is not found in the class, then the plugin will notify you
<img width="600" alt="Снимок экрана 2022-05-01 в 15 29 06" src="https://user-images.githubusercontent.com/7330056/166145961-d1096126-9112-4c54-8d79-18e79edf455f.png">


License
-------

    Copyright (C) 2022 Toxa2033

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
