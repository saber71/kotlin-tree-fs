import heraclius.tree_fs.TreeFsFactory
import java.nio.file.Paths

fun main() {
    val node = TreeFsFactory.create(Paths.get("D:\\WebstormProjects\\garbage-codes\\package.json"))
    println(node.getChildren())
    println(node.getAttributes())
}
