# Introduction #

Set up your working environment in these "very easy" steps.

  1. [Download Eclipse For Java Developers](http://www.eclipse.org/downloads/) and http://developer.android.com/sdk/1.6_r1/installing.html#installingplugin
  1. Checkout sources: hg clone https://aedict.googlecode.com/hg/ aedict
  1. Try to compile sources: [Compile](Compile.md). It may fail but at least it should create $HOME/.m2 directory and download all libraries.
  1. Import the `aedict-apk` directory to Eclipse using File / Import / Existing project to workspace.
  1. In Eclipse: Windows / Preferences / Java / Build Path / Classpath Variables, click New,  name: M2\_REPO, location: $HOME/.m2/repository
  1. To fix the project properties rightclick `aedict-apk`, Properties / Android, set Build Target to 2.2
  1. Make sure that Project / Build Automatically is checked. Click Project / Clean / Clean All Projects.

Refresh & Pray. This should generate debug.keystore in your `$HOME/.android/` directory which the Maven2 build is missing.