{
  inputs = {
    nixpkgs.url = "github:NixOS/nixpkgs/nixos-unstable";
    flake-utils.url = "github:numtide/flake-utils";
    craftinginterpreters-tools.url =
      "github:felixscheinost/craftinginterpreters-book-nix";
  };

  outputs = { self, nixpkgs, flake-utils, craftinginterpreters-tools }:
    flake-utils.lib.eachDefaultSystem (system:
      let pkgs = nixpkgs.legacyPackages.${system};
      in {
        devShell = with pkgs;
          mkShell {
            buildInputs = [
              #
              craftinginterpreters-tools.packages.${system}.craftinginterpreters-tools
              nodejs
              yarn
              jdk21_headless
              (writeShellScriptBin "gradle" ''
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
