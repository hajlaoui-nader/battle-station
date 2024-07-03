# battle-station

battle-station module implementation in `scala`
the design is heavily inspired from [domain driven design](https://en.wikipedia.org/wiki/Domain-driven_design) and [tagless-final](https://okmij.org/ftp/tagless-final/index.html)

# Prerequisites

- `java` 17 and newer
- `sbt`
- `docker` required only if you want to run the program in a docker container

# Libraries

- `cats effect`: effect system
- `http4s`: Http server and client
- `fs2`: functional streams
- `circe`: json serdes
- `ciris`: env vars and application config
- `weaver` + `scalacheck`: test framework and property based testing

# Project structure

```
│
├── app/
│   └── docker-compose.yml            # docker compose containing the battle-station module
│
├── manual_tests/
│   └── test_cases.txt                # manual test cases
│   └── tests.sh                      # manual test script
│
├── project/
│   └── Dependencies.scala            # Project's dependencies
│   └── plugins.sbt                   # Project's plugins
│
├── src/main/scala/com/seedtag
│   └── domain                        # battle station domain
│       └── AttackService.scala       # attack service
│       └── ...                       # domain model
│   └── infra                         # battle station infra
│       └── client                    # http client
│       └── http                      # http server
│       └── resources                 # application resources
│       └── AppConfig.scala           # application configuration
│       └── Main.scala                # Application entrypoint
│
├── test/main/scala/                  # Tests directory
│
├── flake.nix                         # Project's packages (JDK, sbt ..)
│
├── flake.lock                        # Fixed project's packages versions
│
├── build.sbt                         # Project's build tool
├── .scalafmt.conf                    # Project's formatter configuration
├── .scalafix.conf                    # Project's linter configuration
├── .envrc                            # Project's environment variables declaration (direnv)
│
└── BATTLE_STATION.md                 # Project's documentation (<<= you are here)
```

# deep dive

- the application loads its configuration from environment variables;
- the applications inits its resources (http client);
- the application launchs its web server and exposes an endpoint: `POST /attack`:

```bash
curl -vvv -X POST  http://localhost:3000/attack -H "Content-Type: application/json" -d '{
        "protocols": ["avoid-mech"],
        "scan":[
                {
                        "coordinates": { "x": 0, "y": 40 },
                        "enemies": { "type": "soldier", "number": 10 }
                }
        ]
}'
```

- the application choses the next target;
- it identifies the best available ion cannon, by calling the cannons in parallel then choosing the best available;
- it fires the cannon and reports;
- the application sends back its report.

# Run

the application needs these environment variables:

- `HTTP_HOST`: http server host, default to `0.0.0.0`
- `HTTP_PORT`: http server port, default to `3000`
- `CANNON1_HOST`: ion cannon 1 host, default to `0.0.0.0`
- `CANNON1_PORT`: ion cannon 1 port, default to `3001`
- `CANNON2_HOST`: ion cannon 2 host, default to `0.0.0.0`
- `CANNON2_PORT`: ion cannon 2 port, default to `3002`
- `CANNON1_HOST`: ion cannon 3 host, default to `0.0.0.0`
- `CANNON1_PORT`: ion cannon 3 port, default to `3003`

```bash
sbt run
```

## docker run

first generate docker image

```bash
sbt Docker/publishLocal
```

then run the `docker-compose` file:

```bash
docker-compose -f app/docker-compose.yml up

```

# Run tests

```bash
sbt test
```

# tradeoffs

- This program is designed with a purely functional approach, where all exceptions and side effects are encapsulated within the program's constructs. This design choice guarantees better code maintainability and robustness, as it reduces unexpected interactions between different parts of the program. However, this approach comes with a higher upfront cost, as developers need to have prior exposure to functional programming concepts to effectively understand and work with the codebase;
- This program is inspired by domain-driven design principles. As a result, it may exhibit some code duplication, but this duplication serves to achieve better separation of concerns. By adhering to domain-driven design, the codebase aims to reflect the structure and language of the problem domain, which enhances its readability and maintainability;
- This program incorporates ScalaCheck property testing, which enhances the quality of test cases generated, and may reveal unanticipated edge cases.

# bonus

- to make the development environment reproducible, this project uses a combination of [Nix](https://nixos.org/) + [direnv](https://direnv.net/);
- `Nix` creates isolated environments for each project; we specify dependencies explicitly in `flake.nix`. This guarantees that every developer works with exactly the same dependencies, down to the version number, reducing the "works on my machine" problem;
- The same `Nix` configuration can be used in both development and CI, ensuring consistency across all stages of development;
- `flake.lock` assures that all the developers are using the same packages versions;
- The docker image generated using `sbt-native-packager` could be replaced by a `nix` approach using the following command, this approach guarentees a small sized image.

```shell
nix bundle --bundler github:NixOS/bundlers#toDockerImag
```
