package com.strong_links

import sbt._
import sbt.Keys._
import java.io.File
import org.pegdown._

object PegDown {

  val pegdownOptions = TaskKey[Option[Int]]("pegdown-options", "The flags of the extensions to enable as a bitmask, when not set, will default to Extensions.ALL, taken from : http://www.decodified.com/pegdown/api/org/pegdown/Extensions.html")
  
  val pegdownLinkRenderer = TaskKey[Option[LinkRenderer]]("pegdown-link-renderer", "A custom link renderer can override the way links are rendered : http://www.decodified.com/pegdown/api/org/pegdown/LinkRenderer.html")
    
  val pegDownMappings = TaskKey[Seq[(File,File)]]("pegdown-mappings")

  lazy val pegdownRun = TaskKey[Seq[File]]("pegdown-run") <<= pegDownTaskBody

  val pegDownEnableParallelProcessing = TaskKey[Boolean]("pegdown-enable-parallel-processing")
  
  def pegDownTaskBody = 
     (pegDownMappings in Compile, 
      pegdownOptions in Compile, 
      pegdownLinkRenderer in Compile, 
      pegDownEnableParallelProcessing in Compile) map { (mappings, options, linkRenderer, enablePar) =>


    val op = options.getOrElse(Extensions.ALL)
    val lr = linkRenderer.getOrElse(new LinkRenderer)

    // first create the directory structure, single threaded :
    mappings map { srcTodst => ensureParentExists(srcTodst._2)}

    val m = if(enablePar) mappings.par else mappings

    m.map( srcTodst => {
      assert(srcTodst._1.isFile, "Expected markdown file, got a directory" + srcTodst._1.getAbsolutePath)

      val src = scala.io.Source.fromFile(srcTodst._1).toArray
      val pegDownProcessor = new PegDownProcessor(Extensions.ALL)
      val htmlOutputFile = srcTodst._2

      val htmlResult = pegDownProcessor.markdownToHtml(src, lr)

      printToFile(htmlOutputFile)(_.println(htmlResult))

      htmlOutputFile
    }).seq
  }
  
  private def printToFile(f: java.io.File)(op: java.io.PrintWriter => Unit) {
    val p = new java.io.PrintWriter(f)
    try { op(p) } finally { p.close() }
  }  

  private def ensureParentExists(f: File): Unit = {
    val p = f.getParentFile 

    if(!p.exists) {
      ensureParentExists(p)
      p.mkdir      
    }
  }

  val generateOnCompile =
    resourceGenerators in Compile <+= pegDownTaskBody

  def init = Seq(
    pegdownOptions in Compile  := None,
    pegdownLinkRenderer in Compile  := None,    
    pegDownEnableParallelProcessing in Compile := true,
    pegDownMappings in Compile <<= (sourceDirectory in Compile, resourceManaged in Compile) map { (sd,rm) =>
      val markdownFiles = sd ** "*.md"
      val res = markdownFiles x rebase(sd, rm)
      res.map { (filePair) => {
        val (src,dst) = filePair
        val renamedDst = dst.getParentFile / (dst.base + ".html")
        (src, renamedDst)
      }}
    },    
    pegdownRun
  )
}