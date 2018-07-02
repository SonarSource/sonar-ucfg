Sonar Ucfg [![Build Status](https://travis-ci.org/SonarSource/sonar-ucfg.svg?branch=master)](https://travis-ci.org/SonarSource/sonar-ucfg) [![Quality Gate](https://next.sonarqube.com/sonarqube/api/project_badges/measure?project=org.sonarsource.ucfg%3Asonar-ucfg&metric=alert_status)](https://next.sonarqube.com/sonarqube/dashboard?id=org.sonarsource.ucfg%3Asonar-ucfg) [![Coverage](https://next.sonarqube.com/sonarqube/api/project_badges/measure?project=org.sonarsource.ucfg%3Asonar-ucfg&metric=coverage
)](https://next.sonarqube.com/sonarqube/component_measures?id=org.sonarsource.ucfg%3Asonar-ucfg&metric=coverage)
==========

Sonar Ucfg (for Universal Control Flow Graph) is an open source java library that provides the format of expected CFGs to be analyzed by SonarSecurity (which is a SonarSource commercial product).

This library provides a protobuf file in order for any language to generate a universal cfg and serialize it in protobuf binary format. 
A serialization mechanism from/to java object to/from protobuf binary is also provided with this library.

Mapping a language to UCFG
==========================
- Constants : value of constants can be safely stripped away as there is no use of the value in the fixpoint analysis.
- Location in file : Line in file start at one and columns start at zero. 
