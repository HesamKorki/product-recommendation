package utils
import java.io._
import java.nio.file.Paths


object FileOperation {

  case class FileOperationError(msg: String) extends RuntimeException(msg)

  def verify(path: String): Unit = {
    val file = new File(path)
    if (!file.exists()) {
      throw FileOperationError(s"File $path does not exist")
    }
    if (!file.isFile) {
      throw FileOperationError(s"$path is not a file")
    }
  }

  def rmrf(root: String): Unit = rmrf(new File(root))

  def rmrf(root: File): Unit = {
    if (root.isFile) root.delete()
    else if (root.exists) {
      root.listFiles.foreach(rmrf)
      root.delete()
    }
  }

  def rm(file: String): Unit = rm(new File(file))

  def rm(file: File): Unit =
    if (file.delete == false) throw FileOperationError(s"Deleting $file failed!")

  def mkdir(path: String): Unit = (new File(path)).mkdirs

  def write(path: String, fname: String, data: Seq[String]): Unit ={
    val outPath = Paths.get(path, fname).toString
    val file = new File(outPath)
    val writer = new BufferedWriter(new FileWriter(file))
    for (line <- data) writer.write(line + "\n")
    writer.close()
  
  }
}