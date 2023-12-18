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
