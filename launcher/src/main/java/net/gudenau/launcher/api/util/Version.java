package net.gudenau.launcher.api.util;

import net.gudenau.launcher.Versions;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Range;

import java.util.regex.Pattern;

/**
 * A simple version type.
 *
 * @param major A major version, used for breaking changes
 * @param minor A minor version, used for new features
 * @param patch A patch version, used for bug fixes
 * @param build A build version, incremented for each build
 */
public record Version(
    @Range(from = 0, to = Integer.MAX_VALUE) int major,
    @Range(from = ABSENT, to = Integer.MAX_VALUE) int minor,
    @Range(from = ABSENT, to = Integer.MAX_VALUE) int patch,
    @Range(from = ABSENT, to = Integer.MAX_VALUE) int build
) implements Comparable<Version> {
    /**
     * The number used when a version doesn't use a particular value.
     */
    public static final int ABSENT = -1;
    
    /**
     * The version of the launcher.
     */
    public static final @NotNull Version LAUNCHER_VERSION = Versions.LAUNCHER_VERSION;
    
    /**
     * Creates a new version with only a major value.
     *
     * @param major The major version
     */
    public Version(int major) {
        this(major, ABSENT);
    }
    
    /**
     * Creates a new version with a major and minor value.
     *
     * @param major The major version
     * @param minor The minor version
     */
    public Version(int major, int minor) {
        this(major, minor, ABSENT);
    }
    
    /**
     * Creates a new version with a major, minor and patch value.
     *
     * @param major The major version
     * @param minor The minor version
     * @param patch The patch version
     */
    public Version(int major, int minor, int patch) {
        this(major, minor, patch, ABSENT);
    }
    
    /**
     * Creates a new version with a major, minor, patch and build value.
     *
     * @param major The major version
     * @param minor The minor version
     * @param patch The patch version
     * @param build The build version
     */
    public Version {
        // This gets a little weird because you need the previous version components for the later ones.
        if (major == -1) {
            throw new IllegalArgumentException("Major was not specified");
        }
        if (hasBuild() && !(hasPatch() && hasMinor())) {
            throw new IllegalArgumentException("Build specified without a patch or a minor specified");
        }
        if (hasPatch() && !hasMinor()) {
            throw new IllegalArgumentException("Patch specified without a minor specified");
        }
    }
    
    /**
     * The regex used to parse a version string in one of the following formats:
     * <ul>
     * <li>0</li>
     * <li>0.1</li>
     * <li>0.1.2</li>
     * <li>0.1.2.3</li>
     * </ul>
     */
    private static final Pattern VERSION_PATTERN = Pattern.compile("^(\\d+)(\\.(\\d+)(\\.(\\d+)(\\.(\\d+))?)?)?$");
    
    /**
     * Attempts to parse a version string.
     * <p>
     * Must be one of the following formats:
     * <ul>
     * <li>0</li>
     * <li>0.1</li>
     * <li>0.1.2</li>
     * <li>0.1.2.3</li>
     * </ul>
     *
     * @param version The version string to parse.
     * @return The parsed version
     * @throws IllegalArgumentException If the string is not a well formed version string
     */
    @NotNull
    public static Version parse(@NotNull String version) {
        var matcher = VERSION_PATTERN.matcher(version);
        if (!matcher.find()) {
            throw new IllegalArgumentException(version + " is not a valid version string");
        }
        
        try {
            var major = matcher.group(1);
            var minor = matcher.group(3);
            var patch = matcher.group(5);
            var build = matcher.group(7);
            return new Version(
                Integer.parseInt(major),
                minor != null ? Integer.parseInt(minor) : ABSENT,
                patch != null ? Integer.parseInt(patch) : ABSENT,
                build != null ? Integer.parseInt(build) : ABSENT
            );
        } catch (NumberFormatException e) {
            throw new RuntimeException(version + " is not a valid version string", e);
        }
    }
    
    /**
     * Checks if this {@link Version} has a minor value.
     *
     * @return True if present, false otherwise
     */
    public boolean hasMinor() {
        return minor != ABSENT;
    }
    
    /**
     * Checks if this {@link Version} has a patch value.
     *
     * @return True if present, false otherwise
     */
    public boolean hasPatch() {
        return patch != ABSENT;
    }
    
    /**
     * Checks if this {@link Version} has a build value.
     *
     * @return True if present, false otherwise
     */
    public boolean hasBuild() {
        return build != ABSENT;
    }
    
    @SuppressWarnings("DuplicateExpressions")
    @Override
    public String toString() {
        if (hasBuild()) {
            return major + "." + minor + "." + patch + "." + build;
        } else if (hasPatch()) {
            return major + "." + minor + "." + patch;
        } else if (hasMinor()) {
            return major + "." + minor;
        } else {
            return Integer.toString(major);
        }
    }
    
    @Override
    public int compareTo(@NotNull Version o) {
        var result = Integer.compare(major, o.major);
        if(result != 0) {
            return result;
        }
        result = Integer.compare(minor, o.minor);
        if(result != 0) {
            return result;
        }
        result = Integer.compare(patch, o.patch);
        if(result != 0) {
            return result;
        }
        return Integer.compare(build, o.build);
    }
}
