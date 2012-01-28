## An SBT plugin to invoke the Pegdown processor via a build definition.

```scala

  lazy val scalaformsDemo = Project(
    ...
    settings = PegDown.init ++ ...other settings...
  )
```

## Redefining which markdown source files get processed and where the output goes

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

## Credits

Of course, 99.999 % of the credit goes to Mathias, as this plugin is simply a wrapper of PegDown : https://github.com/sirthias
