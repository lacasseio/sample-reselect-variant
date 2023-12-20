# Sample: Reselect variant in dependency tree

As opposed to the previous version of this sample (see Git history), the sample will rewrite the dependencies of each project to avoid rebuilding the same library twice (debug and release).
To accomplish this, we need a global plugin that understand the request (e.g. `-Pdbg`) and perform the modification on affected projects.
The global plugin is `com.example.debuggable-product` and is applied to the `settings.gradle`, aka global to a project.
The plugin will search all project for any C++ components and perform the dependency modification required.

We no longer need to create a conflict as the dependency that we resolves already depends on the right, debug vs release, dependencies.
Note that this sample ignores the case where a transitive dependency of an external dependency is required to be switched to a debug binary.
In this case, you would need to use the trick from the previous version of this sample.

## Demonstration

```
$ ./gradlew :product:verify -Pdbg=com.example:lib1,com.example:lib4
> Task :composite-build:lib4:compileDebugCpp
> Task :composite-build:lib4:linkDebug
> Task :lib1:compileDebugCpp
> Task :lib2:compileReleaseCpp
> Task :lib3:compileReleaseCpp
> Task :lib2:linkRelease
> Task :lib2:stripSymbolsRelease
> Task :app:compileReleaseCpp
> Task :lib1:linkDebug
> Task :lib3:linkRelease
> Task :app:linkRelease
> Task :lib3:stripSymbolsRelease
> Task :app:stripSymbolsRelease

> Task :product:verify
./app/build/exe/main/release/stripped/app
./lib3/build/lib/main/release/stripped/liblib3.dylib
./lib1/build/lib/main/debug/liblib1.dylib
./composite-build/lib4/build/lib/main/debug/liblib4.dylib
./lib2/build/lib/main/release/stripped/liblib2.dylib

BUILD SUCCESSFUL in 2s
```

Note that only the debug variant of `lib1` and `lib4` are built.
