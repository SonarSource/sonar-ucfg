Sonar Ucfg
==========

Sonar Ucfg (for Universal Control Flow Graph) is an open source java library that provides the format of expected CFGs to be analyzed by SonarSecurity (which is a SonarSource commercial product).

This library provides a protobuf file in order for any language to generate a universal cfg and serialize it in protobuf binary format. 
A serialization mechanism from/to java object to/from protobuf binary is also provided with this library.

Mapping a language to UCFG
==========================
- Constants : value of constants can be safely stripped away as there is no use of the value in the fixpoint analysis.
- Location in file : Line in file start at one and columns start at zero. 
