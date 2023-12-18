# Sample: Reselect variant in dependency tree

Gradle resolve all dependencies for a configuration as a single tree.
Once all dependencies found, Gradle perform a conflict resolution.
On version conflict, Gradle selects the highest.
On capability conflict, Gradle fails.
By default, all outgoing configurations has the same capability.
Thus, all native variants (i.e. debug/release) are incompatible with each other as they have the same capability.
Gradle doesn't prevent you from declaring dependencies to multiple variant, but incompatible variants will require a conflict resolution.

In this sample, we show how to declare a "main" dependency to the release variant of the application.
Left unchanged, all release binaires would be resolved thanks to transitivity (e.g. `app`, `lib1`, and `lib2`).
We can specify an additional dependency to `lib1` by using the _debug_ attribute.
In this case, both the release and debug variant of `lib1` are in conflict because of their capability.
Via the capability resolution strategy, we can select the debug variant only for that variant.

## Demonstration

```
$ ./gradlew :product:verify --console=verbose
> Task :lib2:compileReleaseCpp
> Task :lib1:compileReleaseCpp
> Task :lib1:compileDebugCpp
> Task :lib2:compileDebugCpp
> Task :lib2:linkRelease
> Task :lib2:linkDebug
> Task :lib2:stripSymbolsRelease
> Task :lib1:linkDebug
> Task :lib1:linkRelease
> Task :lib1:stripSymbolsRelease
> Task :app:compileReleaseCpp
> Task :app:linkRelease
> Task :app:stripSymbolsRelease

> Task :product:verify
./app/build/exe/main/release/stripped/app
./lib1/build/lib/main/debug/liblib1.dylib
./lib2/build/lib/main/release/stripped/liblib2.dylib

BUILD SUCCESSFUL
```

Note that both `lib1` debug and release variant are built as the dependency tree for `app`'s configuration are not affected.
Thus, `app` builds against the `lib1` release variant but `product` resolve the `lib1` debug variant.
