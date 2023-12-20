import org.gradle.api.Action;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.artifacts.Dependency;
import org.gradle.api.artifacts.ModuleDependency;
import org.gradle.api.artifacts.ProjectDependency;
import org.gradle.api.attributes.AttributeContainer;
import org.gradle.api.initialization.Settings;
import org.gradle.api.logging.Logger;
import org.gradle.api.logging.Logging;
import org.gradle.language.cpp.CppBinary;
import org.gradle.language.cpp.CppComponent;
import org.gradle.language.cpp.CppLibrary;

import java.util.Arrays;
import java.util.Collections;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public /*final*/ abstract class DebuggableProductPlugin implements Plugin<Settings> {
    private static final Logger LOGGER = Logging.getLogger(DebuggableProductPlugin.class);

    @Override
    public void apply(Settings settings) {
        settings.getGradle().allprojects(project -> {
            // Use: -Pdbg=com.example:foo,com.example:bar
            final Set<DebuggableCoordinate> debuggable = project.getProviders().gradleProperty("dbg")
                    .map(DebuggableProductPlugin::toCommaSeparatedList)
                    .map(DebuggableProductPlugin::toCoordinates)
                    .getOrElse(Collections.emptySet());

            project.getComponents().withType(CppComponent.class).configureEach(component -> {
                component.getImplementationDependencies().getDependencies()
                        .configureEach(redirection(project, debuggable));
            });

            project.getComponents().withType(CppLibrary.class).configureEach(component -> {
                component.getApiDependencies().getDependencies()
                        .configureEach(redirection(project, debuggable));
            });
        });
    }

    private static Iterable<String> toCommaSeparatedList(String v) {
        return Arrays.asList(v.split(","));
    }

    private static Set<DebuggableCoordinate> toCoordinates(Iterable<String> v) {
        return StreamSupport.stream(v.spliterator(), false).map(it -> {
            final String[] tokens = it.split(":");
            return new DebuggableCoordinate(tokens[0], tokens[1]);
        }).collect(Collectors.toSet());
    }

    // Mutate dependency according to the debuggable coordinates
    private static Action<Dependency> redirection(Project project, Set<DebuggableCoordinate> coordinates) {
        return new Action<>() {
            private boolean toRelease = coordinates.contains(new DebuggableCoordinate(project.getGroup().toString(), project.getName()));

            @Override
            public void execute(Dependency dependency) {
                if (coordinates.contains(DebuggableCoordinate.ofDependency(dependency))) {
                    LOGGER.info(String.format("Redirect dependency %s to debug variant", dependency));
                    ((ModuleDependency) dependency).attributes(this::forDebug);
                } else if (toRelease) {
                    LOGGER.info(String.format("Redirect dependency %s to release variant", dependency));
                    ((ModuleDependency) dependency).attributes(this::forRelease);
                }
            }

            private void forDebug(AttributeContainer attributes) {
                attributes.attribute(CppBinary.OPTIMIZED_ATTRIBUTE, false);
            }

            private void forRelease(AttributeContainer attributes) {
                attributes.attribute(CppBinary.OPTIMIZED_ATTRIBUTE, true);
            }
        };
    }

    // Represent coordinate to debug
    private static final class DebuggableCoordinate {
        private final String group;
        private final String name;

        private DebuggableCoordinate(String group, String name) {
            this.group = group;
            this.name = name;
        }

        public static DebuggableCoordinate ofDependency(Dependency dependency) {
            if (dependency instanceof ProjectDependency) {
                final Project project = ((ProjectDependency) dependency).getDependencyProject();
                return new DebuggableCoordinate(project.getGroup().toString(), project.getName());
            } else if (dependency instanceof ModuleDependency) {
                return new DebuggableCoordinate(dependency.getGroup(), dependency.getName());
            } else {
                throw new UnsupportedOperationException();
            }
        }

        public String getName() {
            return name;
        }

        public String getGroup() {
            return group;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o)
                return true;
            if (o == null || getClass() != o.getClass())
                return false;
            DebuggableCoordinate that = (DebuggableCoordinate) o;
            return Objects.equals(group, that.group) && Objects.equals(name, that.name);
        }

        @Override
        public int hashCode() {
            return Objects.hash(group, name);
        }
    }
}
