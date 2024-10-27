package heraclius.tree_fs

import heraclius.tools.Tree
import java.io.File
import java.nio.file.Path
import java.nio.file.Paths

object TreeFsFactory {
    fun create(path: Path): Tree.Node {
        return FsNode(File(path.toAbsolutePath().toString()), name = "/")
    }

    fun create(first: String, vararg paths: String): Tree.Node {
        return create(Paths.get(first, *paths))
    }
}
