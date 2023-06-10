{
  inputs = {
    nixpkgs.url = "github:NixOS/nixpkgs/nixos-unstable";
    flake-utils.url = "github:numtide/flake-utils";
  };

  outputs = { self, nixpkgs, flake-utils }:
    flake-utils.lib.eachDefaultSystem (system:
      let pkgs = nixpkgs.legacyPackages.${system};
      in {
        devShell = with pkgs;
          mkShell {
            buildInputs = [
              #
              jdk17_headless
              (pkgs.writeShellScriptBin "gradle" ''
                # find project root
                # taken from https://github.com/gradle/gradle-completion
                dir="$PWD"
                project_root="$PWD"
                while [[ "$dir" != / ]]; do
                  if [[ -f "$dir/settings.gradle" || -f "$dir/settings.gradle.kts" || -f "$dir/gradlew" ]]; then
                    project_root="$dir"
                    break
                  fi
                  dir=$(dirname $dir)
                done
                $project_root/gradlew "$@"
              '')
            ];
          };
      });
}
