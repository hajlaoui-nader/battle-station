{
  inputs = {
    nixpkgs.url = "github:NixOS/nixpkgs/nixos-23.05";
    utils.url = "github:numtide/flake-utils";
    sbt.url = "github:zaninime/sbt-derivation";
    sbt.inputs.nixpkgs.follows = "nixpkgs";
  };

  outputs = { self, nixpkgs, utils, sbt }:
    utils.lib.eachDefaultSystem (system:
      let
        pkgs = import nixpkgs { inherit system; };
      in
      {
        devShells.default = pkgs.mkShell {
          buildInputs = [
            pkgs.sbt
            pkgs.scalafmt
            pkgs.ammonite
            pkgs.graalvm-ce
          ];

          shellHook = ''
            export JAVA_HOME=${pkgs.graalvm-ce}
            PATH="${pkgs.graalvm-ce}/bin:$PATH"
          '';
        };

        packages.default = sbt.mkSbtDerivation.${system} {
          pname = "seedtag-package";
          version = "0.1.0";
          depsSha256 = "sha256-KAlARXfQZ2M1et8kojJuWXTWbsDhUGvLRIs3TIVKcsA=";

          src = ./.;

          buildInputs = [ pkgs.sbt pkgs.graalvm-ce pkgs.makeWrapper ];

          # the build phase requires sbt plugin sbt-native-packager
          buildPhase = "sbt assembly";

          # scala version is hard-coded
          installPhase = ''
            mkdir -p $out/bin
            mkdir -p $out/share/java

            cp target/scala-3.3.0/*.jar $out/share/java

            makeWrapper ${pkgs.graalvm-ce}/bin/java $out/bin/battle-station \
              --add-flags "-cp \"$out/share/java/*\" com.seedtag.Main"
          '';
        };
      });
}
