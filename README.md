# WhatsThis

"What is it?" -- asked Disco the talking budgie.

This is an android example of using mxnet to classify pictures.
```
-main
 |
 ----java
 |   |
 |   ----com.happen.it.make.whatisit -- the UI module
 |   |
 |   ----org.dmlc.mxnet -- the java interface
 |
 ----jniLibs
 |   |
 |   -----armeabi -- the directory containing dynamic link lib for android
 |        |
 |        -------------- libmxnet_predict.so --- the prediction lib
 |
 ----res
     |
     -----raw
          |
          ------params
          |
          ------symbol.json
          |
          ------synset.txt
          |
          ------mean.json
          
 ```

The example in mxnet doesn't include the model and pre-compiled native library for repo-size consideration.

To compile the android lib by yourself, have a look at mxnet/amalgamation.

To download a complete example with precompiled lib and a simple model, clone https://github.com/Leliana/WhatsThis.git .

NOTE: This example doesn't run with emulator unless you build a native lib for android emulator.
