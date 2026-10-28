<img src="assets/icon.png" alt="UniversalAdmin TAB" width="96" align="right">

# UniversalAdmin TAB

[![Build](https://github.com/universaladmin-mc/universaladmin-tab/actions/workflows/build.yml/badge.svg)](https://github.com/universaladmin-mc/universaladmin-tab/actions/workflows/build.yml)
[![License](https://img.shields.io/badge/license-GPL--3.0-blue.svg)](LICENSE)
[![Modrinth](https://img.shields.io/badge/Modrinth-universaladmin--tab-00AF5C)](https://modrinth.com/plugin/universaladmin-tab)

[TAB](https://modrinth.com/plugin/tab-was-taken) for UniversalAdmin: the tab
list and name tags follow nicks, vanish and staff mode.

- **Nicks:** a nicked player is shown in an ordinary TAB group - their rank's
  prefix, suffix and sorting do not give them away - with the nick as name.
- **Vanish:** TAB hides vanished players by itself (UniversalAdmin sets the
  `vanished` metadata TAB looks for); staff who see them get a marker after
  the name.
- **Staff mode:** a marker after the name of players in staff mode.

An official extension for [UniversalAdmin](https://github.com/universaladmin-mc/UniversalAdmin),
the free all-in-one admin plugin for Paper. Needs UniversalAdmin 1.7 or newer
and TAB 6 or newer.

- **Documentation:** [https://universaladmin-mc.github.io/UniversalAdmin/extensions/tab/](https://universaladmin-mc.github.io/UniversalAdmin/extensions/tab/)
- **Download:** [Modrinth](https://modrinth.com/plugin/universaladmin-tab) or the
  [releases](https://github.com/universaladmin-mc/universaladmin-tab/releases) - put the
  jar next to UniversalAdmin and TAB in `plugins/`. UniversalAdmin's update
  check covers this extension too.
- **Bugs and ideas:** [UniversalAdmin's issues](https://github.com/universaladmin-mc/UniversalAdmin/issues)

## Building

```bash
./gradlew build   # build/libs/universaladmin-tab-<version>.jar
```

It compiles against the UniversalAdmin release named in `gradle.properties`
(from Modrinth's Maven repository) and TAB's API (from JitPack);
`-PuniversaladminJar=<path>` builds against a local UniversalAdmin build
instead. Releases: bump `version` in `build.gradle.kts`, add a
`CHANGELOG.md` section, tag `v<version>` - the release workflow publishes to
GitHub and Modrinth.

## License

[GNU GPL v3.0 or later](LICENSE).
