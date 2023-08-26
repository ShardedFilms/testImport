# Project Unity (On the Repairing)

As of the dead mod since This may, I (Sharded or us) Will repair this mod for future extensions.

## Contributing

Discontinuted. but we'il do it.

## Compiling

1. Clone repository.
   ```
   git clone -b Youngcha-part-2 --single-branch https://github.com/AvantTeam/ProjectUnityPublic
   ```

2. Pack sprites. (Only necessary if new sprites are added)
   ```
   gradlew tools:proc
   ```

3. Build.
   ```
   gradlew main:deploy
   ```

Resulting `.jar` file should be in `main/build/libs/`