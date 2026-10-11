# The original LogicView source

`decompiled-src/` is the source of `Logicview.jar` (Main-Class
`com.logicstyle.mosaic.system.Kernel`, built March 2010), recovered with the
[CFR](https://www.benf.org/other/cfr/) 0.152 decompiler, together with the
resources found in the jar (`.properties` bundles, `Repository.xml`,
`ComponentRepository.dtd`). `MANIFEST.MF` is the jar's manifest.

Decompiled code loses comments and local variable names, and CFR occasionally
restructures control flow (it flags those places with "WARNING" comments), but
the classes, fields, methods and logic are the original ones. The JavaScript
port in `../src` follows these files class by class.

The jar depended on Java 3D, JDOM 1.0 and the Java Media Framework; those
libraries are not part of this repository.
