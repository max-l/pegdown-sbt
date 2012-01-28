## An SBT plugin to invoke the Pegdown processor via a build definition.

### Installation

The plugin is only available via source dependeny, see : https://github.com/harrah/xsbt/wiki/Plugins
to add into your project : 

```scala

  lazy val scalaformsDemo = Project(
    ...
    settings = PegDown.init ++ ...other settings...
  )
```

this will create a task `pegdown-run` available in the SBT console, to have this task
invoked during the `resources` task, add the setting 

```scala
  resourceGenerators in Compile <+= PegDown.pegDownTaskBody
```


### Redefining which markdown source files get processed and where the output goes

The mapping between source and output file is completely driven by the `PegDown.pegDownMappings` setting in the `Compile` configuration

```scala
val pegDownMappings = TaskKey[Seq[(File,File)]]("pegdown-mappings")
```

A default mapping is defined that takes *.md files from `sourceDirectory in Compile`, and rebases them in `resourceManaged in Compile`,
which means that the directory structure is preserved.

```scala

PegDown.pegDownMappings in Compile <<= (sourceDirectory in Compile, resourceManaged in Compile) map { (sd,rm) =>
  val markdownFiles = sd ** "*.md"
  val res = markdownFiles x rebase(sd, rm)
  res.map { (filePair) => {
    val (src,dst) = filePair
    val renamedDst = dst.getParentFile / (dst.base + ".html")
    (src, renamedDst)
  }}
}
```

It is of course meant to be overridable, see : http://github.com/harrah/xsbt/wiki/Mapping-Files
for more info on how to define `Seq[(File,File)]` mappings.

### Other settings

```scala
  val pegdownOptions = TaskKey[Option[Int]]("pegdown-options", "The flags of the extensions to enable as a bitmask, when not set, will default to Extensions.ALL, taken from : http://www.decodified.com/pegdown/api/org/pegdown/Extensions.html")
  
  val pegdownLinkRenderer = TaskKey[Option[LinkRenderer]]("pegdown-link-renderer", "A custom link renderer can override the way links are rendered : http://www.decodified.com/pegdown/api/org/pegdown/LinkRenderer.html")    

  val pegDownEnableParallelProcessing = TaskKey[Boolean]("pegdown-enable-parallel-processing")
```

### Credits

Of course, 99.999 % of the credit goes to Mathias, as this plugin is simply a wrapper of PegDown : https://github.com/sirthias
