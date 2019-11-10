# This is minmum sample of tensorflow lite



## how to make this app?
### 0. copy .tflite from your python code to assets folder 
### 1. write following setting into build.gradle
add app level gradle
```{gradle}
android {
    defaultConfig {
        ndk {
            abiFilters 'armeabi-v7a', 'arm64-v8a'
        }
    }
    aaptOptions {
        noCompress "tflite"
    }
}
```

```{gradle}
dependencies {
    implementation 'org.tensorflow:tensorflow-lite:0.0.0-nightly'
}
```

### 2. create TensorflowLiteRunner
process flow is following below.
0. create instance and load model.
1. reading bitmap with BitmapFactory (in MainActivity)
2. extract RGB value and normalize them by dividing by signal max value(255):
3. convert RGB value to Gray scale value;
4. put normalized gray scale value into ByteBuffer.  
    + I don't know why value container should be ByteBuffer.
    + instead of ByteBuffer, it may be possible using float array.
5. run inflate with 2d float array output container.
    + first dim is batch size and second dim is result size.


