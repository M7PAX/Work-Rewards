{ pkgs, lib, config, inputs, ... }:
let
jdkWithFX = pkgs.jdk25.overrideAttrs (previous: {
    enableJavaFX = true; # for JavaFX
    # include following line if JavaFX with Webkit is needed
#    openjfx_jdk = pkgs.openjfx.override { withWebKit = true; };
    nativeBuildInputs = previous.nativeBuildInputs ++ [ pkgs.makeWrapper ];
    postFixup = ''
      wrapProgram $out/bin/java \
        --add-flags "--upgrade-module-path ${lib.getLib pkgs.openjfx25}/lib"
      wrapProgram $out/bin/javac \
        --add-flags "--upgrade-module-path ${lib.getLib pkgs.openjfx25}/lib"
    '';
});
in
{
    packages = with pkgs; [ nixd xorg.libXtst xorg.libXxf86vm libGL gtk3 glib ];
    languages.java.enable = true;
    languages.java.jdk.package = jdkWithFX;
    languages.java.maven.enable = true;
    languages.nix.enable = true;

    enterShell = ''
      export LD_LIBRARY_PATH="${pkgs.lib.makeLibraryPath [
      pkgs.xorg.libXxf86vm
      pkgs.xorg.libXtst
      pkgs.glib
      pkgs.gtk3
      pkgs.libGL
      ]}:$LD_LIBRARY_PATH"
    '';
}
