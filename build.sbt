name := "ProductRecommendation"
version := "1.5.5"
scalaVersion := "2.12.14"
scalacOptions := Seq("-deprecation", "-unchecked", "-encoding", "utf8", "-Xlint")


val sparkVersion = "2.4.8"
val scalaTestVersion = "3.2.10"

unmanagedSources / excludeFilter := (HiddenFileFilter || "*-script.scala")
Compile / unmanagedResourceDirectories   += baseDirectory.value / "conf"
Test / unmanagedResourceDirectories += baseDirectory.value / "conf"
fork := true
run / connectInput := true
Test / parallelExecution := false

libraryDependencies ++= Seq(
  "org.apache.spark"  %% "spark-core"      % sparkVersion,
  "org.apache.spark"  %% "spark-sql"       % sparkVersion,
  "org.apache.spark"  %% "spark-repl"      % sparkVersion,

  "org.scalatest"     %% "scalatest"       % scalaTestVersion  % "test",
)

initialCommands += """
  import org.apache.spark.sql.SparkSession
  import org.apache.spark.SparkContext
  val spark = SparkSession.builder.
    master("local[*]").
    appName("Console").
    config("spark.app.id", "Console").   // To silence Metrics warning.
    getOrCreate()
  val sc = spark.sparkContext
  val sqlContext = spark.sqlContext
  import sqlContext.implicits._
  import org.apache.spark.sql.functions._    // for min, max, etc.
  """

cleanupCommands += """
  println("Closing the SparkSession:")
  spark.stop()
  """