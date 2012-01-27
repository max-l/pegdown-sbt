package com.strong_links

import sbt._
import sbt.Keys._
import java.io.File
import org.pegdown._

object PegDown {

  val pegdownOptions = TaskKey[Option[Int]]("pegdown-mappings", "The flags of the extensions to enable as a bitmask, taken from : http://www.decodified.com/pegdown/api/org/pegdown/Extensions.html")
  
  val pegdownLinkRenderer = TaskKey[Option[LinkRenderer]]("pegdown-link-renderer", "A custom link renderer can override the way links are rendered : http://www.decodified.com/pegdown/api/org/pegdown/LinkRenderer.html")
    
  val pegDownMappings = TaskKey[Seq[(File,File)]]("pegdown-mappings")

  val pegdownRun = TaskKey[Seq[File]]("pegdown-run") <<= pegDownTaskBody

  val pegDownEnableParallelProcessing = TaskKey[Boolean]("pegdown-enable-parallel-processing")
  
  def pegDownTaskBody = (
      pegDownMappings in Compile, 
      pegdownOptions in Compile, 
      pegdownLinkRenderer in Compile, 
      pegDownEnableParallelProcessing in Compile) map { (mappings, options, linkRenderer, enablePar) =>

    val op = options.getOrElse(Extensions.NONE)
    val lr = linkRenderer.getOrElse(new LinkRenderer)
    
    val m = if(enablePar) mappings.par else mappings
    
    mappings map { srcTodst =>

      val src = scala.io.Source.fromFile(srcTodst._1).toArray
      val pegDownProcessor = new PegDownProcessor(op)

      val htmlResult = pegDownProcessor.markdownToHtml(src, lr)

      printToFile(srcTodst._2)(_.println(htmlResult))

      srcTodst._2
    }
  }
  
  private def printToFile(f: java.io.File)(op: java.io.PrintWriter => Unit) {
    val p = new java.io.PrintWriter(f)
    try { op(p) } finally { p.close() }
  }  
  
  def init = Seq( 
    pegdownRun,
    pegDownEnableParallelProcessing in Compile := true,
    resourceGenerators in Compile <+= pegDownTaskBody,
    pegDownMappings in Compile <<= (sourceDirectory in Compile, resourceManaged in Compile) map { (sd,rm) =>
      val markdownFiles = sd ** "*.md"
      val res = markdownFiles x rebase(sd, rm)
      res
    }
  )
  
}